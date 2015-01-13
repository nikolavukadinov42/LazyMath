package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.math.Symbol;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class DefaultNode extends AbstractNode {

    public DefaultNode(RasterRegion region) {
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

    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(region.tag);
    	if (exponent.size() > 0) {
    		sb.append("^");
    	}
    	for (AbstractNode abstractNode : exponent) {
			sb.append(abstractNode);
		}
    	return sb.toString();
    }
    
    @Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof DefaultNode))
			return false;
		DefaultNode node = (DefaultNode) obj;
		if(node.getRasterRegion().equals(this.region))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return region.hashCode();
	}
	
}
