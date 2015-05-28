package com.healthy.ui.menupanel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MenuBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals("com.healthy.action.messages")) {
			MenuPanel.mAdapter.notifyDataSetChanged();
		}
	}
}
