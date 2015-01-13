package ftn.sc.lazymath.ocr;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Template class for OCR algorithm.
 * @author dejan
 *
 */
public abstract class OcrTemplate {
	
	protected List<RasterRegion> regions = new ArrayList<RasterRegion>();

	public BufferedImage processImage(int[][] image) {
		image = getBinaryImage(image);
		regions = findRegions(image);
		regions = processRegions(regions);
		return ImageUtil.matrixToBitmap(image);
	}
	
	public abstract String recognize();
	
	protected abstract int[][] getBinaryImage(int[][] image);
	protected abstract List<RasterRegion> findRegions(int[][] image);
	protected abstract List<RasterRegion> processRegions(List<RasterRegion> regions);
	
	public List<RasterRegion> getRegions() {
		return Collections.unmodifiableList(regions);
	}

}
