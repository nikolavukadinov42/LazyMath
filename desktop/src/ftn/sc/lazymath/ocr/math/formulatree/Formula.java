package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class Formula {

    private List<AbstractNode> nodes;

    public Formula(List<RasterRegion> regions) {
    	nodes = new ArrayList<AbstractNode>();
//        for(RasterRegion region : regions){
            // TODO
//        }
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
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for (AbstractNode abstractNode : nodes) {
			sb.append(abstractNode+ " \n");
		}
    	return sb.toString();
    }
}
