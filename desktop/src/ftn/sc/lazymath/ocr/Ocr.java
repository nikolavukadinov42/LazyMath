package ftn.sc.lazymath.ocr;

import java.util.Map;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.neuralnetwork.BackPropagation;


public class Ocr extends OcrMath {

	public Ocr(Map<String, Integer> alfabet, Map<Integer, String> alfabetInverse, BackPropagation backPropagation) {
		super();
		this.alfabet = alfabet;
		this.alfabetInverse = alfabetInverse;
		this.backPropagation = backPropagation;
	}

	@Override
	public String recognize() {
		StringBuilder stringBuilder = new StringBuilder(regions.size());
		for (RasterRegion region : regions) {
			// region.determineMoments();
			int[][] resizedImage = region.determineNormalImage();
			resizedImage = ImageUtil.getScaledImage(resizedImage, 64, 64);
			double[] input = prepareImageForNeuralNetwork(resizedImage);
			int number = backPropagation.izracunajCifru(input);
			stringBuilder.append(alfabetInverse.get(number));
		}
		System.out.println("recognized: " +stringBuilder.toString());
		return stringBuilder.toString();
	}

}
