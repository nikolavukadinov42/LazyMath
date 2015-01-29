package sc.lazymath.ocr.math.fraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;
import sc.lazymath.ocr.math.root.RootUtil;
import sc.lazymath.ocr.math.simplenode.SimpleNodeUtil;

public class FractionUtil {
	/**
	 * Creates fraction nodes from given regions. Creates them from regions that
	 * are horizontal lines which have elements above and below.
	 *
	 * @param regions
	 *            list of raster regions
	 * @return list of FractionNode-s
	 */
	public static List<AbstractNode> getFractionNodes(List<RasterRegion> regions) {
		List<AbstractNode> ret = new ArrayList<>();

		List<RasterRegion> fractionsLines = new ArrayList<>();

		// get all possible fraction lines, that is all horizontal
		// lines
		for (RasterRegion region : regions) {
			if (isFractionLineOrMinus(region)) {
				fractionsLines.add(region);
			}
		}

		if (fractionsLines.size() > 0) {
			// sort potential fraction lines by length
			Collections.sort(fractionsLines, new Comparator<RasterRegion>() {
				@Override
				public int compare(RasterRegion rr1, RasterRegion rr2) {
					return (int) (rr2.majorAxisLength - rr1.majorAxisLength);
				}
			});

			List<RasterRegion> ignore = new ArrayList<>();
			for (RasterRegion fractionLine : fractionsLines) {
				// check if the fraction line is not already in some other
				// fraction
				if (!ignore.contains(fractionLine)) {
					// call method for creating a fraction node
					FractionNode fn = createFractionNode(fractionLine, regions);

					if (fn != null) {
						fn.setFractionLine(fractionLine);
						ret.add(fn);

						// add all regions that are in the new fraction to the
						// ignore
						ignore.addAll(fn.getRasterRegions());
					}
				}
			}

			// remove all ignore elements from regions
			regions.removeAll(ignore);
		}

		return ret;
	}

	/**
	 * Creates a fraction node if there are elements above and below the given
	 * fractionLine raster region.
	 *
	 *
	 * @param fractionLine
	 *            potential fraction line
	 * @param regions
	 *            potential numerators and denumerators
	 * @return created fraction node or null
	 */
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
							above.add(region);
						} else { // region below line
							below.add(region);
						}

					}
				}
			}
		}

		// if above or below are empty its not a fraction
		if (!above.isEmpty() && !below.isEmpty()) {
			// create a fraction node
			ret = new FractionNode();
			ret.setRasterRegion(fractionLine);

			ret.addNumerators(getFractionNodes(above));
			ret.addDenominators(getFractionNodes(below));

			ret.addNumerators(RootUtil.getNthRootNodes(above, ret.getNumerators()));
			ret.addDenominators(RootUtil.getNthRootNodes(below, ret.getDenominators()));

			ret.addNumerators(SimpleNodeUtil.getSimpleNodes(above));
			ret.addDenominators(SimpleNodeUtil.getSimpleNodes(below));
		}

		return ret;
	}

	/**
	 * Check if given region is a horizontal line.
	 *
	 * @param region
	 * @return true if horizontal line, else false
	 */
	public static boolean isFractionLineOrMinus(RasterRegion region) {
		boolean ret = false;

		double ecentricityTolerance = 0.025;

		// check if eccentricity below tolerance
		if (region.eccentricity < ecentricityTolerance) {
			double theta = region.theta;

			// check if horizontal
			if (theta > Math.PI / 2) {
				theta = Math.abs(theta - Math.PI);
			}

			ret = theta < Math.PI / 4;
		}

		return ret;
	}
}
