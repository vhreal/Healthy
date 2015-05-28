package com.healthy.classifier;

/**
 * 取消地磁标准差阀值，识别自动扶梯
 * @author Administrator
 *
 */
public class Configure {
	//每个Frame中的周期数
	public static final int Nfc = 3; 
	
	// 平均滤波
	public static final int Average_N = 3;
	
	// 阈值,peak平均值的倍数
	public static final double C = 2;
	
	//特征值个数
	public static final int FeatureNum =23;
	
	//识别的活动数
	public static final int ScenesNum = 5;
	
	//文件路径
	//public static final String FilePath = BackgroundService.sdcardDir.getPath()+"//传感器数据"+"//";
	
	//SVM保存名称
	public static final String SVM_MODEL = "SVM_model.txt";
	
	//不同的分类算法
	public static final int SVM_CLASSIFIER = 1; 
	public static final int PNN_CLASSIFIER = 2; 
	public static final int BAYES_CLASSIFIER = 3; 
	
	public static final int Classfier_type = PNN_CLASSIFIER; 
	
	//平均能量，用于区分是否为周期性活动
	public static final double E = 1;
	
	//加速度标准差阈值，用于判断是否为非周期性活动
	public static final double SD_ACC = 0.8;
	
//	//地磁标准差阈值，用于判断是否为自动扶梯
//	public static final double SD_MAG = 3;
	
	//最小合法窗长
	public static final int MIN_WIN = 30;
	
	//每个活动提取的特征向量数
	//public static int sample_num_each_scene = 0;
	
	//每个活动提取的特征向量数,根据a、w的行数来定
	public static int num_total_train_sample = 1400;
	
	////投射窗口
	public static final int ProjectWin = 50;
}
