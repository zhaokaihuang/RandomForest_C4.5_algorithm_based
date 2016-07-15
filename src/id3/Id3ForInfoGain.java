package id3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Id3ForInfoGain {   // for experiment!!
	private final Integer totalSize;
	private Node root;

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	
	public Id3ForInfoGain() {
		this.totalSize = 0;
	}
	
	public Id3ForInfoGain(List<List<String>> dataSet) {
		this.totalSize = dataSet.size();
	}

	

	public Boolean allLabelSame(List<List<String>> dataSet) {
		Map<String, Integer> labelSame = new HashMap<String, Integer>();
		for (List<String> data : dataSet) {
			String label = data.get(data.size() - 1);
			if (!labelSame.containsKey(label)) {
				labelSame.put(label, 1);
			}
		}
		if (labelSame.size() == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param dataSet
	 * @param totalSize
	 * @return
	 */
	public String getCommonLabel(List<List<String>> dataSet, Integer totalSize) {
		Map<String, Integer> common = new HashMap<String, Integer>();
		for (List<String> data : dataSet) {
			String label = data.get(data.size() - 1);
			if(!label.equals("?")){
				if (!common.containsKey(label)) {
					common.put(label, 1);
				} else {
					common.put(label, common.get(label) + 1);
				}
			}
		}
		Iterator<Map.Entry<String, Integer>> ite = common.entrySet().iterator();
		String commonLabel = "";
		Integer value = 0;
		Integer sum = 0;
		while (ite.hasNext()) {
			Map.Entry<String, Integer> entry = ite.next();
			if (value < entry.getValue()) {
				commonLabel = entry.getKey();
				value = entry.getValue();
			}
			sum += entry.getValue();
		}
		double prob = (double) Math.round((double) value / (double) totalSize * 10000) / 10000;
		//return commonLabel + "," + prob;
		return commonLabel + "," + value + "/" + (sum-value);
	}

	/**
	 * 
	 * @param attributeSet
	 * @param dataSet
	 * @param depth
	 * @return
	 */
	public Node buildTree(List<String> attributeSet, List<List<String>> dataSet, int depth) {
		Node root = new Node();
		root.setArrributeSet(attributeSet);
		root.setDataSet(dataSet);
		// Base Case:
		if (depth == 0 || attributeSet.size() <= 1) {
			String nodeName = getCommonLabel(dataSet, totalSize);
			root.setNodeName(nodeName);
			return root;
		}
		if (allLabelSame(dataSet)) {
			String nodeName = getCommonLabel(dataSet, totalSize);
			root.setNodeName(nodeName);
			return root;
		}
		if(dataSet.size()==0){
			root.setNodeName(" ");
			return root;
		}
		// get max information gain ratio
		int index = -1; // the column needs to be discarded
		double maxInfoGainRatio = 0;
		double infoGainRatio = 0;
		String regExp = "-*\\d+\\.?\\d*";
		double threshold = 0;
		double bestThreshold = 0;
		int numericOrNomi = 0;
		for (int i = 0; i < attributeSet.size() - 1; i++) {
			if (attributeSet.get(i) != null) {
				String firstValue= dataSet.get(0).get(i);
				if(!firstValue.matches(regExp)){ //nominal
					infoGainRatio = InformationGain.getInfoGain(dataSet, i);
		    	} else { 
		    		double[] result = IGRatio.getIGRatio4Numeric(dataSet, i);
		    		infoGainRatio = result[0];
		    		threshold = result[1];
		    	}//end#else
				
				if (infoGainRatio > maxInfoGainRatio) {
					index = i;
					maxInfoGainRatio = infoGainRatio;
					bestThreshold = threshold;
				}
			}
		}
		if (maxInfoGainRatio == 0) { // how to avoid complete randomness so that
								// print no branches
			root.setNodeName(getCommonLabel(dataSet, totalSize));
			return root;
		}
		String firstValue= dataSet.get(0).get(index);
		if(!firstValue.matches(regExp)){
			numericOrNomi = 1;   //flag = 1, nominal
		} else{
			numericOrNomi = -1;  //flag = -1, numeric
		}
		// split by best information gain ratio
		root.setNodeName(attributeSet.get(index));
		if(numericOrNomi==1){ //nominal attributes
			List<String> attrTypes = new ArrayList<String>();
			for (List<String> data : dataSet) {
				String type = data.get(index);
				if(!type.equals("?")){
					if (!attrTypes.contains(type)) {
						attrTypes.add(type);
					}
				}
			}
			root.setSplitAttributes(attrTypes); // split attributes = types of it
			//root.setNodeName(attributeSet.get(index));
			// Recursive Base
			for (int j = 0; j < attrTypes.size(); j++) {
				Node childNode = new Node();
				List<String> newAttributeSet = new ArrayList<String>();
				List<List<String>> newDataSet = new ArrayList<List<String>>();
				// left or right branch
				List<List<String>> splitDataSet = IGRatio.getSplitDataSet(
						dataSet, index, attrTypes.get(j));
				// newAttributeSet
				for (String attr : attributeSet) {
					if (!attr.equals(attributeSet.get(index))) {
						newAttributeSet.add(attr);
					}
				}
				if(splitDataSet.size()==0){
					String nodeName = getCommonLabel(dataSet, totalSize);
					childNode.setNodeName(nodeName);
					root.getChildrenNodes().add(childNode);
					return root;
				}
				// newDataSet
				for (List<String> data : splitDataSet) {
					List<String> tmp = new ArrayList<String>();
					for (int k = 0; k < data.size(); k++) {
						if (k != index) {
							tmp.add(data.get(k));
						}
					}
					newDataSet.add(tmp);
				}
				childNode = buildTree(newAttributeSet, newDataSet, depth - 1);
				root.getChildrenNodes().add(childNode);
			}
		} else if(numericOrNomi==-1){ //numeric attributes
			List<String> attrTypes = new ArrayList<String>();
			String type1 = "<=" + bestThreshold; //left
			String type2 = ">" + bestThreshold;  //right
			attrTypes.add(type1);
			attrTypes.add(type2);
			root.setSplitAttributes(attrTypes);
			for (int j = 0; j < attrTypes.size(); j++) {
				Node childNode = new Node();
				List<String> newAttributeSet = new ArrayList<String>();
				List<List<String>> newDataSet = new ArrayList<List<String>>();
				//newAttributeSet
				for (String attr : attributeSet) {
					if (!attr.equals(attributeSet.get(index))) {
						newAttributeSet.add(attr);
					}
				}
				//newDataSet
				if(attrTypes.get(j).contains("<=")){
					newDataSet = IGRatio.getLHSDataSet(dataSet, index, bestThreshold);
				} else if(attrTypes.get(j).contains(">")){
					newDataSet = IGRatio.getRHSDataSet(dataSet, index, bestThreshold);
				}
				childNode = buildTree(newAttributeSet, newDataSet, depth - 1);
				root.getChildrenNodes().add(childNode);
			}
		}
		return root;
	}

	/**
	 * vertical direction
	 * 
	 * @param root
	 */
	public void printId3(Node root) {
		/*
		 * if(root.getChildrenNodes().size()!=0){ //avoiding complete randomness
		 * so that print no branches
		 */
		if (root.getSplitAttributes()!=null) {
			System.out.print(/* "Split Node：" + */root.getNodeName());
			// System.out.println();
			for (String attr : root.getSplitAttributes()) {
				System.out.print(" (" + attr + ") ");
			}
			System.out.println();
		} else {
			System.out.print(/* "Split Node：" + */root.getNodeName() + "; ");
			System.out.println();
		}

		if (null != root.getChildrenNodes()) {
			for (Node node : root.getChildrenNodes()) {
				printId3(node);
			}
		}
		/*
		 * } else{ System.out.println("  No split existed! Completely random!");
		 * }
		 */

	}

	/**
	 * horizontal direction searchTree Hierarchy traversal
	 * 
	 * @param root
	 */
	public void searchTree(Node root) {
		Queue<Node> nodeQueue = new LinkedList<Node>();
		nodeQueue.offer(root); // insert
		while (nodeQueue.size() != 0) {
			Node node = nodeQueue.poll(); // remove
			if (null != node.getSplitAttributes()) {

				System.out.print(/* "Split Node: " + */node.getNodeName());
				for (String attr : node.getSplitAttributes()) {
					System.out.print(" (" + attr + ") ");
				}
				System.out.println();
			} else {
				System.out.println(/* "Split Node: " + */node.getNodeName()
						+ "; ");
			}

			if (null != node.getChildrenNodes()) {
				for (Node nod : node.getChildrenNodes()) {
					nodeQueue.offer(nod);
				}
			}
		}
	}

	/**
	 * only test one record, prepared for model.test()
	 * 
	 * @param root
	 * @param testAttributeSet
	 * @param testData
	 * @return
	 */
	public String testOneRecord(Node root, List<String> testAttributeSet, List<String> testData, List<List<String>> testDataSet) {
		int i = 0;
		int j = 0;
		String result = "";
		int flag = -1;
		for (i = 0; i < testAttributeSet.size() - 1; i++) {
			if (testAttributeSet.get(i).equals(root.getNodeName())) {
				flag = 1;
				break;
			}
		}
		if (flag == -1) {
			result = root.getNodeName();
			return result;
		}
		if (root.getSplitAttributes() == null
				|| root.getChildrenNodes().size() == 0) {
			result = root.getNodeName();
			return result;
		}
		String regExp = "-*\\d+\\.?\\d*";
		String temp = testData.get(i);
		if(temp.equals("?")){
			temp = IGRatio.getCommon4Missing(testDataSet, i);
		}
		if(!temp.matches(regExp)){
			int find = 0;
			for (j = 0; j < root.getSplitAttributes().size(); j++) {
				if (temp.equals(root.getSplitAttributes().get(j))) {
					if (root.getChildrenNodes().get(j) != null) {
						result = testOneRecord(root.getChildrenNodes().get(j),
								testAttributeSet, testData, testDataSet);
					} else {
						result = root.getNodeName();
					}
					find = 1;
				}
			}
			if(find==0){
				result = getCommonLabel(root.getDataSet(), totalSize);
			}
		} else{
			String th = "";
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(root.getSplitAttributes().get(0));
			while(matcher.find()){
				th = matcher.group(0);
			}
			double threshold = Double.valueOf(th);
			if(Double.valueOf(temp)<=threshold){
				result = testOneRecord(root.getChildrenNodes().get(0),
						testAttributeSet, testData, testDataSet);
			} else{
				result = testOneRecord(root.getChildrenNodes().get(1),
						testAttributeSet, testData, testDataSet);
			}
		}
		return result;
	}

	public List<String> test(Node root, List<String> testAttributeSet,
			List<List<String>> testDataSet) {
		List<String> resultList = new ArrayList<String>();
		for (List<String> testData : testDataSet) {
			String result = "";
			result = testOneRecord(root, testAttributeSet, testData, testDataSet);
			resultList.add(result);
		}
		return resultList;
	}
	

	
	public double err(List<String> resultList, List<List<String>> testDataSet) {
		List<String> labelList = new ArrayList<String>();
		double total = resultList.size();
		double err = 0;
		for (List<String> testData : testDataSet) {
			labelList.add(testData.get(testData.size() - 1));
		}
		for (int i = 0; i < resultList.size(); i++) {
			if (!resultList.get(i).contains(labelList.get(i))) {
				err++;
			}
		}
		return (double) Math.round((double) err / (double) total * 10000) / 10000;
	}

}
