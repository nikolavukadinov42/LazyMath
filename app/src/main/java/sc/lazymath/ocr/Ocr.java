package sc.lazymath.ocr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import sc.lazymath.R;
import sc.lazymath.ocr.imageprocessing.ImageUtil;

/**
 * Created by nikola42 on 12/28/2014.
 */
public class Ocr {

    public static byte[] convertImage(byte[] data, Activity activity) {
        byte[] ret;

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        // crop image to window
        PointF pictureSize = new PointF(bmp.getWidth(), bmp.getHeight());

        FrameLayout preview = (FrameLayout) activity.findViewById(R.id.camera_preview);
        PointF screenSize = new PointF(preview.getWidth(), preview.getHeight());

        double scaleX = pictureSize.x / screenSize.x;
        double scaleY = pictureSize.y / screenSize.y;

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

        Bitmap croppedBmp = Bitmap.createBitmap(bmp, (int) left, (int) top, (int) (right - left),
                (int) (bottom - top));

        // rotate ccw by 90degrees
        Matrix mtx = new Matrix();
        mtx.preRotate(-90);

        Bitmap rotated = Bitmap.createBitmap(croppedBmp, 0, 0, croppedBmp.getWidth(),
                croppedBmp.getHeight(), mtx, false);

        byte[][] matrix = ImageUtil.bitmapToByteMatrix(rotated);

        Bitmap processed = ImageUtil.matrixToBitmap(matrix);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        processed.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ret = stream.toByteArray();

        return ret;
    }
}
