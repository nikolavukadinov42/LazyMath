package ftn.sc.lazymath.ocr.math.formulatree;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class NthRootNode extends AbstractNode {

	private AbstractNode exponent;
	private List<AbstractNode> elements;

	public NthRootNode() {
		this.elements = new ArrayList<>();
	}

	@Override
	public List<RasterRegion> getRasterRegions() {
		List<RasterRegion> ret = new ArrayList<>();

		ret.add(this.region);

		if (this.exponent != null) {
			ret.addAll(this.exponent.getRasterRegions());
		}

		for (AbstractNode node : this.elements) {
			ret.addAll(node.getRasterRegions());
		}

		return ret;
	}
	
	public List<DefaultNode> getDefaultNodes() {
		List<DefaultNode> defaultNodes = new ArrayList<DefaultNode>();
		
		if (exponent != null && exponent.getRasterRegion() != null) {
			defaultNodes.add((DefaultNode) exponent);
		}
		
		for (AbstractNode node : elements) {
			if (node instanceof DefaultNode) {
				defaultNodes.add((DefaultNode) node);
			} else {
				defaultNodes.addAll(node.getDefaultNodes());
			}
		}
		
		return defaultNodes;
	}

	@Override
	public Point getCenter() {
		double x = 0;
		double y = 0;

		x += this.region.minX + (this.region.maxX - this.region.minX) / 2;
		y += this.region.minY + (this.region.maxY - this.region.minY) / 2;

		return new Point((int) (x / (this.elements.size() + 1)),
				(int) (y / (this.elements.size() + 1)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Collections.sort(this.elements, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		sb.append("(");

		for (AbstractNode element : this.elements) {
			sb.append(element.toString());
		}

		if (this.exponent != null && this.exponent.getRasterRegion() != null) {
			sb.append(")^(1/(" + this.exponent.toString() + "))");
		} else {
			sb.append(")^(1/2)");
		}

		return sb.toString();
	}

	public void addElement(AbstractNode node) {
		this.elements.add(node);
	}

	public void addElements(List<AbstractNode> nodes) {
		this.elements.addAll(nodes);
	}

	public AbstractNode getExponent() {
		return this.exponent;
	}

	public void setExponent(AbstractNode exponent) {
		this.exponent = exponent;
	}

	public List<AbstractNode> getElements() {
		return this.elements;
	}

	public void setElements(List<AbstractNode> elements) {
		this.elements = elements;
	}
	
	@Override
	public Point getCenterWithoutExponents() {
		return getCenter();
	}
}
