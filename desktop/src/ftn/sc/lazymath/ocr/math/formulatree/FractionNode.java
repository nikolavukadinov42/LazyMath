package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class FractionNode extends AbstractNode {

    private List<AbstractNode> numerators;
    private List<AbstractNode> denominators;
    private RasterRegion fractionLine;

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
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("(");
    	for (AbstractNode above : numerators) {
			sb.append(above);
		}
    	sb.append(")");
    	sb.append("/");
    	sb.append("(");
    	for (AbstractNode below : denominators) {
    		sb.append(below);
		}
    	sb.append(")");
    	
    	return sb.toString();
    }
    
    public boolean isInside(RasterRegion root) {
    	System.out.println("ROOT: " + root.tag);
    	System.out.println("FL: " + fractionLine.tag);
    	if ((fractionLine.xM > root.minX) && (fractionLine.xM < root.maxX) && (fractionLine.yM > root.minY) && (fractionLine.yM < root.maxY)) {
			return true;
		}
    	
    	return false;
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

	public RasterRegion getFractionLine() {
		return fractionLine;
	}

	public void setFractionLine(RasterRegion fraction) {
		this.fractionLine = fraction;
	}

}
