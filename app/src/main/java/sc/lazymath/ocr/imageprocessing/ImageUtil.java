package sc.lazymath.ocr.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import sc.lazymath.ocr.imageprocessing.RasterRegion;

public class ImageUtil {

    private static String toByteString(int color) {
        // Perform a bitwise AND for convenience while printing.
        // Otherwise Integer.toHexString() interprets values as integers and a
        // negative byte 0xFF will be printed as "ffffffff"
        return Integer.toHexString(color & 0xFF);
    }

    public static int[][] bitmapToMatrix(Bitmap image) {
        int iw = image.getWidth();
        int ih = image.getHeight();
        int[][] ret = new int[ih][iw];

        // note that image is processed row by row top to bottom
        for (int y = 0; y < ih; y++) {
            for (int x = 0; x < iw; x++) {

                // returns a packed pixel where each byte is a color channel
                // order is the default ARGB color model
                int pixel = image.getPixel(x, y);

                // Get pixels
                // int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                int average = (int) (((double) blue + (double) green + red) / 3.0);
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
                byte val = (byte) image[y][x];

                data[offset + 0] = val;
                data[offset + 1] = val;
                data[offset + 2] = val;
                data[offset + 3] = -1;
            }
        }

        ret.copyPixelsFromBuffer(ByteBuffer.wrap(data));

        return ret;
    }

