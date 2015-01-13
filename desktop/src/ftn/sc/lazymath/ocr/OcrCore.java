package ftn.sc.lazymath.ocr;

import java.util.Collections;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.util.CollectionUtil;

/**
 * Template Class which is responsible for image and region processing
 * @author dejan
 *
 */
public abstract class OcrCore extends OcrTemplate {
	
	public OcrCore() {
		super();
	}

	@Override
	protected int[][] getBinaryImage(int[][] image) {
		return ImageUtil.matrixToBinary(image, 200);
	}

	@Override
	protected List<RasterRegion> findRegions(int[][] image) {
		int[][] temp = CollectionUtil.deepCopyIntMatrix(image);
		regions = ImageUtil.regionLabeling(temp);
		int regionId = 0;
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
		}
		Collections.sort(regions, new RasterRegion.RegionComparer());
		
		// temp
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.tag = String.valueOf("ab+1ab+1abbb+22x+13+(x+y)2+a".charAt(regionId));
			regionId++;
		}
		return regions;
	}

	/**
	 * Merge regions here (for eg. +-, =,vector, negation)
	 */
	protected List<RasterRegion> processRegions(List<RasterRegion> regions) {
		return regions;
	}

}
