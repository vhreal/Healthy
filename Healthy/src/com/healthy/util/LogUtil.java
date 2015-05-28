package com.healthy.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.healthy.logic.HealthyApplication;

import android.os.Environment;
import android.util.Log;

/**日志工具类*/
public class LogUtil {

	private static String fileName;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static boolean addLog(String log) {

		// 没有安装sd卡
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			return false;
		try {
			log = "\r\n" + log;
			fileName = "log-" + dateFormat.format(new Date()) + ".txt";
			File dir = new File(HealthyApplication.APPLICATION_PATH
					+ File.separator + "Log");
			File file = new File(dir.getAbsoluteFile() + File.separator
					+ fileName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(log);
			bw.close();
		} catch (Exception e) {
			Log.e("LogUtil", "", e);
			return false;
		}
		return true;
	}
}
