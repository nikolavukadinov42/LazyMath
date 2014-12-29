package sc.lazymath.ocr.math;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.formulatree.AbstractNode;
import sc.lazymath.ocr.math.formulatree.DefaultNode;
import sc.lazymath.ocr.math.formulatree.FractionNode;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class OcrMath {

    public static List<AbstractNode> getDefaultNodes(List<RasterRegion> regions) {
        List<AbstractNode> ret = new ArrayList<>();

        for (RasterRegion region : regions) {
            DefaultNode defaultNode = new DefaultNode(region);

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
            if (OcrMath.isFractionLineOrMinus(region)) {
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

        Log.d("getFractionNodes fraction lines num",String.valueOf(fractionsLines.size()));

        List<RasterRegion> ignore = new ArrayList<>();
        for (RasterRegion region : fractionsLines) {
            // check if the fraction line is not already in some other fraction
            if (!ignore.contains(region)) {
                FractionNode fn = new FractionNode(region, regions);

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

    public static void createFractionNode(FractionNode fractionNode, RasterRegion fractionLine,
                                          List<RasterRegion> regions) {
        List<RasterRegion> above = new ArrayList<>();
        List<RasterRegion> below = new ArrayList<>();

        // group regions by above and below fraction line
        for (RasterRegion region : regions) {
            if(region != fractionLine) {
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
            fractionNode.addNumerators(getFractionNodes(above));
            fractionNode.addDenominators(getFractionNodes(below));

            // TODO root

            fractionNode.addNumerators(getDefaultNodes(above));
            fractionNode.addDenominators(getDefaultNodes(below));
        }
    }

    public static boolean isFractionLineOrMinus(RasterRegion region) {
        boolean ret = false;

        if (region.eccentricity < 0.1) {
            double theta = region.theta;

            if (theta > Math.PI / 2) {
                theta = Math.abs(theta - Math.PI);
            }

            ret = theta < Math.PI / 4;
        }

        return ret;

    }

}
