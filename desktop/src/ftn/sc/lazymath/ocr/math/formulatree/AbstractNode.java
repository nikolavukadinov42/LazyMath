package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public abstract class AbstractNode {

	protected AbstractNode parent;
	protected RasterRegion region;
	public double minX;

	public abstract String getCharacters();

	public abstract List<RasterRegion> getRasterRegions();
	
	public RasterRegion getRasterRegion() {
		return this.region;
	}

	public void setRasterRegion(RasterRegion region) {
		this.region = region;
	}

	
}
