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

    public String tag;

    @Override
    public String toString() {
        return this.tag.toString();
    }

    public void determineMoments() {
        this.xM = 0;
        this.yM = 0;
        this.theta = 0;
        this.eccentricity = 0;
        this.majorAxisLength = 0;
        this.minorAxisLength = 0;
        this.n = this.points.size();

        for (Object point : this.points) {
            Point pp = (Point) point;

            this.xM += pp.x;
            this.yM += pp.y;

            if (pp.x < this.minX) {
                this.minX = pp.x;
            }

            if (pp.x > this.maxX) {
                this.maxX = pp.x;
            }

            if (pp.y < this.minY) {
                this.minY = pp.y;
            }

            if (pp.y > this.maxY) {
                this.maxY = pp.y;
            }

        }
        this.xM = this.xM / this.n;
        this.yM = this.yM / this.n;
        for (Object point : this.points) {
            Point pp = (Point) point;
            this.c20 += (pp.x - this.xM) * (pp.x - this.xM);
            this.c11 += (pp.x - this.xM) * (pp.y - this.yM);
            this.c02 += (pp.y - this.yM) * (pp.y - this.yM);
        }

        double a = 1;
        double b = -(this.c20 + this.c02);
        double c = this.c20 * this.c02 - this.c11 * this.c11;
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
                this.eccentricity = alpha2 / alpha1;
            }

            this.majorAxisLength = alpha1;
            this.minorAxisLength = alpha2;
        }

        this.theta = 0.5 * Math.atan2(2 * this.c11, this.c20 - this.c02);
    }

    public int[][] determineImage() {
        if (!this.determinedMoments) {
            this.determineMoments();
        }

        int height = this.maxY - this.minY + 1;
        int width = this.maxX - this.minX + 1;
        int[][] retVal = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                retVal[i][j] = 255;
            }
        }
        for (Object point : this.points) {
            Point p = (Point) point;
            retVal[p.y - this.minY][p.x - this.minX] = 0;
        }
        return retVal;
    }

    public int[][] determineNormalImage() {
        RasterRegion nRegion = new RasterRegion();
        List<Point> nPoints = new ArrayList<>();
        double ugao;

        if (!this.determinedMoments) {
            this.determineMoments();
        }

        ugao = Math.PI / 2 - Math.abs(this.theta);

        for (Object point : this.points) {
            Point p = (Point) point;

            double nX = Math.cos(ugao) * (p.x - this.xM) - Math.sin(ugao) * (p.y - this.yM) + this.xM;
            double nY = Math.sin(ugao) * (p.x - this.xM) + Math.cos(ugao) * (p.y - this.yM) + this.yM;

            nPoints.add(new Point((int) nX, (int) nY));
        }

        nRegion.points = nPoints;

        return nRegion.determineImage();
    }

    public double getWidth() {
        return this.maxX - this.minX;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public static class RegionComparer implements Comparator<RasterRegion> {
        @Override
        public int compare(RasterRegion a, RasterRegion b) {
            return a.minX - b.minX;
        }
    }

}
