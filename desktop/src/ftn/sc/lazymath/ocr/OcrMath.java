package ftn.sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.Expresion;
import ftn.sc.lazymath.ocr.math.ExpresionType;
import ftn.sc.lazymath.ocr.math.MathTree;
import ftn.sc.lazymath.ocr.math.MathTreeNode;
import ftn.sc.lazymath.ocr.math.Symbol;
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

	protected MathTree mathTree;
	private List<RasterRegion> processedRegions;
	// TODO: find average gap between regions
	private int averageRegionGap = 10;

	public OcrMath(NeuralNetwork neuralNetwork) {
		super(neuralNetwork);
		mathTree = new MathTree();
		processedRegions = new ArrayList<RasterRegion>();
	}

	public OcrMath(String inputString) {
		this.inputString = inputString;
		this.formula = new Formula(this.backupRegions);
		mathTree = new MathTree();
		processedRegions = new ArrayList<RasterRegion>();
	}

	@Override
	protected int[][] getBinaryImage(int[][] image) {
		return ImageUtil.matrixToBinary(image, 200);
	}

	@Override
	protected void findRegions(int[][] image) {
		int[][] copiedImage = CollectionUtil.deepCopyIntMatrix(image);
		this.regions = OcrUtil.getRegions(copiedImage);

		if (neuralNetwork == null) {
			NeuralNetwork.tagRegions(regions, inputString);
		}

		this.backupRegions.addAll(this.regions);
	}

	/**
	 * Merge regions and create graph in this method
	 */
	@Override
	protected void processRegions(List<RasterRegion> regions) {
		this.regions = regions;

		findDominantRegions(regions, mathTree.getRoot());

		System.out.println(mathTree);
		System.out.println(mathTree.getFormula());
	}

	private void findDominantRegions(List<RasterRegion> rasterRegions, Expresion parent) {
		List<RasterRegion> regions = new ArrayList<RasterRegion>(rasterRegions);
		System.out.println("I'm in");
		Expresion upperLeft = null;
		processedRegions.removeAll(rasterRegions);
		parent.getChildren().clear();

		for (RasterRegion rasterRegion : regions) {
			for (RasterRegion region : regions) {
				if (processedRegions.contains(rasterRegion)) {
					break;
				}
				if (processedRegions.contains(region)) {
					continue;
				}
				if (isUpperLeft(region, rasterRegion)) {
					List<MathTreeNode> children = new ArrayList<MathTreeNode>();
					upperLeft = new Expresion(children, false, parent, ExpresionType.UPPERLEFT, region);
					children.add(new Symbol((String) region.tag, upperLeft, region));
					processedRegions.add(region);
				} else if (isNthRoot(rasterRegion, region)) {
					System.out.println("found root");
					List<MathTreeNode> children = new ArrayList<MathTreeNode>();
					Expresion nthRoot = new Expresion(children, false, parent, ExpresionType.NTHROOT, rasterRegion);
					if (upperLeft != null) {
						nthRoot.addChild(upperLeft);
						upperLeft = null;
					}
					parent.addChild(nthRoot);
					processRegionsInsideNthRootRecursivly(nthRoot, rasterRegion, regions);
				} else if (isFractionLine(rasterRegion, region) && (isAbove(region, rasterRegion) || isBelow(region, rasterRegion))) {
					List<MathTreeNode> fractionLineChildren = new ArrayList<MathTreeNode>();
					List<MathTreeNode> aboveFractionLineChildren = new ArrayList<MathTreeNode>();
					List<MathTreeNode> belowFractionLineChildren = new ArrayList<MathTreeNode>();
					Expresion fractionLine = new Expresion(fractionLineChildren, false, parent, ExpresionType.FRACTION, rasterRegion);
					Expresion aboveFractionLine = new Expresion(aboveFractionLineChildren, false, fractionLine, ExpresionType.ABOVE,
							rasterRegion);
					Expresion belowFractionLine = new Expresion(belowFractionLineChildren, false, fractionLine, ExpresionType.BELOW,
							rasterRegion);

					parent.addChild(fractionLine);
					fractionLine.addChild(aboveFractionLine);
					fractionLine.addChild(belowFractionLine);

					List<Symbol> aboveList = getAbove(rasterRegion, regions, fractionLine);
					aboveFractionLineChildren.addAll(aboveList);

					List<Symbol> belowList = getBelow(rasterRegion, regions, fractionLine);
					belowFractionLineChildren.addAll(belowList);

					processedRegions.add(rasterRegion);

					findDominantRegions(Symbol.getRegionsFromListOfSymbols(belowList), belowFractionLine);
					findDominantRegions(Symbol.getRegionsFromListOfSymbols(aboveList), aboveFractionLine);
				} else if (isFractionLine(region, rasterRegion) && hasAbove(region, regions)) {
					// stepforward
					setepForward(rasterRegion, regions);
				}

				else if (isUpperRight(region, rasterRegion) && averageRegionGap >= region.minX - rasterRegion.maxX) {
					System.out.println("found upper right");
					List<MathTreeNode> children = new ArrayList<MathTreeNode>();
					List<MathTreeNode> exponents = new ArrayList<MathTreeNode>();
					Expresion exponent = new Expresion(children, false, parent, ExpresionType.EXPONENT, rasterRegion);
					Expresion upperRight = new Expresion(exponents, false, exponent, ExpresionType.UPPERRIGHT, rasterRegion);

					exponent.addChild(upperRight);
					parent.addChild(exponent);

					List<Symbol> upperRightList = getUpperRight(rasterRegion, regions, upperRight);
					exponents.addAll(upperRightList);

					processedRegions.add(rasterRegion);
					findDominantRegions(Symbol.getRegionsFromListOfSymbols(upperRightList), upperRight);
				}
			}
			// add everything else to base line
			if (!processedRegions.contains(rasterRegion)) {
				Symbol symbol = new Symbol((String) rasterRegion.tag, parent, rasterRegion);
				parent.addChild(symbol);
				processedRegions.add(rasterRegion);
			}
		}
	}

	private void setepForward(RasterRegion rasterRegion, List<RasterRegion> regions) {
		System.out.println("should step forward cuz of fraction line");
	}

	private void processRegionsInsideNthRootRecursivly(Expresion nthRoot, RasterRegion rasterRegion, List<RasterRegion> regions) {
		List<MathTreeNode> inside = new ArrayList<MathTreeNode>();
		Expresion insideNthRoot = new Expresion(inside, false, nthRoot, ExpresionType.INSIDE, rasterRegion);
		nthRoot.addChild(insideNthRoot);

		List<RasterRegion> regionsToProcess = new ArrayList<RasterRegion>();

		List<Symbol> insideSymbols = getInside(rasterRegion, regions, nthRoot);
		inside.addAll(insideSymbols);
		regionsToProcess.addAll(Symbol.getRegionsFromListOfSymbols(insideSymbols));

		processedRegions.add(rasterRegion);
		findDominantRegions(regionsToProcess, insideNthRoot);
	}

	private List<Symbol> getUpperRight(RasterRegion base, List<RasterRegion> regions, Expresion parent) {
		List<Symbol> upperRight = new ArrayList<Symbol>();
		int exponentsInRow = 0; // 0 - 0 exponents found, 1 - found exponents,
								// -1 - exponents row ended
		System.out.println("\tUpper Right for:" + base.tag);
		for (RasterRegion region : regions) {
			if (isUpperRight(region, base) && exponentsInRow != -1) {
				System.out.println("\t" + region.tag);
				Symbol symbol = new Symbol((String) region.tag, parent, region);
				upperRight.add(symbol);
				processedRegions.add(region);
				exponentsInRow = 1;
			} else if (exponentsInRow == 1) {
				exponentsInRow = -1;
			}
		}
		System.out.println();
		return upperRight;
	}

	private List<Symbol> getBelow(RasterRegion fractionLine, List<RasterRegion> regions, Expresion parent) {
		List<Symbol> regionsBelow = new ArrayList<Symbol>();
		System.out.println("\tbelow:");
		for (RasterRegion region : regions) {
			if (isFractionLine(fractionLine, region) && (isAbove(region, fractionLine) || isBelow(region, fractionLine))) {
				if (isBelow(region, fractionLine)) {
					System.out.println("\t" + region.tag);
					Symbol symbol = new Symbol((String) region.tag, parent, region);
					regionsBelow.add(symbol);
					processedRegions.add(region);
				}
			}
		}
		System.out.println();
		return regionsBelow;
	}

	private List<Symbol> getAbove(RasterRegion fractionLine, List<RasterRegion> regions, Expresion parent) {
		List<Symbol> regionsAbove = new ArrayList<Symbol>();
		System.out.println("\tabove:");
		for (RasterRegion region : regions) {
			if (isFractionLine(fractionLine, region) && (isAbove(region, fractionLine) || isBelow(region, fractionLine))) {
				if (isAbove(region, fractionLine)) {
					System.out.println("\t" + region.tag);
					Symbol symbol = new Symbol((String) region.tag, parent, region);
					regionsAbove.add(symbol);
					processedRegions.add(region);
				}
			}
		}
		System.out.println();
		return regionsAbove;
	}

	private List<Symbol> getInside(RasterRegion nthRoot, List<RasterRegion> regions, Expresion parent) {
		List<Symbol> regionsInside = new ArrayList<Symbol>();
		System.out.println("Inside:");
		for (RasterRegion region : regions) {
			if (isNthRoot(nthRoot, region)) {
				Symbol symbol = new Symbol((String) region.tag, parent, region);
				regionsInside.add(symbol);
				processedRegions.add(region);
				System.out.println("\t" + region.tag);
			}
		}
		return regionsInside;
	}

	private boolean isNthRoot(RasterRegion nthRoot, RasterRegion region) {
		if ((region.xM > nthRoot.minX) && (region.xM < nthRoot.maxX) && (region.yM > nthRoot.minY) && (region.yM < nthRoot.maxY)
				&& (region != nthRoot) && !isUpperLeft(region, nthRoot)) {
			return true;
		}
		return false;
	}

	private boolean isUpperLeft(RasterRegion upperLeft, RasterRegion region) {
		double cornerSize = (region.maxY - upperLeft.minY) / 2;
		if ((upperLeft.xM > region.minX) && (upperLeft.xM < (region.minX + cornerSize)) && (upperLeft.yM > region.minY)
				&& (upperLeft.yM < region.minY + cornerSize) && (upperLeft != region) && upperLeft.minX > region.minX
				&& region.maxX > upperLeft.maxX) {
			return true;
		}
		return false;
	}
	
	private boolean hasAbove(RasterRegion fractionLine, List<RasterRegion> regions) {
		for (RasterRegion region : regions) {
			if(isAbove(region, fractionLine)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAbove(RasterRegion above, RasterRegion region) {
		if (above.xM > region.minX && above.xM < region.maxX) {
			if (above.yM < region.yM) {
				return true;
			}
		}
		return false;
	}

	private boolean isBelow(RasterRegion below, RasterRegion region) {
		if (below.xM > region.minX && below.xM < region.maxX) {
			if (below.yM > region.yM) {
				return true;
			}
		}
		return false;
	}

	private boolean isUpperRight(RasterRegion upperRight, RasterRegion base) {
		return base.yM > upperRight.yM && base.yM > upperRight.minY && upperRight.minX - base.xM > 0 && base.maxX < upperRight.maxX
				&& base.yM > upperRight.maxY && upperRight != base;
	}

	private boolean isFractionLine(RasterRegion fractionLine, RasterRegion region) {
		double ecentricityTreshold = 0.025;
		if (fractionLine.eccentricity < ecentricityTreshold) {
			double theta = fractionLine.theta;

			if (theta > Math.PI / 2) {
				theta = Math.abs(theta - Math.PI);
			}

			return theta < Math.PI / 4;
		}
		return false;
	}

	@Override
	public String recognize() {
		for (MathTreeNode child : mathTree.getChildren()) {
			this.setTag(child);
		}
		return this.mathTree.getFormula();
	}

	private void setTag(MathTreeNode child) {
		RasterRegion region = null;
		if (child instanceof Symbol) {
			region = ((Symbol) child).getRegion();
			region.tag = this.neuralNetwork.recognize(region);
		} else if (child instanceof Expresion) {
			if (((Expresion) child).getType() == ExpresionType.EXPONENT) {
				((Expresion) child).getRegion().tag = this.neuralNetwork.recognize(((Expresion) child).getRegion());
			}
			for (MathTreeNode nestedChild : ((Expresion) child).getChildren()) {
				setTag(nestedChild);
			}
		}
	}

	public String getInputString() {
		return this.inputString;
	}

	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

}
