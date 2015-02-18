package sc.lazymath.ocr.neuralnetwork;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sc.lazymath.ocr.imageprocessing.ImageUtil;
import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.neuralnetwork.backpropagation.BackPropagation;
import sc.lazymath.ocr.neuralnetwork.backpropagation.BackPropagationOutput;

public class NeuralNetwork implements Serializable {
	public BackPropagation backPropagation;
	public Map<String, Integer> alphabet = new HashMap<String, Integer>();
	public Map<Integer, String> alphabetInverse = new HashMap<Integer, String>();

	public NeuralNetwork(List<RasterRegion> regions, String input) {
        Log.d("Camera", "tag");
        this.tagRegions(regions, input);
        Log.d("Camera", "alphabets");
		this.createAlphabets(regions);Log.d("Camera", "training set");
		double[][][] trainingSet = this.createTrainingSet(regions);
        Log.d("Camera", "bp init");
		this.backPropagation = new BackPropagation(regions.size(), this.alphabet.size(),
				trainingSet);
        Log.d("Camera", "bp train");
		this.backPropagation.train();
	}

	public NeuralNetwork(BackPropagation backPropagation, Map<String, Integer> alphabet,
			Map<Integer, String> alphabetInverse) {
		super();

		this.backPropagation = backPropagation;
		this.alphabet = alphabet;
		this.alphabetInverse = alphabetInverse;
	}

	public NeuralNetworkResult recognize(RasterRegion region) {
		int[][] image = region.determineImage();
		image = ImageUtil.getScaledImage(image, 64);

		double[] input = prepareImageForNeuralNetwork(image);

		BackPropagationOutput result = this.backPropagation.calculateOutput(input);

		return new NeuralNetworkResult(this.alphabetInverse.get(result.getIndex()),
				result.getOutputValue());
	}

	public static void tagRegions(List<RasterRegion> regions, String input) {
		for (int i = 0; i < regions.size(); i++) {
			regions.get(i).tag = String.valueOf(input.charAt(i));
		}
	}

	private void createAlphabets(List<RasterRegion> regions) {
		int count = 0;

		for (RasterRegion rasterRegion : regions) {
			if (!this.alphabet.containsKey(rasterRegion.tag)) {
				this.alphabet.put(rasterRegion.tag, count);
				this.alphabetInverse.put(count, rasterRegion.tag);

				count++;
			}
		}
	}

	private double[][][] createTrainingSet(List<RasterRegion> regions) {
		int numberOfSamples = regions.size();
		double[][][] trainingSet = new double[numberOfSamples][2][64];

		for (int bpSample = 0; bpSample < numberOfSamples; bpSample++) {
			RasterRegion region = regions.get(bpSample);

			int[][] regionImage = region.determineImage();
			int[][] scaledImage = ImageUtil.getScaledImage(regionImage, 64);

			double[] bpInput = prepareImageForNeuralNetwork(scaledImage);

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

    private double[] prepareImageForNeuralNetwork(int[][] image) {
        double[] retVal = new double[64];

        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
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
