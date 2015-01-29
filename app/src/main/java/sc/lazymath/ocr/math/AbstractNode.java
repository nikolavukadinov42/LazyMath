package sc.lazymath.ocr.math;

import android.graphics.Point;

import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.simplenode.SimpleNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public abstract class AbstractNode {
	protected AbstractNode parent;
	protected RasterRegion region;

	public abstract List<RasterRegion> getRasterRegions();

	public abstract Point getCenter();
	public abstract Point getCenterWithoutExponents();
	
	public abstract double getMinY();
	public abstract double getMaxY();
	
	public abstract List<SimpleNode> getDefaultNodes();

	public double getMinX() {
		return this.region.minX;
	}
	
	public double getMaxX() {
		return this.region.maxX;
	}

	public RasterRegion getRasterRegion() {
		return this.region;
	}

	public void setRasterRegion(RasterRegion region) {
		this.region = region;
	}
}
