package id3;

import java.util.ArrayList;
import java.util.List;

public class Node {  
    private String nodeName; 
    private List<List<String>> dataSet;  
    private List<String> arrributeSet; 
    private List<String> splitAttributes;  
    private List<Node> childrenNodes; 
      
    public Node(){  
        this.childrenNodes = new ArrayList<Node>();
    }

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public List<List<String>> getDataSet() {
		return dataSet;
	}

	public void setDataSet(List<List<String>> dataSet) {
		this.dataSet = dataSet;
	}

	public List<String> getArrributeSet() {
		return arrributeSet;
	}

	public void setArrributeSet(List<String> arrributeSet) {
		this.arrributeSet = arrributeSet;
	}

	public List<String> getSplitAttributes() {
		return splitAttributes;
	}

	public void setSplitAttributes(List<String> splitAttributes) {
		this.splitAttributes = splitAttributes;
	}

	public List<Node> getChildrenNodes() {
		return childrenNodes;
	}

	public void setChildrenNodes(List<Node> childrenNodes) {
		this.childrenNodes = childrenNodes;
	}  
    
    
}  