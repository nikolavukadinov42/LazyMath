package sc.lazymath.ocr.math.simplenode;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;


/**
 * Created by nikola42 on 12/29/2014.
 */
public class SimpleNode extends AbstractNode {

	protected List<AbstractNode> exponent = new ArrayList<AbstractNode>();
	private SimpleNode index;

	public SimpleNode(RasterRegion region) {
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

	@Override
	public List<SimpleNode> getDefaultNodes() {
		List<SimpleNode> defaultNodes = new ArrayList<SimpleNode>();
		if (this.region != null) {
			defaultNodes.add(this);
		}
		return defaultNodes;
	}

	@Override
	public Point getCenter() {
		double x = 0;
		double y = 0;

		x += this.region.minX + (this.region.maxX - this.region.minX) / 2;
		y += this.region.minY + (this.region.maxY - this.region.minY) / 2;

		for (AbstractNode node : this.exponent) {
			Point center = node.getCenter();

			x += center.x;
			y += center.y;
		}

		return new Point((int) (x / (this.exponent.size() + 1)),
				(int) (y / (this.exponent.size() + 1)));
	}

	@Override
	public Point getCenterWithoutExponents() {
		double x = 0;
		double y = 0;

		x += this.region.minX + (this.region.maxX - this.region.minX) / 2;
		y += this.region.minY + (this.region.maxY - this.region.minY) / 2;

		return new Point((int) x, (int) y);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.region.tag);

		if (this.index != null) {
			sb.append("_(" + this.index.getRasterRegion().tag + ")");
		}

		if (this.exponent.size() > 0) {
			sb.append("^(");
		}

		for (AbstractNode abstractNode : this.exponent) {
			sb.append(abstractNode);
		}

		if (this.exponent.size() > 0) {
			sb.append(")");
		}

		return sb.toString();
	}

	public AbstractNode getExponent(RasterRegion region) {
		for (AbstractNode abstractNode : this.exponent) {
			if (abstractNode.getRasterRegion() == region) {
				return abstractNode;
			}
		}

		return null;
	}

	public List<AbstractNode> getExponents() {
		return this.exponent;
	}

	public List<RasterRegion> getExponentsRegions() {
		List<RasterRegion> regions = new ArrayList<RasterRegion>();
		for (AbstractNode node : this.exponent) {
			regions.add(node.getRasterRegion());
		}
		return regions;
	}

	public void addExponent(AbstractNode exponent) {
		this.exponent.add(exponent);
	}

	public void removeExponent(AbstractNode exponent) {
		this.exponent.remove(exponent);
	}

	public void setExponent(List<AbstractNode> exponent) {
		this.exponent = exponent;
	}

	public void removeExponents(List<AbstractNode> exponents) {
		this.exponent.removeAll(exponents);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof SimpleNode)) {
			return false;
		}
		SimpleNode node = (SimpleNode) obj;
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

	@Override
	public double getMinY() {
		return this.region.minY;
	}

	@Override
	public double getMaxY() {
		return this.region.maxY;
	}

	public void addIndex(SimpleNode index) {
		this.index = index;
	}

	public SimpleNode getIndex() {
		return this.index;
	}

	public void setIndex(SimpleNode index) {
		this.index = index;
	}

}
