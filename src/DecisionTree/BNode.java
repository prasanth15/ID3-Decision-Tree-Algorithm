/**
 * Implementation of ID3 algorithm to design decision trees
 * 
 * @author Prasanth Kesava Pillai (pxk163630)
 */
package DecisionTree;

import java.util.ArrayList;
import java.util.HashMap;

public class BNode {

	private HashMap<Integer, ArrayList<String>> data;
	private BNode left;
	private BNode right;
	private BNode parent;
	private String attribute; //attribute of the node
	private int label;	//labeling of the class
	private int tag; //for pruning
	
	BNode() {}

	BNode(HashMap<Integer, ArrayList<String>> data, String attribute, int tag) {
		this.data = data;
		this.attribute = attribute;
		this.left = null;
		this.right = null;
		this.tag = tag;
	}
	
	public void setParent(BNode parent) {
		this.parent = parent;
	}
	
	public BNode getParent() {
		return parent;
	}	

	public HashMap<Integer, ArrayList<String>> getData() {
		return data;
	}

	public void setData(HashMap<Integer, ArrayList<String>> data) {
		this.data = data;
	}

	public BNode getLeft() {
		return left;
	}

	public void setLeft(BNode left) {
		this.left = left;
	}

	public BNode getRight() {
		return right;
	}

	public void setRight(BNode right) {
		this.right = right;
	}

	public void setAttribute(String attributeIndex) {
		this.attribute = attributeIndex;
	}

	public String getAttribute() {
		return attribute;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
}



