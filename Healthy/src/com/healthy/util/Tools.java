package com.healthy.util;

import android.os.Environment;

public class Tools {
	
	/**
	 * ºÏ≤È «∑Ò¥Ê‘⁄SDCard
	 * @return
	 */
	public static boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}
}
