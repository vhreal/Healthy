package com.healthy.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.healthy.logic.BackgroundService;

public class PNNClassifier {

	/*private File file01 = null;
	private File file02 = null;
	private File file03 = null;
	private File file04 = null;*/
	private BufferedReader BR01 = null;
	/*private PrintWriter PW01 = null;
	private PrintWriter PW02 = null;*/

	public static int num_total_train_sample = Configure.num_total_train_sample;
	public static int num_total_scenes = Configure.ScenesNum;
	public static int num_vector_dimesion = Configure.FeatureNum;
	private String[] scenes = { "上楼梯", "下楼梯", "步行", "跑步", "骑自行车" };
	//private double[] mid_metrix = new double[num_total_train_sample];
	private int[][] a = new int[num_total_train_sample][num_total_scenes];
	private double[][] w = new double[num_total_train_sample][num_vector_dimesion];
	private double[] net = new double[num_total_train_sample];
	private double[] g = new double[num_total_scenes];

	private String temp_save_string = null;
	private String[] temp_save_sample = null;
	private Feature_Vector Temp_FV = new Feature_Vector();
	//private ArrayList<Feature_Vector> Sample_list_train = new ArrayList<Feature_Vector>();
	private ArrayList<Feature_Vector> Sample_list_test = new ArrayList<Feature_Vector>();

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub

