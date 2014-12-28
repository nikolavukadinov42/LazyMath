//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:55
//

package sc.lazymath.ocr.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageUtil {
    public static int[][] bitmapToMatrix(Bitmap bitmap) {
        int byteSize = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        buffer.rewind();
        bitmap.copyPixelsToBuffer(buffer);
        buffer.rewind();
        byte[] data = new byte[byteSize];
        buffer.get(data);
        buffer.rewind();

        int pixelSize = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int rowBytes = bitmap.getRowBytes();

        int[][] ret = new int[height][width];

        for (int y = 0; y < height; y++) {
            byte[] row = Arrays.copyOfRange(data, y * rowBytes, y * rowBytes + rowBytes);

            for (int x = 0; x < width; x++) {
                int b = Math.abs((int)row[x * pixelSize + 0] + 128);// Blue
                int g = Math.abs((int)row[x * pixelSize + 1] + 128);// Green
                int r = Math.abs((int)row[x * pixelSize + 2] + 128);// Red

                int average = (int) (((double) b + (double) g + (double) r) / 3.0);

                ret[y][x] = average;
            }
        }

        return ret;
    }

    public static Bitmap matrixToBitmap(int[][] image) {
        int width = image[0].length;
        int height = image.length;
        int pixelSize = 4;

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        byte[] data = new byte[width * height * 4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int offset = y * width * pixelSize + x * pixelSize;
                byte val = (byte) (image[y][x] - 128);

                data[offset + 0] = val;
                data[offset + 1] = val;
                data[offset + 2] = val;
                data[offset + 3] = -1;
            }
        }

        ret.copyPixelsFromBuffer(ByteBuffer.wrap(data));

        return ret;
    }

    public static double mean(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        double mean = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                mean += image[y][x];
            }
        }

        return mean / (w * h);
    }

    public static double mean2(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int mean = 0;
        int count = 0;

        List<Integer> foo = new ArrayList<>();
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

    public static int[][] matrixToBinary(int[][] image, int mean) {
        int w = image[0].length;
        int h = image.length;
        int[][] retVal = new int[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (image[y][x] < mean) {
                    retVal[y][x] = 0;
                } else {
                    retVal[y][x] = 255;
                }
            }
        }

        return retVal;
    }

    public static List<PointF> histogram(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int dV = 1;
        int L = (256 / dV);
        int[] histogram = new int[L];
        List<PointF> points = new ArrayList<>();

        for (int i = 0; i < L; i++) {
            histogram[i] = 0;
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int b = image[y][x];
                int index = b / dV;
                histogram[index]++;
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            points.add(new PointF(i * dV, histogram[i]));
        }

        return points;
    }

    public static int[][] erosion(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int[][] retVal = new int[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Boolean b = true;
                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w) {
                        continue;
                    }

                    if (image[y + ii[t]][x + jj[t]] != 0) {
                        b = false;
                        break;
                    }

                }

                retVal[y][x] = b ? 0 : 255;
            }
        }

        return retVal;
    }

    public static int[][] dilation(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int[][] retVal = new int[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Boolean b = false;
                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w) {
                        continue;
                    }

                    if (image[y + ii[t]][x + jj[t]] == 0) {
                        b = true;
                        break;
                    }

                }
                if (b == true) {
                    retVal[y][x] = 0;
                } else {
                    retVal[y][x] = 255;
                }
            }
        }

        return retVal;
    }

    public static List<RasterRegion> regionLabeling(int[][] image) {
        List<RasterRegion> regions = new ArrayList<>();
        int w = image[0].length;
        int h = image.length;
        int[][] retVal = new int[h][w];
        int[] ii = {0, 1, 0, -1, 0};
        int[] jj = {1, 0, -1, 0, 0};
        int n = ii.length;
        int regNum = 0;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (image[y][x] == 0) {
                    regNum++;
                    int rr = regNum * 50;
                    if (rr == 0) {
                        rr = 1;
                    }

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
                                int pp = image[point.y][point.x];
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

    public static int[][] invert(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int[][] iImage = new int[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = image[y][x];
                int diff = 255 - p;
                iImage[y][x] = diff;
            }
        }

        return iImage;
    }

    public static int[][] otsu(int[][] image) {
        int ww = image[0].length;
        int hh = image.length;
        int total = ww * hh;
        List<PointF> h = ImageUtil.histogram(image);

        int sum = 0;
        for (int i = 1; i < 256; ++i) {
            sum += i * (int) h.get(i).y;
        }

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

            if (wB == 0) {
                continue;
            }

            wF = total - wB;

            if (wF == 0) {
                break;
            }

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

        return ImageUtil.matrixToBinary(image, (int) ((threshold1 + threshold2) / 2.0));
    }
}


