package sc.lazymath.ocr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sc.lazymath.R;
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

        Bitmap croppedBmp = Bitmap.createBitmap(bmp, cropWindow.left, cropWindow.top,
                cropWindow.right - cropWindow.left, cropWindow.bottom - cropWindow.top);

        // rotate ccw by 90degrees
        Matrix mtx = new Matrix();
        mtx.preRotate(-90);

        Bitmap rotated = Bitmap.createBitmap(croppedBmp, 0, 0, croppedBmp.getWidth(),
                croppedBmp.getHeight(), mtx, false);

        int[][] image = ImageUtil.bitmapToMatrix(rotated);

        image = ImageUtil.christian(image);

        return image;
    }

    public static List<NeuralNetwork> trainNeuralNetworks(Activity activity) {
        List<NeuralNetwork> neuralNetworks = new ArrayList<NeuralNetwork>();

        String chars = "0123456789abcdefghijklmnopqrstuvwxyzαβγδθ+-*/=∫()[]{}±!'";

        Bitmap image = BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.ts_computermodern);
        List<RasterRegion> regions = OcrUtil.getRegions(image);
        OcrMath.mergeRegions(regions);
        NeuralNetwork nn = new NeuralNetwork(regions, chars);
        neuralNetworks.add(nn);

        image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ts_latinmodern);
        regions = OcrUtil.getRegions(image);
        OcrMath.mergeRegions(regions);
        nn = new NeuralNetwork(regions, chars);
        neuralNetworks.add(nn);

        chars = "arcosintegxplmud";

        image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ts_latinmodern2);
        regions = OcrUtil.getRegions(image);
        OcrMath.mergeRegions(regions);
        nn = new NeuralNetwork(regions, chars);
        neuralNetworks.add(nn);

        return neuralNetworks;
    }

    public static Bitmap convertImage(Bitmap bitmap) {
        int[][] image = convertImageToMatrix(bitmap);

        Bitmap processed = ImageUtil.matrixToBitmap(image);

        return processed;
    }

    public static int[][] convertImageToMatrix(Bitmap bitmap) {
        int[][] image = ImageUtil.bitmapToMatrix(bitmap);

        image = ImageUtil.christian(image);

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
        image = ImageUtil.matrixToBinary(image, 250);

        regions = ImageUtil.regionLabeling(image);

        for (RasterRegion rasterRegion : regions) {
            rasterRegion.determineMoments();
        }

        Collections.sort(regions, new RasterRegion.RegionComparer());

        return regions;
    }

    public static List<RasterRegion> getRegions(int[][] image) {
        List<RasterRegion> regions;

        int h = image.length;
        int w = image[0].length;

        regions = ImageUtil.regionLabeling(image);

        for (RasterRegion rasterRegion : regions) {
            rasterRegion.determineMoments();
        }

        List<RasterRegion> discarded = new ArrayList<RasterRegion>();
        for (RasterRegion rasterRegion : regions) {
            if (rasterRegion.points.size() < 20) {
                discarded.add(rasterRegion);
            }

            if (rasterRegion.minX == 0 || rasterRegion.maxX == w - 1 || rasterRegion.minY == 0 ||
                    rasterRegion.maxY == h - 1) {
                discarded.add(rasterRegion);
            }
        }

        regions.removeAll(discarded);

        Collections.sort(regions, new RasterRegion.RegionComparer());

        return regions;
    }

    public static void serializeNeuralNetworks(List<NeuralNetwork> neuralNetworks) {
        try {
            File dir = Environment.getExternalStorageDirectory();
            File file = new File(dir, "neuralnetworks.lzm");

            Log.d("dir", dir.getAbsolutePath());

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(neuralNetworks);

            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<NeuralNetwork> deserializeNeuralNetworks() {
        List<NeuralNetwork> neuralNetworks = null;

        try {
            File dir = Environment.getExternalStorageDirectory();
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
