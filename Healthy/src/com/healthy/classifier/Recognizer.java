package com.healthy.classifier;

import java.util.ArrayList;
import java.util.List;

import com.healthy.logic.BackgroundService;
import com.healthy.logic.model.SensorInDb;

/**
 * 删除自动扶梯改动类
 * @author Administrator
 *
 */
public class Recognizer {
	public static int recResult = 0;
	public static int strideCount = 0;

	public static int recognize(SensorInDb data,
			BackgroundService backgroundService) {
		Lift lift = new Lift();
		if (lift.isLift(data.xAcc, data.yAcc, data.zAcc)) {
			strideCount = 0;
			return ActivityCategories.Lift;
		}

		if (nonPeriodic(data)) {
//			recResult = decisionTree(data.MagxData, data.MagyData,
//					data.MagzData);
			recResult = ActivityCategories.Stationary;
			strideCount = 0;
			return recResult;
		} else {

			List<Double> features = Features.getFeatures(data);

			List<Double> feature = new ArrayList<Double>();
			// 如果得到了特征值，取第一组
			if (features.size() > 0) {
				feature = features.subList(0, Configure.FeatureNum);
				recResult = getResult(feature, backgroundService);
				strideCount = Features.getstrides().size();
				return recResult;
			}
			// 如果没得到特征值，把它当做非周期活动来识别
			else {
//				recResult = decisionTree(data.MagxData, data.MagyData,
//						data.MagzData);
				recResult = ActivityCategories.Stationary;
				strideCount = 0;
				return recResult;
			}

		}
	}

	/**
	 * 将加速度数据投射到竖直方向，判断是否为非周期性活动。
	 * 
	 * @param data
	 * @return
	 */
	private static boolean nonPeriodic(SensorInDb data) {
		// 移动平均滤波之后
		List<Double> Sx = new ArrayList<Double>();
		List<Double> Sy = new ArrayList<Double>();
		List<Double> Sz = new ArrayList<Double>();
		// 投射之后
		List<Double> projects = new ArrayList<Double>();
		// 标准差
		double sd = 0;

		Features.getSmooth(data.xAcc, data.yAcc, data.zAcc, Sx, Sy, Sz);
		Features.project(Sx, Sy, Sz, projects);
		sd = Compute.getStandardDeviation(projects);

		if (sd > Configure.SD_ACC) {
			return false;
		} else
			return true;
		/*
		 * Features.getFeatures(x, y, z); List<Double> projects =
		 * Features.getProjects(); if(Compute.getAverageEnergy(projects) >
		 * Configure.E){ return true; } else return false;
		 */
	}

//	/**
//	 * 判断是静止还是扶梯 根据mag求模之后的方差，如果大于阈值，则判断为乘坐扶梯
//	 * 
//	 * @param x
//	 * @param y
//	 * @param z
//	 * @return
//	 */
//	private static int decisionTree(List<Double> x, List<Double> y,
//			List<Double> z) {
//		// 求模
//		List<Double> mods = new ArrayList<Double>();
//		for (int i = 0; i < x.size(); ++i) {
//			mods.add(Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2)
//					+ Math.pow(z.get(i), 2)));
//		}
//		// 求方差
//		double sd = Compute.getStandardDeviation(mods);
//
//		if (sd > Configure.SD_MAG) {
//			return ActivityCategories.Escalator;
//		} else
//			return ActivityCategories.Stationary;
//	}

	/**
	 * 使用分类器得到结果
	 * 
	 * @param feature
	 * @param backgroundService
	 * @return
	 */
	private static int getResult(List<Double> feature,
			BackgroundService backgroundService) {
		String result = null;

		PNNClassifier pnn = new PNNClassifier();
		result = pnn.predict(feature, backgroundService);

		if (result.equals("步行")) {
			return ActivityCategories.Walking;
		} else if (result.equals("跑步")) {
			return ActivityCategories.Jogging;
		} else if (result.equals("上楼梯")) {
			return ActivityCategories.AscendingStairs;
		} else if (result.equals("下楼梯")) {
			return ActivityCategories.DescendingStairs;
		} else if (result.equals("骑自行车")) {
			return ActivityCategories.Bicycling;
		} else {
			return 0;
		}
	}

}
