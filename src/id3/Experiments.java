package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experiments {
	public static void main(String[] args) throws IOException {

		File f = new File("C:/Users/Administrator/Desktop/voteall.txt");  //training data
		
		List<String> attributeSet = new ArrayList<String>();
		List<List<String>> dataSet = new ArrayList<List<String>>();

		
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
            List<Double> errRatio = new ArrayList<Double>();
            List<Double> errInfoGain = new ArrayList<Double>();
			for(int i = 0; i<10; i++){
				double err = 0;
				double err2 = 0;
				for(int k = 0; k<5; k++){
					List<List<String>> trainDataSet = new ArrayList<List<String>>();
					List<List<String>> testDataSet = new ArrayList<List<String>>();
					trainDataSet.addAll(dataSet.subList(0,Integer.valueOf((int) (k*dataSet.size()/(double)5))));
					trainDataSet.addAll(dataSet.subList(Integer.valueOf((int) ((k+1)*dataSet.size()/(double)5)),dataSet.size()));
					testDataSet.addAll(dataSet.subList(Integer.valueOf((int) (k*dataSet.size()/(double)5)),Integer.valueOf((int) ((k+1)*dataSet.size()/(double)5))));
					Id3 id3 = new Id3(dataSet);
					Id3ForInfoGain id32 = new Id3ForInfoGain(dataSet);
					Node root = id3.buildTree(attributeSet, trainDataSet, depth);
					Node root2 = id32.buildTree(attributeSet, trainDataSet, depth);
					id3.setRoot(root);
					id32.setRoot(root2);
					List<String> resultList = id3.test(id3.getRoot(), attributeSet,
							testDataSet);
					List<String> resultList2 = id32.test(id32.getRoot(), attributeSet,
							testDataSet);
					err += id3.err(resultList, testDataSet);
					err2 += id32.err(resultList2, testDataSet);
					Collections.shuffle(dataSet);
				}
				err = err/5;
				err2 = err2/5;
				errRatio.add(err);
				errInfoGain.add(err2);
				
			}
			System.out.print("err rate ratio: ");
			for(Double err :errRatio){
				System.out.print(err+", ");
			}
			System.out.println();
			System.out.print("err rate info gain: ");
			for(Double err :errInfoGain){
				System.out.print(err+", ");
			}
			

			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
