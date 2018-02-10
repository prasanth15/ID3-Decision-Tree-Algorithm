/**
 * Implementation of ID3 algorithm to design decision trees
 * 
 * @author Prasanth Kesava Pillai (pxk163630)
 */

package DecisionTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class DTree {

	private BNode root;
	private int nodeCount;
	
	DTree(BNode node) {
		root = node;
	}
	
	public BNode getRootNode() {
		return root;
	}
	
	public String getAttr(BNode node) {
		return node.getAttribute();
	}
	
	public void add(BNode parent, BNode node, String flag) {
		BNode n = find(parent);
		if(flag=="left")
			n.setLeft(node);
		else
			n.setRight(node);
		node.setParent(parent);
	}

	private BNode find(BNode parent) {
		Queue<BNode> q = new LinkedList<>();
		BNode n = null;
		q.add(root);
		while(!q.isEmpty()){
			n = q.remove();
			if(n == parent)
				return n;
			if(n.getLeft()!=null)
				q.add(n.getLeft());
			if(n.getRight()!=null)
				q.add(n.getRight());
		}
		return n;
	}
	
	public ArrayList<BNode> leaf(BNode node) {
		ArrayList<BNode> leaf = new ArrayList<>();
		Queue<BNode> q = new LinkedList<>();
		q.add(node);
		while(!q.isEmpty()){
			BNode n = q.remove();
			if(n.getLeft().getAttribute()!=null)
				q.add(n.getLeft());
			if(n.getRight().getAttribute()!=null)
				q.add(n.getRight());
			if(n.getLeft().getAttribute()==null && n.getRight().getAttribute()==null)
				leaf.add(n);
		}
		return leaf;
	}
	
	public void print(BNode node){
		print(node.getLeft(),0,"");
		print(node.getRight(),1,"");

	}
	public void print(BNode n,int path, String bar){
		if(n == null)
			return;
		else{
			if(n.getLeft() == null && n.getRight() == null)
				System.out.println(bar+""+n.getParent().getAttribute() + " = " + path +" : "+n.getLabel());
			else
				System.out.println(bar+""+n.getParent().getAttribute() + " = " + path);

			bar = bar + "| ";
			print(n.getLeft(),0,bar);
			print(n.getRight(),1,bar);
		}
	}
	
	public void deleteTag(int tag, BNode node) {
		Queue<BNode> q = new LinkedList<>();
		BNode n = null;
		q.add(node);
		while(!q.isEmpty()) {
			n = q.remove();
			if(n.getTag() == tag) {
				n.setAttribute(null);
				break;
			}if(n.getLeft() != null) {
				q.add(n.getLeft());
			}if(n.getRight() != null) {
				q.add(n.getRight());
			}
		}
	}

	public int getNodeCount(BNode node) {
		nodeCount = 0;
		Queue<BNode> q = new LinkedList<>();
		BNode n = null;
		q.add(node);
		while(!q.isEmpty()){
			n = q.remove();
			if(n.getAttribute() == null) {
				continue;
			}
			if(n.getAttribute() != null) {
				nodeCount++;
			}
			if(n.getLeft()!=null)
				q.add(n.getLeft());
			if(n.getRight()!=null)
				q.add(n.getRight());
		}
		return nodeCount;	
	}
	
	static BNode cloneTree(BNode decisionTreeRoot) {
        BNode newNode = new BNode();
        newNode.setAttribute(decisionTreeRoot.getAttribute());
        newNode.setLabel(decisionTreeRoot.getLabel());
        newNode.setLeft(decisionTreeRoot.getLeft());
        newNode.setParent(decisionTreeRoot.getParent());
        newNode.setRight(decisionTreeRoot.getRight());
        newNode.setTag(decisionTreeRoot.getTag());
        
        cloneTree(decisionTreeRoot, newNode);
        return newNode;
}
    //Clone recursively
    static void cloneTree(BNode root, BNode newNode) {
        if (root == null) {
            return;
        }
        if (root.getLeft() != null) {
            newNode.setLeft(new BNode());
            newNode.getLeft().setAttribute(root.getLeft().getAttribute());
            newNode.getLeft().setLabel(root.getLeft().getLabel());
            newNode.getLeft().setLeft(root.getLeft().getLeft());
            newNode.getLeft().setParent(root.getLeft().getParent());
            newNode.getLeft().setRight(root.getLeft().getRight());
            newNode.getLeft().setTag(root.getLeft().getTag());
            cloneTree(root.getLeft(), newNode.getLeft());
        }
        if (root.getRight() != null) {
        	newNode.setRight(new BNode());
            newNode.getRight().setAttribute(root.getRight().getAttribute());
            newNode.getRight().setLabel(root.getRight().getLabel());
            newNode.getRight().setLeft(root.getRight().getLeft());
            newNode.getRight().setParent(root.getRight().getParent());
            newNode.getRight().setRight(root.getRight().getRight());
            newNode.getRight().setTag(root.getRight().getTag());
            cloneTree(root.getRight(), newNode.getRight());
        }
    }

}
