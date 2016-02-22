package com.tangyx.work.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;


import com.tangyx.work.util.SLog;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;


public class HttpConnectWork{
    public final static String GET="GET";
    public final static String POST="POST";
	private final static int TimeOut = 60 * 1000;
	private InputStream inputStream=null;
	private HttpURLConnection huc;
	private int code;
	public String getRequestManager(String url, Map<String, String> headMap) {
		return connectNetwork(url,headMap,null,GET);
	}

	public String postRequestManager(String url, Map<String, String> headMap,byte[] postByte) {
		return connectNetwork(url,headMap,postByte,POST);
	}

    private String connectNetwork(String url, Map<String, String> headMap,byte[] postByte,String method){
        try {
            if(initConnection(url, headMap, method)){
				if(postByte!=null){
					huc.getOutputStream().write(postByte);
					huc.getOutputStream().flush();
					huc.getOutputStream().close();
				}
				code = huc.getResponseCode();
				if(SLog.debug) SLog.d("Connect Code:" + code);
				if(code==200){
					inputStream = huc.getInputStream();
					return getRespContent(getContentEncoding());
				}
			}
        }catch (SocketException e){
			e.printStackTrace();
		} catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
	private boolean initConnection(String url,Map<String,String> headMap,String method){
		try {
			URL u = new URL(url);
			huc = (HttpURLConnection) u.openConnection();
			huc.setConnectTimeout(TimeOut);
			huc.setReadTimeout(TimeOut);
			if(headMap!=null){
				Iterator<String> iterator = headMap.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String val = headMap.get(key);
					huc.setRequestProperty(key,val);
				}
			}
			huc.setRequestMethod(method);
			if(POST.equals(method)){
				huc.setDoInput(true);
				huc.setDoOutput(true);
			}
			return true;
		}catch (Exception e){
			e.printStackTrace();;
		}
		return false;
	}
	private synchronized String getRespContent(String encoding)
			throws IOException {
		if (encoding.toLowerCase().equals("gzip")) {
			return getRespContentGzip();
		}
        String content = "";
        try {
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch=-1;
            while ((ch = inputStream.read()) != -1) {
                bytestream.write(ch);
            }
            inputStream.close();
            byte[] byteContent = bytestream.toByteArray();
            content = new String(byteContent, encoding);
        } catch (Exception e){
            e.printStackTrace();;
        }
		return content;
	}
	private String getRespContentGzip() throws IOException {
		GZIPInputStream gzipis = new GZIPInputStream(inputStream);
		InputStreamReader inreader = new InputStreamReader(gzipis, "utf-8");
		char[] bufArr = new char[128];

		StringBuilder strBuilder = new StringBuilder();
		int nBufLen = inreader.read(bufArr);

		String mlinestr=null;
		while (nBufLen != -1) {
			mlinestr = new String(bufArr, 0, nBufLen);
			strBuilder.append(mlinestr);
			nBufLen = inreader.read(bufArr);
		}
		inreader.close();
		gzipis.close();
		String content = strBuilder.toString();
		return content;
	}
	public String getContentEncoding() {
		String encode = getHeaderVal("content-encoding");
		if(encode!=null&& encode.length() > 0) {
				return encode;
		}
		return "UTF-8";
	}

	public String getHeaderVal(String key) {
		if(TextUtils.isEmpty(key)){
			return null;
		}
		return huc.getHeaderField(key);
	}
	public int getCode() {
		return code;
	}
	public void disconnect() {
		try {
			if(huc!=null){
				huc.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean downFile(String url, Map<String, String> headMap,
			String savePath) {
		try {
			if(downStream(url,headMap)){
				FileOutputStream out = new FileOutputStream(savePath);
				byte[] buff = new byte[1024*8];
				int temp=-1;
				while((temp=inputStream.read(buff))!=-1){
					out.write(buff, 0, temp);
				}
				out.flush();
				out.close();
				inputStream.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public Bitmap downFile(String url, Map<String, String> headMap) {
        if(downStream(url,headMap)){
            return BitmapFactory.decodeStream(inputStream);
        }
		return null;
	}
    /**
     * 获取下载的信息
     */
    private boolean downStream(String url, Map<String, String> headMap){
        try{
           if(initConnection(url,headMap,GET)){
			   code = huc.getResponseCode();
			   if(code==200){
				   inputStream = huc.getInputStream();
				   return true;
			   }
		   }
        }catch(Exception e){
            e.printStackTrace();;
        }
        return false;
    }

	/**
	 *
	 * @param url 地址
	 * @return
	 */

	public String UploadFileManager(String url,Map<String,File> fileParams,Map<String,String> textParams,Map<String,String> headMap){
		String hyphens = "--";
		String boundary = UUID.randomUUID().toString();
		String end = "\r\n";
		try{
			if(initConnection(url,headMap,POST)){
				huc.setChunkedStreamingMode(128 * 1024);
				huc.setRequestProperty("Connection","Keep-Alive");
				huc.setRequestProperty("Charset","UTF-8");
				huc.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
				DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
				dos.writeBytes(hyphens+boundary+end);
				if(fileParams!=null){
					Iterator<String> iterator = fileParams.keySet().iterator();
					while(iterator.hasNext()){
						String key = iterator.next();
						File file = fileParams.get(key);
						dos.writeBytes("Content-Disposition: form-data; name=\""+key+"\"; filename=\""
								+ encode(file.getName())
								+ "\""
								+ end);
						dos.writeBytes(end);
						FileInputStream fis = new FileInputStream(file);
						byte[] buffer = new byte[1024 * 8]; // 8k
						int count;
						// 读取文件
						while ((count = fis.read(buffer)) != -1) {
							dos.write(buffer, 0, count);
						}
						fis.close();
						dos.writeBytes(end);
						dos.writeBytes(hyphens+boundary+end);
					}
				}
				if(textParams!=null){
					Iterator<String> iterator = textParams.keySet().iterator();
//					dos.writeBytes(end);
					while(iterator.hasNext()){
						String key = iterator.next();
						String str = textParams.get(key);
						dos.writeBytes(hyphens + boundary + end);
						dos.writeBytes("Content-Disposition: form-data; name=\"" + key
								+ "\"\r\n");
						dos.writeBytes(end);
						dos.writeBytes(encode(str) + end);
					}
				}
				dos.flush();
				dos.writeBytes(hyphens + boundary + hyphens + end);
				dos.writeBytes(end);
				code = huc.getResponseCode();
				if(SLog.debug) SLog.d("Upload Code:" + code);
				if(code==200){
					inputStream = huc.getInputStream();
					return getRespContent(getContentEncoding());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 中文转码
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String encode(String value) throws Exception{
		return URLEncoder.encode(value, "UTF-8");
	}
}
