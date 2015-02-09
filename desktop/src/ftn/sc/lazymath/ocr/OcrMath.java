package ftn.sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.AbstractNode;
import ftn.sc.lazymath.ocr.math.Formula;
import ftn.sc.lazymath.ocr.math.fraction.FractionNode;
import ftn.sc.lazymath.ocr.math.fraction.FractionUtil;
import ftn.sc.lazymath.ocr.math.root.NthRootNode;
import ftn.sc.lazymath.ocr.math.root.RootUtil;
import ftn.sc.lazymath.ocr.math.simplenode.SimpleNode;
import ftn.sc.lazymath.ocr.math.simplenode.SimpleNodeUtil;
import ftn.sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import ftn.sc.lazymath.ocr.neuralnetwork.NeuralNetworkResult;
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

	public OcrMath(List<NeuralNetwork> neuralNetworks) {
		super(neuralNetworks);
	}

	public OcrMath(String inputString) {
		this.inputString = inputString;
		this.formula = new Formula();
	}

	@Override
	protected int[][] getBinaryImage(int[][] image) {
		return ImageUtil.matrixToBinary(image, 200);
	}

	@Override
	protected void findRegions(int[][] image) {
		int[][] copiedImage = CollectionUtil.deepCopyIntMatrix(image);
		this.regions = OcrUtil.getRegions(copiedImage);

		// if (neuralNetwork == null)
		// NeuralNetwork.tagRegions(regions, inputString);

		this.backupRegions.addAll(this.regions);
	}

	/**
	 * Merge regions and create graph in this method
	 */
	@Override
	protected void processRegions(List<RasterRegion> regions) {
		this.formula = new Formula();

		List<AbstractNode> nodes = new ArrayList<AbstractNode>();
		nodes.addAll(FractionUtil.getFractionNodes(regions));
		nodes.addAll(RootUtil.getNthRootNodes(regions, nodes));
		nodes.addAll(SimpleNodeUtil.getSimpleNodes(regions));

		for (AbstractNode node : nodes) {
			this.setNodeCharacter(node);
		}

		Collections.sort(nodes, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.getMinX() - secondNode.getMinX());
			}
		});

		SimpleNodeUtil.getExponents(nodes, null, new ArrayList<AbstractNode>());

