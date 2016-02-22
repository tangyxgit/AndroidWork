package com.tangyx.work.network;

import android.os.AsyncTask;


public abstract class ConnectDataTask extends AsyncTask<Object, Void, String>{
	public final static String GET="GET";
	public final static String POST="POST";
	protected HttpConnectWork hc;
	protected OnResultDataListener onResultDataLintener;
	protected RequestParams params;
	public ConnectDataTask(RequestParams parame){
		hc = new HttpConnectWork();
		hc.disconnect();
		this.params = parame;
		this.onResultDataLintener = parame.onResultDataListener;
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		params.start = System.currentTimeMillis();
	}
	@Override
	protected abstract String doInBackground(Object... params);
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		params.end = System.currentTimeMillis();
		if(onResultDataLintener!=null){
			try {
				params.setResult(result);
				params.status = hc.getCode();
				onResultDataLintener.onResult(params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public interface OnResultDataListener {
		public void onResult(RequestParams params) throws Exception;
	}
}
