package ftn.sc.lazymath.ocr.math;

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
public class MathOcrUtil {

	public static List<AbstractNode> getNthRootNodes(List<RasterRegion> regions) {
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

		List<RasterRegion> ignore = new ArrayList<>();
		for (RasterRegion r1 : regions) {
			if (!ignore.contains(r1)) {
				NthRootNode nthRootNode = createNthRootNode(r1, regions);

				if (nthRootNode != null) {
					ret.add(nthRootNode);
					ignore.addAll(nthRootNode.getRasterRegions());
				}
			}
		}

		regions.removeAll(ignore);

		return ret;
	}

	public static NthRootNode createNthRootNode(RasterRegion r1, List<RasterRegion> regions) {
		NthRootNode ret = null;

		// List<RasterRegion> exponent = new ArrayList<>();
		RasterRegion exponent = null;
		List<RasterRegion> elements = new ArrayList<>();

		for (RasterRegion r2 : regions) {
			if (r2 != r1) {
				boolean used = false;

				// check if r2 root exponent for r1
				double cornerSize = (r1.maxY - r2.minY) / 2;
				if ((r2.xM > r1.minX) && (r2.xM < (r1.minX + cornerSize)) && (r2.yM > r1.minY)
						&& (r2.yM < r1.minY + cornerSize)) {
					exponent = r2;
					used = true;
				}

				// check if r2 element inside r1
				if (!used) {
					if ((r2.xM > r1.minX) && (r2.xM < r1.maxX) && (r2.yM > r1.minY)
							&& (r2.yM < r1.maxY)) {
						elements.add(r2);
					}
				}
			}
		}

		if (!elements.isEmpty()) {
			ret = new NthRootNode();
			ret.setRasterRegion(r1);

			ret.setExponent(new DefaultNode(exponent));

			ret.addElements(getNthRootNodes(elements));
			ret.addElements(getDefaultNodes(elements));
		}

		return ret;
	}

	public static List<AbstractNode> getDefaultNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<AbstractNode>();
		List<RasterRegion> sortedRegionsByX = new ArrayList<RasterRegion>(regions);

		Collections.sort(sortedRegionsByX, new RasterRegion.RegionComparer());
		ret = getExponents(sortedRegionsByX, ret, null);
		// for (int i = 0; i < sortedRegionsByX.size(); i++) {
		// DefaultNode defaultNode = new DefaultNode(sortedRegionsByX.get(i));
		// System.out.println("created default node from: " +
		// sortedRegionsByX.get(i).tag);
		//
		//
		//
		// ret.add(defaultNode);
		// }
		for (AbstractNode r : ret) {
			System.out.println(r);
		}
		regions.removeAll(ret);

		return ret;
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
	 * @param ret
	 * @param parent
	 */
	public static List<AbstractNode> getExponents(List<RasterRegion> regions,
			List<AbstractNode> ret, DefaultNode parent) {
		// List<RasterRegion> exponents = new ArrayList<RasterRegion>();
		// List<AbstractNode> exponentsAbstract = new ArrayList<AbstractNode>();
		// for (int i = 0; i < regions.size(); i++) {
		// for (int j = i + 1; j < regions.size(); j++) {
		// if (isUpperRight(regions.get(i), regions.get(j))) {
		// exponents.add(regions.get(j));
		// System.out.print("\n" + regions.get(j).tag + " is above " +
		// regions.get(i).tag);
		//
		// // exponentsAbstract.addAll(getFractionNodes(regions));
		// // exponentsAbstract.addAll(getNthRootNodes(regions));
		// DefaultNode dn = new DefaultNode(regions.get(j));
		// exponentsAbstract.add(dn);
		// // System.out.println("\tcreated default node from: " +
		// // dn.getRasterRegion().tag);
		// } else {
		// break;
		// }
		// }
		//
		// if (exponents.size() > 0) {
		// AbstractNode next = null;
		// if (parent == null) {
		// parent = new DefaultNode(regions.get(i));
		// parent.setExponent(new ArrayList<AbstractNode>(exponentsAbstract));
		// OcrMath.formula.addNode(parent);
		// ret.add(parent);
		// next = parent;
		// } else {
		// parent.removeExponents(exponentsAbstract);
		// AbstractNode child = parent.getExponent(regions.get(i));
		// child.setExponent(new ArrayList<AbstractNode>(exponentsAbstract));
		// next = child;
		// }
		// // DefaultNode child = new DefaultNode(regions.get(i));
		//
		// System.out.println("----------------------");
		// System.out.println();
		// regions.removeAll(exponents);
		// getExponents(exponents, ret, next);
		// parent = null;
		// }
		// exponents.clear();
		// exponentsAbstract.clear();
		// }
		return ret;
	}

	private static boolean isUpperRight(RasterRegion r1, RasterRegion r2) {
		return r1.yM > r2.yM && r1.minY > r2.minY && r2.minX - r1.xM > 0 && r1.maxX < r2.maxX
				&& r1.yM > r2.maxY;
	}

	public static List<AbstractNode> getFractionNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<>();

		List<RasterRegion> fractionsLines = new ArrayList<>();

		// get all possible fraction lines
		for (RasterRegion region : regions) {
			if (MathOcrUtil.isFractionLineOrMinus(region)) {
				fractionsLines.add(region);
			}
		}

		// sort by length
		Collections.sort(fractionsLines, new Comparator<RasterRegion>() {
			@Override
			public int compare(RasterRegion rr1, RasterRegion rr2) {
				return (int) (rr2.majorAxisLength - rr1.majorAxisLength);
			}
		});

		System.out.println("getFractionNodes fraction lines num "
				+ String.valueOf(fractionsLines.size()));
		List<RasterRegion> ignore = new ArrayList<>();
		for (RasterRegion fractionLine : fractionsLines) {
			// check if the fraction line is not already in some other fraction
			if (!ignore.contains(fractionLine)) {
				FractionNode fn = createFractionNode(fractionLine, regions);

				// if the its a fraction line create a fraction
				if (fn != null) {
					ret.add(fn);
					System.out.println("getFractionNodes-" + "new fraction");
					System.out.println("getFractionNodes num-"
							+ String.valueOf(fn.getNumerators().size()));
					System.out.println("getFractionNodes denom-"
							+ String.valueOf(fn.getDenominators().size()));

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
					// region above line
					if (region.yM < fractionLine.yM) {
						above.add(region);
					} else { // region below line
						below.add(region);
					}
				}
			}
		}

		System.out.println("createFractionNode " + above.size() + ", " + below.size());
		;

		// if above or below are empty its not a fraction
		if (!above.isEmpty() && !below.isEmpty()) {
			// create a fraction node
			ret = new FractionNode();
			ret.setRasterRegion(fractionLine);

			ret.addNumerators(getFractionNodes(above));
			ret.addDenominators(getFractionNodes(below));

			ret.addNumerators(getNthRootNodes(above));
			ret.addDenominators(getNthRootNodes(below));

			ret.addNumerators(getDefaultNodes(above));
			ret.addDenominators(getDefaultNodes(below));
		}

		return ret;
	}

	public static boolean isFractionLineOrMinus(RasterRegion region) {
		boolean ret = false;

		if (region.eccentricity < 0.025) {
			double theta = region.theta;

			if (theta > Math.PI / 2) {
				theta = Math.abs(theta - Math.PI);
			}

			ret = theta < Math.PI / 4;
		}

		return ret;

	}

}
