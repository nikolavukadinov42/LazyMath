package sc.lazymath.ocr.math.formulatree;

import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class Formula {

    private List<AbstractNode> nodes;

    public Formula(List<RasterRegion> regions) {
        for(RasterRegion region : regions){
            // TODO
        }
    }

    public void addNode(AbstractNode node){
        this.nodes.add(node);
    }

    public List<AbstractNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AbstractNode> nodes) {
        this.nodes = nodes;
    }
}
