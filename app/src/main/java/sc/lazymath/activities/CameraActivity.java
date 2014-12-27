package sc.lazymath.activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sc.lazymath.R;
import sc.lazymath.util.CameraWindowUtil;
import sc.lazymath.views.CameraView;

public class CameraActivity extends ActionBarActivity {

    private static Camera camera = null;
    private CameraView cameraView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
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
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        camera.autoFocus(autoFocusCallback);
                    }
                }
        );

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
        this.finish();

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
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            // crop image to window
            PointF pictureSize = new PointF(bmp.getWidth(), bmp.getHeight());

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            PointF screenSize = new PointF(preview.getWidth(), preview.getHeight());

            double scaleX = pictureSize.x / screenSize.x;
            double scaleY = pictureSize.y / screenSize.y;

            Button seButton = (Button) findViewById(R.id.button_se);
            Button nwButton = (Button) findViewById(R.id.button_nw);

            int minX = nwButton.getLeft();
            int minY = nwButton.getTop();
            int maxX = seButton.getRight();
            int maxY = seButton.getBottom();

            double left = minX * scaleX >= 0 ? minX * scaleX : 0;
            double top = minY * scaleY >= 0 ? minY * scaleY : 0;
            double right = maxX * scaleX <= pictureSize.x ? maxX * scaleX : pictureSize.x;
            double bottom = maxY * scaleY <= pictureSize.y ? maxY * scaleY : pictureSize.y;

            Bitmap croppedBmp = Bitmap.createBitmap(bmp, (int) left, (int) top,
                    (int) (right - left), (int) (bottom - top));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            data = stream.toByteArray();

            File pictureFile = getOutputMediaFile(1);
            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}

