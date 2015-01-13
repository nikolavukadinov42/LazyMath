package ftn.sc.lazymath.ocr.math;

import java.util.Collections;
import java.util.List;

public class Expresion implements MathTreeNode {

	private List<MathTreeNode> children;
	private boolean isRoot;
	private MathTreeNode parent;
	private ExpresionType type;
	
	public Expresion(List<MathTreeNode> children, boolean isRoot, MathTreeNode parent, ExpresionType type) {
		super();
		this.children = children;
		this.isRoot = isRoot;
		this.parent = parent;
		this.type = type;
	}
	
	public void removeChild(MathTreeNode toRemove) {
		children.remove(toRemove);
	}
	
	public void removeChildren(List<MathTreeNode> toRemove) {
		children.removeAll(toRemove);
	}

	public MathTreeNode getParent() {
		return parent;
	}

	public void setParent(MathTreeNode parent) {
		this.parent = parent;
	}

	public List<MathTreeNode> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "[isRoot: " + isRoot + ",type: " + this.type + "]\t");
		if (parent != null) {
			sb.append(parent.toString() + "\t");
		}
		for (MathTreeNode node : children) {
				sb.append( node.toString());
		}
		return sb.toString();
	}
}
