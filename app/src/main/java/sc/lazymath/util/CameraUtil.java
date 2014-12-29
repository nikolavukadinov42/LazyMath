package sc.lazymath.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;

import sc.lazymath.R;
import sc.lazymath.activities.HomeActivity;

/**
 * Created by nikola42 on 12/27/2014.
 */
public class CameraUtil {

    //    private static Point size;
    private static final float marginX = 100;
    private static final float marginY = 200;

    public static void initCamera(Camera camera) {
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
        if (params.getSupportedFlashModes() != null) {
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        }

        // set Camera parameters
        camera.setParameters(params);
    }

    public static void setFlashUsage(Camera camera, boolean useFlash) {
        Camera.Parameters params = camera.getParameters();

        if (useFlash) {
            // enable flash
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        } else {
            // disable flash
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }

        camera.setParameters(params);
    }

    public static Camera getCameraInstance(Context context) {
        Camera ret = null;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d(HomeActivity.TAG, "Loaded back face camera.");

            ret = CameraUtil.getBackFacingCameraInstance();
        } else if (context.getPackageManager().hasSystemFeature(PackageManager
                .FEATURE_CAMERA_ANY)) {
            Log.d(HomeActivity.TAG, "Loaded front face camera.");

            ret = CameraUtil.getFrontFacingCameraInstance();
        } else {
            Log.e(HomeActivity.TAG, "Failed to load camera from system.");
        }

        return ret;
    }

    private static Camera getBackFacingCameraInstance() {
        Camera c = null;

        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    private static Camera getFrontFacingCameraInstance() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(HomeActivity.TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    public static void initRectangleButtons(final Activity activity) {
        Button seButton = (Button) activity.findViewById(R.id.button_se);
        Button swButton = (Button) activity.findViewById(R.id.button_sw);
        Button neButton = (Button) activity.findViewById(R.id.button_ne);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);

        // get screen size
        //        FrameLayout preview = (FrameLayout) activity.findViewById(R.id.camera_preview);
        //        screenSize = new PointF(preview.getWidth(), preview.getHeight());

        View.OnTouchListener myOnTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    moveRectangleButton(activity, (Button) view, e);
                }
                return true;
            }
        };

        seButton.setOnTouchListener(myOnTouchListener);
        swButton.setOnTouchListener(myOnTouchListener);
        neButton.setOnTouchListener(myOnTouchListener);
        nwButton.setOnTouchListener(myOnTouchListener);
    }

    public static void moveRectangleButton(Activity activity, Button button, MotionEvent e) {
        int width = button.getWidth();
        int height = button.getHeight();

        float x = e.getRawX() - width / 2;
        float y = e.getRawY() - height * 2;

        boolean flagX = false;
        boolean flagY = false;

        // move adjacent buttons
        switch (button.getId()) {
            case R.id.button_se: {
                Button neButton = (Button) activity.findViewById(R.id.button_ne);
                Button swButton = (Button) activity.findViewById(R.id.button_sw);

                if (((x - swButton.getX()) < marginX)
                    //|| (x > (size.x - (width / 2)))
                        ) {
                    flagX = true;
                } else {
                    // move sw by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) neButton.getY());
                    neButton.setLayoutParams(params);
                }

                if (((y - neButton.getY()) < marginY)
                    //|| (y > (size.y - (height / 2)))
                        ) {
                    flagY = true;
                } else {
                    // move ne by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) swButton.getX(), (int) y);
                    swButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_sw: {
                Button nwButton = (Button) activity.findViewById(R.id.button_nw);
                Button seButton = (Button) activity.findViewById(R.id.button_se);

                if (((seButton.getX() - x) < marginX)
                    //|| (x < (width / 2))
                        ) {
                    flagX = true;
                } else {
                    // move se by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) nwButton.getY());
                    nwButton.setLayoutParams(params);
                }

                if (((y - nwButton.getY()) < marginY)
                    //|| (y > (size.y - (height / 2)))
                        ) {
                    flagY = true;
                } else {
                    // move nw by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) seButton.getX(), (int) y);
                    seButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_ne: {
                Button seButton = (Button) activity.findViewById(R.id.button_se);
                Button nwButton = (Button) activity.findViewById(R.id.button_nw);

                if (((x - nwButton.getX()) < marginX)
                    //|| (x > (size.x - (width / 2)))
                        ) {
                    flagX = true;
                } else {
                    // move se by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) seButton.getY());
                    seButton.setLayoutParams(params);
                }

                if (((seButton.getY() - y) < marginY)
                    //|| (y < (height / 2))
                        ) {
                    flagY = true;
                } else {
                    // move nw by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) nwButton.getX(), (int) y);
                    nwButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_nw: {
                Button swButton = (Button) activity.findViewById(R.id.button_sw);
                Button neButton = (Button) activity.findViewById(R.id.button_ne);

                if (((neButton.getX() - x) < marginX)
                    //|| (x < (width / 2))
                        ) {
                    flagX = true;
                } else {
                    // move sw by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) swButton.getY());
                    swButton.setLayoutParams(params);
                }

                if (((swButton.getY() - y) < marginY)
                    //|| (y < (height / 2))
                        ) {
                    flagY = true;
                } else {
                    // move ne by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) neButton.getX(), (int) y);
                    neButton.setLayoutParams(params);
                }
            }
            break;
            default:
                break;
        }

        // move button
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width, height,
                (int) (flagX ? button.getX() : x), (int) (flagY ? button.getY() : y));
        button.setLayoutParams(params);
    }
}
