package com.beichen;

import java.io.File;

public class Tester {

	/**
	 * 运行说明：
	 * 		1、请保证 OCRHttpUtil 类中的 SECRET_ID、SECRET_KEY、APPID、BUCKET 已替换为正确的
	 * 		2、请保证下方的识别图片文件夹路径和图片名正确
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: 如需要，请替换为正确的文件夹路径
		String originPath = "E:/Eclipse/EclipseWorkSpace/TencentOCRDemo/src/";
		String dirName = "";
		// TODO: 如需要，请替换为正确的图片名
		String path = originPath + File.separator + dirName;
		String imageName = "test.png";
		OCRHttpUtil.instance().execute(new ResultCallBack() {
			@Override
			public void onSuccess(String imageName, String result) {
				System.out.println("数据请求成功了，" + imageName + "=====" + result);
			}

			@Override
			public void onFailed(String errorMsg) {
				System.out.println("网络请求失败，errorMsg" + "=====" + errorMsg);
			}
		}).uploadImage(path, imageName);
	}
	
}
