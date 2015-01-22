package sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class SimpleNode extends AbstractNode {

    private List<AbstractNode> exponent;

    public SimpleNode(RasterRegion region) {
        this.region = region;

        this.exponent = new ArrayList<>();
    }

    @Override
    public String getCharacters() {
        // karakter + eksponent
        return null;
    }

    @Override
    public List<RasterRegion> getRasterRegions() {
        List<RasterRegion> ret = new ArrayList<>();

        ret.add(region);

        for (AbstractNode node : exponent){
            ret.addAll(node.getRasterRegions());
        }

        return ret;
    }

    public List<AbstractNode> getExponent() {
        return exponent;
    }

    public void setExponent(List<AbstractNode> exponent) {
        this.exponent = exponent;
    }
}
