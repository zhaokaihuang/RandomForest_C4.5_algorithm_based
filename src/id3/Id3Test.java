package id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import id3.Id3;

public class Id3Test {

	public static void main(String[] args) throws IOException {

		File f = new File("C:/Users/Administrator/Desktop/playtennis.txt");  //training data
		File f2 = new File("C:/Users/Administrator/Desktop/playtennis.txt"); //testing data
		List<String> attributeSet = new ArrayList<String>();
		List<List<String>> dataSet = new ArrayList<List<String>>();
		List<List<String>> testDataSet = new ArrayList<List<String>>();
		Node root = new Node();
		int depth = 10;
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
			System.out.println("start building trees");
			Id3 id3 = new Id3(dataSet);
			root = id3.buildTree(attributeSet, dataSet, depth);
			id3.setRoot(root);
			System.out.println("finish building trees");
			System.out.println("----print tree in vertical direction----");
			id3.printId3(id3.getRoot());
			System.out.println();
			System.out.println("----print tree in horizontal direction----");
			id3.searchTree(id3.getRoot());
			System.out.println();
			
			List<String> resultList = id3.test(id3.getRoot(), attributeSet,
					testDataSet);
			double err = id3.err(resultList, testDataSet);
			System.out.println("err rate: " + err);

			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
