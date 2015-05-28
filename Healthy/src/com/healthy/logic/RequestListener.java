package com.healthy.logic;

import com.healthy.logic.model.ResponseBean;

public abstract class RequestListener<T extends ResponseBean> {
	public abstract void onStart();
	public abstract void onComplete(T bean);
}
