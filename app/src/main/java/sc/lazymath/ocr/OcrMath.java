package sc.lazymath.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        SimpleNodeUtil.getExponents(nodes, null, new ArrayList<AbstractNode>());

        this.formula.addNodes(nodes);
        System.out.println(this.formula);
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

    public String getInputString() {
        return this.inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

}
