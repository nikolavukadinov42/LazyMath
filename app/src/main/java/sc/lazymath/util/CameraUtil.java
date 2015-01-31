package sc.lazymath.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.R;
import sc.lazymath.activities.HomeActivity;
import sc.lazymath.views.CameraOverlay;

/**
 * Created by nikola42 on 12/27/2014.
 */
@SuppressWarnings("ALL")
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
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        // set white balance to auto
        if (params.getSupportedWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        // set flash mode on
        if (params.getSupportedFlashModes() != null) {
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
        }

        // set metering area
        if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
            List<Camera.Area> meteringAreas = new ArrayList<>();

            meteringAreas.add(new Camera.Area(new Rect(-350, -350, 350, 350),
                    1000)); // set weight to 100%
            params.setMeteringAreas(meteringAreas);
        }

        Camera.Size previewSize = params.getPreviewSize();
        double previewRatio = (double) previewSize.height / previewSize.width;

        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        Camera.Size newPictureSize = null;

        int max = Integer.MIN_VALUE;
        for (Camera.Size pictureSize : pictureSizes) {
            int size = pictureSize.height + pictureSize.width;
            double ratio = (double) pictureSize.height / pictureSize.width;

            if (size < 2500 && size > max && previewRatio - ratio < 0.1) {
                newPictureSize = pictureSize;
                max = size;
            }
        }

        if (newPictureSize != null) {
            params.setPictureSize(newPictureSize.width, newPictureSize.height);
        }

        Log.d("Picture size", newPictureSize.width + ", " + newPictureSize.height);

        // set Camera parameters
        camera.setParameters(params);
    }

    public static void changeFlash(Camera camera) {
        Camera.Parameters params = camera.getParameters();

        if (params.getFlashMode() == Camera.Parameters.FLASH_MODE_OFF) {
            // enable flash
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
        } else {
            // disable flash
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }

        // set Camera parameters
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

    /**
     * Get crop window rect in image coordinates.
     */
    public static Rect getCropWindow(Activity activity, Bitmap bmp) {
        Rect ret;

        // crop image
        Point pictureSize = new Point(bmp.getWidth(), bmp.getHeight());

        FrameLayout preview = (FrameLayout) activity.findViewById(R.id.camera_preview);
        Point screenSize = new Point(preview.getWidth(), preview.getHeight());

        double scaleX = (double) pictureSize.x / screenSize.x;
        double scaleY = (double) pictureSize.y / screenSize.y;

        Button seButton = (Button) activity.findViewById(R.id.button_se);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);

        int minX = nwButton.getLeft();
        int minY = nwButton.getTop();
        int maxX = seButton.getRight();
        int maxY = seButton.getBottom();

        double left = minX * scaleX >= 0 ? minX * scaleX : 0;
        double top = minY * scaleY >= 0 ? minY * scaleY : 0;
        double right = maxX * scaleX <= pictureSize.x ? maxX * scaleX : pictureSize.x;
        double bottom = maxY * scaleY <= pictureSize.y ? maxY * scaleY : pictureSize.y;

        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }

    public static void setMeteringArea(Camera camera, Rect rect) {
        Camera.Parameters params = camera.getParameters();

        if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
            List<Camera.Area> meteringAreas = new ArrayList<>();

            meteringAreas.add(new Camera.Area(rect, 1000)); // set weight to 100%
            params.setMeteringAreas(meteringAreas);
        }

        camera.setParameters(params);
    }

    public static Rect transformToMeteringArea(Activity activity) {
        Button seButton = (Button) activity.findViewById(R.id.button_se);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);

        int left = nwButton.getLeft();
        int top = nwButton.getTop();
        int right = seButton.getRight();
        int bottom = seButton.getBottom();

        FrameLayout preview = (FrameLayout) activity.findViewById(R.id.camera_preview);

        int x = (int) (2000 * ((double) (right - left) / preview.getWidth()) / 2);
        int y = (int) (2000 * ((double) (bottom - top) / preview.getHeight()) / 2);

        return new Rect(-x, -y - 100, x, y - 100);
    }
}