		PNNClassifier pnn = new PNNClassifier();
		// pnn.train();
		// pnn.predict();
	}*/

	/**
	 * 根据feature，使用分类器识别
	 * 
	 * @param feature
	 * @param backgroundService
	 * @return
	 */
	public String predict(List<Double> feature,
			BackgroundService backgroundService) {
		String result = "";
		/*
		 * file01 = new File(Configure.FilePath+"a.txt"); file02 = new
		 * File(Configure.FilePath+"w.txt");
		 */
		try {
			InputStreamReader inputReaderA = new InputStreamReader(
					backgroundService.getResources().getAssets().open("a.txt"));
			InputStreamReader inputReaderW = new InputStreamReader(
					backgroundService.getResources().getAssets().open("w.txt"));

			/*
			 * file01 = new File("a.txt"); file02 = new File("w.txt");
			 */

			// BR01 = new BufferedReader(new FileReader(file01));
			BR01 = new BufferedReader(inputReaderA);
			int i1 = 0;
			while ((temp_save_string = BR01.readLine()) != null) {
				temp_save_sample = temp_save_string.split("\t");
				for (int i2 = 0; i2 < num_total_scenes; i2++) {
					a[i1][i2] = Integer.parseInt(temp_save_sample[i2]);
				}
				i1++;
			}
			i1 = 0;
			BR01 = new BufferedReader(inputReaderW);
			while ((temp_save_string = BR01.readLine()) != null) {
				temp_save_sample = temp_save_string.split("\t");
				for (int i2 = 0; i2 < num_vector_dimesion; i2++) {
					w[i1][i2] = Double.parseDouble(temp_save_sample[i2]);
				}
				i1++;
			}
		} catch (IOException e) {
			System.out.println("Read File Error--->PNN.predict");
		}
		/*
		 * BR01 = new BufferedReader(new FileReader(file03)); while
		 * ((temp_save_string = BR01.readLine()) != null) { temp_save_sample =
		 * temp_save_string.split("\t"); Temp_FV = new Feature_Vector();
		 * 
		 * for (int i2 = 0; i2 < num_vector_dimesion; i2++) {
		 * Temp_FV.Feature[i2] = ((Double .parseDouble(temp_save_sample[i2])));
		 * } Temp_FV.scene = temp_save_sample[temp_save_sample.length - 1];
		 * Sample_list_test.add(Temp_FV); } BR01.close();
		 */
		Temp_FV = new Feature_Vector();
		for (int i = 0; i < feature.size(); ++i) {
			Temp_FV.Feature[i] = feature.get(i);
		}
		Sample_list_test.add(Temp_FV);

		// 初始化g
		for (int i0 = 0; i0 < Sample_list_test.size(); i0++) {
			Temp_FV = new Feature_Vector();
			Temp_FV = Sample_list_test.get(i0);
			for (int i3 = 0; i3 < num_total_scenes; i3++) {
				g[i3] = 0;
			}

			for (int k0 = 0; k0 < num_total_train_sample; k0++) {
				net[k0] = 0;
				for (int k1 = 0; k1 < num_vector_dimesion; k1++) {
					net[k0] += w[k0][k1] * Temp_FV.Feature[k1];
				}

				for (int k2 = 0; k2 < num_total_scenes; k2++) {
					if (a[k0][k2] == 1) {
						g[k2] += Math.exp((net[k0] - 1) / Math.pow(10, 2.0));
					}
				}
			}

			double temp1 = g[0];
			int temp2 = 0;

			for (int i4 = 0; i4 < num_total_scenes; i4++) {
				if (g[i4] > temp1) {
					temp1 = g[i4];
					temp2 = i4;
				}
			}
			/*
			 * System.out.println(i0 + ":" + "Actual : " + Temp_FV.scene +
			 * "; Recognize : " + scenes[temp2]);
			 */
			result = scenes[temp2];

		}

		return result;
	}

	/*
	 * public void train(File file) { //file01 = new File("train_set.txt");
	 * file01 = file;
	 * 
	 * try { BR01 = new BufferedReader(new FileReader(file01)); while
	 * ((temp_save_string = BR01.readLine()) != null) { Temp_FV = new
	 * Feature_Vector(); temp_save_sample = temp_save_string.split("\t"); for
	 * (int i = 0; i < num_vector_dimesion; i++) { Temp_FV.Feature[i] = Double
	 * .parseDouble(temp_save_sample[i]); } Temp_FV.scene =
	 * temp_save_sample[num_vector_dimesion]; Sample_list_train.add(Temp_FV); //
	 * System.out.println(Sample_list_train.lastElement().scene + // "\n" +
	 * Sample_list_train.size()); }
	 * 
	 * BR01.close();
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * for (int i1 = 0; i1 < num_total_train_sample; i1++) { mid_metrix[i1] = 0;
	 * for (int i2 = 0; i2 < num_total_scenes; i2++) { a[i1][i2] = 0; } for (int
	 * i3 = 0; i3 < num_vector_dimesion; i3++) { mid_metrix[i1] = mid_metrix[i1]
	 * + Math.pow(Sample_list_train.get(i1).Feature[i3], 2.0); } mid_metrix[i1]
	 * = Math.pow(mid_metrix[i1], 0.5); // System.out.println(sum[i1]); }
	 * 
	 * for (int j1 = 0; j1 < num_total_train_sample; j1++) { for (int j2 = 0; j2
	 * < num_vector_dimesion; j2++) { Sample_list_train.get(j1).Feature[j2] =
	 * Sample_list_train .get(j1).Feature[j2] / mid_metrix[j1]; w[j1][j2] =
	 * Sample_list_train.get(j1).Feature[j2]; // w[j1][j2] =
	 * Math.abs(w[j1][j2]); // System.out.println(w1[j1][j2]); } for (int j3 =
	 * 0; j3 < num_total_scenes; j3++) { //
	 * System.out.println(Sample_list_train.get(j1).scene+" : " + //
	 * scenes[j3]); if (Sample_list_train.get(j1).scene.equals( scenes[j3])) {
	 * a[j1][j3] = 1; } } }
	 * 
	 * file01 = new File(Configure.FilePath+"a.txt"); file02 = new
	 * File(Configure.FilePath+"w.txt");
	 * 
	 * try { file01.createNewFile(); file02.createNewFile(); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace();
	 * }
	 * 
	 * try { PW01 = new PrintWriter(new FileOutputStream(file01)); PW02 = new
	 * PrintWriter(new FileOutputStream(file02));
	 * 
	 * for (int k1 = 0; k1 < num_total_train_sample; k1++) { temp_save_string =
	 * ""; for (int k2 = 0; k2 < num_total_scenes; k2++) { if (k2 <
	 * num_total_scenes - 1) { temp_save_string = temp_save_string +
	 * Integer.toString(a[k1][k2]) + "	"; } else { temp_save_string =
	 * temp_save_string + Integer.toString(a[k1][k2]); } }
	 * PW01.println(temp_save_string); PW01.flush(); temp_save_string = ""; for
	 * (int k3 = 0; k3 < num_vector_dimesion; k3++) { if (k3 <
	 * num_vector_dimesion - 1) { temp_save_string = temp_save_string +
	 * Double.toString(w[k1][k3]) + "	"; } else { temp_save_string =
	 * temp_save_string + Double.toString(w[k1][k3]); } }
	 * PW02.println(temp_save_string); PW02.flush(); }
	 * 
	 * PW01.close(); PW02.close();
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */
}
