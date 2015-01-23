package ftn.sc.lazymath.ocr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.MathOcrUtil;
import ftn.sc.lazymath.ocr.math.formulatree.AbstractNode;
import ftn.sc.lazymath.ocr.neuralnetwork.BackPropagation;

/**
 * Created by nikola42 on 12/28/2014.
 */
public class OcrUtil {

	public static List<RasterRegion> regions = new ArrayList<RasterRegion>();
	public static boolean isBinaryMatrix;
	public static int[][] image;
	public static Map<String, Integer> alfabet = new HashMap<String, Integer>();
	public static Map<Integer, String> alfabetInv = new HashMap<Integer, String>();

	static double[][][] obucavajuciSkup = null;
	static int brojUzoraka = 0;
	private static BackPropagation bp;
	public static String rez = "";

	public static BufferedImage convertImage(BufferedImage bitmap) {

		int[][] image = ImageUtil.bitmapToMatrix(bitmap);

		int[][] blackAndWhite = ImageUtil.matrixToBinary(image, 200);

		// int[][] erosioned = ImageUtil.erosion(blackAndWhite);
		//
		// int[][] dilated = ImageUtil.dilation(erosioned);

		BufferedImage processed = ImageUtil.matrixToBitmap(blackAndWhite);

		regions = ImageUtil.regionLabeling(blackAndWhite);
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
		}

		Collections.sort(regions, new RasterRegion.RegionComparer());

		return processed;
	}

	public static void getBinaryMatrix(BufferedImage bitmap) {
		int[][] image = ImageUtil.bitmapToMatrix(bitmap);
		OcrUtil.image = ImageUtil.matrixToBinary(image, 200);
	}

	public static void getRegions(String slova) {
		int[][] temp = deepCopyIntMatrix(image);
		regions = ImageUtil.regionLabeling(temp);

		Collections.sort(regions, new RasterRegion.RegionComparer());

		int regId = 0;
		int redBr = 0;
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
			rasterRegion.tag = String.valueOf(slova.charAt(regId));
			if (!alfabet.containsKey(rasterRegion.tag)) {
				alfabet.put((String) rasterRegion.tag, redBr);
				alfabetInv.put(redBr, (String) rasterRegion.tag);
				redBr++;
			}
			regId++;
		}
	}

	public static int[][] deepCopyIntMatrix(int[][] input) {
		if (input == null)
			return null;
		int[][] result = new int[input.length][];
		for (int r = 0; r < input.length; r++) {
			result[r] = input[r].clone();
		}
		return result;
	}

	public static void trainingSet() {
		brojUzoraka = regions.size();
		obucavajuciSkup = new double[brojUzoraka][2][64];
		Dimension size = new Dimension(64, 64);
		for (int uzorak = 0; uzorak < brojUzoraka; uzorak++) {
			RasterRegion reg = regions.get(uzorak);
			int[][] regSlika = reg.determineNormalImage();
			// ispravi region da stoji uspravno
			int[][] nSlika = null;
			nSlika = ImageUtil.getScaledImage(regSlika, 64, 64);
			// resize na 64x64
			double[] ulaz = pripremiSlikuZaVNM(nSlika);
			// kreiraj vektor od 64 elementa na osnovu 64x64 slike
			int index = alfabet.get(reg.tag);
			for (int k = 0; k < ulaz.length; k++) {
				// nadji koji je index (u izlaznom sloju) obelezeni region
				// formiranje obucavajuceg skupa
				// - na ulaze neuronske mreze dovodi se kreirani vektor od 64
				// elementa
				obucavajuciSkup[uzorak][0][k] = ulaz[k];
				// System.out.println(obucavajuciSkup[uzorak][0][k]);
			}
			for (int ii = 0; ii < alfabet.size(); ii++) {
				// - na izlaz koji odgovara indeksu postaviti 1, na ostale 0
				if (ii == index) {
					obucavajuciSkup[uzorak][1][ii] = 1;
				} else {
					obucavajuciSkup[uzorak][1][ii] = 0;
				}
			}
		}
		JOptionPane.showMessageDialog(null, "Obucavajuci skup formiran! ", "success", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void train() {
		System.out.println("*" + brojUzoraka);
		bp = new BackPropagation(brojUzoraka, obucavajuciSkup);
		bp.train();
		JOptionPane.showMessageDialog(null, "Trained", "success", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void prepoznaj() {
		rez = "";

		// for (Entry<Integer, String> is : alfabetInv.entrySet()) {
		// System.out.println(is.getValue());
		// System.out.println(is.getKey());
		// }

		for (int i = 0; i < regions.size(); i++) {
			RasterRegion reg = regions.get(i);
			// reg.determineMoments();
			int[][] regSlika = reg.determineNormalImage();
			regSlika = ImageUtil.getScaledImage(regSlika, 64, 64);
			double[] ulaz = pripremiSlikuZaVNM(regSlika);
			int cifra = bp.izracunajCifru(ulaz);
			rez += alfabetInv.get(cifra);
		}
		System.out.println(rez);

	}

	private static double[] pripremiSlikuZaVNM(int[][] slika) {

		// na osnovu slike koja je dimenzija 64x64 napraviti vektor od 64
		// elementa
		double[] retVal = new double[64];

		for (int i = 0; i < slika.length; i++) {
			for (int j = 0; j < slika[1].length; j++) {
				if (slika[i][j] < 255) {
					int ii = i / 8;
					int jj = j / 8;

					retVal[ii * 8 + jj]++;
				}
			}
		}

		System.out.println();
		// skaliranje sa [0,64] na [-1,1]
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = retVal[i] / 32 - 1;
			System.out.println(retVal[i]);
		}
		return retVal;
	}
	
	public static double[] prepareImageForNeuralNetwork(int[][] image) {
		double[] retVal = new double[64];

		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[1].length; j++) {
				if (image[i][j] < 255) {
					int ii = i / 8;
					int jj = j / 8;
					retVal[ii * 8 + jj]++;
				}
			}
		}

		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = retVal[i] / 32 - 1;
		}
		return retVal;
	}
}
