package ftn.sc.lazymath.ocr;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/28/2014.
 */
public class OcrUtil {

	public static BufferedImage convertImage(BufferedImage bitmap) {
		int[][] image = convertImageToMatrix(bitmap);

		BufferedImage processed = ImageUtil.matrixToBitmap(image);

		return processed;
	}

	public static int[][] convertImageToMatrix(BufferedImage bitmap) {
		int[][] image = ImageUtil.bitmapToMatrix(bitmap);

		image = ImageUtil.christian(image);

		return image;
	}

	/**
	 * Use for screenshots, that is already black and white images.
	 *
	 * @param bitmap
	 *            black and white image
	 * @return list of raster regions from the given image
	 */
	public static List<RasterRegion> getRegions(BufferedImage bitmap) {
		List<RasterRegion> regions = null;

		int[][] image = ImageUtil.bitmapToMatrix(bitmap);
		image = ImageUtil.matrixToBinary(image, 200);

		regions = ImageUtil.regionLabeling(image);

		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
		}

		OcrMath.mergeRegions(regions);

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

			if (rasterRegion.minX == 0 || rasterRegion.maxX == w - 1 || rasterRegion.minY == 0
					|| rasterRegion.maxY == h - 1) {
				discarded.add(rasterRegion);
			}
		}

		regions.removeAll(discarded);

		Collections.sort(regions, new RasterRegion.RegionComparer());

		return regions;
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
