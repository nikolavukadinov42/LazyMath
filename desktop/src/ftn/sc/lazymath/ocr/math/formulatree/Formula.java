package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class Formula {

	private List<AbstractNode> nodes;
	private List<RasterRegion> backupRegions;

	public Formula(List<RasterRegion> backupRegions) {
		this.backupRegions = backupRegions;
		this.nodes = new ArrayList<AbstractNode>();
	}

	public void addNodes(List<AbstractNode> nodes) {
		this.nodes.addAll(nodes);
	}

	public void addNode(AbstractNode node) {
		this.nodes.add(node);
	}

	public List<AbstractNode> getNodes() {
		return this.nodes;
	}

	public void setNodes(List<AbstractNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Collections.sort(this.nodes, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.minX - secondNode.minX);
			}
		});

		for (AbstractNode abstractNode : this.nodes) {
			sb.append(abstractNode + "  ");
		}

		return sb.toString();
	}
}
