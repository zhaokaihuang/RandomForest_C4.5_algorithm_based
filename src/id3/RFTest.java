package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RFTest {

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
			double err = rf.err(resultList, testDataSet);
			System.out.println("Average err rate by Random Forest: " + err);
			String preLabel = rf.vote(rf.getDecisionTrees(), attributeSet, testDataSet.get(0), testDataSet);
			System.out.println("Predict label by Random Forest: " + preLabel + ", actual label is: "
					+ testDataSet.get(0).get(attributeSet.size() - 1));
			//rf.printRandomForest(rf.getDecisionTrees());
			//rf.searchRandomForest(rf.getDecisionTrees());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
