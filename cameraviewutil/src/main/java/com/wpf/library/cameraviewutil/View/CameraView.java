package com.wpf.library.cameraviewutil.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.wpf.library.cameraviewutil.CustomData;
import com.wpf.library.cameraviewutil.CustomFunction;
import com.wpf.library.cameraviewutil.Listener.OnNV21DataCallback;
import com.wpf.library.cameraviewutil.Utils.CompareCameraSizesByArea;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * Created by wpf on 6-20-0020.
 * CameraView---5.0-
 */

public class CameraView extends AutoFitTextureView implements
        TextureView.SurfaceTextureListener,
        CustomFunction,
        Camera.PreviewCallback {

    private CustomData customData;

    private Camera camera;
    private Camera.Parameters parameters;
    private Camera.Size mPreviewSize;

    private OnNV21DataCallback onNV21DataCallback;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus) {
            if(isAvailable()) {
                openCamera(getWidth(),getHeight());
            } else {
                setSurfaceTextureListener(this);
            }
        } else {
            closeCamera();
        }
    }

    private void openCamera(int width,int height) {
        camera = Camera.open(getCameraId());
        if(camera == null) return;
//        configureTransform(width,height);
        try {
            setParameters();
            SurfaceTexture texture = getSurfaceTexture();
            if(texture == null) return;
            camera.setPreviewTexture(texture);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setParameters() {
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        camera.setDisplayOrientation(90*rotation);
        parameters = camera.getParameters();
        mPreviewSize = getSupportedPreviewSizes();
        parameters.setPreviewSize(mPreviewSize.width,mPreviewSize.height);
        parameters.setFocusMode("auto");
        camera.setParameters(parameters);
    }

    private Camera.Size getSupportedPreviewSizes() {
        List<Camera.Size> previewSize = camera.getParameters().getSupportedPreviewSizes();
        return Collections.max(previewSize, new CompareCameraSizesByArea());
    }

    private void closeCamera() {
        if(null != camera) {
            camera.stopPreview();
            camera.release();
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
        RectF bufferRect = new RectF(0, 0, mPreviewSize.height, mPreviewSize.width);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.height,
                    (float) viewWidth / mPreviewSize.width);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        setTransform(matrix);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(onNV21DataCallback != null) {
            int width = camera.getParameters().getPreviewSize().width;
            int height = camera.getParameters().getPreviewSize().height;
            onNV21DataCallback.previewImageCallBack(width,height,data);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera(width,height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void changeCameraId(String cameraId) {
        closeCamera();
        customData.setCameraId(cameraId);
        openCamera(getWidth(),getHeight());
    }

    public void setOnNV21DataCallback(OnNV21DataCallback onNV21DataCallback) {
        this.onNV21DataCallback = onNV21DataCallback;
    }

    public void setCustomData(CustomData customData) {
        this.customData = customData;
    }

    private Activity getActivity() {
        return (AppCompatActivity) getContext();
    }

    private int getCameraId() {
        if(customData == null || TextUtils.isEmpty(customData.getCameraId())) {
            return 0;
        }
        return Integer.valueOf(customData.getCameraId());
    }
}
