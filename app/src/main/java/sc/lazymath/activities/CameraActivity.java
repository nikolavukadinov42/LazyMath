package sc.lazymath.activities;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import sc.lazymath.R;
import sc.lazymath.ocr.Ocr;
import sc.lazymath.util.CameraUtil;
import sc.lazymath.views.CameraView;

public class CameraActivity extends ActionBarActivity {

    private static Camera camera = null;
    private CameraView cameraView;

    private boolean startingIntent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        camera = CameraUtil.getCameraInstance(getApplicationContext());

        CameraUtil.initCamera(camera);
        CameraUtil.initRectangleButtons(this);

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
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Camera.Parameters params = camera.getParameters();
        boolean supportsFlash = params.getSupportedFlashModes() != null;

        // show menu only if camera supports flash
        if (supportsFlash) {
            getMenuInflater().inflate(R.menu.menu_camera, menu);
            MenuItem checkable = menu.findItem(R.id.menu_flash);
            checkable.setChecked(true);
        }

        return supportsFlash;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
            case R.id.menu_flash:
                item.setChecked(!item.isChecked());
                CameraUtil.setFlashUsage(camera, item.isChecked());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (camera == null) {
            camera = CameraUtil.getCameraInstance(getApplicationContext());
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
        }
    };
}

