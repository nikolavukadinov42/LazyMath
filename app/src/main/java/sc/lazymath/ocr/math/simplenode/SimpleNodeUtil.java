package sc.lazymath.ocr.math.simplenode;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;
import sc.lazymath.ocr.math.fraction.FractionNode;
import sc.lazymath.ocr.math.root.NthRootNode;

public class SimpleNodeUtil {
	private static List<AbstractNode> processed = new ArrayList<AbstractNode>();

	/**
	 * Creates SimpleNode-s from given raster regions.
	 *
	 * @param regions
	 *            list of raster regions
	 * @return list of SimpleNode-s
	 */
	public static List<AbstractNode> getSimpleNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<>();

		for (RasterRegion region : regions) {
			SimpleNode simpleNode = new SimpleNode(region);
			ret.add(simpleNode);
		}

		for (AbstractNode abstractNode : ret) {
			regions.remove(abstractNode.getRasterRegion());
		}

		return ret;
	}

	public static boolean isUpperRight(SimpleNode base, AbstractNode upperRight) {
		double tolerance = (base.getMaxY() - base.getMinY()) / 10;
		return base.getCenterWithoutExponents().y > upperRight.getCenter().y
				&& base.getMinY() > upperRight.getMinY() - tolerance
				&& upperRight.getMinX() - base.getCenterWithoutExponents().x > 0
				&& base.getMaxX() < upperRight.getMaxX()
				&& (base.getMaxY() > upperRight.getMaxY())
				&& (base.getCenterWithoutExponents().y + tolerance > upperRight.getMaxY() || !(upperRight instanceof SimpleNode));
	}

	private static boolean isDownRight(SimpleNode base, SimpleNode down) {
		return base.getMaxY() < down.getMaxY() && base.getMinY() < down.getMinY()
				&& down.getMinX() - base.getMaxX() > -5
				&& down.getMinY() - base.getMinY() > ((base.getMaxY() - base.getMinY()) / 4);
	}

	/**
	 * 1. Find region with exponents in given regions 2. Get all exponents of
	 * region 3. if parent is not null remove all exponents from parent 4.
	 * repeat 1. with found exponents
	 *
	 * x^(2+y^(a^b)) -> x^(2+yab) // first itteration -> x^(2+y^(ab)) // second
	 * -> x^(2+y^(a^b)) // third
	 *
	 */
	public static List<AbstractNode> getExponents(List<AbstractNode> nodes, SimpleNode parent,
			List<AbstractNode> processed) {
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i) instanceof SimpleNode && !processed.contains(nodes.get(i))) {
				SimpleNode simpleNode = (SimpleNode) nodes.get(i);
				processSimpleNodes(simpleNode, i, nodes, parent);
			} else if (nodes.get(i) instanceof FractionNode && !processed.contains(nodes.get(i))) {
				FractionNode fraction = (FractionNode) nodes.get(i);

				for (AbstractNode abstractNode : fraction.getDenominators()) {
					System.out.println(abstractNode);
				}
				getExponents(fraction.getNumerators(), null, SimpleNodeUtil.processed);
				getExponents(fraction.getDenominators(), null, processed);
			} else if (nodes.get(i) instanceof NthRootNode && !processed.contains(nodes.get(i))) {
				NthRootNode nthRoot = (NthRootNode) nodes.get(i);
				getExponents(nthRoot.getElements(), null, SimpleNodeUtil.processed);
			}
		}
		nodes.removeAll(toRemoveExp);
		return nodes;
	}

	static List<AbstractNode> toRemoveExp = new ArrayList<AbstractNode>();

	public static void processSimpleNodes(SimpleNode base, int index, List<AbstractNode> nodes,
			SimpleNode parent) {
		List<AbstractNode> toProcess = new ArrayList<AbstractNode>();
        boolean indexes = false;

		for (int j = index + 1; j < nodes.size(); j++) {
			if (nodes.get(j) instanceof SimpleNode && isDownRight(base, (SimpleNode) nodes.get(j))) {
				base.addIndex((SimpleNode) nodes.get(j));
				toProcess.add(nodes.get(j));
				toRemoveExp.add(nodes.get(j));
                indexes = true;
				break;
			} else if (isUpperRight(base, nodes.get(j))) {
				toProcess.add(nodes.get(j));
				base.addExponent(nodes.get(j));
				toRemoveExp.add(nodes.get(j));
			} else {
				break;
			}
		}
		if (toProcess.size() > 0) {
			if (parent != null) {
                if (indexes) {
                    parent.removeIndexes(toProcess);
                } else {
                    parent.removeExponents(toProcess);
                }
			}
			processed.add(base);
			nodes.removeAll(toProcess);
			getExponents(toProcess, base, processed);
		}
	}
}
