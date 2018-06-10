//package com.wpf.library.cameraviewutil.View;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.graphics.ImageFormat;
//import android.graphics.Rect;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.Image;
//import android.media.ImageReader;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.util.AttributeSet;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
//import com.wpf.library.cameraviewutil.Listener.OnPreviewYUVDataCallback;
//import com.wpf.library.cameraviewutil.R;
//import com.wpf.library.cameraviewutil.Utils.CompareSizesByArea;
//import com.wpf.library.cameraviewutil.Utils.ImageUtil;
//import com.wpf.library.libyuv_android.Data.RotationMode;
//import com.wpf.requestpermission.RequestPermission;
//import com.wpf.requestpermission.RequestResult;
//
//import java.util.Arrays;
//import java.util.Collections;
//
///**
// * Created by 王朋飞 on 2017/3/13.
// * 返回图像数据的SurfaceView
// */
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class CameraView2Old extends SurfaceView implements
//        SurfaceHolder.Callback2 {
//
//    private Handler mHandler = new Handler();
//
//    private SurfaceHolder mSurfaceHolder;
//    private CameraManager mCameraManager;
//    private CameraDevice mCameraDevice;
//    private CaptureRequest.Builder mPreviewBuilder;
//    private CaptureRequest.Builder mCaptureBuilder;
//
//    private CameraCaptureSession mSession;
//    private ImageReader mPreviewImageReader;
//    private ImageReader mCaptureImageReader;
//    private OnPreviewYUVDataCallback mOnPreviewYUVDataCallback;
//
//    private int previewImageFormat = ImageFormat.YUV_420_888;
//    private int captureImageFormat = ImageFormat.YUV_420_888;
//
//    // 可定制
//    private Size previewSize;
//    private boolean previewBackData = false;
//    private Rect findViewRect = new Rect();
//    private int viewWidth, viewHeight;
//    private boolean onlyY = false;
//    private String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
//    private RotationMode rotationMode = RotationMode.kRotate0;
//
//    //获取的数据
//    private Size[] previewSupportSize;
//
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            mCameraDevice = cameraDevice;
//            createCameraPreviewSession();
//            createCameraCaptureSession();
//
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            cameraDevice.close();
//            mCameraDevice = null;
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice cameraDevice, int i) {
//            cameraDevice.close();
//            mCameraDevice = null;
//        }
//    };
//    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new
//            CameraCaptureSession.StateCallback() {
//
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession session) {
//                    try {
//                        mSession = session;
//                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                        session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                        session.close();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    cameraCaptureSession.close();
//                }
//            };
//    //    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
////            new CameraCaptureSession.CaptureCallback() {
////                @Override
////                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
////                                               @NonNull CaptureRequest request,
////                                               @NonNull TotalCaptureResult result) {
////                    mSession = session;
////                    mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
////                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
////                    mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
////                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
////                }
////            };
//    private ImageReader.OnImageAvailableListener onPreviewImageAvailableListener =
//            new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader imageReader) {
//                    previewCallback(imageReader);
//                }
//            };
//
//    private ImageReader.OnImageAvailableListener onCaptureImageAvailableListener =
//            new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader imageReader) {
//                    captureCallback(imageReader);
//                }
//            };
//
////    private TaskListProcessingThread<Image> previewProcessingImage;
////    private TaskListProcessingThread<Image> captureProcessingImage;
//
//    private void previewCallback(ImageReader imageReader) {
//        try {
//            if (findViewRect == null || findViewRect.isEmpty()) return;
//            Image image = imageReader.acquireLatestImage();
//            if (image != null) {
//
//                int width = image.getWidth();
//                int height = image.getHeight();
//                if(rotationMode == RotationMode.kRotate90 ||
//                        rotationMode == RotationMode.kRotate270) {
//                    width = image.getHeight();
//                    height = image.getWidth();
//                }
//                if (mOnPreviewYUVDataCallback != null) {
//                    Image.Plane planeY = ImageUtil.getYPlane(image);
//                    Image.Plane planeU = ImageUtil.getUPlane(image);
//                    Image.Plane planeV = ImageUtil.getVPlane(image);
//
//                    if (planeY == null || planeU == null || planeV == null) return;
//                    byte[] yBytes,uBytes = null,vBytes = null;
//                    yBytes = null;
//
//                    ImageUtil.Image2I420(image,onlyY,
//                            yBytes,uBytes,vBytes,
//                            viewWidth,viewHeight,null,rotationMode);
//                    image.close();
//
//                    mOnPreviewYUVDataCallback.previewImageCallBack(
//                            width, height,
//                            yBytes,uBytes,vBytes);
//                }
////                image.close();
////                if(previewProcessingImage == null) {
////                    previewProcessingImage = new TaskListProcessingThread<Image>() {
////
////                        @Override
////                        public void process(Image image) {
////                            KLog.e("获取最后Image");
////                            ByteBuffer data = ImageUtil.imageToJPEGBuffer(image,onlyY,
////                                    findViewRect.left, findViewRect.top,
////                                    findViewRect.width(), findViewRect.height());
////                            int width = image.getWidth();
////                            int height = image.getHeight();
////                            image.close();
////                            KLog.e("处理完Image发送回调");
////                            sendPreviewImageMessage(width,height, data);
////                        }
////                    };
////                }
////                previewProcessingImage.addTaskData(image);
//            }
//
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void captureCallback(ImageReader imageReader) {
//        try {
//            if (findViewRect == null || findViewRect.isEmpty()) return;
//            Image image = imageReader.acquireLatestImage();
//            if (image != null) {
////                if (captureProcessingImage == null) {
////                    captureProcessingImage = new TaskListProcessingThread<Image>() {
////
////                        @Override
////                        public void process(Image image) {
////                            captureImage = image;
////                            byte[] data = ImageUtil.image2I420(image, onlyY,
////                                    viewWidth, viewHeight, null, rotationMode);
////                            int width = image.getWidth();
////                            int height = image.getHeight();
////                            sendCaptureImageMessage(width, height, data);
////                        }
////                    };
////                }
////                captureProcessingImage.addTaskData(image);
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendPreviewImageMessage(int width, int height, byte[] imageData) {
//        Message message = new Message();
//        message.what = 1;
//        message.arg1 = width;
//        message.arg2 = height;
//        message.obj = imageData;
//        mHandler.sendMessage(message);
//    }
//
//    private void sendCaptureImageMessage(int width, int height, byte[] imageData) {
//        Message message = new Message();
//        message.what = 2;
//        message.arg1 = width;
//        message.arg2 = height;
//        message.obj = imageData;
//        mHandler.sendMessage(message);
//    }
//
//    public CameraView2Old(Context context) {
//        this(context, null);
//    }
//
//    public CameraView2Old(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public CameraView2Old(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }
//
//    private void onDestroy() {
//        if (mSession != null) {
//            mSession.close();
//            mSession = null;
//        }
//        if (mCameraDevice != null) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//        mPreviewBuilder = null;
//        mCaptureBuilder = null;
////        if (previewProcessingImage != null) previewProcessingImage.setDestroy(true);
////        if (captureProcessingImage != null) captureProcessingImage.setDestroy(true);
//    }
//
//    private void init() {
//        mSurfaceHolder = getHolder();
//        mSurfaceHolder.addCallback(this);
//    }
//
//    @SuppressLint("MissingPermission")
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void initCamera() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) return;
////        if (previewProcessingImage != null) previewProcessingImage.setDestroy(false);
////        if (captureProcessingImage != null) captureProcessingImage.setDestroy(false);
//        if (mCameraManager == null) {
//            try {
//                mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
//                Size imageSize = getImageSize();
//                if (imageSize == null) {
//                    new AlertDialog.Builder(getContext())
//                            .setMessage(R.string.dialog_message)
//                            .setPositiveButton(R.string.dialog_button_cancel,
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            ((AppCompatActivity) getContext()).finish();
//                                        }
//                                    }).show();
//                    return;
//                }
//
//                mPreviewImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(),
//                        previewImageFormat, 3);
//                mPreviewImageReader.setOnImageAvailableListener(onPreviewImageAvailableListener, mHandler);
//                mCaptureImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(),
//                        captureImageFormat, 1);
//                mCaptureImageReader.setOnImageAvailableListener(onCaptureImageAvailableListener, mHandler);
//                mCameraManager.openCamera(cameraId, stateCallback, mHandler);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void createCameraPreviewSession() {
//        if (mPreviewBuilder == null) {
//            try {
//                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                mPreviewBuilder.addTarget(mSurfaceHolder.getSurface());
//                if (previewBackData) mPreviewBuilder.addTarget(mPreviewImageReader.getSurface());
//                mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotationMode.getValue());
//                mCameraDevice.createCaptureSession(
//                        previewBackData ?
//                                Arrays.asList(mSurfaceHolder.getSurface(),
//                                        mPreviewImageReader.getSurface()):
//                                Arrays.asList(mSurfaceHolder.getSurface(),
//                                        mCaptureImageReader.getSurface())
//                        , mSessionPreviewStateCallback, mHandler);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void createCameraCaptureSession() {
//        if (mCaptureBuilder == null) {
//            try {
//                mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//                mCaptureBuilder.addTarget(mCaptureImageReader.getSurface());
//                mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                // 根据设备方向计算设置照片的方向
//                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotationMode.getValue());
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 拍照
//     */
//    public void takePicture() {
//        if (previewBackData) return;
//        try {
//            mSession.capture(mCaptureBuilder.build(), null, mHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private @Nullable Size getImageSize() {
//        Size largest = null;
//        try {
//            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            if(map == null) return null;
//            previewSupportSize = map.getOutputSizes(previewImageFormat);
//            largest = Collections.max(Arrays.asList(previewSupportSize), new CompareSizesByArea());
//            if (rotationMode == RotationMode.kRotate90)
//                largest = new Size(largest.getHeight(), largest.getWidth());
//        } catch (CameraAccessException | IllegalArgumentException e) {
//            e.printStackTrace();
//        }
//        return largest;
//    }
//
//    @Override
//    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
//
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//        viewWidth = i1;
//        viewHeight = i2;
//        RequestPermission.request((AppCompatActivity) getContext(),
//                new String[]{Manifest.permission.CAMERA}, 1, new RequestResult() {
//                    @Override
//                    public void onSuccess() {
//                        initCamera();
//                    }
//
//                    @Override
//                    public void onFail(String[] failList) {
//
//                    }
//                });
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        onDestroy();
//    }
//
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        RequestPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    public void setOnYUVDataCallback(OnPreviewYUVDataCallback mOnPreviewYUVDataCallback) {
//        this.mOnPreviewYUVDataCallback = mOnPreviewYUVDataCallback;
//    }
//
//    public void closeListening() {
//        this.mOnPreviewYUVDataCallback = null;
//        this.mCameraDevice.close();
//    }
//
//    public void setFindViewRect(Rect findViewRect) {
//        this.findViewRect = findViewRect;
//    }
//
//    public void setRotationMode(RotationMode rotationMode) {
//        this.rotationMode = rotationMode;
//    }
//
//    public boolean isPreviewBackData() {
//        return previewBackData;
//    }
//
//    public void setPreviewBackData(boolean previewBackData) {
//        this.previewBackData = previewBackData;
//    }
//
//    public void setOnlyY(boolean onlyY) {
//        this.onlyY = onlyY;
//    }
//
//    public Size getPreviewSize() {
//        return previewSize;
//    }
//
//    public void setPreviewSize(Size previewSize) {
//        this.previewSize = previewSize;
//    }
//
//    public Size[] getPreviewSupportSize() {
//        return previewSupportSize;
//    }
//}
