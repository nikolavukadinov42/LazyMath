package ftn.sc.lazymath.ocr.neuralnetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.sc.lazymath.ocr.OcrUtil;
import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

public class NeuralNetwork {
	public BackPropagation backPropagation;
	public Map<String, Integer> alphabet = new HashMap<String, Integer>();
	public Map<Integer, String> alphabetInverse = new HashMap<Integer, String>();

	public NeuralNetwork(List<RasterRegion> regions, String input) {
		this.tagRegions(regions, input);
		this.createAlphabets(regions, input);
		double[][][] trainingSet = this.createTrainingSet(regions, input);

		this.backPropagation = new BackPropagation(regions.size(), this.alphabet.size(),
				trainingSet);
		this.backPropagation.train();
	}

	public NeuralNetwork(BackPropagation backPropagation, Map<String, Integer> alphabet,
			Map<Integer, String> alphabetInverse) {
		super();

		this.backPropagation = backPropagation;
		this.alphabet = alphabet;
		this.alphabetInverse = alphabetInverse;
	}

	public String recognize(RasterRegion region) {
		int[][] image = region.determineNormalImage();
		image = ImageUtil.getScaledImage(image, 64, 64);

		double[] input = OcrUtil.prepareImageForNeuralNetwork(image);

		int num = this.backPropagation.izracunajCifru(input);

		return this.alphabetInverse.get(num);
	}

	private void tagRegions(List<RasterRegion> regions, String input) {
		for (int i = 0; i < regions.size(); i++) {
			regions.get(i).tag = String.valueOf(input.charAt(i));
		}
	}

	private void createAlphabets(List<RasterRegion> regions, String input) {
		int count = 0;

		for (RasterRegion rasterRegion : regions) {
			if (!this.alphabet.containsKey(rasterRegion.tag)) {
				this.alphabet.put((String) rasterRegion.tag, count);
				this.alphabetInverse.put(count, (String) rasterRegion.tag);

				count++;
			}
		}
	}

	private double[][][] createTrainingSet(List<RasterRegion> regions, String input) {
		int numberOfSamples = regions.size();
		double[][][] trainingSet = new double[numberOfSamples][2][64];

		for (int bpSample = 0; bpSample < numberOfSamples; bpSample++) {
			RasterRegion region = regions.get(bpSample);

			int[][] regionImage = region.determineNormalImage();
			int[][] scaledImage = ImageUtil.getScaledImage(regionImage, 64, 64);

			double[] bpInput = OcrUtil.prepareImageForNeuralNetwork(scaledImage);

			int index = this.alphabet.get(region.tag);

			for (int k = 0; k < bpInput.length; k++) {
				trainingSet[bpSample][0][k] = bpInput[k];
			}

			for (int ii = 0; ii < this.alphabet.size(); ii++) {
				if (ii == index) {
					trainingSet[bpSample][1][ii] = 1;
				} else {
					trainingSet[bpSample][1][ii] = 0;
				}
			}
		}

		return trainingSet;
	}
}
