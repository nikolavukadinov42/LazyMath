package ftn.sc.lazymath.ocr;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/28/2014.
 */
public class OcrUtil {

	public static List<RasterRegion> regions = new ArrayList<RasterRegion>();
	public static boolean isBinaryMatrix;
	public static int[][] image;
	public static Map<String, Integer> alfabet = new HashMap<String, Integer>();
	public static Map<Integer, String> alfabetInv = new HashMap<Integer, String>();

	public static BufferedImage convertImage(BufferedImage bitmap) {
		int[][] image = convertImageToMatrix(bitmap);

		BufferedImage processed = ImageUtil.matrixToBitmap(image);

		return processed;
	}

	public static int[][] convertImageToMatrix(BufferedImage bitmap) {
		int[][] image = ImageUtil.bitmapToMatrix(bitmap);

		// image = ImageUtil.getScaledImage(image, 600, 600);

		// int h = (int) Math.floor((double) image.length / 100);
		// int w = (int) Math.floor((double) image[0].length / 100);

		image = ImageUtil.christiansMethod(image);

		// image = ImageUtil.dilation(image);
		// image = ImageUtil.erosion(image);
		// image = ImageUtil.dilation(image);

		return image;
	}

	public static List<RasterRegion> getRegions(BufferedImage bitmap) {
		List<RasterRegion> regions = null;

		int[][] image = ImageUtil.bitmapToMatrix(bitmap);
		image = ImageUtil.matrixToBinary(image, 200);
		// image = ImageUtil.dilation(image);

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

	public static int[][] deepCopyIntMatrix(int[][] input) {
		if (input == null) {
			return null;
		}

		int[][] result = new int[input.length][];
		for (int r = 0; r < input.length; r++) {
			result[r] = input[r].clone();
		}

		return result;
	}

	public static double[] prepareImageForNeuralNetwork(int[][] image) {
		double[] retVal = new double[64];

		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[1].length; j++) {
				if (image[i][j] < 255) {
					int ii = i / 8;
					int jj = j / 8;
					retVal[ii * 8 + jj]++;
				}
			}
		}

		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = retVal[i] / 32 - 1;
		}
		return retVal;
	}
}
