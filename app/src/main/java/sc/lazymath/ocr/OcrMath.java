package sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sc.lazymath.ocr.imageprocessing.ImageUtil;
import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;
import sc.lazymath.ocr.math.Formula;
import sc.lazymath.ocr.math.fraction.FractionNode;
import sc.lazymath.ocr.math.fraction.FractionUtil;
import sc.lazymath.ocr.math.root.NthRootNode;
import sc.lazymath.ocr.math.root.RootUtil;
import sc.lazymath.ocr.math.simplenode.SimpleNode;
import sc.lazymath.ocr.math.simplenode.SimpleNodeUtil;
import sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import sc.lazymath.ocr.neuralnetwork.NeuralNetworkResult;

/**
 * Template class which is responsible for graph building.
 *
 * @author dejan
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
        this.regions = OcrUtil.getRegions(image);
    }

    /**
     * Merge regions and create graph in this method
     */
    @Override
    protected void processRegions(List<RasterRegion> regions) {
        this.formula = new Formula();

        List<AbstractNode> nodes = new ArrayList<>();
        nodes.addAll(FractionUtil.getFractionNodes(regions));
        nodes.addAll(RootUtil.getNthRootNodes(regions, nodes));
        nodes.addAll(SimpleNodeUtil.getSimpleNodes(regions));

        Collections.sort(nodes, new Comparator<AbstractNode>() {
            @Override
            public int compare(AbstractNode firstNode, AbstractNode secondNode) {
                return (int) (firstNode.getMinX() - secondNode.getMinX());
            }
        });

        for (AbstractNode node : nodes) {
            this.setNodeCharacter(node);
        }

        SimpleNodeUtil.getExponents(nodes, null, new ArrayList<AbstractNode>());

        this.formula.addNodes(nodes);
    }

    @Override
    public String recognize() {
//        for (AbstractNode node : this.formula.getNodes()) {
//            this.setNodeCharacter(node);
//        }

        return /*parseFormulaString(*/this.formula.toString();
    }

    private String parseFormulaString(String input) {
        String out = input;
        out = out.replaceAll("\\p{Blank}", "");
        out = parseIntegral(out);
        System.out.println(out);
        out = parseIndexes(out);
        return out;
    }

    private String parseIntegral(String input) {
        String newInput = input.replaceAll("R", "integrate ");
        if (!newInput.equals(input)) {
            newInput = newInput.replaceAll("d\\p{Lower}", "");
        }
        return newInput;
    }

    private String parseIndexes(String input) {
        String out = input;
        out = out.replaceAll("l_\\(og(.*?)", "log(");
        if (out == input) {
            out = out.replaceAll("l_\\(og\\)", "log");
        }

        System.out.println(out);

        Pattern pattern = Pattern.compile("_\\((.*?)\\)");
        Matcher matcher = pattern.matcher(out);
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
            out = out.replaceFirst("_\\((.*?)\\)", matcher.group(1));
        }

        out = out.replaceAll("\\(og", "log");

        pattern = Pattern.compile("log\\(\\)");
        matcher = pattern.matcher(out);
        if (matcher.find()) {
            out = out.replaceAll("log\\(\\)", "log");
        } else {
            out = out.replaceAll("log\\(", "log_(");
        }

        return out;
    }

    private void setNodeCharacter(AbstractNode node) {
        RasterRegion r = node.getRasterRegion();

        if (r != null) {
            if (node instanceof SimpleNode) {
                SimpleNode simpleNode = (SimpleNode) node;

                for (AbstractNode abstractNode : simpleNode.getExponents()) {
                    this.setNodeCharacter(abstractNode);
                }

                for (SimpleNode index : simpleNode.getIndex()) {
                    this.setNodeCharacter(index);
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
                if (regionFirst != regionSecond && !(toRemove.contains(regionFirst) || toRemove
                        .contains(regionSecond))) {
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

        if (secondHeight < firstHeight * 0.3 && secondWidth < firstWidth * 1.2 && Math.abs
                (regionFirst.xM - regionSecond.xM) < firstWidth * 1.3) {
            if ((topDistance > 0 && topDistance < firstHeight * 0.5) || (bottomDistance > 0 &&
                    bottomDistance < firstHeight * 0.5)) {
                regionFirst.points.addAll(regionSecond.points);
                toRemove.add(regionSecond);
            }
        }
    }

    private static void concatEquals(RasterRegion regionFirst, RasterRegion regionSecond,
                                     List<RasterRegion> toRemove) {
        if (FractionUtil.isFractionLineOrMinus(regionFirst) && FractionUtil.isFractionLineOrMinus
                (regionSecond)) {
            double firstWidth = regionFirst.maxX - regionFirst.minX;
            double secondWidth = regionSecond.maxX - regionSecond.minX;

            if (Math.abs(firstWidth - secondWidth) < firstWidth * 0.2 && Math.abs(regionFirst.xM
                    - regionSecond.xM) < firstWidth * 0.25 && Math.abs(regionFirst.yM -
                    regionSecond.yM) < firstWidth * 0.33) {

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
}