//		Collections.sort(nodes, new Comparator<AbstractNode>() {
//			@Override
//			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
//				return (int) (firstNode.getMinX() - secondNode.getMinX());
//			}
//		});
//
//		SimpleNodeUtil.getExponents(nodes, null, new ArrayList<AbstractNode>());

		this.formula.addNodes(nodes);
	}

	@Override
	public String recognize() {
		return this.formula.toString();
	}

	private void setNodeCharacter(AbstractNode node) {
		RasterRegion r = node.getRasterRegion();

		if (r != null) {
			if (node instanceof SimpleNode) {
				SimpleNode simpleNode = (SimpleNode) node;

				for (AbstractNode abstractNode : simpleNode.getExponents()) {
					this.setNodeCharacter(abstractNode);
				}

				if (simpleNode.getIndex() != null) {
					this.setNodeCharacter(simpleNode.getIndex());
				}

				for (AbstractNode abstractNode : simpleNode.getExponents()) {
					this.setNodeCharacter(abstractNode);
				}

				if (simpleNode.getIndex() != null) {
					this.setNodeCharacter(simpleNode.getIndex());
				}

				// recognize node - get its character
				double max = Double.MIN_VALUE;
				String character = null;
				for (NeuralNetwork neuralNetwork : this.neuralNetworks) {
					NeuralNetworkResult result = neuralNetwork.recognize(r);

					if (result.getOutputValue() > max) {
						max = result.getOutputValue();
						character = result.getCharacter();
					}
				}

				r.tag = character;
			} else if (node instanceof NthRootNode) {
				NthRootNode nrn = (NthRootNode) node;

				// call recognizing of exponent node
				AbstractNode e = nrn.getDegree();
				if (e != null) {
					this.setNodeCharacter(e);
				}

				// call recognizing of element nodes
				for (AbstractNode n : nrn.getElements()) {
					this.setNodeCharacter(n);
				}
			} else if (node instanceof FractionNode) {
				FractionNode fn = (FractionNode) node;

				// call recognizing of numerator nodes
				for (AbstractNode n : fn.getNumerators()) {
					this.setNodeCharacter(n);
				}

				// call recognizing of denominator nodes
				for (AbstractNode n : fn.getDenominators()) {
					this.setNodeCharacter(n);
				}
			}
		}
	}

	public static void mergeRegions(List<RasterRegion> regions) {
		List<RasterRegion> toRemove = new ArrayList<RasterRegion>();

		for (RasterRegion regionFirst : regions) {
			for (RasterRegion regionSecond : regions) {
				if (regionFirst != regionSecond
						&& !(toRemove.contains(regionFirst) || toRemove.contains(regionSecond))) {
					concatAboveBelow(regionFirst, regionSecond, toRemove);
					concatEquals(regionFirst, regionSecond, toRemove);
				}
			}
		}

		regions.removeAll(toRemove);
	}

	private static void concatAboveBelow(RasterRegion regionFirst, RasterRegion regionSecond,
			List<RasterRegion> toRemove) {
		double topDistance = regionFirst.minY - regionSecond.maxY;
		double bottomDistance = regionSecond.minY - regionFirst.maxY;
		double firstHeight = regionFirst.maxY - regionFirst.minY;
		double firstWidth = regionFirst.maxX - regionFirst.minX;
		double secondHeight = regionSecond.maxY - regionSecond.minY;
		double secondWidth = regionSecond.maxX - regionSecond.minX;

		if (secondHeight < firstHeight * 0.2 && secondWidth < firstWidth * 1.2
				&& Math.abs(regionFirst.xM - regionSecond.xM) < firstWidth * 1.5) {
			if ((topDistance > 0 && topDistance < firstHeight * 0.5)
					|| (bottomDistance > 0 && bottomDistance < firstHeight * 0.5)) {
				regionFirst.points.addAll(regionSecond.points);
				toRemove.add(regionSecond);
			}
		}
	}

	private static boolean isBetweenXRegion(RasterRegion regionFirst, RasterRegion regionSecond,
			double tolerance) {
		return regionSecond.minX > regionFirst.minX - tolerance
				&& regionSecond.maxX < regionFirst.maxX + tolerance;
	}

	private static void concatEquals(RasterRegion regionFirst, RasterRegion regionSecond,
			List<RasterRegion> toRemove) {
		if (FractionUtil.isFractionLineOrMinus(regionFirst)
				&& FractionUtil.isFractionLineOrMinus(regionSecond)) {
			double firstWidth = regionFirst.maxX - regionFirst.minX;
			double secondWidth = regionSecond.maxX - regionSecond.minX;

			if (Math.abs(firstWidth - secondWidth) < firstWidth * 0.2
					&& Math.abs(regionFirst.xM - regionSecond.xM) < firstWidth * 0.25
					&& Math.abs(regionFirst.yM - regionSecond.yM) < firstWidth * 0.33) {

				regionFirst.points.addAll(regionSecond.points);
				toRemove.add(regionSecond);
			}
		}
	}

	public static boolean isIndex(RasterRegion left, RasterRegion right) {
		boolean ret = false;

		String leftChar = left.tag;
		String rightChar = right.tag;

		if (isHighChar(leftChar)) {
			if (isLowChar(rightChar)) {
				if (left.maxY - left.getHeight() / 3 < right.minY) {
					ret = true;
				}
			} else if (isHighChar(rightChar) || isNormalChar(rightChar)) {
				if (left.maxY + left.getHeight() / 6 < right.maxY) {
					ret = true;
				}
			}
		} else if (isLowChar(leftChar)) {
			if (isHighChar(rightChar)) {
				if (left.minY < right.minY) {
					ret = true;
				}
			} else if (isLowChar(rightChar) || isNormalChar(rightChar)) {
				if (left.minY + left.getHeight() / 3 < right.minY) {
					ret = true;
				}
			}
		} else {
			if (isLowChar(rightChar)) {
				if (left.minY + left.getHeight() / 3 < right.minY) {
					ret = true;
				}
			} else if (isHighChar(rightChar) || isNormalChar(rightChar)) {
				if (left.maxY + left.getHeight() / 6 < right.maxY) {
					ret = true;
				}
			}
		}

		return ret;
	}

	public static boolean isHighChar(String c) {
		return "bdhikltβδθ0123456789".contains(c);
	}

	public static boolean isNormalChar(String c) {
		return "acemnorsuvwxzα".contains(c);
	}

	public static boolean isLowChar(String c) {
		return "gjpqyγ".contains(c);
	}

	public static boolean isIndexable(String c) {
		return !"0123456789+-*/=∫()[]{}±!'".contains(c);
	}

	public String getInputString() {
		return this.inputString;
	}

	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

}
