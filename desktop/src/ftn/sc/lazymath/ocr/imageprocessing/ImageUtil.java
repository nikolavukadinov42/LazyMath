package ftn.sc.lazymath.ocr.imageprocessing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageUtil {

	private static String toByteString(int color) {
		// Perform a bitwise AND for convenience while printing.
		// Otherwise Integer.toHexString() interprets values as integers and a
		// negative byte 0xFF will be printed as "ffffffff"
		return Integer.toHexString(color & 0xFF);
	}

	public static int[][] bitmapToMatrix(BufferedImage image) {
		int iw = image.getWidth();
		int ih = image.getHeight();
		int[][] ret = new int[ih][iw];

		// note that image is processed row by row top to bottom
		for (int y = 0; y < ih; y++) {
			for (int x = 0; x < iw; x++) {

				// returns a packed pixel where each byte is a color channel
				// order is the default ARGB color model
				int pixel = image.getRGB(x, y);

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

	public static BufferedImage matrixToBitmap(int[][] image) {
		int width = image[0].length;
		int height = image.length;
		int pixelSize = 3;

		BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

		byte[] data = new byte[width * height * pixelSize];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = y * width * pixelSize + x * pixelSize;
				byte val = (byte) image[y][x];

				data[offset + 0] = val;
				data[offset + 1] = val;
				data[offset + 2] = val;
			}
		}

		byte[] array = ((DataBufferByte) ret.getRaster().getDataBuffer()).getData();
		System.arraycopy(data, 0, array, 0, array.length);

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

	public static List<Point2D.Float> histogram(int[][] image) {
		int w = image[0].length;
		int h = image.length;
		int dV = 1;
		int L = (256 / dV);
		int[] histogram = new int[L];
		List<Point2D.Float> points = new ArrayList<>();

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
			points.add(new Point2D.Float(i * dV, histogram[i]));
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
		List<Point2D.Float> h = ImageUtil.histogram(image);

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

	public static int[][] christiansMethod(int[][] image, double a, double b) {
		int h = image.length;
		int w = image[0].length;

		int tY = (int) (h / a);
		int tX = (int) (w / b);

		int dH = h / tY;
		int dW = w / tY;

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
		for (int y = 0; y < tY + 1; y++) {
			for (int x = 0; x < tX + 1; x++) {
				// calculate min val
				// TODO correct?
				int val = image[y][x];
				if (val < minVal) {
					minVal = val;
				}

				// calculate local mean
				double mean = 0;
				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						if (y * dH + i < h && x * dW + j < w) {
							mean += image[y * dH + i][x * dW + j];
						}
					}
				}

				mean /= dH * dW;
				means.put(y + x * 100, mean);

				// calculate local standard deviation
				double stDev = 0;
				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						if (y * dH + i < h && x * dW + j < w) {
							stDev += Math.abs(mean - image[y * dH + i][x * dW + j]);
						}
					}
				}

				stDev /= dH * dW;
				stDevs.put(y + x * 100, stDev);

				// calculate maximum local standard deviation
				if (stDev > maxStDev) {
					maxStDev = stDev;
				}
			}
		}

		// apply method to locals
		double k = 0.5;
		for (int y = 0; y < tY + 1; y++) {
			for (int x = 0; x < tX + 1; x++) {
				double mean = means.get(y + x * 100);
				double stDev = stDevs.get(y + x * 100);

				int threshold = (int) ((1 - k) * mean + k * minVal + k * (stDev / maxStDev)
						* (mean - minVal));

				int[][] tile = new int[dH][dW];
				int[][] cTile = null;

				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						if (y * dH + i < h && x * dW + j < w) {
							tile[i][j] = image[y * dH + i][x * dW + j];
						}
					}
				}

				cTile = matrixToBinary(tile, threshold);

				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						if (y * dH + i < h && x * dW + j < w) {
							ret[y * dH + i][x * dW + j] = cTile[i][j];
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

	public static int[][] getScaledImage(int[][] bImage, int newWidth, int newHeight) {
		BufferedImage imageToResize = ImageUtil.matrixToBitmap(bImage);

		int currentWidth = imageToResize.getWidth();
		int currentHeight = imageToResize.getHeight();

		int scaledWidth;
		int scaledHeight;
		if (currentWidth == 0 || currentHeight == 0
				|| (currentWidth == newWidth && currentHeight == newHeight)) {
			return bImage;
		} else if (currentWidth == currentHeight) {
			scaledWidth = newWidth;
			scaledHeight = newHeight;
		} else if (currentWidth >= currentHeight) {
			scaledWidth = newWidth;
			double scale = (double) newWidth / (double) currentWidth;
			scaledHeight = (int) Math.round(currentHeight * scale);
		} else {
			scaledHeight = newHeight;
			double scale = (double) newHeight / (double) currentHeight;
			scaledWidth = (int) Math.round(currentWidth * scale);
		}

		int x = (newWidth - scaledWidth) / 2;
		int y = (newHeight - scaledHeight) / 2;

		BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite(AlphaComposite.SrcOver);
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, newWidth, newHeight);
		g.drawImage(imageToResize, x, y, x + scaledWidth, y + scaledHeight, 0, 0, currentWidth,
				currentHeight, new Color(0, 0, 0, 0), null);
		g.dispose();

		return ImageUtil.bitmapToMatrix(resized);
	}

	public void saveImage(BufferedImage image) {
		try {
			File outputfile = new File(String.valueOf(System.currentTimeMillis()) + ".png");
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int[][] fengTanMethod(int[][] image) {
		int h = image.length;
		int w = image[0].length;

		int d = 12;

		int dH = h / d;
		int dW = w / d;

		int[][] ret = new int[h][w];

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				ret[i][j] = 255;
			}
		}

		// hash for maps is y + x * 100
		Map<Integer, Double> means = new HashMap<Integer, Double>();
		Map<Integer, Double> stDevs = new HashMap<Integer, Double>();
		Map<Integer, Integer> minVals = new HashMap<Integer, Integer>();
		for (int y = 0; y < d; y++) {
			for (int x = 0; x < d; x++) {
				int minVal = Integer.MAX_VALUE;

				// calculate local mean
				double mean = 0;
				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						mean += image[y * dH + i][x * dW + j];
					}
				}

				mean /= dH * dW;
				means.put(y + x * 100, mean);

				// calculate local standard deviation
				double stDev = 0;
				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						stDev += Math.abs(mean - image[y * dH + i][x * dW + j]);

						// check min val
						int val = image[y * dH + i][x * dW + j];
						if (val < minVal) {
							minVal = val;
						}
					}
				}

				stDev /= dH * dW;
				stDevs.put(y + x * 100, stDev);

				minVals.put(y + x * 100, minVal);
			}
		}

		for (int y = 0; y < d; y++) {
			for (int x = 0; x < d; x++) {

				// calculate larger window mean
				int count = 0;
				double lwMean = 0;

				lwMean += means.get(y + x * 100);
				count++;

				for (int i = -2; i <= 2; i++) {
					for (int j = -2; j <= 2; j++) {
						int yy = y + i;
						int xx = x + j;

						if (yy > 0 && yy < d && xx > 0 && xx < d) {
							lwMean += means.get(yy + xx * 100);
							count++;
						}
					}
				}

				lwMean = lwMean / count;

				// calculate larger window standard deviation
				double lwStDev = 0;
				count = 0;
				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						lwStDev += Math.abs(lwMean - image[y * dH + i][x * dW + j]);
						count++;

						for (int ii = -1; ii <= 1; ii++) {
							for (int jj = -1; jj <= 1; jj++) {
								int yy = y + ii;
								int xx = x + jj;

								if (yy > 0 && yy < d && xx > 0 && xx < d) {
									lwMean += means.get(yy + xx * 100);
									count++;
								}
							}
						}
					}
				}

				lwStDev = lwStDev / count;

				// apply method to locals
				double mean = means.get(y + x * 100);
				double stDev = stDevs.get(y + x * 100);
				double minVal = minVals.get(y + x * 100);

				double gamma = 2;
				double alpha1 = 0.12;
				double k1 = 0.25;
				double k2 = 0.04;

				double alpha2 = k1 * Math.pow(stDev / lwStDev, gamma);
				double alpha3 = k2 * Math.pow(stDev / lwStDev, gamma);

				int threshold = (int) ((1 - alpha1) * mean + alpha2 * (stDev / lwStDev)
						* (mean - minVal) + alpha3 * minVal);

				int[][] tile = new int[dH][dW];
				int[][] cTile = null;

				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						tile[i][j] = image[y * dH + i][x * dW + j];
					}
				}

				cTile = matrixToBinary(tile, threshold);

				for (int i = 0; i < dH; i++) {
					for (int j = 0; j < dW; j++) {
						ret[y * dH + i][x * dW + j] = cTile[i][j];
					}
				}
			}
		}

		return ret;
	}
}
