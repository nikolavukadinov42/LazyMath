package ftn.sc.lazymath.ocr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.MathOcrUtil;
import ftn.sc.lazymath.ocr.math.formulatree.Formula;
import ftn.sc.lazymath.ocr.neuralnetwork.BackPropagation;

/**
 * Template class which is responsible for graph building.
 *
 * @author dejan
 *
 */
public abstract class OcrMath extends OcrCore {

	protected Map<String, Integer> alfabet = new HashMap<String, Integer>();
	protected Map<Integer, String> alfabetInverse = new HashMap<Integer, String>();
	protected BackPropagation backPropagation;
	// private MathTree mathTree;

	public static Formula formula = new Formula(null);

	public OcrMath() {
		super();
	}

	@Override
	public abstract String recognize();

	/**
	 * Create graph in this method
	 */
	@Override
	protected List<RasterRegion> processRegions(List<RasterRegion> regions) {
		super.processRegions(regions);
		formula = new Formula(null);
		// mathTree = new MathTree();
		// for (RasterRegion region : regions) {
		// try {
		// mathTree.addChild(new Symbol(region.tag.toString(),
		// mathTree.getRoot(), region));
		// } catch (NullPointerException e) {
		// throw new
		// NullPointerException("@See OcrBasic#findRegions hardcoded string (temporary)");
		// }
		// }
		MathOcrUtil.getDefaultNodes(regions);
		System.out.println(formula);

		// findAboveDeprecated(regions, null);
		// System.out.println(mathTree.toString());

		System.out.println("number of nodes: " + regions.size());

		this.regions = regions;
		return this.regions;
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

	private boolean isUpperRight(RasterRegion r1, RasterRegion r2) {
		return r1.yM > r2.yM && r1.minY > r2.minY && r2.minX - r1.xM > 0 && r1.maxX < r2.maxX
				&& r1.yM > r2.maxY;
	}

	protected double[] prepareImageForNeuralNetwork(int[][] image) {
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

}
