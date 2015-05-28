package com.healthy.classifier;

import java.util.ArrayList;
import java.util.List;


public class Compute {

	public static double getSum(List<Double> data) {
		double sum = 0;
		for (double i : data) {
			sum += i;
		}
		return sum;
	}

	// 最大值
	public static double getMax(List<Double> data) {
		double max = -100;
		for (double i : data) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	// 最小值
	public static double getMin(List<Double> data) {
		double min = 100;
		for (double i : data) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}

	// 幅度
	public static double getRange(List<Double> data) {
		double max = getMax(data);
		double min = getMin(data);
		return (max - min);

	}

	// 平均值
	public static double getAverage(List<Double> data) {
		double sum = getSum(data);
		return (sum / data.size());
	}

	// 标准差
	public static double getStandardDeviation(List<Double> data) {
		double average = getAverage(data);
		double sum = 0;
		for (double i : data) {
			sum += Math.pow((i - average), 2);
		}
		return (Math.sqrt(sum / data.size()));
	}

	// 平均绝对偏差
	public static double getAverageAbsoluteDifference(List<Double> data) {
		double average = getAverage(data);
		double sum = 0;
		for (double i : data) {
			sum += Math.abs(i - average);
		}
		return (sum / data.size());
	}

	// 分布
	public static int[] getBinnedDistribution(List<Double> data) {
		int[] bd = new int[5];
		for (int i=0;i<bd.length;i++)
			bd[i]=0;
		double min = getMin(data);
		double max = getMax(data);
		double range = getRange(data);
		for (double i : data) {
			if (i <= range / 5 + min) {
				bd[0]++;
			} else if (i <= 2 * range / 5 + min) {
				bd[1]++;
			} else if (i <= 3 * range / 5 + min) {
				bd[2]++;
			} else if (i <= 4 * range / 5 + min) {
				bd[3]++;
			} else if (i <= max) {
				bd[4]++;
			} else {
				System.out.println("getBinnedDistribution Error!");
			}
		}
		return bd;
	}

	// 平均能量
	public static double getAverageEnergy(List<Double> data) {
		double sum = 0;
		for (double i : data) {
			sum += Math.pow(i, 2);
		}
		return (sum / data.size());
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return依次得到xy、xz和yz的相关性，按顺序存在List中
	 */
	public static List<Double> getCorrelation(List<Double> x, List<Double> y,
			List<Double> z) {
		List<Double> correlation = new ArrayList<Double>();
		double aveX = Compute.getAverage(x);
		double aveY = Compute.getAverage(y);
		double aveZ = Compute.getAverage(z);

		double covXY = 0;
		double covXZ = 0;
		double covYZ = 0;
		for (int i = 0; i < x.size(); i++) {
			covXY += (x.get(i) - aveX) * (y.get(i) - aveY);
			covXZ += (x.get(i) - aveX) * (z.get(i) - aveZ);
			covYZ += (y.get(i) - aveY) * (z.get(i) - aveZ);
		}

		double pXY = covXY
				/ (Compute.getStandardDeviation(x) * Compute
						.getStandardDeviation(y));
		double pXZ = covXZ
				/ (Compute.getStandardDeviation(x) * Compute
						.getStandardDeviation(z));
		double pYZ = covYZ
				/ (Compute.getStandardDeviation(y) * Compute
						.getStandardDeviation(z));
		correlation.add(pXY);
		correlation.add(pXZ);
		correlation.add(pYZ);
		return correlation;
	}

	/**
	 * 求模
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static double getModel(double x, double y, double z) {
		double model = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
				+ Math.pow(z, 2));
		return model;
	}

	/**
	 * 插值，使得方向传感器数据与加速度传感器数据对应起来
	 * 
	 * @param data
	 * @param start
	 * @param end
	 * @return
	 */
	/*public static List<Double> GetInterpolation(Data data, int start, int end) {
		List<Double> oriY = new ArrayList<Double>();
		double starttime = data.accTime.get(start);
		double endtime = data.accTime.get(end);
		int oristart = 0;
		int oriend = 0;
		for (int i = 0; i < data.oriTime.size() - 1; i++) {
			if (data.oriTime.get(i) < starttime
					&& data.oriTime.get(i + 1) > starttime) {
				oristart = i;
			}
			if (data.oriTime.get(i) < endtime
					&& data.oriTime.get(i + 1) > endtime) {
				oriend = i;
			}
		}
		oriY = data.OriyData.subList(oristart, oriend);
		return oriY;
	}*/

	// 求偏度
	public static double GetSkewness(List<Double> data) {
		double result = 0;
		double sum = 0;
		double ave = getAverage(data);
		double sd = getStandardDeviation(data);
		for (int i = 0; i < data.size(); i++) {
			sum += Math.pow(data.get(i) - ave, 3);
		}
		sum = sum / data.size();
		result = sum / Math.pow(sd, 3);

		return result;
	}
}