    public static int[][] matrixToBinaryTiles(int[][] image, int R, int C) {
        int w = image[0].length;
        int h = image.length;
        double dW = (double) w / C;
        double dH = (double) h / R;
        double[][] means = new double[R][C];
        int[][] retVal = new int[h][w];

        int[] histogram = new int[255 / 2];

        int D = 4;

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                means[r][c] = 0;

                int minD = 0;
                int maxD = 0;

                int maxDif = 0;

                for (int y = 0; y < dH; y++) {
                    int A = 0;
                    int B = 0;

                    for (int x = 0; x < D; x++) {
                        A += image[(int) (r * dH) + y][(int) (c * dW) + x];
                    }

                    for (int x = D; x < 2 * D; x++) {
                        B += image[(int) (r * dH) + y][(int) (c * dW) + x];
                    }

                    for (int x = D; x < dW - D; x++) {
                        int diff = Math.abs(A - B);

                        if (diff >= maxDif) {
                            maxDif = diff;
                            minD = Math.min(A, B);
                            maxD = Math.max(A, B);
                        }

                        A -= image[(int) (r * dH) + y][(int) (c * dW) + x - D];
                        A += image[(int) (r * dH) + y][(int) (c * dW) + x];
                        B -= image[(int) (r * dH) + y][(int) (c * dW) + x];
                        B += image[(int) (r * dH) + y][(int) (c * dW) + x + D - 1];
                    }
                }

                int TT = (maxD + minD) / (2 * D);
                int DD = (maxD - minD) / D;

                histogram[DD / 2]++;

                // meanDD += DD;
                boolean allBlack = false;
                int count = (int) (dH * dW);
                for (int y = 0; y < dH; y++) {
                    for (int x = 0; x < dW; x++) {

                        if (DD > 20) {
                            if (image[(int) (r * dH) + y][(int) (c * dW) + x] < TT) {// means[r,
                                // c]){
                                retVal[(int) (r * dH) + y][(int) (c * dW) + x] = 0;
                                count--;
                            } else {
                                retVal[(int) (r * dH) + y][(int) (c * dW) + x] = 255;
                            }
                        } else {
                            if (TT < 80) {
                                retVal[(int) (r * dH) + y][(int) (c * dW) + x] = 0;
                                count--;
                            } else {
                                retVal[(int) (r * dH) + y][(int) (c * dW) + x] = 255;
                            }
                        }
                    }
                }

                if (count < 10) {
                    allBlack = true;
                }

                if (allBlack) {
                    for (int y = 0; y < dH; y++) {
                        for (int x = 0; x < dW; x++) {
                            retVal[(int) (r * dH) + y][(int) (c * dW) + x] = 255;
                        }
                    }
                }
            }
        }

        return retVal;
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
        int[] ii = { 0, 1, 0, -1, 0 };
        int[] jj = { 1, 0, -1, 0, 0 };
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int count = 0;

                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w) {
                        continue;
                    }

                    if (image[y + ii[t]][x + jj[t]] == 0) {
                        count++;
                    }

                }

                retVal[y][x] = count <= 2 ? 255 : 0;
            }
        }

        return retVal;
    }

    public static int[][] dilation(int[][] image) {
        int w = image[0].length;
        int h = image.length;
        int[][] retVal = new int[h][w];
        int[] ii = { 0, 1, 0, -1, 0 };
        int[] jj = { 1, 0, -1, 0, 0 };
        int n = ii.length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int count = 0;

                for (int t = 0; t < n; t++) {
                    if (y + ii[t] < 0 || y + ii[t] >= h || x + jj[t] < 0 || x + jj[t] >= w) {
                        continue;
                    }

                    if (image[y + ii[t]][x + jj[t]] == 0) {
                        count++;
                    }

                }

                retVal[y][x] = count >= 2 ? 0 : 255;
            }
        }

        return retVal;
    }

    public static List<RasterRegion> regionLabeling(int[][] image) {
        List<RasterRegion> regions = new ArrayList<>();

        int w = image[0].length;
        int h = image.length;

        int[] ii = { 0, 1, 0, -1, 0 };
        int[] jj = { 1, 0, -1, 0, 0 };

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

    public static int[][] christian(int[][] image) {
        int h = image.length;
        int w = image[0].length;

        int tileHeight = 40;
        int tileWidth = 40;

        int dH = (int) (Math.ceil(h / tileHeight));
        int dW = (int) (Math.ceil(w / tileWidth));

        int[][] ret = new int[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                ret[i][j] = 255;
            }
        }

        int minVal = Integer.MAX_VALUE;
        double maxStDev = -1;
        // hash for maps is y + x * 100
        Map<Integer, Double> means = new HashMap<Integer, Double>();
        Map<Integer, Double> stDevs = new HashMap<Integer, Double>();
        for (int y = 0; y < dH + 1; y++) {
            for (int x = 0; x < dW + 1; x++) {
                double mean = 0;
                double stDev = 0;

                // calculate local mean and min value
                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            int val = image[y * tileHeight + i][x * tileWidth + j];
                            mean += val;

                            if (val < minVal) {
                                minVal = val;
                            }
                        }
                    }
                }

                mean /= tileHeight * tileWidth;
                means.put(y + x * 100, mean);

                // calculate local standard deviation
                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            stDev += Math.abs(mean - image[y * tileHeight + i][x * tileWidth + j]);
                        }
                    }
                }

                stDev /= tileHeight * tileWidth;
                stDevs.put(y + x * 100, stDev);

                // calculate maximum local standard deviation
                if (stDev > maxStDev) {
                    maxStDev = stDev;
                }
            }
        }

        // apply method to locals
        double k = 0.2;
        for (int y = 0; y < dH + 1; y++) {
            for (int x = 0; x < dW + 1; x++) {
                double mean = means.get(y + x * 100);
                double stDev = stDevs.get(y + x * 100);
                int[][] tile = new int[tileHeight][tileWidth];
                int[][] cTile;

                // calculate threshold
                // T= (I - k) * m + k * M + k * s / R * (m - M)
                int threshold = (int) ((1 - k) * mean + k * minVal + k * (stDev / maxStDev)
                        * (mean - minVal));

                // apply binarization
                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            tile[i][j] = image[y * tileHeight + i][x * tileWidth + j];
                        }
                    }
                }

                cTile = matrixToBinary(tile, threshold);

                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            ret[y * tileHeight + i][x * tileWidth + j] = cTile[i][j];
                        }
                    }
                }
            }
        }

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

    public static int[][] getScaledImage(int[][] bImage, int maxSize) {
        Bitmap ret;
        Bitmap resizedBitmap;
        Bitmap imageToResize = ImageUtil.matrixToBitmap(bImage);

        int outWidth;
        int outHeight;
        int inWidth = imageToResize.getWidth();
        int inHeight = imageToResize.getHeight();

        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        resizedBitmap = Bitmap.createScaledBitmap(imageToResize, outWidth, outHeight, false);

        ret = Bitmap.createBitmap(maxSize, maxSize, resizedBitmap.getConfig());

        Canvas canvas = new Canvas(ret);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(resizedBitmap, 0, 0, null);

        return ImageUtil.bitmapToMatrix(ret);
    }

    public static int[][] sauvola(int[][] image) {
        int h = image.length;
        int w = image[0].length;

        int tileHeight = 40;
        int tileWidth = 40;

        int dH = (int) (Math.ceil(h / tileHeight));
        int dW = (int) (Math.ceil(w / tileWidth));

        int[][] ret = new int[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                ret[i][j] = 255;
            }
        }

        double k = 0.1;
        double r = 128;
        // hash for maps is y + x * 100
        for (int y = 0; y < dH + 1; y++) {
            for (int x = 0; x < dW + 1; x++) {
                // calculate local mean
                double mean = 0;
                double stDev = 0;
                int threshold;

                int[][] tile = new int[tileHeight][tileWidth];
                int[][] cTile = null;

                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            mean += image[y * tileHeight + i][x * tileWidth + j];
                        }
                    }
                }

                mean /= tileHeight * tileWidth;

                // calculate local standard deviation
                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            stDev += Math.abs(mean - image[y * tileHeight + i][x * tileWidth + j]);
                        }
                    }
                }

                stDev /= tileHeight * tileWidth;

                // calculate threshold
                threshold = (int) (mean * (1 + k * (stDev / r - 1)));

                // apply binarization
                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            tile[i][j] = image[y * tileHeight + i][x * tileWidth + j];
                        }
                    }
                }

                cTile = matrixToBinary(tile, threshold);

                for (int i = 0; i < tileHeight; i++) {
                    for (int j = 0; j < tileWidth; j++) {
                        if (y * tileHeight + i < h && x * tileWidth + j < w) {
                            ret[y * tileHeight + i][x * tileWidth + j] = cTile[i][j];
                        }
                    }
                }
            }
        }

        return ret;
    }
}
