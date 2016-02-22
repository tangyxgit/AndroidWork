package com.tangyx.work.network;



import com.tangyx.work.util.SLog;

import java.io.Serializable;

public class HcNetWorkTask extends ConnectDataTask implements Serializable{

    private boolean isUpload=false;

    public HcNetWorkTask(RequestParams params) {
        super(params);
    }

    @Override
	protected String doInBackground(Object... param) {
        String method = params.method;
		if(method.equals(GET)){
			return hc.getRequestManager(params.url, params.headMap);
		}else if(method.equals(POST)){
            if(isUpload){
                return hc.UploadFileManager(params.url,params.fileParams,params.textParams,params.headMap);
            }
			return hc.postRequestManager(params.url, params.headMap, params.postData);
		}
		return null;
	}
	/**
	 * Get请求
	 */
	public void doGet(){
        if(params==null){
            return;
        }
		if(SLog.debug) SLog.d("Get：" + params.url);
        params.method = GET;
        this.execute();
	}
	
	/**
	 * Post请求
	 */
	public void doPost(){
        if(params==null){
            return;
        }
		if(SLog.debug) SLog.d("Post：" + params.url);
        params.method = POST;
        this.execute();
    }
    /**
     * 文件上传
     */
    public void doPostFileText(){
        isUpload=true;
        doPost();
    }
    /**
     * 关闭网络链接
     */
    public void discount(){
        if(hc!=null){
            hc.disconnect();
        }
    }
}
