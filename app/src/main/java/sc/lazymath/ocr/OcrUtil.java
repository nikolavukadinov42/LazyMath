package sc.lazymath.ocr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sc.lazymath.R;
import sc.lazymath.activities.CameraActivity;
import sc.lazymath.ocr.imageprocessing.ImageUtil;
import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import sc.lazymath.util.CameraUtil;

/**
 * Created by nikola42 on 12/28/2014.
 */
public class OcrUtil {

    public static int[][] convertImage(byte[] data, Activity activity) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        Rect cropWindow = CameraUtil.getCropWindow(activity, bmp);

        Bitmap croppedBmp = Bitmap.createBitmap(bmp, cropWindow.left, cropWindow.top, cropWindow.right - cropWindow.left,
                cropWindow.bottom - cropWindow.top);

        // rotate ccw by 90degrees
        Matrix mtx = new Matrix();
        mtx.preRotate(-90);

        Bitmap rotated = Bitmap.createBitmap(croppedBmp, 0, 0, croppedBmp.getWidth(),
                croppedBmp.getHeight(), mtx, false);

        int[][] image = ImageUtil.bitmapToMatrix(rotated);

        image = ImageUtil.christiansMethod(image);

        return image;
    }

    public static List<NeuralNetwork> trainNeuralNetworks(Activity activity) {
        List<NeuralNetwork> neuralNetworks = new ArrayList<NeuralNetwork>();
        String[] paths = new String[]{"./res/ts.png", "./res/tsH.png", "./res/tsVerdana.png"};
        String chars = "0123456789abcdxyz+-/*±()";

        Bitmap image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ts1);
        List<RasterRegion> regions = OcrUtil.getRegions(image);
        NeuralNetwork nn1 = new NeuralNetwork(regions, chars);
        neuralNetworks.add(nn1);

        //        image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ts2);
        //        regions = OcrUtil.getRegions(image);
        //        NeuralNetwork nn2 = new NeuralNetwork(regions, chars);
        //        neuralNetworks.add(nn2);
        //
        //        image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ts3);
        //        regions = OcrUtil.getRegions(image);
        //        NeuralNetwork nn3 = new NeuralNetwork(regions, chars);
        //        neuralNetworks.add(nn3);

        return neuralNetworks;
    }

    public static Bitmap convertImage(Bitmap bitmap) {
        int[][] image = convertImageToMatrix(bitmap);

        Bitmap processed = ImageUtil.matrixToBitmap(image);

        return processed;
    }

    public static int[][] convertImageToMatrix(Bitmap bitmap) {
        int[][] image = ImageUtil.bitmapToMatrix(bitmap);

        image = ImageUtil.christiansMethod(image);

        return image;
    }

    /**
     * Use for screenshots, that is already black and white images.
     *
     * @param bitmap black and white image
     * @return list of raster regions from the given image
     */
    public static List<RasterRegion> getRegions(Bitmap bitmap) {
        List<RasterRegion> regions = null;

        int[][] image = ImageUtil.bitmapToMatrix(bitmap);
        image = ImageUtil.matrixToBinary(image, 200);

        regions = ImageUtil.regionLabeling(image);

        for (RasterRegion rasterRegion : regions) {
            rasterRegion.determineMoments();
        }

        Collections.sort(regions, new RasterRegion.RegionComparer());

        return regions;
    }

    public static List<RasterRegion> getRegions(int[][] image) {
        List<RasterRegion> regions = null;

        regions = ImageUtil.regionLabeling(image);

        List<RasterRegion> filtered = new ArrayList<RasterRegion>();
        for (RasterRegion rasterRegion : regions) {
            if (!(rasterRegion.points.size() < 20)) {
                filtered.add(rasterRegion);
            }
        }

        regions = filtered;

        for (RasterRegion rasterRegion : regions) {
            rasterRegion.determineMoments();
        }

        Collections.sort(regions, new RasterRegion.RegionComparer());

        return regions;
    }

    public static void serializeNeuralNetworks(Context context, List<NeuralNetwork> neuralNetworks) {
        try {
            File dir = context.getDir("LazyMath", Context.MODE_PRIVATE);
            File file = new File(dir, "neuralnetworks.lzm");

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(neuralNetworks);

            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<NeuralNetwork> deserializeNeuralNetworks(Context context) {
        List<NeuralNetwork> neuralNetworks = null;

        try {
            File dir = context.getDir("LazyMath", Context.MODE_PRIVATE);
            File file = new File(dir, "neuralnetworks.lzm");

            FileInputStream fileInputStream = new FileInputStream(file);

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            neuralNetworks = (List<NeuralNetwork>) objectInputStream.readObject();

            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return neuralNetworks;
    }
}
