package ftn.sc.lazymath.ocr.math.formulatree;

import java.awt.Point;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public abstract class AbstractNode {
	protected AbstractNode parent;
	protected RasterRegion region;

	public abstract List<RasterRegion> getRasterRegions();

	public abstract Point getCenter();

	public double getMinX() {
		return this.region.minX;
	}

	public RasterRegion getRasterRegion() {
		return this.region;
	}

	public void setRasterRegion(RasterRegion region) {
		this.region = region;
	}
}
