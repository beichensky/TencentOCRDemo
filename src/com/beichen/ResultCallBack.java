package com.beichen;

/***
 * 请求结束之后将数据带到主线程时需要调用的接口
 * @author Beichen Sky
 *
 */
public interface ResultCallBack {
	/**
	 * 网络请求成功的回调函数
	 * @param imageName
	 * @param result
	 */
	void onSuccess(String imageName, String result);
	
	/**
	 * 网络请求失败的回调函数
	 * @param errorMsg
	 */
	void onFailed(String errorMsg);
}
