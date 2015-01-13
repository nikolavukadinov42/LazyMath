package ftn.sc.lazymath.ocr.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MathTree {

	private Expresion root;
	private List<MathTreeNode> children;
	
	public MathTree() {
		root = new Expresion(new ArrayList<MathTreeNode>(), true, null, ExpresionType.EXPRESION);
		children = new ArrayList<MathTreeNode>();
	}
	
	public void addChild(MathTreeNode child) {
		children.add(child);
	}
	
	public void removeChild(MathTreeNode child) {
		children.remove(child);
	}
	
	public List<MathTreeNode> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public int getNumberOfChildren() {
		return children.size();
	}

	public Expresion getRoot() {
		return root;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (MathTreeNode node : children) {
			sb.append("\t" + node.toString());
		}
		return sb.toString();
	}
	
}
