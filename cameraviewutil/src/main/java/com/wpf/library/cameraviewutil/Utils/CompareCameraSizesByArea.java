package com.wpf.library.cameraviewutil.Utils;

import android.hardware.Camera;
import java.util.Comparator;

public class CompareCameraSizesByArea implements Comparator<Camera.Size> {
    @Override
    public int compare(Camera.Size lhs, Camera.Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.width * lhs.height -
                (long) rhs.width * rhs.height);
    }

}
