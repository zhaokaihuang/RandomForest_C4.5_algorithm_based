package id3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomForest extends Id3{
	private List<Node> decisionTrees;
	private Random random;
	private int sampleNum;  //size(samples)
	private int featureNum; //num of attribute - normally sqrt(size(features))
	private int treeNum;
	private final Integer totalSize;
	
	public List<Node> getDecisionTrees() {
		return decisionTrees;
	}

	public void setDecisionTrees(List<Node> decisionTrees) {
		this.decisionTrees = decisionTrees;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public int getSampleNum() {
		return sampleNum;
	}

	public void setSampleNum(int sampleNum) {
		this.sampleNum = sampleNum;
	}

	public int getFeatureNum() {
		return featureNum;
	}

	public void setFeatureNum(int featureNum) {
		this.featureNum = featureNum;
	}
	
	
	public int getTreeNum() {
		return treeNum;
	}

	public void setTreeNum(int treeNum) {
		this.treeNum = treeNum;
	}

	//constructor
	public RandomForest(){
		this.totalSize = 0;
	}
	
	public RandomForest(List<List<String>> dataSet){
		this.totalSize = dataSet.size();
	}
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
					infoGainRatio = IGRatio.getIGRatio4Nominal(dataSet, i);
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
		if(firstValue.equals("?")){
			firstValue = IGRatio.getCommon4Missing(dataSet, index);
		}
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
	 * 
	 * @param attributeSet
	 * @param dataSet
	 * @param depth
	 * @return
	 */
	public List<Node> buildRandomForest(List<String> attributeSet, List<List<String>> dataSet, int depth, int treeNum) {
		List<Node> decisionTrees1 = new ArrayList<Node>();
		this.setSampleNum(dataSet.size());
		this.setFeatureNum((int) Math.sqrt((double)(attributeSet.size()-1)));
		this.setTreeNum(treeNum);
		Random rm = new Random();
		for(int i = 0; i < treeNum; i++){
			Node node = new Node();
			List<String> newAttributeSet = new ArrayList<String>();
			List<List<String>> newDataSet = new ArrayList<List<String>>();
			List<Integer> featureNums = new ArrayList<Integer>(); // storing X.th column
			for(int k = 0; k < this.getFeatureNum(); ){
				int index = rm.nextInt(attributeSet.size()-1);
				if(!featureNums.contains(index)){
					featureNums.add(index);
					k++;
				}
			}
			//newDataSet
			for(int j = 0; j < this.getSampleNum(); j++){
				int index = rm.nextInt(this.getSampleNum());
				List<String> data = new ArrayList<String>();
				for(int z : featureNums){
					data.add(dataSet.get(index).get(z));
				}
				data.add(dataSet.get(index).get(attributeSet.size()-1));
				newDataSet.add(data);
			}
			//newAttributeSet
			for(int m : featureNums){
				newAttributeSet.add(attributeSet.get(m));
			}
			newAttributeSet.add(attributeSet.get(attributeSet.size()-1));
			
			node = buildTree(newAttributeSet, newDataSet, depth);
			decisionTrees1.add(node);
		}
			this.setDecisionTrees(decisionTrees1);
			return decisionTrees1;
	}
	/**
	 * vertical direction
	 * 
	 * @param root
	 */
	public void printRandomForest(List<Node> root){
		
		for(Node node : root){
			System.out.println("----print Random Forest in vertical direction----");
			printId3(node);
		}
		System.out.println();
	}
	/**
	 * horizontal direction searchTree Hierarchy traversal
	 * 
	 * @param root
	 */
	public void searchRandomForest(List<Node> root){
		
		for(Node node : root){
			System.out.println("----print Random Forest in horizontal direction----");
			searchTree(node);
		}
		System.out.println();
	}
	
	/**
	 * error rate
	 * 
	 * @param root
	 * @param testAttributeSet
	 * @param testDataSet
	 * @return
	 */
	
	public List<String> test(List<Node> root, List<String> testAttributeSet, List<List<String>> testDataSet) {
		List<String> resultList = new ArrayList<String>();
		for (List<String> testData : testDataSet) {
			String result = "";
			result = vote(root, testAttributeSet, testData, testDataSet);
			resultList.add(result);
		}
		return resultList;
	}
	
	public String vote(List<Node> root, List<String> testAttributeSet, List<String> testData, List<List<String>> testDataSet){
		String label = null;
		String preLabel = null;
		String negativeLabel = null;
		int count = 0;
		int countSum = 0;
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(Node node : root){
			label = testOneRecord(node, testAttributeSet, testData, testDataSet);
			label = label.split(",")[0];
			if(!map.containsKey(label)){
				map.put(label, 1); 
			} else{
				map.put(label, map.get(label)+1);
			}
		}
		Iterator<Map.Entry<String, Integer>> ite = map.entrySet().iterator();
		while(ite.hasNext()){       //predict by most common label
			Map.Entry<String, Integer> entry = ite.next();
			countSum += entry.getValue();
			if(count<entry.getValue()){
				preLabel = entry.getKey();
				
				count = entry.getValue();
			} 
		}
		if(countSum == 2 * count){
			preLabel = testData.get(testData.size()-1);
		} 
		preLabel = preLabel.split(",")[0];
		return preLabel;
		//return preLabel+", prob: "+(double) Math.round((double) count / (double) countSum * 10000) / 10000;
	}
	
}





