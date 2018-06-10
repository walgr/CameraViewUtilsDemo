package com.wpf.library.cameraviewutil;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.wpf.library.cameraviewutil.Listener.OnCaptureYUVDataCallback;
import com.wpf.library.cameraviewutil.Listener.OnNV21DataCallback;
import com.wpf.library.cameraviewutil.Listener.OnPreviewYUVDataCallback;
import com.wpf.library.cameraviewutil.View.CameraView;
import com.wpf.library.cameraviewutil.View.CameraView2;
import com.wpf.requestpermission.RequestPermission;

/**
 * Created by 王朋飞 on 2018/5/19.
 */
public class WPFCameraView extends FrameLayout implements CustomFunction {

    private CameraView cameraView;
    private CameraView2 cameraView2;

    private boolean onlyY;

    private Rect findRect;

    private int orientation = getResources().getConfiguration().orientation;

    private CustomData customData;

    private OnPreviewYUVDataCallback onPreviewYUVDataCallback;
    private OnCaptureYUVDataCallback onCaptureYUVDataCallback;
    private OnNV21DataCallback onNV21DataCallback;

    public WPFCameraView(@NonNull Context context) {
        this(context,null);
    }

    public WPFCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WPFCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(getChildCount() == 0)
            initView();
    }

    private void initView() {
        customData = new CustomData();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraView2 = new CameraView2(getContext());
            cameraView2.setOnPreviewYUVDataCallback(onPreviewYUVDataCallback);
            cameraView2.setCustomData(customData);
            addView(cameraView2);
        } else {
            cameraView = new CameraView(getContext());
            cameraView.setCustomData(customData);
            cameraView.setOnNV21DataCallback(onNV21DataCallback);
            addView(cameraView);
        }
    }

    public void setOnlyY(boolean onlyY) {
        this.onlyY = onlyY;
        customData.setOnlyY(onlyY);
    }

    public void setFindRect(Rect findRect) {
        this.findRect = findRect;
        customData.setOutRect(findRect);
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        RequestPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setOnPreviewYUVDataCallback(OnPreviewYUVDataCallback onPreviewYUVDataCallback) {
        this.onPreviewYUVDataCallback = onPreviewYUVDataCallback;
    }

    public void setOnCaptureYUVDataCallback(OnCaptureYUVDataCallback onCaptureYUVDataCallback) {
        this.onCaptureYUVDataCallback = onCaptureYUVDataCallback;
    }

    public void setOnNV21DataCallback(OnNV21DataCallback onNV21DataCallback) {
        this.onNV21DataCallback = onNV21DataCallback;
    }

    @Override
    public void changeCameraId(String cameraId) {
        if(cameraView != null) {
            cameraView.changeCameraId(cameraId);
        } else if(cameraView2 != null) {
            cameraView2.changeCameraId(cameraId);
        }
    }
}
