package com.beichen;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/***
 * 网络请求工具类
 * @author Beichen Sky
 *
 */
public class OCRHttpUtil {
	
	/**
	 * 
	 * TODO: 下面的SECRET_ID、SECRET_KEY、APPID、BUCKET使用时替换成自己的即可正常使用
	 * 
	 */

	// SECRET_ID
    public static final String SECRET_ID = "xxx";
    // SECRET_KEY
    public static final String SECRET_KEY = "xxx";
    // 云API秘钥中API秘钥管理中的APPID
    public static final long APPID = 0;
    // 万象优图中存储桶 BUCKET 的名称
    public static final String BUCKET = "xxx";
    
    // 接口文档中有，固定的
    public static final String HOST = "recognition.image.myqcloud.com";
    
    public static final String LINE_END = "\r\n";
    public static final String BOUNDARY = "--------------" + java.util.UUID.randomUUID().toString();

	private static final String URL = "http://recognition.image.myqcloud.com/ocr/general";
	private ResultCallBack callBack;
	private static OCRHttpUtil httpUtil;
	
	private OCRHttpUtil () {
	}
	
	public static OCRHttpUtil instance () {
		if (null == httpUtil) {
			synchronized (OCRHttpUtil.class) {
				if (null == httpUtil) {
					httpUtil = new OCRHttpUtil();
				}
			}
		}
		return httpUtil;
	}
	
	/***
	 * 设置 callBack 对象
	 * @param callBack	ResultCallBack 对象
	 * @return
	 */
	public OCRHttpUtil execute(ResultCallBack callBack) {
		this.callBack = callBack;
		return httpUtil;
	}
	
	/***
	 * 上传图片
	 * @param urlStr	请求地址
	 * @param path		图片所在文件夹的路径
	 * @param imageName	图片名称
	 */
	public void uploadImage(String path, String imageName) {
		new Thread(){
	        @Override
	        public void run() {
	            try {
	                // 配置HttpURLConnection对象
	                HttpURLConnection connection = handlerConnection(path, imageName);
	                // 连接HttpURLConnection
	                connection.connect();
	                // 得到响应
	                int responseCode = connection.getResponseCode();
	                if(responseCode == HttpURLConnection.HTTP_OK){
	                    String result = readInputStream(connection.getInputStream());//将流转换为字符串。
	                    callBack.onSuccess(imageName, result);
	                } else {
	                    String errorMsg = readInputStream(connection.getErrorStream());//将流转换为字符串。
	                    callBack.onFailed(errorMsg);
	                }
	            } catch (Exception e) {
	            	e.printStackTrace();
	                System.out.println( "网络请求出现异常: " + e.getMessage());
	            }
	        }
	    }.start();
	}
	
	/**
	 * 配置Connection对象
	 * @throws Exception 
	 */
	private static HttpURLConnection handlerConnection(String path, String imageName) throws Exception {
		URL url = new URL(URL);
		// 获取HttpURLConnection对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");	// 设置 Post 请求方式
        connection.setDoOutput(true);			// 允许输出流
        connection.setDoInput(true);			// 允许输入流
        connection.setUseCaches(false);			// 禁用缓存

        //设置请求头
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
        connection.setRequestProperty("authorization", sign());
        connection.setRequestProperty("host", HOST);
        System.out.println( "请求头设置完成");

        // 获取HttpURLConnection的输出流
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        
        StringBuffer strBufparam = new StringBuffer();
        strBufparam.append(LINE_END);
        // 封装键值对数据参数
        String inputPartHeader1 = "--" + BOUNDARY + LINE_END + "Content-Disposition:form-data;name=\""+ "appid" +"\";" + LINE_END + LINE_END + APPID + LINE_END;
        String inputPartHeader2 = "--" + BOUNDARY + LINE_END + "Content-Disposition:form-data;name=\""+ "bucket" +"\";" + LINE_END + LINE_END + BUCKET + LINE_END;
        strBufparam.append(inputPartHeader1);
        strBufparam.append(inputPartHeader2);
        //拼接完成后，一起写入
        outputStream.write(strBufparam.toString().getBytes());

        //写入图片文件
        String imagePartHeader = "--" + BOUNDARY + LINE_END +
                "Content-Disposition: form-data; name=\"" + "image" + "\"; filename=\"" + imageName + "\"" + LINE_END +
                "Content-Type: image/jpeg" + LINE_END + LINE_END;
        byte[] bytes = imagePartHeader.getBytes();
        outputStream.write(bytes);
        // 获取图片的文件流
        String imagePath = path + File.separator + imageName;
        InputStream fileInputStream = getImgIns(imagePath);
        byte[] buffer = new byte[1024*2];
        int length = -1;
        while ((length = fileInputStream.read(buffer)) != -1){
            outputStream.write(buffer,0,length);
        }
        outputStream.flush();
        fileInputStream.close();

        //写入标记结束位
        byte[] endData = ("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + LINE_END + BOUNDARY + "--" + LINE_END).getBytes();//写结束标记位
        outputStream.write(endData);
        outputStream.flush();
        return connection;
	} 
	
	/**
	 * 根据文件名获取文件输入流
	 * @throws FileNotFoundException 
	 */
	private static InputStream getImgIns(String imagePath) throws FileNotFoundException {
    	File file = new File(imagePath);
    	FileInputStream is = new FileInputStream(file);
        return is;
    }
	
	/**
     * 把输入流的内容转化成字符串
     * @param is
     * @return
	 * @throws IOException 
     */
    public static String readInputStream(InputStream is) throws IOException{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        int length=0;
        byte[] buffer=new byte[1024];
        while((length=is.read(buffer))!=-1){
            baos.write(buffer, 0, length);
        }
        is.close();
        baos.close();
        return baos.toString();
    }
    
    /**
     * 签名方法，调用Sign文件中的appSign方法生成签名
     * @return 生成后的签名
     */
    public static String sign(){
        long expired = 10000;
        try {
			return Sign.appSign(APPID, SECRET_ID, SECRET_KEY, BUCKET, expired);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
}
