package sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.MathOcr;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class FractionNode extends AbstractNode {

    private List<AbstractNode> numerators;
    private List<AbstractNode> denominators;

    public FractionNode() {
        this.numerators = new ArrayList<>();
        this.denominators = new ArrayList<>();
    }

    @Override
    public String getCharacters() {
        // brojioci + / + imenioci
        return null;
    }

    @Override
    public List<RasterRegion> getRasterRegions() {
        List<RasterRegion> ret = new ArrayList<>();

        ret.add(region);

        for (AbstractNode node : numerators) {
            ret.addAll(node.getRasterRegions());
        }

        for (AbstractNode node : denominators) {
            ret.addAll(node.getRasterRegions());
        }

        return ret;
    }

    public void addNumerator(AbstractNode node) {
        numerators.add(node);
    }

    public void addNumerators(List<AbstractNode> nodes) {
        numerators.addAll(nodes);
    }

    public void addDenominators(List<AbstractNode> nodes) {
        denominators.addAll(nodes);
    }

    public void addDenominator(AbstractNode node) {
        denominators.add(node);
    }

    public List<AbstractNode> getNumerators() {
        return numerators;
    }

    public void setNumerators(List<AbstractNode> numerators) {
        this.numerators = numerators;
    }

    public List<AbstractNode> getDenominators() {
        return denominators;
    }

    public void setDenominators(List<AbstractNode> denominators) {
        this.denominators = denominators;
    }
}
