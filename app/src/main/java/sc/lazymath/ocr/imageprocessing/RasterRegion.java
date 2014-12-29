//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:55
//

package sc.lazymath.ocr.imageprocessing;


import android.graphics.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RasterRegion {
    public int regId = 0;

    public List<Point> points = new ArrayList<>();

    public Boolean determinedMoments = false;

    public int n = 0;

    public double c20 = 0;
    public double c11 = 0;
    public double c02 = 0;

    public int minX = Integer.MAX_VALUE;
    public int maxX = 0;
    public int minY = Integer.MAX_VALUE;
    public int maxY = 0;

    public double xM = 0;
    public double yM = 0;

    public double theta = 0;
    public double eccentricity = 0;

    public double majorAxisLength = 0;
    public double minorAxisLength = 0;

    public void determineMoments() {
        xM = 0;
        yM = 0;
        theta = 0;
        eccentricity = 0;
        majorAxisLength = 0;
        minorAxisLength = 0;
        n = points.size();

        for (Object point : points) {
            Point pp = (Point) point;

            xM += pp.x;
            yM += pp.y;

            if (pp.x < minX) {
                minX = pp.x;
            }

            if (pp.x > maxX) {
                maxX = pp.x;
            }

            if (pp.y < minY) {
                minY = pp.y;
            }

            if (pp.y > maxY) {
                maxY = pp.y;
            }

        }
        xM = xM / n;
        yM = yM / n;
        for (Object point : points) {
            Point pp = (Point) point;
            c20 += (pp.x - xM) * (pp.x - xM);
            c11 += (pp.x - xM) * (pp.y - yM);
            c02 += (pp.y - yM) * (pp.y - yM);
        }

        double a = 1;
        double b = -(c20 + c02);
        double c = c20 * c02 - c11 * c11;
        double D = b * b - 4 * c;
        double alpha1 = 0;
        double alpha2 = 0;

        if (D > 0) {
            D = Math.sqrt(D);
            alpha1 = (-b + D) / 2 * a;
            alpha2 = (-b - D) / 2 * a;
            double temp1 = Math.max(alpha1, alpha2);
            double temp2 = Math.min(alpha1, alpha2);
            alpha1 = temp1;
            alpha2 = temp2;
            if (alpha1 != 0) {
                eccentricity = alpha2 / alpha1;
            }

            majorAxisLength = alpha1;
            minorAxisLength = alpha2;
        }

        theta = 0.5 * Math.atan2(2 * c11, c20 - c02);
    }

    public byte[][] determineImage() {
        if (!determinedMoments) {
            determineMoments();
        }

        int height = maxY - minY + 1;
        int width = maxX - minX + 1;
        byte[][] retVal = new byte[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                retVal[i][j] = (byte) 255;
            }
        }
        for (Object point : points) {
            Point p = (Point) point;
            retVal[p.y - minY][p.x - minX] = 0;
        }
        return retVal;
    }

    public byte[][] determineNormalImage() {
        RasterRegion nRegion = new RasterRegion();
        List<Point> nPoints = new ArrayList<>();
        double ugao;

        if (!determinedMoments) {
            determineMoments();
        }

        ugao = Math.PI / 2 - Math.abs(theta);

        for (Object point : points) {
            Point p = (Point) point;

            double nX = Math.cos(ugao) * (p.x - xM) - Math.sin(ugao) * (p.y - yM) + xM;
            double nY = Math.sin(ugao) * (p.x - xM) + Math.cos(ugao) * (p.y - yM) + yM;

            nPoints.add(new Point((int) nX, (int) nY));
        }

        nRegion.points = nPoints;

        return nRegion.determineImage();
    }

    public static class RegionComparer implements Comparator<RasterRegion> {
        public int compare(RasterRegion a, RasterRegion b) {
            return a.minX - b.minX;
        }
    }

}


