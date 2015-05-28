package com.healthy.classifier;

import java.util.ArrayList;
import java.util.List;

public class Lift {

	private static final double HIGH = 10.5;
	private static final double LOW = 10;
	private static final int WIN = 200;
	private static double coef = 0.5;

	/**
	 * 判断是否为电梯：一个滑动的窗口，判断窗口内是否有足够的点大于阈值，如果是，判断是否有过多的点大于阈值，如果为否，则得到一个正脉冲，负脉冲同理
	 * 得到一个脉冲，即认为是在乘坐电梯
	 * @param xData
	 * @param yData
	 * @param zData
	 * @return
	 */
	public boolean isLift(List<Double> xData,List<Double> yData,List<Double> zData) {
		// 记录正负脉冲位置
		//int po=0 ;
		//int ne=0 ;
		// 是否有过正负脉冲
		boolean poPulse = false;
		boolean nePulse = false;

		//先求模值
		List<Double> mod = new ArrayList<Double>();
		for (int i = 0; i < xData.size(); ++i) {
			mod.add(Math.sqrt(Math.pow(xData.get(i), 2)
					+ Math.pow(yData.get(i), 2) + Math.pow(zData.get(i), 2)));
		}
		int countH = 0;
		int countL = 0;
		for (int i = 0; i < mod.size(); ++i) {
			if (mod.get(i) > HIGH) {
				countH++;
				if (countH == WIN * coef && poPulse == false) {
					//po = i;
					poPulse = true;
				}
				if (countH == WIN) {
					// System.out.println("not a pulse  "+i);
				}
			} else {
				if (countH > 0) {
					countH--;
				}
			}

			if (mod.get(i) < LOW) {
				countL++;
				if (countL == WIN * coef && nePulse == false) {
					//ne = i;
					nePulse = true;
				}
				if (countL == WIN) {
				}
			} else {
				if (countL > 0) {
					countL--;
				}
			}

			if (countH == 0) {
				if (poPulse) {
					return true;
				}
			}
			if (countL == 0) {
				if (nePulse) {
					return true;
				}
			}
		}
		return false;
	}
}
