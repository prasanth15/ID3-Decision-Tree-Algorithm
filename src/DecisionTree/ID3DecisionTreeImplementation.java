/**
 * Implementation of ID3 algorithm to design decision trees
 * 
 * @author Prasanth Kesava Pillai (pxk163630)
 */

package DecisionTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ID3DecisionTreeImplementation {
	
	private static int trainingInstances = 0, trainingAttributes = 0;
	private static HashMap<Integer, ArrayList<String>> Input = new HashMap<>();
	private static HashMap<Integer, ArrayList<String>> temp = new HashMap<>();
	private static ArrayList<String> datasetClass = new ArrayList<String>();
	private static HashMap<Integer, ArrayList<String>> labelZero, labelOne;
	private static DTree id3DT;
	private static HashMap<Integer,ArrayList<String>> n;
	private static int instanceCount = 0;
	private static int pruneNodeCount = 0;
	private static double pruningFactor = 0;
	private static double accuracyOnPrunedData = 0;
	static String trainingDataset = null, testDataset = null, validationDataset = null;

	public static void main(String[] args) throws IOException {
		
		trainingDataset = args[0];
		validationDataset = args[1];
		testDataset = args[2];
		pruningFactor = Double.parseDouble(args[3]);
		trainDataset(trainingDataset);		
	}
	
	
	public static String trainDataset(String trainingDataset) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(trainingDataset));
		String lines = null;
		String line[] = null;
		Queue<BNode> queue = new LinkedList<>();
		double classEntrophy = 0, informationGain = 0;
		int index = 0;
		HashMap<Integer,ArrayList<String>> label0 = new HashMap<>();
		HashMap<Integer,ArrayList<String>> label1 = new HashMap<>();
		//Convert Input into hashmap starting from 0 to 600
		while((lines = br.readLine())!=null){
			line = lines.split(",");
			Input.put(trainingInstances++, new ArrayList<>(Arrays.asList(line)));
		}
		temp = Input;
		//Create a node and add to queue
		BNode root = new BNode(Input, null, 1);
		id3DT = new DTree(root);		
		queue.add(root);
		BNode currentNode = new BNode(Input, null, 0);
		trainingAttributes = currentNode.getData().get(0).size()-1;
		int tag = 1;
				
		while(!queue.isEmpty()) {
			currentNode = queue.remove();
			Input = currentNode.getData();
			for(int j = 1; j < trainingInstances; j++) {
				if(Input.get(j) != null) {
					datasetClass.add(Input.get(j).get(Input.get(0).size()-1));
				}
			}
			classEntrophy = classEntrophy(datasetClass);
		
			//Calculate Info gain
			int columns = 0; double maximum = -1;
			while(columns < Input.get(0).size() - 1) {
				informationGain = calculateInfoGain(Input, classEntrophy, columns);
				if(informationGain > maximum) {
					maximum = informationGain;
					index = columns;
					label0 = labelZero;
					label1 = labelOne;
				}
				columns++;
			}
			currentNode.setAttribute(Input.get(0).get(index));
			
			BNode left = new BNode();
			BNode right = new BNode();
			
			if(label0!=null){		
				ArrayList<String> za = new ArrayList<>();
				ArrayList<String> zaTemp = new ArrayList<>();
				for (int m = 0; m < label0.size(); m++) {
					za = label0.get(m);
					zaTemp = new ArrayList<String>(za);
					zaTemp.remove(index);
					label0.put(m, zaTemp);
				}
				
				left = new BNode(label0, null, tag++);
				id3DT.add(currentNode, left, "left");
				if(!isPureNode(label0, index)) {
					queue.add(left);
				}else{
					left.setLabel(Integer.parseInt(label0.get(1).get(label0.get(0).size() - 1)));
				}
				if(label1!=null){					
					ArrayList<String> oa = new ArrayList<>();
					ArrayList<String> oaTemp = new ArrayList<>();
					for (int n = 0; n < label1.size(); n++) {
						oa = label1.get(n);
						oaTemp = new ArrayList<String>(oa);
						oaTemp.remove(index);
						label1.put(n, oaTemp);
					}						
					right = new BNode(label1, null, tag++);
					id3DT.add(currentNode, right, "right");
					if(!isPureNode(label1, index)) {
						queue.add(right);
					}else{
						right.setLabel(Integer.parseInt(label1.get(1).get(label1.get(0).size() - 1)));
					}
				}
			}
		}
		br.close();		
		id3DT.print(id3DT.getRootNode());
		
		System.out.println("Pre-Pruned Accuracy");
		System.out.println("-------------------");
		System.out.println("Number of training instances: "+(trainingInstances-1));
		System.out.println("Number of training attributes:  "+trainingAttributes);
		System.out.println("Total number of nodes in the tree: "+id3DT.getNodeCount(id3DT.getRootNode()));
		System.out.println("Number of Leaf Nodes in the tree: "+id3DT.leaf(id3DT.getRootNode()).size());	
		System.out.println("Accurarcy of the model on the training dataset: " + findAccuracy(id3DT, fileToMap(trainingDataset)));
		System.out.println();
		
		fileToMap(validationDataset);
		System.out.println("Number of validation instances: "+(instanceCount-1));
		System.out.println("Number of validation attributes:  "+ (n.get(0).size()-1));
		double accuracyOnPrePrunedData = findAccuracy(id3DT, fileToMap(validationDataset));
		System.out.println("Accurarcy of the model on validation dataset: " + accuracyOnPrePrunedData);	
		System.out.println();
		
		
		fileToMap(testDataset);
		System.out.println("Number of testing instances: "+(instanceCount-1));
		System.out.println("Number of testing attributes:  "+ (n.get(0).size()-1));	
		System.out.println("Accurarcy of the model on the testing dataset: " + findAccuracy(id3DT, fileToMap(testDataset)));
		System.out.println();
		int tryPruning = 0;
		while(tryPruning < 20 && accuracyOnPrePrunedData >= accuracyOnPrunedData) {
			BNode bnode = new BNode();
			bnode = DTree.cloneTree(id3DT.getRootNode());
			DTree tempTree = new DTree(bnode);
			System.out.println("Post-Pruned Accuracy");
			System.out.println("-------------------");
			pruning(tempTree);
			System.out.println();
			tryPruning++;
		}
		return trainingDataset;
	}
	
	//Pruning random nodes from tree depending upon the pruning factor
	public static void pruning(DTree dTree) throws IOException {
		pruneNodeCount = (int) (dTree.getNodeCount(dTree.getRootNode()) * pruningFactor);
		while(pruneNodeCount > 0) {
			int deleteNodeWithTag = (int) (dTree.getNodeCount(dTree.getRootNode()) - ((Math.random()* 20)) );
			dTree.deleteTag(deleteNodeWithTag, dTree.getRootNode());
			pruneNodeCount--;
		}
		System.out.println("Number of validation instances: "+(instanceCount-1));
		System.out.println("Number of validation attributes:  "+ (n.get(0).size()-1));
		accuracyOnPrunedData = findAccuracy(dTree, fileToMap(validationDataset));
		System.out.println("Accurarcy of model on the validation dataset: " + accuracyOnPrunedData);	
		System.out.println();
		System.out.println("Number of training instances: "+(trainingInstances-1));
		System.out.println("Number of training attributes:  "+trainingAttributes);
		System.out.println("Total number of nodes in the tree: "+dTree.getNodeCount(dTree.getRootNode()));
		System.out.println("Number of Leaf Nodes in the tree: "+dTree.leaf(dTree.getRootNode()).size());
		System.out.println("Accurarcy of the model on the training dataset: " + findAccuracy(dTree, fileToMap(trainingDataset)));	
		System.out.println();
		fileToMap(testDataset);
		System.out.println("Number of testing instances: "+(instanceCount-1));
		System.out.println("Number of testing attributes:  "+ (n.get(0).size()-1));
		System.out.println("Accurarcy of the model on the testing dataset: " + findAccuracy(dTree, fileToMap(testDataset)));	
	}
	
	//Calculating the accuracy of the algorithm
	private static double findAccuracy(DTree tree, HashMap<Integer, ArrayList<String>> al) {
		BNode node = tree.getRootNode();
		ArrayList<String> rows = new ArrayList<>();
		int totalInstance = al.size()-1, satisfy = 0;
		BNode input;
		for (int j = 1; j < al.size(); j++) {
			rows = al.get(j);		
			if (getResult(al.get(j), tree.getRootNode()))
				satisfy = satisfy + 1;
			}
		return ((double)(satisfy*100)/(totalInstance));
	}
	
	private static boolean getResult(ArrayList<String> row, BNode currNode) {
		if (currNode.getAttribute() == null) {
			if (currNode.getLabel() == Integer.parseInt(row.get(row.size() - 1))) {
				return true;
			} else
				return false;
		}
		int index = getIndexOfAttribute(currNode.getAttribute());
		int value = Integer.parseInt(row.get(index));
		if (value == 0) {
			if (getResult(row, currNode.getLeft()))
				return true;
		} else if (value == 1) {
			if (getResult(row, currNode.getRight()))
				return true;
		}
		return false;
	}

	private static int getIndexOfAttribute(String attribute) {
		for (int i = 0; i < temp.get(0).size(); i++)
			if (temp.get(0).get(i).equals(attribute)) {
				return i;
			}
		return -1;
	}
	
	//Read file to find the accuracy
	private static HashMap<Integer,ArrayList<String>> fileToMap(String trainingDataset) throws IOException {
		n = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(trainingDataset));
		String lines;
		String[] line;
		instanceCount = 0;
		while((lines = br.readLine())!=null){
			line = lines.split(",");
			n.put(instanceCount++, new ArrayList<>(Arrays.asList(line)));
		}
		br.close();
		return n;
	}
	
	//Function to check purity of nodes
	private static boolean isPureNode(HashMap<Integer, ArrayList<String>> list, int index) {
			
		for(int i = 1 , j = 2; i < list.size()-1 && j  <list.size(); i++,j++) {
			String value1 = list.get(i).get(list.get(0).size() - 1);
			String value2 = list.get(j).get(list.get(0).size() - 1);
			if(!value1.equals(value2))
				return false;
			}
			return true;
	}
	
	//Calculate Information Gain
	private static double calculateInfoGain(HashMap<Integer, ArrayList<String>> input, double classEntrophy,
				int attribute) {
		labelZero = new HashMap<>();
		labelOne = new HashMap<>();
		double zeroCount = 0, oneCount = 0, attrZeroClassZero = 0, attrZeroClassOne = 0;
		double attrOneClassZero = 0, attrOneClassOne = 0;
		int instancecount1 = 1, instancecount2 = 1;
		String classValue;
		labelZero.put(0, input.get(0));
		labelOne.put(0, input.get(0));
		for(int j = 1; j < input.size(); j++) {
			classValue = input.get(j).get(input.get(0).size()-1);
			if(input.get(j).get(attribute).equals("0")) {
				labelZero.put(instancecount2++, input.get(j));
				zeroCount++;
				if(classValue.equals("0")) {
					attrZeroClassZero++;
				}else{
					attrZeroClassOne++;
				}
			}else{
				labelOne.put(instancecount1++, input.get(j));
				oneCount++;
				if(classValue.equals("0")) {
					attrOneClassZero++;
				}else{
					attrOneClassOne++;
				}
			}
		}
		double zeroEntrophy = entrophy(attrZeroClassZero, attrZeroClassOne);
		double oneEntrophy = entrophy(attrOneClassZero, attrOneClassOne);
		double infogain = netInfoGain(classEntrophy, zeroCount, zeroEntrophy, 
						oneCount, oneEntrophy);
		return infogain;
	}
	
	//Calculate NetInfoGain
	private static double netInfoGain(double classEntrophy, double zeroCount, double zeroEntrophy, double oneCount,
				double oneEntrophy) {
		double totalCount = zeroCount + oneCount;
		double output = classEntrophy - (((zeroCount/totalCount)*zeroEntrophy)+((oneCount/totalCount)*oneEntrophy));
		return output;
	}
	
	
	//Calculate classEntrophy
	private static double classEntrophy(ArrayList<String> datasetClass) {
		double zeroCount = 0, oneCount = 0;
		for(String data : datasetClass) {
			if(data.equals("0")) {
				zeroCount++;
			}else{
				oneCount++;
			}
		}
		return entrophy(zeroCount, oneCount);
	}

	//Calculate Entrophy
	private static double entrophy(double zeroCount, double oneCount) {
		double netEntrophy = 0;
		double totalCount = zeroCount + oneCount;
		double pZeroCount = zeroCount / totalCount ;
		double pOneCount = oneCount / totalCount;
		netEntrophy = -((pZeroCount) * log2(pZeroCount)) - ((pOneCount) * log2(pOneCount));
		return netEntrophy;		
	}
		
	//Calculate log value
	private static double log2(double n) {
		if(n == 0) {
			return 0;
		}
		double logValue = (Math.log(n) / Math.log(2));
		if(!Double.isNaN(logValue))
			return logValue;
		return 0;
	}


}
