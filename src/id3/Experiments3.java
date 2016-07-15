package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Experiments3 {
	public static void main(String[] args) throws IOException {
		File f = new File("C:/Users/Administrator/Desktop/dataset/adult.data"); // training data
		File f2 = new File("C:/Users/Administrator/Desktop/dataset/adult.test"); // testing data
		List<String> attributeSet = new ArrayList<String>();
		List<List<String>> dataSet = new ArrayList<List<String>>();
		List<List<String>> testDataSet = new ArrayList<List<String>>();
		int depth = 10;// default depth
		int treeNum = 200;// num of decision trees
		try {
			FileReader fr = new FileReader(f);
			FileReader fr2 = new FileReader(f2);
			BufferedReader br = new BufferedReader(fr);
			BufferedReader br2 = new BufferedReader(fr2);
			String str = br.readLine();
			String str2 = br2.readLine();
			String[] attributes = str.split(",");
			for (int i = 0; i < attributes.length; i++) {
				attributeSet.add(attributes[i]);
			}
			while ((str = br.readLine()) != null) {
				List<String> dataList = new ArrayList<String>();
				String[] data = str.split(",");
				for (int j = 0; j < data.length; j++) {
					dataList.add(data[j]);
				}
				dataSet.add(dataList);
			}
			while ((str2 = br2.readLine()) != null) {
				List<String> testDataList = new ArrayList<String>();
				String[] data = str2.split(",");
				for (int j = 0; j < data.length; j++) {
					testDataList.add(data[j]);
				}
				testDataSet.add(testDataList);
			}
			RandomForest rf = new RandomForest(dataSet);
			rf.buildRandomForest(attributeSet, dataSet, depth, treeNum);
			List<String> resultList = rf.test(rf.getDecisionTrees(), attributeSet, testDataSet);
			Map<String, Integer> confusionRF = rf.getConfusionMatrix(resultList, testDataSet);
			int tp = confusionRF.get("tp");
			int tn = confusionRF.get("tn");
			int fp = confusionRF.get("fp");
			int fn = confusionRF.get("fn");
			double tprate = (double)tp/(tp+fn);
			double fprate = (double)fp/(fp+tn); 
			
			System.out.println("Rf tprate is: "+tprate+", fprate is: "+fprate);
			System.out.println(tp);
			System.out.println(fn);
			System.out.println(fp);
			System.out.println(tn);
			System.out.println(rf.err(resultList, testDataSet));
			Id3 id3 = new Id3(dataSet);
			Node root = id3.buildTree(attributeSet, dataSet, depth);
			id3.setRoot(root);
			List<String> resultList2 = id3.test(id3.getRoot(), attributeSet,
					testDataSet);
			Map<String, Integer> confusionDT = id3.getConfusionMatrix(resultList2, testDataSet);
			int tp2 = confusionDT.get("tp");
			int tn2 = confusionDT.get("tn");
			int fp2 = confusionDT.get("fp");
			int fn2 = confusionDT.get("fn");
			
			double tprate2 = (double)tp2/(tp2+fn2);
			double fprate2 = (double)fp2/(fp2+tn2);
			System.out.println("DT tprate is: "+tprate2+", fprate is: "+fprate2);
			System.out.println(tp2);
			System.out.println(fn2);
			System.out.println(fp2);
			System.out.println(tn2);
			System.out.println(id3.err(resultList2, testDataSet));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
