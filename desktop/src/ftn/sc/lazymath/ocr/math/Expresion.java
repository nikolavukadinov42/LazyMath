package ftn.sc.lazymath.ocr.math;

import java.util.List;

import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

public class Expresion implements MathTreeNode {

	private List<MathTreeNode> children;
	private boolean isRoot;
	private MathTreeNode parent;
	private ExpresionType type;
	private RasterRegion region;

	public Expresion(List<MathTreeNode> children, boolean isRoot, MathTreeNode parent, ExpresionType type, RasterRegion region) {
		super();
		this.children = children;
		this.isRoot = isRoot;
		this.parent = parent;
		this.type = type;
		this.region = region;
	}

	public void addChild(MathTreeNode node) {
		children.add(node);
	}

	public void removeChild(MathTreeNode toRemove) {
		children.remove(toRemove);
	}

	public void removeChildren(List<MathTreeNode> toRemove) {
		children.removeAll(toRemove);
	}

	public void removeAllChildren() {
		children.clear();
	}

	public MathTreeNode getParent() {
		return parent;
	}

	public void setParent(MathTreeNode parent) {
		this.parent = parent;
	}

	public List<MathTreeNode> getChildren() {
		return children;
	}

	public RasterRegion getRegion() {
		return region;
	}

	public void setRegion(RasterRegion region) {
		this.region = region;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "[isRoot: " + isRoot + ",type: " + this.type + "]\t");
		// if (parent != null) {
		// sb.append(parent.toString() + "\t");
		// }
		sb.append("\n\t");
		for (MathTreeNode node : children) {
			sb.append(node.toString());
		}
		return sb.toString();
	}

	@Override
	public String getFormula() {
		StringBuilder sb = new StringBuilder();
		for (MathTreeNode mathTreeNode : children) {
			if (mathTreeNode instanceof Symbol) {
				sb.append(mathTreeNode.getFormula());
			} else if (mathTreeNode instanceof Expresion) {
				Expresion expresion = (Expresion) mathTreeNode;
				switch (expresion.getType()) {
				case NTHROOT:
					sb.append("sqrt(");
					for (MathTreeNode child : expresion.getChildren()) {
						if (((Expresion) child).getType() == ExpresionType.INSIDE) {
							sb.append(child.getFormula());
						}
					}
					sb.append(",");
					boolean exist = false;
					for (MathTreeNode child : expresion.getChildren()) {
						if (((Expresion) child).getType() == ExpresionType.UPPERLEFT) {
							sb.append(child.getFormula());
							exist = true;
						}
					}
//					if (!exist) {
//						sb.append("1/2)");
//					} else {
//						sb.append(")");
//					}
					sb.append(")");
					break;
				case FRACTION:
					sb.append("(");
					for (MathTreeNode child : expresion.getChildren()) {
						if (((Expresion) child).getType() == ExpresionType.ABOVE) {
							sb.append(child.getFormula());
						}
					}
					sb.append(")/(");
					for (MathTreeNode child : expresion.getChildren()) {
						if (((Expresion) child).getType() == ExpresionType.BELOW) {
							sb.append(child.getFormula());
						}
					}
					sb.append(")");
					break;
				case EXPONENT:
					sb.append(expresion.getRegion().tag);
					sb.append("^(");
					for (MathTreeNode child : expresion.getChildren()) {
						if (((Expresion) child).getType() == ExpresionType.UPPERRIGHT) {
							sb.append(child.getFormula());
						}
					}
					sb.append(")");
					break;
				default:
					sb.append(mathTreeNode.getFormula());
					break;
				}
			}
		}
		return sb.toString();
	}

	public ExpresionType getType() {
		return type;
	}

	public void setType(ExpresionType type) {
		this.type = type;
	}
}
