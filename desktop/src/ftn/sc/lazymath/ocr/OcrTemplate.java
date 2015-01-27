package ftn.sc.lazymath.ocr;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.neuralnetwork.NeuralNetwork;

/**
 * Template class for OCR algorithm.
 *
 * @author dejan
 *
 */
public abstract class OcrTemplate {

	protected List<NeuralNetwork> neuralNetworks;

	protected List<RasterRegion> regions = new ArrayList<RasterRegion>();
	protected List<RasterRegion> backupRegions = new ArrayList<RasterRegion>();

	public OcrTemplate() {
	}

	public OcrTemplate(List<NeuralNetwork> neuralNetworks) {
		this.neuralNetworks = neuralNetworks;
	}

	public BufferedImage processImage(int[][] image) {
		image = this.getBinaryImage(image);
		this.findRegions(image);
		this.processRegions(this.regions);

		return ImageUtil.matrixToBitmap(image);
	}

	public abstract String recognize();

	protected abstract int[][] getBinaryImage(int[][] image);

	protected abstract void findRegions(int[][] image);

	protected abstract void processRegions(List<RasterRegion> regions);

	public List<RasterRegion> getRegions() {
		return Collections.unmodifiableList(this.regions);
	}

	public void clearRegions() {
		this.regions.clear();
	}

	public List<RasterRegion> getBackupRegions() {
		return this.backupRegions;
	}

}
