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

        mergeRegions(regions);

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

        SimpleNodeUtil.getExponents(nodes, null, new ArrayList<AbstractNode>());

        this.formula.addNodes(nodes);
    }

    @Override
    public String recognize() {
        for (AbstractNode node : this.formula.getNodes()) {
            this.setNodeCharacter(node);
        }

        return parseFormulaString(this.formula.toString());
        // return this.formula.toString();
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
        if(matcher.find()) {
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
                if (regionFirst != regionSecond && !(toRemove.contains(regionFirst) || toRemove.contains(regionSecond))) {
                    concatAbove(regionFirst, regionSecond, toRemove);
                    concatEquals(regionFirst, regionSecond, toRemove);
                }
            }
        }
        regions.removeAll(toRemove);
    }

    private static void concatAbove(RasterRegion regionFirst, RasterRegion regionSecond, List<RasterRegion> toRemove) {
        double topDistance = regionFirst.minY - regionSecond.maxY;
        double smallerHeight = regionSecond.maxY - regionSecond.minY;
        double biggerHeight = regionFirst.maxY - regionFirst.minY;
        double biggerWidth = regionFirst.maxX - regionFirst.minX;
        double smallerWidth = regionSecond.maxX - regionSecond.minX;

        if (topDistance > 0 && topDistance < biggerHeight * 0.7 && smallerHeight < biggerHeight / 5 && smallerWidth < biggerWidth * 0.6
                && isBetweenXRegion(regionFirst, regionSecond, biggerWidth * 0.7)) {
            regionFirst.points.addAll(regionSecond.points);
            toRemove.add(regionSecond);
        }
    }

    private static boolean isBetweenXRegion(RasterRegion regionFirst, RasterRegion regionSecond, double tolerance) {
        return regionSecond.minX - tolerance >= (regionFirst.minX - tolerance) && regionSecond.maxX <= (regionFirst.maxX + tolerance);
    }

    private static void concatEquals(RasterRegion regionFirst, RasterRegion regionSecond, List<RasterRegion> toRemove) {
        if (regionFirst.points.size() == regionSecond.points.size() && regionFirst.minX == regionSecond.minX
                && regionSecond.maxX == regionFirst.maxX) {
            regionFirst.points.addAll(regionSecond.points);
            toRemove.add(regionSecond);
        }
    }

    public String getInputString() {
        return this.inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

}
