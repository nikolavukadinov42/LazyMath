package ftn.sc.lazymath.ocr.math.fraction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.AbstractNode;
import ftn.sc.lazymath.ocr.math.simplenode.SimpleNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class FractionNode extends AbstractNode {

	private List<AbstractNode> numerators;
	private List<AbstractNode> denominators;
	private RasterRegion fractionLine;

	public FractionNode() {
		this.numerators = new ArrayList<>();
		this.denominators = new ArrayList<>();
	}

	@Override
	public List<RasterRegion> getRasterRegions() {
		List<RasterRegion> ret = new ArrayList<>();

		ret.add(this.region);

		for (AbstractNode node : this.numerators) {
			ret.addAll(node.getRasterRegions());
		}

		for (AbstractNode node : this.denominators) {
			ret.addAll(node.getRasterRegions());
		}

		return ret;
	}

	@Override
	public List<SimpleNode> getDefaultNodes() {
		List<SimpleNode> defaultNodes = new ArrayList<SimpleNode>();

		for (AbstractNode node : this.numerators) {
			defaultNodes.addAll(node.getDefaultNodes());
		}

		for (AbstractNode node : this.denominators) {
			defaultNodes.addAll(node.getDefaultNodes());
		}

		return defaultNodes;
	}

	/**
	 * Returns center as center of fraction line region, all numerators and all
	 * denominators.
	 *
	 * @return center of fraction node
	 */
	@Override
	public Point getCenter() {
		double x = 0;
		double y = 0;

		x += this.region.minX + (this.region.maxX - this.region.minX) / 2;
		y += this.region.minY + (this.region.maxY - this.region.minY) / 2;

		for (AbstractNode node : this.numerators) {
			Point center = node.getCenter();

			x += center.getX();
			y += center.getY();
		}

		for (AbstractNode node : this.denominators) {
			Point center = node.getCenter();

			x += center.getX();
			y += center.getY();
		}

		return new Point((int) (x / (this.numerators.size() + this.denominators.size() + 1)),
				(int) (y / (this.numerators.size() + this.denominators.size() + 1)));
	}

	/**
	 * @return "(above)/(below)"
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Collections.sort(this.numerators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		Collections.sort(this.denominators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		sb.append("(");

		for (AbstractNode above : this.numerators) {
			sb.append(above);
		}

		sb.append(")/(");

		for (AbstractNode below : this.denominators) {
			sb.append(below);
		}

		sb.append(")");

		return sb.toString();
	}

	public void addNumerator(AbstractNode node) {
		this.numerators.add(node);
	}

	public void addNumerators(List<AbstractNode> nodes) {
		this.numerators.addAll(nodes);
	}

	public void addDenominators(List<AbstractNode> nodes) {
		this.denominators.addAll(nodes);
	}

	public void addDenominator(AbstractNode node) {
		this.denominators.add(node);
	}

	public List<AbstractNode> getNumerators() {
		Collections.sort(this.numerators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		return this.numerators;
	}

	public void setNumerators(List<AbstractNode> numerators) {
		this.numerators = numerators;
	}

	public List<AbstractNode> getDenominators() {
		Collections.sort(this.denominators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		return this.denominators;
	}

	public void setDenominators(List<AbstractNode> denominators) {
		this.denominators = denominators;
	}

	public RasterRegion getFractionLine() {
		return this.fractionLine;
	}

	public void setFractionLine(RasterRegion fraction) {
		this.fractionLine = fraction;
	}

	@Override
	public double getMinY() {
		double min = Double.MAX_VALUE;

		for (AbstractNode abstractNode : this.numerators) {
			double minY = abstractNode.getMinY();
			if (minY < min) {
				min = minY;
			}
		}

		return min;
	}

	@Override
	public double getMaxY() {
		double max = Double.MIN_VALUE;

		for (AbstractNode abstractNode : this.denominators) {
			double maxY = abstractNode.getMaxY();
			if (maxY > max) {
				max = maxY;
			}
		}

		return max;
	}

	@Override
	public Point getCenterWithoutExponents() {
		return this.getCenter();
	}

}
