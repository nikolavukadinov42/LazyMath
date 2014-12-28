package sc.lazymath.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sc.lazymath.R;
import sc.lazymath.ocr.Ocr;
import sc.lazymath.util.CameraWindowUtil;
import sc.lazymath.views.CameraView;

public class CameraActivity extends ActionBarActivity {

    private static Camera camera = null;
    private CameraView cameraView;

    private boolean startingIntent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager
                .FEATURE_CAMERA)) {
            camera = getCameraInstance();
        }

        Camera.Parameters params = camera.getParameters();

        camera.setDisplayOrientation(90);
        params.setRotation(90);

        // set focus mode to auto
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        // set white balance to auto
        if (params.getSupportedWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }
        // set flash mode on
        if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }

        // set Camera parameters
        camera.setParameters(params);

        cameraView = new CameraView(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(this.cameraView, 0);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(autoFocusCallback);
            }
        });

        CameraWindowUtil.initCorners(this);
    }

    @Override
    protected void onResume() {
        if (camera == null) {
            camera = getCameraInstance();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        camera.release();
        camera = null;
        if (!startingIntent) {
            this.finish();
        }

        super.onPause();
    }

    public static Camera getCameraInstance() {
        Camera c = null;

        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            camera.takePicture(null, null, pictureCallback);
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap image = Ocr.convertImage(data, CameraActivity.this);

            startingIntent = true;

            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageBitmap(image);

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(imageView);

            //            String picturePath = getOutputMediaFile(1);
            //
            //            try {
            //                FileOutputStream fos = new FileOutputStream(picturePath);
            //                image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            //                fos.close();
            //            } catch (FileNotFoundException e) {
            //                e.printStackTrace();
            //            } catch (IOException e) {
            //                e.printStackTrace();
            //            }
        }
    };

    private static String getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg";
    }
}

