package sc.lazymath.activities;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import sc.lazymath.R;
import sc.lazymath.ocr.OcrMath;
import sc.lazymath.ocr.OcrUtil;
import sc.lazymath.ocr.imageprocessing.ImageUtil;
import sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import sc.lazymath.util.CameraUtil;
import sc.lazymath.views.CameraOverlay;
import sc.lazymath.views.CameraView;

public class CameraActivity extends ActionBarActivity {

    private static Camera camera = null;
    private CameraView cameraView;

    private boolean startingIntent = false;

    private OcrMath ocrMath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.ocrMath = new OcrMath(getNeuralNetworks());

        setContentView(R.layout.activity_camera);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        camera = CameraUtil.getCameraInstance(getApplicationContext());

        CameraUtil.initCamera(camera);

        cameraView = new CameraView(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(this.cameraView, 0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams
                .FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

        final CameraOverlay overlay = new CameraOverlay(this.getApplicationContext());
        preview.addView(overlay, 1, params);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Rect rect = CameraUtil.getCropWindow(CameraActivity.this);
//                CameraUtil.focusCamera(camera, rect);
                camera.autoFocus(autoFocusCallback);
            }
        });

        final AbsoluteLayout window = (AbsoluteLayout) this.findViewById(R.id.camera_window);
        ViewTreeObserver vto = window.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = window.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

                List<Button> buttons = CameraUtil.initRectangleButtons(CameraActivity.this, overlay);
                overlay.setButtons(buttons);
                overlay.invalidate();
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
            int[][] image = OcrUtil.convertImage(data, CameraActivity.this);

            startingIntent = true;

            // get result from ocr math
            CameraActivity.this.ocrMath.processImage(image);
            String result = CameraActivity.this.ocrMath.recognize();

            // put result as extra for solution activity
            Intent intent = new Intent(CameraActivity.this, SolutionActivity.class);
            intent.putExtra("query", result);

            // display image
            image = ImageUtil.getScaledImage(image, 300);

            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageBitmap(ImageUtil.matrixToBitmap(image));

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(imageView);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // start solution activity
            CameraActivity.this.startActivity(intent);
        }
    };

    private List<NeuralNetwork> getNeuralNetworks(){
        // try to open neural networks from disk
        List<NeuralNetwork> neuralNetworks = OcrUtil.deserializeNeuralNetworks(this.getApplicationContext());

        // if there are no neural networks on disk create them and save them
        if(neuralNetworks == null){
            neuralNetworks = OcrUtil.trainNeuralNetworks(this);
            OcrUtil.serializeNeuralNetworks(this.getApplicationContext(), neuralNetworks);
        }

        return neuralNetworks;
    }


}

