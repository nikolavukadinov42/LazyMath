package ftn.sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.FormulaUtil;
import ftn.sc.lazymath.ocr.math.formulatree.AbstractNode;
import ftn.sc.lazymath.ocr.math.formulatree.DefaultNode;
import ftn.sc.lazymath.ocr.math.formulatree.Formula;
import ftn.sc.lazymath.ocr.math.formulatree.FractionNode;
import ftn.sc.lazymath.ocr.math.formulatree.NthRootNode;
import ftn.sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import ftn.sc.lazymath.util.CollectionUtil;

/**
 * Template class which is responsible for graph building.
 *
 * @author dejan
 *
 */
public class OcrMath extends OcrTemplate {

	protected Formula formula;
	private String inputString;

	public OcrMath(NeuralNetwork neuralNetwork) {
		super(neuralNetwork);
	}

	public OcrMath(String inputString) {
		this.inputString = inputString;
		this.formula = new Formula(this.backupRegions);
	}

	@Override
	protected int[][] getBinaryImage(int[][] image) {
		return ImageUtil.matrixToBinary(image, 200);
	}

	@Override
	protected void findRegions(int[][] image) {
		int[][] copiedImage = CollectionUtil.deepCopyIntMatrix(image);
		this.regions = ImageUtil.regionLabeling(copiedImage);

		for (RasterRegion rasterRegion : this.regions) {
			rasterRegion.determineMoments();
		}

		Collections.sort(this.regions, new RasterRegion.RegionComparer());

		this.backupRegions.addAll(this.regions);
	}

	/**
	 * Merge regions and create graph in this method
	 */
	@Override
	protected void processRegions(List<RasterRegion> regions) {
		this.formula = new Formula(this.backupRegions);

		List<AbstractNode> nodes = new ArrayList<AbstractNode>();
		nodes.addAll(FormulaUtil.getFractionNodes(regions));
		nodes.addAll(FormulaUtil.getNthRootNodes(regions));
		nodes.addAll(FormulaUtil.getDefaultNodes(regions));

		this.formula.addNodes(nodes);
	}

	@Override
	public String recognize() {
		for (AbstractNode node : this.formula.getNodes()) {
			this.setNodeCharacter(node);
		}

		return this.formula.toString();
	}

	private void setNodeCharacter(AbstractNode node) {
		RasterRegion r = node.getRasterRegion();

		if (node instanceof DefaultNode) {
			r.tag = this.neuralNetwork.recognize(r);
		} else if (node instanceof NthRootNode) {
			NthRootNode nrn = (NthRootNode) node;

			AbstractNode e = nrn.getExponent();
			if (e != null) {
				this.setNodeCharacter(e);
			}

			for (AbstractNode n : nrn.getElements()) {
				this.setNodeCharacter(n);
			}
		} else if (node instanceof FractionNode) {
			FractionNode fn = (FractionNode) node;

			for (AbstractNode n : fn.getNumerators()) {
				this.setNodeCharacter(n);
			}

			for (AbstractNode n : fn.getDenominators()) {
				this.setNodeCharacter(n);
			}
		}
	}

	private boolean isUpperRight(RasterRegion r1, RasterRegion r2) {
		return r1.yM > r2.yM && r1.minY > r2.minY && r2.minX - r1.xM > 0 && r1.maxX < r2.maxX
				&& r1.yM > r2.maxY;
	}

	public String getInputString() {
		return this.inputString;
	}

	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

}
