package sc.lazymath.ocr.math;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.formulatree.AbstractNode;
import sc.lazymath.ocr.math.formulatree.SimpleNode;
import sc.lazymath.ocr.math.formulatree.FractionNode;
import sc.lazymath.ocr.math.formulatree.NthRootNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class MathOcr {

    public static List<AbstractNode> getNthRootNodes(List<RasterRegion> regions) {
        List<AbstractNode> ret = new ArrayList<>();

        // sort by length
        List<RasterRegion> sortedBySize = new ArrayList<>(regions);
        Collections.sort(sortedBySize, new Comparator<RasterRegion>() {
            @Override
            public int compare(RasterRegion rr1, RasterRegion rr2) {
                return (int) (rr2.majorAxisLength * rr2.minorAxisLength - rr1.majorAxisLength *
                        rr1.minorAxisLength);
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

        //List<RasterRegion> exponent = new ArrayList<>();
        RasterRegion exponent = null;
        List<RasterRegion> elements = new ArrayList<>();

        for (RasterRegion r2 : regions) {
            if(r2 != r1) {
                boolean used = false;

                // check if r2 root exponent for r1
                double cornerSize = (r1.maxY - r2.minY) / 2;
                if ((r2.xM > r1.minX) && (r2.xM < (r1.minX + cornerSize)) && (r2.yM > r1.minY) &&
                        (r2.yM < r1.minY + cornerSize)) {
                    exponent = r2;
                    used = true;
                }

                // check if r2 element inside r1
                if (!used) {
                    if ((r2.xM > r1.minX) && (r2.xM < r1.maxX) && (r2.yM > r1.minY) && (r2.yM < r1.maxY)) {
                        elements.add(r2);
                    }
                }
            }
        }

        if (!elements.isEmpty()) {
            ret = new NthRootNode();
            ret.setRasterRegion(r1);

            ret.setExponent(new SimpleNode(exponent));

            ret.addElements(getNthRootNodes(elements));
            ret.addElements(getDefaultNodes(elements));
        }

        return ret;
    }

    public static List<AbstractNode> getDefaultNodes(List<RasterRegion> regions) {
        List<AbstractNode> ret = new ArrayList<>();

        for (RasterRegion region : regions) {
            SimpleNode defaultNode = new SimpleNode(region);

            // TODO exponent

            ret.add(defaultNode);
        }

        return ret;
    }

    public static List<AbstractNode> getFractionNodes(List<RasterRegion> regions) {
        List<AbstractNode> ret = new ArrayList<>();

        List<RasterRegion> fractionsLines = new ArrayList<>();

        // get all possible fraction lines
        for (RasterRegion region : regions) {
            if (MathOcr.isFractionLineOrMinus(region)) {
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

        Log.d("getFractionNodes fraction lines num", String.valueOf(fractionsLines.size()));

        List<RasterRegion> ignore = new ArrayList<>();
        for (RasterRegion fractionLine : fractionsLines) {
            // check if the fraction line is not already in some other fraction
            if (!ignore.contains(fractionLine)) {
                FractionNode fn = createFractionNode(fractionLine, regions);

                // if the its a fraction line create a fraction
                if (fn != null) {
                    ret.add(fn);
                    Log.d("getFractionNodes", "new fraction");

                    Log.d("getFractionNodes num", String.valueOf(fn.getNumerators().size()));
                    Log.d("getFractionNodes denom", String.valueOf(fn.getDenominators().size()));

                    // add all regions that are in the new fraction to the ignore
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
                //regions X centre in fraction lines X area
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

        Log.d("createFractionNode", above.size() + ", " + below.size());

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
