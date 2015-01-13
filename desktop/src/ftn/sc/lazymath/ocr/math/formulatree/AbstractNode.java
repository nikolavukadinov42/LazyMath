package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public abstract class AbstractNode {
	protected RasterRegion region;

	public abstract String getCharacters();

	public abstract List<RasterRegion> getRasterRegions();

	protected List<AbstractNode> exponent = new ArrayList<AbstractNode>();

	public RasterRegion getRasterRegion() {
		return region;
	}

	public void setRasterRegion(RasterRegion region) {
		this.region = region;
	}

	public AbstractNode getExponent(RasterRegion region) {
		for (AbstractNode abstractNode : exponent) {
			if (abstractNode.region == region) {
				return abstractNode;
			}
		}
		return null;
	}

	public void setExponent(List<AbstractNode> exponent) {
		this.exponent = exponent;
	}

	public void removeExponents(List<AbstractNode> exponents) {
		this.exponent.removeAll(exponents);
	}

}
