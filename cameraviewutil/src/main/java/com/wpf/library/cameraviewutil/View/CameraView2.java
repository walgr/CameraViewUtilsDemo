package com.wpf.library.cameraviewutil.View;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.wpf.library.cameraviewutil.CustomData;
import com.wpf.library.cameraviewutil.CustomFunction;
import com.wpf.library.cameraviewutil.Listener.OnCaptureYUVDataCallback;
import com.wpf.library.cameraviewutil.Listener.OnPreviewYUVDataCallback;
import com.wpf.library.cameraviewutil.R;
import com.wpf.library.cameraviewutil.Utils.CompareSizesByArea;
import com.wpf.library.cameraviewutil.Utils.ImageUtil;
import com.wpf.requestpermission.RequestPermission;
import com.wpf.requestpermission.RequestResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by 王朋飞 on 2018/5/21.
 *
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraView2 extends AutoFitTextureView implements
        TextureView.SurfaceTextureListener,
        CustomFunction {

    private String TAG = "CameraView2";

    /**
     * 可定制数据
     */
    private CustomData customData;

    private CameraManager mCameraManager;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    /**
     * 预览大小
     */
    private Size mPreviewSize;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * 非UI线程处理预览数据
     */
    private HandlerThread mPreviewBackgroundThread;

    /**
     * 预览任务处理后台
     */
    private Handler mPreviewBackgroundHandler;

    /**
     * 预览请求
     */
    private CaptureRequest mPreviewRequest;

    /**
     * 预览图像捕捉
     */
    private ImageReader mPreviewImageReader;

    /**
     * 预览参数构造器
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * 预览图像处理
     */
    private ImageReader.OnImageAvailableListener onPreviewImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    mPreviewBackgroundHandler.post(new ImageRunnable(imageReader.acquireLatestImage()));
                }
            };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

    };

    /**
     * 非UI线程处理拍照数据
     */
    private HandlerThread mCaptureBackgroundThread;

    /**
     * 拍照任务处理后台
     */
    private Handler mCaptureBackgroundHandler;

    /**
     * 拍照图像捕捉
     */
    private ImageReader mCaptureImageReader;

    /**
     * 拍照图像处理
     */
    private ImageReader.OnImageAvailableListener onCaptureImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
//                    mPreviewBackgroundHandler.post();
                }
            };

    /**
     * 是否支持闪光灯
     */
    private boolean mFlashSupported;

    private OnPreviewYUVDataCallback onPreviewYUVDataCallback;
    private OnCaptureYUVDataCallback onCaptureYUVDataCallback;

    public CameraView2(Context context) {
        this(context,null);
    }

    public CameraView2(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CameraView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus) {
            startBackgroundThread();
            if(isAvailable()) {
                openCamera(getWidth(),getHeight());
            } else {
                setSurfaceTextureListener(this);
            }
        } else {
            closeCamera();
            stopBackgroundThread();
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        setUpCameraOutputs(width, height);
        configureTransform(width,height);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("开启摄像头超时");
            }
            mCameraManager.openCamera(getCameraId(), mStateCallback, mPreviewBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("打开摄像头时发生错误", e);
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        setTransform(matrix);
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
//            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mPreviewImageReader.getSurface());
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(
                    Arrays.asList(surface, mPreviewImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mPreviewBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mPreviewImageReader) {
                mPreviewImageReader.close();
                mPreviewImageReader = null;
            }
            if (null != mCaptureImageReader) {
                mCaptureImageReader.close();
                mCaptureImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("摄像头关闭时发生错误", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(getCameraId());
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(map == null) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.dialog_message)
                        .setPositiveButton(R.string.dialog_button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getActivity().finish();
                                    }
                                }).show();
                return;
            }
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(getPreviewFormat())),
                    new CompareSizesByArea());

            mPreviewImageReader = ImageReader.newInstance(largest.getWidth(),largest.getHeight(),
                    getPreviewFormat(),3);
            mPreviewImageReader.setOnImageAvailableListener(onPreviewImageAvailableListener,
                    mPreviewBackgroundHandler);

            mCaptureImageReader = ImageReader.newInstance(largest.getWidth(),largest.getHeight(),
                    getCaptureFormat(),2);
            mCaptureImageReader.setOnImageAvailableListener(onCaptureImageAvailableListener,
                    mCaptureBackgroundHandler);

            //屏幕方向
            int displayRotation = getActivity().getWindowManager()
                    .getDefaultDisplay().getRotation();
            //传感器方向
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "屏幕旋转方向无效: " + displayRotation);
            }

            Point displaySize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;
            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest);

