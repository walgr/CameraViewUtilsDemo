package com.wpf.cameraviewutilsdemo;

import android.content.res.Configuration;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wpf.library.cameraviewutil.View.CameraView2;
import com.wpf.library.cameraviewutil.WPFCameraView;
import com.wpf.requestpermission.RequestPermission;

public class MainActivity extends AppCompatActivity {

    private WPFCameraView wpfCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wpfCameraView = findViewById(R.id.wpfCameraView);

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wpfCameraView.changeCameraId(String.valueOf(CameraCharacteristics.LENS_FACING_BACK));
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        wpfCameraView.setOrientation(newConfig.orientation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RequestPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
