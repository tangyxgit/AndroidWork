package com.tangyx.work.network;

import android.text.TextUtils;


import com.tangyx.work.util.SLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by tangyx on 15/9/1.
 * 封装网络上传信息
 */
public class RequestParams implements Serializable{

    /**
     * 请求地址
     */
    public String url="";
    /**
     * 请求头信息
     */
    public Map<String,String> headMap;
    /**
     * post内容
     */
    public byte[] postData;
    /**
     * 请求方式
     */
    public String method;
    /**
     * 请求结果
     */
    public String result;
    /**
     * 开始发起请求时间
     */
    public long start=0;
    /**
     * 结束时间
     */
    public long end=0;
    /**
     * 当前网络请求的唯一标示(主要一个容器中同时发起多个请求)
     */
    public int eventCode=-1;
    /**
     * 图文混合上传
     */
    public Map<String,File> fileParams;
    public Map<String,String> textParams;
    /**
     * 网络返回状态
     */
    public int status=-1;
    public ConnectDataTask.OnResultDataListener onResultDataListener;
    private HcNetWorkTask hcNetWorkTask;
    public RequestParams(){
        this.fileParams = new HashMap<>();
        this.textParams = new HashMap<>();
    }

    /**
     * 请求地址
     * @param url
     */
    public RequestParams(String url){
        this();
        this.url = url;
    };
    /**
     *
     * @param url 请求网络地址
     * @param headMap header内容
     */
    public RequestParams(String url,Map<String,String> headMap){
        this(url);
        this.headMap = headMap;
        if(eventCode==-1){
            eventCode = new Random().nextInt();
        }
    }

    /**
     *
     * @param url
     * @param headMap
     * @param postData post上传内容
     */
    public RequestParams(String url,Map<String,String> headMap,byte[] postData){
        this(url,headMap);
        this.postData = postData;
    }
    /**
     * 添加文件
     */
    public void addFileParam(String name,String path){
        File file = new File(path);
        addFileParam(name, file);
    }
    public void addFileParam(String name,File file){
        if(!file.exists()){
            if(SLog.debug) SLog.e("上传文件不存在:" + file);
            return;
        }
        fileParams.put(name, file);
    }
    /**
     * 添加文字
     */
    public void addTextParam(String name,String text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        textParams.put(name,text);
    }
    /**
     * JSON转换
     */
    private JSONObject resultJson;
    public JSONObject getResultJson(){
        try {
            if(resultJson==null){
                resultJson = new JSONObject(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }
    /**
     * 获取JSON值
     */
    public <T extends Object> T get(String key){
        if(resultJson!=null) {
            try{
                return (T) resultJson.get(key);
            }catch (Exception e){
            }
        }
        return null;
    }
    private LinkedList paramsList;
    public void setParamsList(Object... params){
        if(paramsList == null){
            paramsList = new LinkedList<>();
        }
        for (int i=0;i<params.length;i++){
            paramsList.add(params[i]);
        }
    }

    public <T extends Object> T getPositionParams(int index) {
        if(paramsList==null){
            return null;
        }
        return (T) paramsList.get(index);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeadMap() {
        return headMap;
    }

    public void setHeadMap(Map<String, String> headMap) {
        this.headMap = headMap;
    }

    public byte[] getPostData() {
        return postData;
    }

    public void setPostData(byte[] postData) {
        this.postData = postData;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        resultJson = getResultJson();
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setOnResultDataListener(ConnectDataTask.OnResultDataListener onResultDataListener) {
        this.onResultDataListener = onResultDataListener;
    }

    public void setHcNetWorkTask(HcNetWorkTask hcNetWorkTask) {
        this.hcNetWorkTask = hcNetWorkTask;
    }

    public HcNetWorkTask getHcNetWorkTask() {
        return hcNetWorkTask;
    }
}
