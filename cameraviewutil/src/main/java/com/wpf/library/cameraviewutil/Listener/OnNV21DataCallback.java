package com.wpf.library.cameraviewutil.Listener;

/**
 * Created by wangpengfei on 2017/3/13.
 * API 19 4.4-
 */

public interface OnNV21DataCallback {
    void previewImageCallBack(int imageWidth, int imageHeight, byte[] imageData);
    void captureImageCallBack(int imageWidth, int imageHeight, byte[] imageData);
}
