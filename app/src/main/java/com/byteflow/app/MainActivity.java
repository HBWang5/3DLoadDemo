
package com.byteflow.app;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;
import static com.byteflow.app.MyNativeRender.*;

public class MainActivity extends AppCompatActivity
{
    private static final String[] REQUEST_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    private static final int PERMISSION_REQUEST_CODE = 1;

    private ViewGroup mRootView;
    private MyGLRender mGLRender = new MyGLRender();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = (ViewGroup) findViewById(R.id.rootView);
        mGLRender.init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
        init3D();


    }

    private void init3D() {
        ///sdcard/Android/data/com.byteflow.app/files/Download
        String fileDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//        CommonUtils.copyAssetsDirToSDCard(MainActivity.this, "poly", fileDir + "/model");
        CommonUtils.copyAssetsDirToSDCard(MainActivity.this, "three", fileDir + "/model");
//        CommonUtils.copyAssetsDirToSDCard(MainActivity.this, "book", fileDir + "/model");
//        CommonUtils.copyAssetsDirToSDCard(MainActivity.this, "fonts", fileDir);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        MyGLSurfaceView mGLSurfaceView = new MyGLSurfaceView(MainActivity.this, mGLRender);
        mRootView.addView(mGLSurfaceView, layoutParams);


        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);

        if (mRootView.getWidth() != mGLSurfaceView.getWidth() || mRootView.getHeight() != mGLSurfaceView.getHeight()) {
            mGLSurfaceView.setAspectRatio(mRootView.getWidth(), mRootView.getHeight());
        }

        mGLRender.setParamsInt(SAMPLE_TYPE, SAMPLE_TYPE_3D_MODEL , 0);
        mGLSurfaceView.requestRender();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
                Toast.makeText(this, "We need the permission: WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootView.removeAllViews();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLRender.unInit();
        /*
         * Once the EGL context gets destroyed all the GL buffers etc will get destroyed with it,
         * so this is unnecessary.
         * */
    }



    protected boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
