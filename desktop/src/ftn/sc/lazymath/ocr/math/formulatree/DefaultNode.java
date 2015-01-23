package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class DefaultNode extends AbstractNode {

	protected List<AbstractNode> exponent = new ArrayList<AbstractNode>();

	public DefaultNode(RasterRegion region) {
		this.region = region;

		this.exponent = new ArrayList<>();
	}

	@Override
	public List<RasterRegion> getRasterRegions() {
		List<RasterRegion> ret = new ArrayList<>();

		ret.add(this.region);

		for (AbstractNode node : this.exponent) {
			ret.addAll(node.getRasterRegions());
		}

		return ret;
	}

	public AbstractNode getExponent(RasterRegion region) {
		for (AbstractNode abstractNode : this.exponent) {
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.region.tag);

		if (this.exponent.size() > 0) {
			sb.append("^");
		}

		for (AbstractNode abstractNode : this.exponent) {
			sb.append(abstractNode);
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DefaultNode)) {
			return false;
		}
		DefaultNode node = (DefaultNode) obj;
		if (node.getRasterRegion().equals(this.region)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.region.hashCode();
	}

}
