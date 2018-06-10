package com.wpf.library.cameraviewutil.Listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by 王朋飞 on 2018/5/19.
 * API 20 5.0+
 */

public interface OnCaptureYUVDataCallback {

    void captureImageCallBack(int imageWidth, int imageHeight,
                              @NonNull byte[] yData, @Nullable byte[] uData, @Nullable byte[] vData);
}
