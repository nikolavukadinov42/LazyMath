package ftn.sc.lazymath.ocr.math;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.formulatree.AbstractNode;
import ftn.sc.lazymath.ocr.math.formulatree.DefaultNode;
import ftn.sc.lazymath.ocr.math.formulatree.FractionNode;
import ftn.sc.lazymath.ocr.math.formulatree.NthRootNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class FormulaUtil {

	private static List<AbstractNode> processed = new ArrayList<AbstractNode>();

	public static List<AbstractNode> getNthRootNodes(List<RasterRegion> regions, List<AbstractNode> nodes) {
		List<AbstractNode> ret = new ArrayList<>();

		// sort by length
		List<RasterRegion> sortedBySize = new ArrayList<>(regions);
		Collections.sort(sortedBySize, new Comparator<RasterRegion>() {
			@Override
			public int compare(RasterRegion rr1, RasterRegion rr2) {
				return (int) (rr2.majorAxisLength * rr2.minorAxisLength - rr1.majorAxisLength
						* rr1.minorAxisLength);
			}
		});

		List<RasterRegion> ignoreRegions = new ArrayList<>();
		// List<AbstractNode> ignoreNodes = new ArrayList<>();
		for (RasterRegion r1 : regions) {
			if (!ignoreRegions.contains(r1)) {
				NthRootNode nthRootNode = createNthRootNode(r1, regions, nodes);

				if (nthRootNode != null) {
					ret.add(nthRootNode);

					ignoreRegions.addAll(nthRootNode.getRasterRegions());
					// ignoreNodes.addAll(nthRootNode.getElements());
				}
			}
		}

		regions.removeAll(ignoreRegions);
		// nodes.removeAll(ignoreNodes);

		return ret;
	}

	public static NthRootNode createNthRootNode(RasterRegion rootRegion,
			List<RasterRegion> regions, List<AbstractNode> nodes) {
		NthRootNode ret = null;

		RasterRegion exponent = null;
		List<RasterRegion> elements = new ArrayList<>();
		List<AbstractNode> nodeElements = new ArrayList<>();

		List<AbstractNode> nodesToRemove = new ArrayList<AbstractNode>();

		for (AbstractNode node : nodes) {
			Point center = node.getCenter();

			if ((center.getX() > rootRegion.minX) && (center.getX() < rootRegion.maxX) && (center.getY() > rootRegion.minY)
					&& (center.getY() < rootRegion.maxY)) {
				nodeElements.add(node);
				nodesToRemove.add(node);
			}
		}
		nodes.removeAll(nodesToRemove);

		for (RasterRegion r2 : regions) {
			if (r2 != rootRegion) {
				boolean used = false;

				// check if r2 root exponent for r1
				double cornerSize = (rootRegion.maxY - r2.minY) / 2;
				if ((r2.xM > rootRegion.minX) && (r2.xM < (rootRegion.minX + cornerSize))
						&& (r2.yM > rootRegion.minY) && (r2.yM < rootRegion.minY + cornerSize)) {
					exponent = r2;
					used = true;
				}

				// check if r2 element inside r1
				if (!used) {
					if ((r2.xM > rootRegion.minX) && (r2.xM < rootRegion.maxX)
							&& (r2.yM > rootRegion.minY) && (r2.yM < rootRegion.maxY)) {
						elements.add(r2);
					}
				}
			}
		}

		for (AbstractNode node : nodes) {
			Point center = node.getCenter();

			if ((center.getX() > rootRegion.minX) && (center.getX() < rootRegion.maxX)
					&& (center.getY() > rootRegion.minY) && (center.getY() < rootRegion.maxY)) {
				nodeElements.add(node);
			}
		}

		nodes.removeAll(nodeElements);

		if (!elements.isEmpty() || !nodeElements.isEmpty()) {
			ret = new NthRootNode();
			ret.setRasterRegion(rootRegion);

			ret.setExponent(new DefaultNode(exponent));

			ret.addElements(getNthRootNodes(elements, nodeElements));
			ret.addElements(getDefaultNodes(elements));

			ret.addElements(nodeElements);
		}

		return ret;
	}

	public static List<AbstractNode> getDefaultNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<>();

		for (RasterRegion region : regions) {
			DefaultNode defaultNode = new DefaultNode(region);
			ret.add(defaultNode);
		}
		for (AbstractNode abstractNode : ret) {
			regions.remove(abstractNode.getRasterRegion());
		}

		return ret;
	}

	public static boolean isUpperRight(DefaultNode base, AbstractNode upperRight) {
		double tolerance = (base.getMaxY() - base.getMinY()) / 10;
		return base.getCenterWithoutExponents().y > upperRight.getCenter().y
				&& base.getMinY() > upperRight.getMinY() - tolerance
				&& upperRight.getMinX() - base.getCenterWithoutExponents().x > 0
				&& base.getMaxX() < upperRight.getMaxX()
				&& (base.getMaxY() > upperRight.getMaxY())
				&& (base.getCenterWithoutExponents().y + tolerance > upperRight.getMaxY() || !(upperRight instanceof DefaultNode));
	}
	
	private static boolean isDownRight(DefaultNode base, DefaultNode down) {
		return base.getMaxY() < down.getMaxY() && base.getMinY() < down.getMinY() && down.getMinX() - base.getMaxX() > -5
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
	 * @param regions
	 * @param nodes
	 * @param parent
	 */
	public static List<AbstractNode> getExponents(List<AbstractNode> nodes, DefaultNode parent, List<AbstractNode> processed) {
		System.out.println();
		System.out.println();

		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i) instanceof DefaultNode && !processed.contains(nodes.get(i))) {
				DefaultNode defaultNode = (DefaultNode) nodes.get(i);
				processDefaultNodes(defaultNode, i, nodes, parent);
			} else if (nodes.get(i) instanceof FractionNode && !processed.contains(nodes.get(i))) {
				System.out.println("fraction");
				FractionNode fraction = (FractionNode) nodes.get(i);

				for (AbstractNode abstractNode : fraction.getDenominators()) {
					System.out.println(abstractNode);
				}
				getExponents(fraction.getNumerators(), null, FormulaUtil.processed);
				getExponents(fraction.getDenominators(), null, FormulaUtil.processed);
			} else if (nodes.get(i) instanceof NthRootNode && !processed.contains(nodes.get(i))) {
				System.out.println("nth");
				NthRootNode nthRoot = (NthRootNode) nodes.get(i);
				getExponents(nthRoot.getElements(), null, FormulaUtil.processed);
			}
		}
		nodes.removeAll(toRemoveExp);
		return nodes;
	}

	static List<AbstractNode> toRemoveExp = new ArrayList<AbstractNode>();

	public static void processDefaultNodes(DefaultNode base, int index, List<AbstractNode> nodes, DefaultNode parent) {
		List<AbstractNode> toProcess = new ArrayList<AbstractNode>();
		for (int j = index + 1; j < nodes.size(); j++) {
			if (nodes.get(j) instanceof DefaultNode && isDownRight(base, (DefaultNode) nodes.get(j))) {
				System.out.println(base.getRasterRegion().tag + " down " + nodes.get(j).getRasterRegion().tag);
				base.addIndex((DefaultNode) nodes.get(j));
				toProcess.add(nodes.get(j));
				toRemoveExp.add(nodes.get(j));
				break;
			} else if (isUpperRight((DefaultNode) base, nodes.get(j))) {
				toProcess.add(nodes.get(j));
				base.addExponent(nodes.get(j));
				toRemoveExp.add(nodes.get(j));
			} else {
				break;
			}
		}
		if (toProcess.size() > 0) {
			if (parent != null) {
				parent.removeExponents(toProcess);
			}
			processed.add(base);
			nodes.removeAll(toProcess);
			getExponents(toProcess, (DefaultNode) base, processed);
		}
	}

	public static List<AbstractNode> getFractionNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<>();

		List<RasterRegion> fractionsLines = new ArrayList<>();

		// get all possible fraction lines
		System.out.println("fraction Lines:");
		for (RasterRegion region : regions) {
			if (FormulaUtil.isFractionLineOrMinus(region)) {
				fractionsLines.add(region);
				System.out.println(region.tag + " " + region.eccentricity);
			}
		}

		if (fractionsLines.size() == 0) {
			return ret;
		}

		// for (RasterRegion rasterRegion : fractionsLines) {
		// regions.remove(rasterRegion);
		// }

		// sort by length
		Collections.sort(fractionsLines, new Comparator<RasterRegion>() {
			@Override
			public int compare(RasterRegion rr1, RasterRegion rr2) {
				return (int) (rr2.majorAxisLength - rr1.majorAxisLength);
			}
		});

		System.out.println("getFractionNodes fraction lines num " + String.valueOf(fractionsLines.size()));
		List<RasterRegion> ignore = new ArrayList<>();
		for (RasterRegion fractionLine : fractionsLines) {
			// check if the fraction line is not already in some other fraction
			if (!ignore.contains(fractionLine)) {
				FractionNode fn = createFractionNode(fractionLine, regions);
				// if the its a fraction line create a fraction
				if (fn != null) {
					fn.setFractionLine(fractionLine);
					ret.add(fn);
					System.out.println("Successfully created fraction");
					System.out.println("\tAbove: " + String.valueOf(fn.getNumerators().size()));
					System.out.println("\tBelow: " + String.valueOf(fn.getDenominators().size()));

					// add all regions that are in the new fraction to the
					// ignore
					ignore.addAll(fn.getRasterRegions());
				}
			}
		}

		// remove all ignore elements from regions
		regions.removeAll(ignore);

		return ret;
	}

	public static FractionNode createFractionNode(RasterRegion fractionLine,
			List<RasterRegion> regions) {
		FractionNode ret = null;

		List<RasterRegion> above = new ArrayList<>();
		List<RasterRegion> below = new ArrayList<>();

		// group regions by above and below fraction line
		for (RasterRegion region : regions) {
			if (region != fractionLine) {
				// regions X centre in fraction lines X area
				if (region.xM > fractionLine.minX && region.xM < fractionLine.maxX) {
					// fraction line not inside the region - eg root
					if (!(fractionLine.xM > region.minX && fractionLine.xM < region.maxX
							&& fractionLine.yM > region.minY && fractionLine.yM < region.maxY)) {
						// region above line
						if (region.yM < fractionLine.yM) {
							System.out.println("above: " + region.tag);
							above.add(region);
						} else { // region below line
							System.out.println("below: " + region.tag);
							below.add(region);
						}

					}
				}
			}
		}

		System.out.println("createFractionNode " + above.size() + ", " + below.size());

		// if above or below are empty its not a fraction
		if (!above.isEmpty() && !below.isEmpty()) {
			// create a fraction node
			ret = new FractionNode();
			ret.setRasterRegion(fractionLine);

			ret.addNumerators(getFractionNodes(above));
			ret.addDenominators(getFractionNodes(below));

			ret.addNumerators(getNthRootNodes(above, ret.getNumerators()));
			ret.addDenominators(getNthRootNodes(below, ret.getDenominators()));

			ret.addNumerators(getDefaultNodes(above));
			ret.addDenominators(getDefaultNodes(below));
		}

		return ret;
	}

	public static boolean isFractionLineOrMinus(RasterRegion region) {
		boolean ret = false;

		// old value = 0.025
		double ecentricityTreshold = 0.025;
		if (region.eccentricity < ecentricityTreshold) {
			double theta = region.theta;

			if (theta > Math.PI / 2) {
				theta = Math.abs(theta - Math.PI);
			}

			ret = theta < Math.PI / 4;
		}

		return ret;
	}

}
