package sc.lazymath.ocr.math.root;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;
import sc.lazymath.ocr.math.simplenode.SimpleNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class NthRootNode extends AbstractNode {

	private AbstractNode degree;
	private List<AbstractNode> elements;

	public NthRootNode() {
		this.elements = new ArrayList<>();
	}

	@Override
	public List<RasterRegion> getRasterRegions() {
		List<RasterRegion> ret = new ArrayList<>();

		ret.add(this.region);

		if (this.degree != null) {
			ret.addAll(this.degree.getRasterRegions());
		}

		for (AbstractNode node : this.elements) {
			ret.addAll(node.getRasterRegions());
		}

		return ret;
	}

	@Override
	public List<SimpleNode> getDefaultNodes() {
		List<SimpleNode> defaultNodes = new ArrayList<SimpleNode>();

		if (this.degree != null && this.degree.getRasterRegion() != null) {
			defaultNodes.add((SimpleNode) this.degree);
		}

		for (AbstractNode node : this.elements) {
			if (node instanceof SimpleNode) {
				defaultNodes.add((SimpleNode) node);
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

	/**
	 * @return "(elements)^(1/degree)" or if no degree "(elements)^(1/2)"
	 */
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

		if (this.degree != null && this.degree.getRasterRegion() != null) {
			sb.append(")^(1/(" + this.degree.toString() + "))");
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

	public AbstractNode getDegree() {
		return this.degree;
	}

	public void setDegree(AbstractNode degree) {
		this.degree = degree;
	}

	public List<AbstractNode> getElements() {
		return this.elements;
	}

	public void setElements(List<AbstractNode> elements) {
		this.elements = elements;
	}

	@Override
	public double getMinY() {
		return this.region.minY;
	}

	@Override
	public double getMaxY() {
		return this.region.maxY;
	}

	@Override
	public Point getCenterWithoutExponents() {
		return this.getCenter();
	}

}
