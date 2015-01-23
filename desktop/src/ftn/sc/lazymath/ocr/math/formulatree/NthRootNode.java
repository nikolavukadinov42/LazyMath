package ftn.sc.lazymath.ocr.math.formulatree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public class NthRootNode extends AbstractNode {

    private AbstractNode exponent;
    private List<AbstractNode> elements;

    public NthRootNode() {
        this.elements = new ArrayList<>();
    }

    @Override
    public String getCharacters() {
        // sqrt(elements)^(1/exponent)
        return null;
    }

    @Override
    public List<RasterRegion> getRasterRegions() {
        List<RasterRegion> ret = new ArrayList<>();

        ret.add(region);

        if (exponent != null) {
            ret.addAll(exponent.getRasterRegions());
        }

        for (AbstractNode node : elements) {
            ret.addAll(node.getRasterRegions());
        }

        return ret;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("sqrt(");
    	
    	Collections.sort(elements, new Comparator<AbstractNode>() {
			@Override
			public int compare(AbstractNode firstNode, AbstractNode secondNode) {
				return (int) (firstNode.minX - secondNode.minX);
			}
		});

    	for (AbstractNode element : elements) {
			sb.append(element.toString());
		}
    	if (exponent != null && exponent.getRasterRegion() != null) {
    		sb.append("," + exponent.toString());
    	}
    	sb.append(")");
    	return sb.toString();
    }

    public void addElement(AbstractNode node) {
        elements.add(node);
    }

    public void addElements(List<AbstractNode> nodes) {
        elements.addAll(nodes);
    }

    public AbstractNode getExponent() {
        return exponent;
    }

    public void setExponent(AbstractNode exponent) {
        this.exponent = exponent;
    }

    public List<AbstractNode> getElements() {
        return elements;
    }

    public void setElements(List<AbstractNode> elements) {
        this.elements = elements;
    }
}
