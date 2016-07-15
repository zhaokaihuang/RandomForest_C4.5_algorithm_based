package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experiments2 {

	public static void main(String[] args) throws IOException {
		File f = new File("C:/Users/Administrator/Desktop/voteall.txt");  //training data
		
		List<String> attributeSet = new ArrayList<String>();
		List<List<String>> dataSet = new ArrayList<List<String>>();
		int treeNum = 100;
		
		int depth = 10;
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String str = br.readLine();
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
            List<Double> errDT = new ArrayList<Double>();
            List<Double> errRF = new ArrayList<Double>();
			for(int i = 0; i<10; i++){
				double err = 0;
				double err2 = 0;
	
					List<List<String>> trainDataSet = new ArrayList<List<String>>();
					List<List<String>> testDataSet = new ArrayList<List<String>>();
					testDataSet.addAll(dataSet.subList(0, 1*dataSet.size()/5));
					trainDataSet.addAll(dataSet.subList(1*dataSet.size()/5, dataSet.size()));
					Id3 id3 = new Id3(dataSet);
					RandomForest rf = new RandomForest(dataSet);
					rf.buildRandomForest(attributeSet, dataSet, depth, treeNum);
					
					
					Node root = id3.buildTree(attributeSet, trainDataSet, depth);

					id3.setRoot(root);

					List<String> resultList = id3.test(id3.getRoot(), attributeSet,
							testDataSet);
					List<String> resultList2 = rf.test(rf.getDecisionTrees(), attributeSet, testDataSet);
					err = id3.err(resultList, testDataSet);
					err2 = rf.err(resultList2, testDataSet);
					Collections.shuffle(dataSet);
				

				errDT.add(err);
				errRF.add(err2);
				
			}
			System.out.print("err rate DT: ");
			for(Double err :errDT){
				System.out.print(err+", ");
			}
			System.out.println();
			System.out.print("err rate RF: ");
			for(Double err :errRF){
				System.out.print(err+", ");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
