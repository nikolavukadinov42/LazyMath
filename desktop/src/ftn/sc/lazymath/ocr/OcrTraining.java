package ftn.sc.lazymath.ocr;

import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.neuralnetwork.BackPropagation;

/**
 * Class is training BP. If we one day move this to server-client app, this
 * should be located on server and expose alphabet and necessary methods from BP
 * class.
 * 
 * @author dejan
 *
 */
public class OcrTraining extends OcrMath {

	private double[][][] trainingSet = null;
	private int numberOfSamples = 0;
	private String inputString;

	public OcrTraining(String inputString) {
		this.inputString = inputString;
	}

	@Override
	public String recognize() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected List<RasterRegion> findRegions(int[][] image) {
		super.findRegions(image);
		int regionId = 0;
		int count = 0;
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.tag = String.valueOf(inputString.charAt(regionId));
			if (!alfabet.containsKey(rasterRegion.tag)) {
				alfabet.put((String) rasterRegion.tag, count);
				alfabetInverse.put(count, (String) rasterRegion.tag);
				count++;
			}
			regionId++;
		}
		return regions;
	}

	@Override
	protected List<RasterRegion> processRegions(List<RasterRegion> regions) {
		this.regions = super.processRegions(regions);
		createTrainingSet();
		train();
		return this.regions;
	}

	protected void createTrainingSet() {
		numberOfSamples = regions.size();
		trainingSet = new double[numberOfSamples][2][64];
		for (int bpSample = 0; bpSample < numberOfSamples; bpSample++) {
			RasterRegion region = regions.get(bpSample);
			int[][] regionImage = region.determineNormalImage();
			int[][] scaledImage = ImageUtil.getScaledImage(regionImage, 64, 64);
			double[] bpInput = prepareImageForNeuralNetwork(scaledImage);
			int index = alfabet.get(region.tag);
			for (int k = 0; k < bpInput.length; k++) {
				trainingSet[bpSample][0][k] = bpInput[k];
			}
			for (int ii = 0; ii < alfabet.size(); ii++) {
				if (ii == index) {
					trainingSet[bpSample][1][ii] = 1;
				} else {
					trainingSet[bpSample][1][ii] = 0;
				}
			}
		}
	}

	protected void train() {
		// TODO: BackPropagation instance already exist
		backPropagation = new BackPropagation(numberOfSamples, trainingSet);
		backPropagation.train();
		System.out.println("Trained");
		// JOptionPane.showMessageDialog(null, "Trained", "success",
		// JOptionPane.INFORMATION_MESSAGE);
	}

}
