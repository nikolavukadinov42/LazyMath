package ftn.sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.MathOcrUtil;
import ftn.sc.lazymath.ocr.math.formulatree.AbstractNode;
import ftn.sc.lazymath.ocr.math.formulatree.Formula;
import ftn.sc.lazymath.ocr.neuralnetwork.BackPropagation;
import ftn.sc.lazymath.util.CollectionUtil;

/**
 * Template class which is responsible for graph building.
 *
 * @author dejan
 *
 */
public class OcrMath extends OcrTemplate {

	protected Map<String, Integer> alfabet = new HashMap<String, Integer>();
	protected Map<Integer, String> alfabetInverse = new HashMap<Integer, String>();
	protected BackPropagation backPropagation;

	protected Formula formula;
	private String inputString;

	public OcrMath(Map<String, Integer> alfabet, Map<Integer, String> alfabetInverse, BackPropagation backPropagation) {
		super();
		this.alfabet = alfabet;
		this.alfabetInverse = alfabetInverse;
		this.backPropagation = backPropagation;
		formula = new Formula(backupRegions);
	}

	public OcrMath(String inputString) {
		this.inputString = inputString;
		formula = new Formula(backupRegions);
	}

	@Override
	protected int[][] getBinaryImage(int[][] image) {
		return ImageUtil.matrixToBinary(image, 200);
	}

	@Override
	protected List<RasterRegion> findRegions(int[][] image) {
		int[][] copiedImage = CollectionUtil.deepCopyIntMatrix(image);
		regions = ImageUtil.regionLabeling(copiedImage);
		int regionId = 0;
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.determineMoments();
		}
		Collections.sort(regions, new RasterRegion.RegionComparer());

		// temp
		for (RasterRegion rasterRegion : regions) {
			rasterRegion.tag = String.valueOf(inputString.charAt(regionId));
			regionId++;
		}
		backupRegions.addAll(regions);
		return regions;
	}

	/**
	 * Merge regions and create graph in this method
	 */
	@Override
	protected List<RasterRegion> processRegions(List<RasterRegion> regions) {
		List<AbstractNode> nodes = new ArrayList<AbstractNode>();
		nodes.addAll(MathOcrUtil.getFractionNodes(regions));
		nodes.addAll(MathOcrUtil.getNthRootNodes(regions));
		nodes.addAll(MathOcrUtil.getDefaultNodes(regions));

		formula.addNodes(nodes);

		System.out.println(formula);
		System.out.println("number of nodes: " + regions.size());

		this.regions = regions;
		return this.regions;
	}

	@Override
	public String recognize() {
		if (backPropagation == null) {
			return "Backpropagation is null";
			// throw new NullPointerException("Backpropagation is null");
		}
		StringBuilder stringBuilder = new StringBuilder(regions.size());
		for (RasterRegion region : regions) {
			// region.determineMoments();
			int[][] resizedImage = region.determineNormalImage();
			resizedImage = ImageUtil.getScaledImage(resizedImage, 64, 64);
			double[] input = OcrUtil.prepareImageForNeuralNetwork(resizedImage);
			int number = backPropagation.izracunajCifru(input);
			stringBuilder.append(alfabetInverse.get(number));
		}
		System.out.println("recognized: " + stringBuilder.toString());
		return stringBuilder.toString();
	}

	private boolean isUpperRight(RasterRegion r1, RasterRegion r2) {
		return r1.yM > r2.yM && r1.minY > r2.minY && r2.minX - r1.xM > 0 && r1.maxX < r2.maxX && r1.yM > r2.maxY;
	}

	public Map<String, Integer> getAlfabet() {
		return this.alfabet;
	}

	public void setAlfabet(Map<String, Integer> alfabet) {
		this.alfabet = alfabet;
	}

	public Map<Integer, String> getAlfabetInverse() {
		return this.alfabetInverse;
	}

	public void setAlfabetInverse(Map<Integer, String> alfabetInverse) {
		this.alfabetInverse = alfabetInverse;
	}

	public BackPropagation getBackPropagation() {
		return this.backPropagation;
	}

	public void setBackPropagation(BackPropagation backPropagation) {
		this.backPropagation = backPropagation;
	}

	public String getInputString() {
		return inputString;
	}

	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

	// public void findAbove(List<RasterRegion> regions, List<MathTreeNode>
	// previous, Expresion parent) {
	// List<RasterRegion> upperAboveRegions = new ArrayList<RasterRegion>();
	// for (int i = 0; i < regions.size(); i++) {
	// for (int j = i + 1; j < regions.size(); j++) {
	// if (this.isUpperRight(regions.get(i), regions.get(j))) {
	// upperAboveRegions.add(regions.get(j));
	// System.out.println(regions.get(j).tag + " is above");
	// } else if (regions.get(i) != regions.get(j)) {
	// break;
	// }
	// }
	// if (upperAboveRegions.size() > 0) {
	// System.out.println("----------------------");
	// System.out.println();
	// regions.removeAll(upperAboveRegions);
	// }
	// upperAboveRegions.clear();
	// }
	// }
	//
	// @Deprecated
	// public void findAboveDeprecated(List<RasterRegion> regions, Expresion
	// parent) {
	// List<RasterRegion> upperAboveRegions = new ArrayList<RasterRegion>();
	// List<MathTreeNode> upperAboveCurrent = new ArrayList<MathTreeNode>();
	// Expresion child = null;
	// for (int i = 0; i < regions.size(); i++) {
	// for (int j = i + 1; j < regions.size(); j++) {
	// if (this.isUpperRight(regions.get(i), regions.get(j))) {
	// upperAboveRegions.add(regions.get(j));
	// upperAboveCurrent.add(new Symbol(regions.get(j).tag.toString(), child,
	// regions
	// .get(j)));
	// System.out.println(regions.get(j).tag + " is above");
	// } else if (regions.get(i) != regions.get(j)) {
	// break;
	// }
	//
	// }
	// if (upperAboveCurrent.size() > 0) {
	// System.out.println("----------------------");
	// System.out.println();
	// if (parent == null) {
	// Symbol temp = new Symbol(regions.get(i).tag.toString(), null,
	// regions.get(i));
	// child = new Expresion(new ArrayList<MathTreeNode>(upperAboveCurrent),
	// false,
	// temp, ExpresionType.UPPERRIGHT);
	// } else {
	// child = new Expresion(new ArrayList<MathTreeNode>(upperAboveCurrent),
	// false,
	// parent, ExpresionType.UPPERRIGHT);
	// }
	// this.mathTree.addChild(child);
	// regions.removeAll(upperAboveRegions);
	// if (parent != null) {
	// parent.removeChildren(upperAboveCurrent);
	// }
	// this.findAboveDeprecated(upperAboveRegions, child);
	// }
	// upperAboveCurrent.clear();
	// upperAboveRegions.clear();
	// }
	// }

}
