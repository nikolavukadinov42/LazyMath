//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:55
//

package sc.lazymath.ocr.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class ImageUtil {
    public static Bitmap matrixToBitmap(byte[][] image) {
//        int w = image.GetLength(1);
//        // .GetLowerBound(0);
//        int h = image.GetLength(0);
//        int PixelSize = 4;
//        Bitmap into = new Bitmap(w, h);
//        Rectangle lrEntire = new Rectangle(new Point(), new Size(w, h));
//        BitmapData lbdDest = into.LockBits(lrEntire, ImageLockMode.ReadWrite, PixelFormat.Format32bppRgb);
//        for (int y = 0;y < h;y++)
//        {
//            byte* rowDest = (byte*)lbdDest.Scan0 + (y * lbdDest.Stride);
//            for (int x = 0;x < w;x++)
//            {
//                rowDest[x * PixelSize + 0] = image[y][x];
//                rowDest[x * PixelSize + 1] = image[y][x];
//                rowDest[x * PixelSize + 2] = image[y][x];
//            }
//        }
//        into.UnlockBits(lbdDest);
        return null;
    }

    public static double mean(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        byte[][] retVal = new byte[h][w];
        double mean = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                mean += image[y][x];
            }
        }

        return mean / (w * h);
    }

    public static double mean2(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        int mean = 0;
        int count = 0;

        List<Byte> foo = new ArrayList<>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!foo.contains(image[y][x])) {
                    foo.add(image[y][x]);
                    mean += image[y][x];
                    count++;
                }

            }
        }

        return mean / count;
    }

    public static byte[][] matrixToBinary(byte[][] image, byte mean) {
        int w = image[0].length;
        int h = image.length;
        byte[][] retVal = new byte[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (image[y][x] < mean)
                    retVal[y][x] = 0;
                else
                    retVal[y][x] = (byte) 255;
            }
        }

        return retVal;
    }

    public static List<PointF> histogram(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        int dV = 1;
        int L = (256 / dV);
        int[] histogram = new int[L];
        List<PointF> points = new ArrayList<>();

        for (int i = 0; i < L; i++)
            histogram[i] = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                byte b = image[y][x];
                int index = b / dV;
                histogram[index]++;
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            points.add(new PointF(i * dV, histogram[i]));
        }

        return points;
    }

    public static byte[][] erosion(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        byte[][] retVal = new byte[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Boolean b = true;
                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w)
                        continue;

                    if (image[y + ii[t]][x + jj[t]] != 0) {
                        b = false;
                        break;
                    }

                }

                retVal[y][x] = b ? 0 : (byte) 255;
            }
        }

        return retVal;
    }

    public static byte[][] dilation(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        byte[][] retVal = new byte[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Boolean b = false;
                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w)
                        continue;

                    if (image[y + ii[t]][x + jj[t]] == 0) {
                        b = true;
                        break;
                    }

                }
                if (b == true)
                    retVal[y][x] = 0;
                else
                    retVal[y][x] = (byte) 255;
            }
        }

        return retVal;
    }

    public static List<RasterRegion> regionLabeling(byte[][] image) {
        List<RasterRegion> regions = new ArrayList<>();
        int w = image[0].length;
        int h = image.length;
        byte[][] retVal = new byte[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;
        byte regNum = 0;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (image[y][x] == 0) {
                    regNum++;
                    byte rr = (byte) (regNum * 50);
                    if (rr == 0)
                        rr = 1;

                    image[y][x] = rr;
                    List<Point> front = new ArrayList<>();
                    Point pt = new Point(x, y);
                    RasterRegion region = new RasterRegion();
                    region.regId = regNum;
                    region.points.add(pt);
                    regions.add(region);
                    front.add(pt);

                    while (front.size() > 0) {
                        Point p = front.get(0);
                        front.remove(0);

                        for (int t = 0; t < n; t++) {
                            Point point = new Point(p.x + jj[t], p.y + ii[t]);
                            if (point.x > -1 && point.x < w && point.y > -1 && point.y < h) {
                                byte pp = image[point.y][point.x];
                                if (pp == 0) {
                                    image[point.y][point.x] = image[y][x];
                                    region.points.add(point);
                                    front.add(point);
                                }

                            }

                        }
                    }
                }

            }
        }

        return regions;
    }

    public static byte[][] invert(byte[][] image) {
        int w = image[0].length;
        int h = image.length;
        byte[][] nImage = new byte[h][w];

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                nImage[y][x] = (byte) (255 - image[y][x]);
            }
        }

        return nImage;
    }

    public static byte[][] otsu(byte[][] image) {
        int ww = image[0].length;
        int hh = image.length;
        int total = ww * hh;
        List<PointF>h = ImageUtil.histogram(image);

        int sum = 0;
        for (int i = 1; i < 256; ++i)
            sum += i * (int) h.get(i).y;

        int sumB = 0;
        int wB = 0;
        int wF = 0;
        double mB;
        double mF;
        double max = 0.0;
        double between = 0.0;
        double threshold1 = 0.0;
        double threshold2 = 0.0;

        for (int i = 0; i < 256; ++i) {
            wB += (int) h.get(i).y;

            if (wB == 0)
                continue;

            wF = total - wB;

            if (wF == 0)
                break;

            sumB += i * (int) h.get(i).y;
            mB = sumB / wB;
            mF = (sum - sumB) / wF;
            between = wB * wF * Math.pow(mB - mF, 2);

            if (between >= max) {
                threshold1 = i;
                if (between > max) {
                    threshold2 = i;
                }
                max = between;
            }
        }

        return ImageUtil.matrixToBinary(image, (byte) ((threshold1 + threshold2) / 2.0));
    }
}


