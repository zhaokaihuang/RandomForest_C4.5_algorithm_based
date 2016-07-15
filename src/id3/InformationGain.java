package id3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InformationGain {

	public static double getInfoGain(List<List<String>> dataSet, int columnIndex){
		double entropy = 0; 
		double combinedEntropy = 0;
		double infoGain = 0;
		//Count labels' number here
		Map<String, Integer> labelCounts = getAttrCounts(dataSet, dataSet.get(0).size()-1);
		//Compute Entire Entropy - dataSet.get(0).size()-1
		Iterator<Map.Entry<String, Integer>> ite = labelCounts.entrySet().iterator();
		while(ite.hasNext()){
			Map.Entry<String, Integer> entry = ite.next();
			double prob = (double)entry.getValue()/(double)dataSet.size();
			entropy += (-1)*prob*Math.log(prob)/Math.log(2);
		}
		//Compute single entropy
			//compute the counts of criteria standard of the attribute  
			Map<String, Integer> counts = getAttrCounts(dataSet, columnIndex);
			Iterator<Map.Entry<String, Integer>> ite2 = counts.entrySet().iterator();
			while(ite2.hasNext()){
				Map.Entry<String, Integer> entry2 = ite2.next();
				//get left dataSet after split, responsing to detailed state of attribute
				List<List<String>> splitDataSet = getSplitDataSet(dataSet, columnIndex, entry2.getKey());
				double probC = (double)splitDataSet.size()/(double)dataSet.size(); 
				double singleEntropy = 0; 
				if(splitDataSet.size()>0){
					int labelColumn = splitDataSet.get(0).size()-1; //last column - label/class
					Map<String, Integer> map3 = getAttrCounts(splitDataSet, labelColumn);
					Iterator<Map.Entry<String, Integer>> ite3 = map3.entrySet().iterator();
					while(ite3.hasNext()){
						Map.Entry<String, Integer> entry3 = ite3.next();
						double prob = (double)entry3.getValue()/(double)splitDataSet.size();
						singleEntropy += (-1)*prob*Math.log(prob)/Math.log(2);
					}
				}
				combinedEntropy += probC*singleEntropy;
			}
			infoGain = entropy - combinedEntropy;
			if(infoGain<0){
				System.out.println("InfoGain<0");
			}
			return infoGain;
	}
	
    public static List<List<String>> getSplitDataSet(List<List<String>> dataSet, int index, String attrClass) {  
    	List<List<String>> splitDataSet = new ArrayList<List<String>>();  
        for(List<String> data : dataSet) {  
            if(data.get(index).equals(attrClass)) {  
                splitDataSet.add(data);  
            }  
        }    
        return splitDataSet;  
    }  

    public static Map<String, Integer> getAttrCounts(List<List<String>> dataSet, int index){
    	Map<String, Integer> attrCounts = new HashMap<String, Integer>();
		for(List<String> data : dataSet){
			String label = data.get(index);   //index of column
			if(!attrCounts.containsKey(label)){
				attrCounts.put(label, 1); 
			} else{
				attrCounts.put(label, attrCounts.get(label)+1);
			}
		}
		return attrCounts;
    }
    
    public static List<String> getTypes(List<List<String>> dataSet, int columnIndex) {  
        List<String> list = new ArrayList<String>();  
        for(List<String> data : dataSet) {  
            if(!list.contains(data.get(columnIndex))) {  
                list.add(data.get(columnIndex));  
            }  
        }  
        return list;  
    }  
}
