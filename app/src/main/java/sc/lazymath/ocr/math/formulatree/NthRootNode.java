package sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class NthRootNode extends AbstractNode {

    private RasterRegion exponent;
    private List<AbstractNode> elements;

    @Override
    public String getCharacters() {
        // sqrt(elements)^(1/exponent)
        return null;
    }

    @Override
    public List<RasterRegion> getRasterRegions() {
        List<RasterRegion> ret = new ArrayList<>();

        ret.add(region);

        ret.add(exponent);

        for (AbstractNode node : elements){
            ret.addAll(node.getRasterRegions());
        }

        return ret;
    }

    public RasterRegion getExponent() {
        return exponent;
    }

    public void setExponent(RasterRegion exponent) {
        this.exponent = exponent;
    }

    public List<AbstractNode> getElements() {
        return elements;
    }

    public void setElements(List<AbstractNode> elements) {
        this.elements = elements;
    }
}
