package ftn.sc.lazymath.ocr.math;

import java.util.ArrayList;
import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

public class Symbol implements MathTreeNode {

	private String value;
	private Expresion parent;
	private RasterRegion region;

	public Symbol(String value, Expresion parent, RasterRegion region) {
		super();
		this.value = value;
		this.parent = parent;
		this.region = region;
	}
	
	public RasterRegion getRegion() {
		return region;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
//		return this.getClass().getSimpleName() + "[value: " + this.value + ", belongsTo: " + this.belongsTo + ", region: "
//				+ region.toString() + "]";
		return "\t" + value + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Symbol))
			return false;
		Symbol symbol = (Symbol) obj;
		if(symbol.getRegion().equals(this.getRegion()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return region.hashCode();
	}
	
	@Override
	public String getFormula() {
		return (String) region.tag;
	}
	
	public static List<RasterRegion> getRegionsFromListOfSymbols(List<Symbol> listOfSymbols) {
		List<RasterRegion> regions = new ArrayList<RasterRegion>();
		for (Symbol symbol : listOfSymbols) {
			regions.add(symbol.getRegion());
		}
		return regions;
	}

}
