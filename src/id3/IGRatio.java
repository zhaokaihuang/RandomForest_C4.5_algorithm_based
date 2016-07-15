package id3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class IGRatio {
	/**
	 * 
	 * @param dataSet
	 * @param columnIndex
	 * @return
	 */
	public static String getCommon4Missing(List<List<String>> dataSet, int columnIndex) {
		Map<String, Integer> common = new HashMap<String, Integer>();
		for (List<String> data : dataSet) {
			String value = data.get(columnIndex);
			if(!value.equals("?")){
				if (!common.containsKey(value)) {
					common.put(value, 1);
				} else {
					common.put(value, common.get(value) + 1);
				}
			}
		}
		Iterator<Map.Entry<String, Integer>> ite = common.entrySet().iterator();
		String result = "";
		Integer value = 0;
		while (ite.hasNext()) {
			Map.Entry<String, Integer> entry = ite.next();
			if (value < entry.getValue()) {
				result = entry.getKey();
				value = entry.getValue();
			}
		}
		return result;
	}
	/**
	 * 
	 * @param dataSet
	 * @param columnIndex
	 * @return
	 */
    public static double getIGRatio4Nominal(List<List<String>> dataSet, int columnIndex){
    	double entropy = 0; 
		double combinedEntropy = 0;
		double infoGain = 0;
		double splitInfoGain = 0;
		double IGRatio = 0;
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
				splitInfoGain += (-1)*probC*Math.log(probC)/Math.log(2);
				combinedEntropy += probC*singleEntropy;
				
			}
			infoGain = entropy - combinedEntropy;
			if(infoGain<0){
				System.out.println("InfoGain<0");
			}
			IGRatio = infoGain/splitInfoGain;
			return IGRatio;
    }
    /**
     * 
     * @param dataSet
     * @param columnIndex
     * @return
     */
    public static double[] getIGRatio4Numeric(List<List<String>> dataSet, int columnIndex){
    	 //numeric attributes
    	double[] result= new double[2]; 
		double entropy = 0; 
		double finalThreshold = 0;
		double maxIGRatio = 0;
		double maxInfoGain = 0;
		double bestSplitInfoGain = 0;
		//Count class/label number here, dataSet.get(0).size()-1 (first data column has class)
		Map<String, Integer> labelCounts = getAttrCounts(dataSet, dataSet.get(0).size()-1);
		//Compute Entire Entropy 
		Iterator<Map.Entry<String, Integer>> ite = labelCounts.entrySet().iterator();
		while(ite.hasNext()){
			Map.Entry<String, Integer> entry = ite.next();
			double prob = (double)entry.getValue()/(double)dataSet.size();
			entropy += (-1)*prob*Math.log(prob)/Math.log(2);
		}
		List<String> thresholdSet = new ArrayList<String>();
		for(List<String> data : dataSet){            
			String threshold = data.get(columnIndex);   //get values of specific column in every sample
			if(threshold.equals("?")){
				threshold = getCommon4Missing(dataSet, columnIndex);//proceed missing val while calculating info gain ratio
			}
			thresholdSet.add(threshold);
		}
		List<Double> sortThresholdSet = getSortThresholdSet(thresholdSet); //after sorting
		for(int i = 0; i < sortThresholdSet.size()-1; i++){  //pick best threshold for best information gain ratio
			double conditionEntropy = 0;
			double lConditionEntropy = 0;
			double rConditionEntropy = 0;
			
			//threshold - mean between 2 values
			double threshold = (sortThresholdSet.get(i)+sortThresholdSet.get(i+1))/2;
			double lsize = i+1;
			double rsize = sortThresholdSet.size()-(i+1);
			if(sortThresholdSet.get(i+1)<=threshold){
				lsize = i+2;
				rsize = sortThresholdSet.size()-(i+2);
			}

			//LHS
			Map<String, Integer> LHSLabelCounts = getLHSLabelCounts(dataSet, columnIndex, threshold);
			Iterator<Map.Entry<String, Integer>> iteLeft = LHSLabelCounts.entrySet().iterator();
			//RHS
			Map<String, Integer> RHSLabelCounts = getRHSLabelCounts(dataSet, columnIndex, threshold);
			Iterator<Map.Entry<String, Integer>> iteRight = RHSLabelCounts.entrySet().iterator();
			while(iteLeft.hasNext()){
				Map.Entry<String, Integer> entry = iteLeft.next();
				double prob = (double)entry.getValue()/(double)lsize;
				lConditionEntropy += (-1)*prob*Math.log(prob)/Math.log(2);
			}
			while(iteRight.hasNext()){
				Map.Entry<String, Integer> entry = iteRight.next();
				double prob = (double)entry.getValue()/(double)rsize;
				rConditionEntropy += (-1)*prob*Math.log(prob)/Math.log(2);
			}
			double lprob = lsize/(double)dataSet.size();
			double rprob = rsize/(double)dataSet.size();
			conditionEntropy = lprob*lConditionEntropy + rprob*rConditionEntropy;
			double infoGain = entropy - conditionEntropy;
			double splitInfoGain = (-1)*lprob*Math.log(lprob)/Math.log(2) + (-1)*rprob*Math.log(rprob)/Math.log(2);
		    if(maxInfoGain<infoGain){
		    	maxInfoGain = infoGain;
		    	finalThreshold = threshold;
		    	bestSplitInfoGain = splitInfoGain;
		    }
		}
		if(maxInfoGain<0){
			System.out.println("maxInfoGain<0");
		}
		result[0] = (maxInfoGain-(Math.log(dataSet.size()-1)/Math.log(2))/dataSet.size())/bestSplitInfoGain;
		result[1] = finalThreshold;
		return result;
    }
    /**
     * get counts in indexTH column
     * for nominal attributes
     * @param dataSet
     * @param index
     * @return
     */
    public static Map<String, Integer> getAttrCounts(List<List<String>> dataSet, int index){
    	Map<String, Integer> attrCounts = new HashMap<String, Integer>();
    	String common = getCommon4Missing(dataSet, index);
		for(List<String> data : dataSet){
			String label = data.get(index);   //index of column
			if(label.equals("?")){  //missing value, using common as it
				if(!attrCounts.containsKey(common)){
					attrCounts.put(common, 1); 
				} else{
					attrCounts.put(common, attrCounts.get(common)+1);
				}
			} else{ //unmissing value
				if(!attrCounts.containsKey(label)){
					attrCounts.put(label, 1); 
				} else{
					attrCounts.put(label, attrCounts.get(label)+1);
				}
			}
		}
		return attrCounts;
    }
    /**
     * 
     * @param dataSet
     * @param index
     * @param threshold
     * @return
     */
    public static List<List<String>> getLHSDataSet(List<List<String>> dataSet, int index, double threshold){
    	List<List<String>> LHSDataSet = new ArrayList<List<String>>();
    	String common = getCommon4Missing(dataSet, index);
    	for(List<String> data : dataSet){
    		String temp = data.get(index);
    		if(temp.equals("?")){
    			temp = common;
    		}
    		if(Double.valueOf(temp)<=threshold){
       			List<String> tmp = new ArrayList<String>();
    			for (int k = 0; k < data.size(); k++) {
					if (k != index) {
						tmp.add(data.get(k));
					}
				}
    			LHSDataSet.add(tmp);
    		}
    	}
		return LHSDataSet;
    }
    public static List<List<String>> getRHSDataSet(List<List<String>> dataSet, int index, double threshold){
    	List<List<String>> RHSDataSet = new ArrayList<List<String>>();
    	String common = getCommon4Missing(dataSet, index);
    	for(List<String> data : dataSet){
    		String temp = data.get(index);
    		if(temp.equals("?")){
    			temp = common;
    		}
    		if(Double.valueOf(temp)>threshold){
       			List<String> tmp = new ArrayList<String>();
    			for (int k = 0; k < data.size(); k++) {
					if (k != index) {
						tmp.add(data.get(k));
					}
				}
    			RHSDataSet.add(tmp);
    		}
    	}
		return RHSDataSet;
    }
    /**
     * getLHSLabelCounts
     * @param dataSet
     * @param columnIndex
     * @param threshold
     * @return
     */
    public static Map<String, Integer> getLHSLabelCounts(List<List<String>> dataSet, int columnIndex, double threshold){
    	Map<String, Integer> LHSLabelCounts = new HashMap<String, Integer>();
    	String common = getCommon4Missing(dataSet, columnIndex);
		for(List<String> data : dataSet){
			String target = data.get(columnIndex);
			if(target.equals("?")){ //missing
				target = common;
			}
			double temp = Double.valueOf(target);
			if(temp<=threshold){
				String label = data.get(data.size()-1);   //index of column
				if(!LHSLabelCounts.containsKey(label)){
					LHSLabelCounts.put(label, 1); 
				} else{
					LHSLabelCounts.put(label, LHSLabelCounts.get(label)+1);
				}
			}
		}
		return LHSLabelCounts;
    }
    /**
     * RHSLabelCounts
     * @param dataSet
     * @param columnIndex
     * @param threshold
     * @return
     */
    public static Map<String, Integer> getRHSLabelCounts(List<List<String>> dataSet, int columnIndex, double threshold){
    	Map<String, Integer> RHSLabelCounts = new HashMap<String, Integer>();
    	String common = getCommon4Missing(dataSet, columnIndex);
		for(List<String> data : dataSet){
			String target = data.get(columnIndex);
			if(target.equals("?")){ //missing
				target = common;
			}
			double temp = Double.valueOf(target);
			if(temp>threshold){
				String label = data.get(data.size()-1);   //index of column
				if(!RHSLabelCounts.containsKey(label)){
					RHSLabelCounts.put(label, 1); 
				} else{
					RHSLabelCounts.put(label, RHSLabelCounts.get(label)+1);
				}
			}
		}
		return RHSLabelCounts;
    }
    /**
     * 
     * @param dataSet
     * @param index
     * @param attrClass
     * @return
     */
    
    public static List<List<String>> getSplitDataSet(List<List<String>> dataSet, int index, String attrClass) {  
    	List<List<String>> splitDataSet = new ArrayList<List<String>>();  
        String common = getCommon4Missing(dataSet, index);
    	for(List<String> data : dataSet) {
        	String temp = data.get(index);
            if(temp.equals(attrClass)) {  
                splitDataSet.add(data);  
            } else if(temp.equals("?")){  //value of the temp using common while missing 
            	 if(common.equals(attrClass)) {  
                     splitDataSet.add(data);  
                 } 
            }
        }    
        return splitDataSet;  
    }  
    /**
     * 
     * @param thresholdSet
     * @return
     */
    public static List<Double> getSortThresholdSet(List<String> thresholdSet){
    	List<Double> sortThresholdSet = new ArrayList<Double>();
    	for(String threshold : thresholdSet){
    		Double th = Double.valueOf(threshold);
    		sortThresholdSet.add(th);
    	}
    	Collections.sort(sortThresholdSet);
    	return sortThresholdSet;
    }
    /**
     * 
     * @param dataSet
     * @param columnIndex
     * @return
     */

}
