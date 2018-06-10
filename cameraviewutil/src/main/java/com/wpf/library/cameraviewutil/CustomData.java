package com.wpf.library.cameraviewutil;

import android.graphics.Rect;
import android.util.Size;

/**
 * Created by 王朋飞 on 2018/5/21.
 */
public class CustomData {

    /**
     * 打开的摄像头
     */
    private String cameraId;

    /**
     * 预览格式
     */
    private int previewOutFormat;

    /**
     * 拍照格式
     */
    private int captureOutFormat;

    /**
     * 输出区域
     */
    private Rect outRect;

    /**
     * 预览大小
     */
    private Size previewSize;

    /**
     * 是否打开闪光灯
     */
    private boolean isOpenFlash;

    private boolean onlyY;


    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public int getPreviewOutFormat() {
        return previewOutFormat;
    }

    public void setPreviewOutFormat(int previewOutFormat) {
        this.previewOutFormat = previewOutFormat;
    }

    public int getCaptureOutFormat() {
        return captureOutFormat;
    }

    public void setCaptureOutFormat(int captureOutFormat) {
        this.captureOutFormat = captureOutFormat;
    }

    public Rect getOutRect() {
        return outRect;
    }

    public void setOutRect(Rect outRect) {
        this.outRect = outRect;
    }

    public Size getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(Size previewSize) {
        this.previewSize = previewSize;
    }

    public boolean isOpenFlash() {
        return isOpenFlash;
    }

    public void setOpenFlash(boolean openFlash) {
        isOpenFlash = openFlash;
    }

    public boolean isOnlyY() {
        return onlyY;
    }

    public void setOnlyY(boolean onlyY) {
        this.onlyY = onlyY;
    }
}
