package sc.lazymath.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.R;
import sc.lazymath.activities.HomeActivity;
import sc.lazymath.views.CameraOverlay;

/**
 * Created by nikola42 on 12/27/2014.
 */
public class CameraUtil {

    //    private static Point size;
    private static final float MARGIN_X = 100;
    private static final float MARGIN_Y = 200;

    // TODO move somewhere
    private static CameraOverlay overlay;

    public static void initCamera(Camera camera) {
        Camera.Parameters params = camera.getParameters();

        camera.setDisplayOrientation(90);
        params.setRotation(90);

        // set focus mode to auto
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        } else {
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

    public static List<Button> initRectangleButtons(final Activity activity,
                                                    CameraOverlay overlayy) {
        List<Button> ret = new ArrayList<>();

        Button seButton = (Button) activity.findViewById(R.id.button_se);
        Button swButton = (Button) activity.findViewById(R.id.button_sw);
        Button neButton = (Button) activity.findViewById(R.id.button_ne);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);

        AbsoluteLayout window = (AbsoluteLayout) activity.findViewById(R.id.camera_window);

        int h = window.getHeight() - 80;
        int w = window.getWidth();

        int width = seButton.getWidth();
        int height = seButton.getHeight();

        seButton.setLayoutParams(new AbsoluteLayout.LayoutParams(width, height,
                (int) (w * 0.75 - width / 2), (int) (h * 0.75 - height / 2)));

        swButton.setLayoutParams(new AbsoluteLayout.LayoutParams(width, height,
                (int) (w * 0.25 - width / 2), (int) (h * 0.75 - height / 2)));

        neButton.setLayoutParams(new AbsoluteLayout.LayoutParams(width, height,
                (int) (w * 0.75 - width / 2), (int) (h * 0.25 - height / 2)));

        nwButton.setLayoutParams(new AbsoluteLayout.LayoutParams(width, height,
                (int) (w * 0.25 - width / 2), (int) (h * 0.25 - height / 2)));

        ret.add(seButton);
        ret.add(swButton);
        ret.add(neButton);
        ret.add(nwButton);

        overlay = overlayy;

        View.OnTouchListener myOnTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    moveRectangleButton(activity, (Button) view, e);
                }

                overlay.invalidate();

                return true;
            }
        };

        seButton.setOnTouchListener(myOnTouchListener);
        swButton.setOnTouchListener(myOnTouchListener);
        neButton.setOnTouchListener(myOnTouchListener);
        nwButton.setOnTouchListener(myOnTouchListener);

        return ret;
    }

    public static void moveRectangleButton(Activity activity, Button button, MotionEvent e) {
        int width = button.getWidth();
        int height = button.getHeight();

        int statusBarHeight = AndroidUtil.getStatusBarHeight(activity);

        AbsoluteLayout layout = (AbsoluteLayout) button.getParent();
        int layoutHeight = layout.getHeight() - statusBarHeight;
        int layoutWidth = layout.getWidth();

        int x = (int) (e.getRawX() - width / 2);
        int y = (int) (e.getRawY() - height * 3);

        int dX;
        int dY;

        Point nePosition = new Point();
        Point nwPosition = new Point();
        Point sePosition = new Point();
        Point swPosition = new Point();

        Button neButton = (Button) activity.findViewById(R.id.button_ne);
        Button swButton = (Button) activity.findViewById(R.id.button_sw);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);
        Button seButton = (Button) activity.findViewById(R.id.button_se);

        boolean resizeX = false;
        boolean resizeY = false;

        // move adjacent buttons
        switch (button.getId()) {
            case R.id.button_se: {
                dX = layoutWidth - (x + width);
                dY = layoutHeight - (y + height);

                if (dX < layoutWidth / 3) {
                    //|| (x > (size.x - (width / 2)))
                    resizeX = true;

                    sePosition.x = x;
                    nePosition.x = x;

                    nwPosition.x = dX;
                    swPosition.x = dX;
                }

                if (dY < layoutHeight / 3) {
                    //|| (y > (size.y - (height / 2)))
                    resizeY = true;

                    sePosition.y = y;
                    swPosition.y = y;

                    nwPosition.y = dY;
                    nePosition.y = dY;
                }
            }
            break;
            case R.id.button_sw: {
                dX = x;
                dY = layoutHeight - (y + height);

                if (dX < layoutWidth / 3) {
                    //|| (x < (width / 2))
                    resizeX = true;

                    swPosition.x = x;
                    nwPosition.x = x;

                    nePosition.x = layoutWidth - dX - width;
                    sePosition.x = layoutWidth - dX - width;
                }

                if (dY < layoutHeight / 3) {
                    //|| (y > (size.y - (height / 2)))
                    resizeY = true;

                    sePosition.y = y;
                    swPosition.y = y;

                    nwPosition.y = dY;
                    nePosition.y = dY;
                }
            }
            break;
            case R.id.button_ne: {
                dX = layoutWidth - (x + width);
                dY = y - statusBarHeight;

                if (dX < layoutWidth / 3) {
                    //|| (x > (size.x - (width / 2)))
                    resizeX = true;

                    nePosition.x = x;
                    sePosition.x = x;

                    swPosition.x = dX;
                    nwPosition.x = dX;
                }

                if (dY < layoutHeight / 3) {
                    //|| (y < (height / 2))
                    resizeY = true;

                    nePosition.y = y;
                    nwPosition.y = y;

                    sePosition.y = layoutHeight - dY - height;
                    swPosition.y = layoutHeight - dY - height;
                }
            }
            break;
            case R.id.button_nw: {
                dX = x;
                dY = y - statusBarHeight;

                if (dX < layoutWidth / 3) {
                    //|| (x < (width / 2))
                    resizeX = true;

                    nwPosition.x = x;
                    swPosition.x = x;

                    nePosition.x = layoutWidth - dX - width;
                    sePosition.x = layoutWidth - dX - width;
                }

                if (dY < layoutHeight / 3) {
                    //|| (y < (height / 2))
                    resizeY = true;

                    nePosition.y = y;
                    nwPosition.y = y;

                    sePosition.y = layoutHeight - dY - height;
                    swPosition.y = layoutHeight - dY - height;
                }
            }
            break;
            default:
                break;
        }

        // move button
        AbsoluteLayout.LayoutParams seParams = new AbsoluteLayout.LayoutParams(width, height,
                resizeX ? sePosition.x : (int) seButton.getX(), resizeY ? sePosition.y : (int)
                seButton.getY());
        seButton.setLayoutParams(seParams);

        AbsoluteLayout.LayoutParams swParams = new AbsoluteLayout.LayoutParams(width, height,
                resizeX ? swPosition.x : (int) swButton.getX(), resizeY ? swPosition.y : (int)
                swButton.getY());
        swButton.setLayoutParams(swParams);

        AbsoluteLayout.LayoutParams neParams = new AbsoluteLayout.LayoutParams(width, height,
                resizeX ? nePosition.x : (int) neButton.getX(), resizeY ? nePosition.y : (int)
                neButton.getY());
        neButton.setLayoutParams(neParams);

        AbsoluteLayout.LayoutParams nwParams = new AbsoluteLayout.LayoutParams(width, height,
                resizeX ? nwPosition.x : (int) nwButton.getX(), resizeY ? nwPosition.y : (int)
                nwButton.getY());
        nwButton.setLayoutParams(nwParams);
    }
}
