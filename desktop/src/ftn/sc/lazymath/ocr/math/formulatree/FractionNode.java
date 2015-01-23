package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

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
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Collections.sort(this.numerators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.minX - secondNode.minX);
			}
		});

		Collections.sort(this.denominators, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.minX - secondNode.minX);
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

	public boolean isInside(RasterRegion root) {
		System.out.println("ROOT: " + root.tag);
		System.out.println("FL: " + this.fractionLine.tag);
		if ((this.fractionLine.xM > root.minX) && (this.fractionLine.xM < root.maxX)
				&& (this.fractionLine.yM > root.minY) && (this.fractionLine.yM < root.maxY)) {
			return true;
		}

		return false;
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
		return this.numerators;
	}

	public void setNumerators(List<AbstractNode> numerators) {
		this.numerators = numerators;
	}

	public List<AbstractNode> getDenominators() {
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

}