//            int orientation = getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            } else {
//                setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            }
            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    private void startBackgroundThread() {
        mPreviewBackgroundThread = new HandlerThread("CameraPreviewBackground");
        mPreviewBackgroundThread.start();
        mPreviewBackgroundHandler = new Handler(mPreviewBackgroundThread.getLooper());

        if(onCaptureYUVDataCallback != null) {
            mCaptureBackgroundThread = new HandlerThread("CameraCaptureBackground");
            mCaptureBackgroundThread.start();
            mCaptureBackgroundHandler = new Handler(mCaptureBackgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if(mPreviewBackgroundThread != null) {
            mPreviewBackgroundThread.quitSafely();
            try {
                mPreviewBackgroundThread.join();
                mPreviewBackgroundThread = null;
                mPreviewBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(mCaptureBackgroundThread != null) {
            mCaptureBackgroundThread.quitSafely();
            try {
                mCaptureBackgroundThread.join();
                mCaptureBackgroundThread = null;
                mCaptureBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width,final int height) {
        RequestPermission.request(getActivity(),
                new String[]{Manifest.permission.CAMERA}, 1, new RequestResult() {
                    @Override
                    public void onSuccess() {
                        openCamera(width,height);
                    }

                    @Override
                    public void onFail(String[] failList) {

                    }
                });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, final int width, final int height) {
        configureTransform(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void setCustomData(CustomData customData) {
        this.customData = customData;
    }

    public void setOnPreviewYUVDataCallback(OnPreviewYUVDataCallback onPreviewYUVDataCallback) {
        this.onPreviewYUVDataCallback = onPreviewYUVDataCallback;
    }

    public void setOnCaptureYUVDataCallback(OnCaptureYUVDataCallback onCaptureYUVDataCallback) {
        this.onCaptureYUVDataCallback = onCaptureYUVDataCallback;
    }

    @Override
    public void changeCameraId(String cameraId) {
        closeCamera();
        customData.setCameraId(cameraId);
        openCamera(getWidth(),getHeight());
    }

    private String getCameraId() {
        if(customData == null || TextUtils.isEmpty(customData.getCameraId())) {
            return String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        }
        return customData.getCameraId();
    }

    private int getPreviewFormat() {
        if(customData == null || customData.getPreviewOutFormat() == 0)
            return ImageFormat.YUV_420_888;
        return customData.getPreviewOutFormat();
    }

    private int getCaptureFormat() {
        if(customData == null || customData.getCaptureOutFormat() == 0)
            return ImageFormat.JPEG;
        return customData.getCaptureOutFormat();
    }

    private boolean isOnlyY() {
        return customData != null && customData.isOnlyY();
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    private class ImageRunnable implements Runnable {

        private Image image;

        public ImageRunnable(Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            if(image != null) {
                if (onPreviewYUVDataCallback != null) {
                    onPreviewYUVDataCallback.previewImageCallBack(image.getWidth(), image.getHeight(),
                            ImageUtil.byteBuffer2Byte(ImageUtil.getYPlane(image).getBuffer()),
                            isOnlyY()?null:ImageUtil.byteBuffer2Byte(ImageUtil.getUPlane(image).getBuffer()),
                            isOnlyY()?null:ImageUtil.byteBuffer2Byte(ImageUtil.getVPlane(image).getBuffer()));
                } else if(onCaptureYUVDataCallback != null) {
                    onCaptureYUVDataCallback.captureImageCallBack(image.getWidth(), image.getHeight(),
                            ImageUtil.byteBuffer2Byte(ImageUtil.getYPlane(image).getBuffer()),
                            isOnlyY()?null:ImageUtil.byteBuffer2Byte(ImageUtil.getUPlane(image).getBuffer()),
                            isOnlyY()?null:ImageUtil.byteBuffer2Byte(ImageUtil.getVPlane(image).getBuffer()));
                }
                image.close();
            }
        }
    }
}
