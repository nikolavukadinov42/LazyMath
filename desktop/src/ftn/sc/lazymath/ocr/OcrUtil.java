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

	public static List<RasterRegion> convertImage(BufferedImage bitmap) {
		List<RasterRegion> regions = null;

		int[][] image = ImageUtil.bitmapToMatrix(bitmap);

		int[][] blackAndWhite = ImageUtil.matrixToBinary(image, 200);

		// int[][] erosioned = ImageUtil.erosion(blackAndWhite);
		//
		// int[][] dilated = ImageUtil.dilation(erosioned);

		BufferedImage processed = ImageUtil.matrixToBitmap(blackAndWhite);

		regions = ImageUtil.regionLabeling(blackAndWhite);
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
		}

		Collections.sort(regions, new RasterRegion.RegionComparer());

		return regions;
	}

	public static void getRegions(String slova) {
		int[][] temp = deepCopyIntMatrix(image);
		regions = ImageUtil.regionLabeling(temp);

		Collections.sort(regions, new RasterRegion.RegionComparer());

		int regId = 0;
		int redBr = 0;
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
			rasterRegion.tag = String.valueOf(slova.charAt(regId));
			if (!alfabet.containsKey(rasterRegion.tag)) {
				alfabet.put((String) rasterRegion.tag, redBr);
				alfabetInv.put(redBr, (String) rasterRegion.tag);
				redBr++;
			}
			regId++;
		}
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
