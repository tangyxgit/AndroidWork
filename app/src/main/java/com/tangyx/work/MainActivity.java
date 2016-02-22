package com.tangyx.work;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.tangyx.work.network.ConnectDataTask;
import com.tangyx.work.network.HcNetWorkTask;
import com.tangyx.work.network.RequestParams;
import com.tangyx.work.util.SLog;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        ConnectDataTask.OnResultDataListener{

    /**
     * 倒计时
     */
    private TextView mGetValidate;
    private int time=60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        onInitView();
    }

    public void onInitView(){
        mGetValidate = (TextView) findViewById(R.id.timer);
        mGetValidate.setOnClickListener(this);
        findViewById(R.id.get).setOnClickListener(this);
        findViewById(R.id.post).setOnClickListener(this);
    }

    /**
     * 启动倒计时
     */
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(time>0){
                time--;
                mGetValidate.setText(time+" S");
                handler.postDelayed(this,1000);
            }else{
                handler.removeCallbacks(this);
                mGetValidate.setEnabled(true);
                mGetValidate.setText("点击我进行倒计时");
                time = 60;
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        String url = "http://www.weather.com.cn/data/cityinfo/101010100.html";
        //RequestParams方法有个构造函数，对应不同的场景需求
        RequestParams params = null;
        //该容器由多个网络请求，通过此方法区分
        params = new RequestParams(url);
        params.setEventCode(v.getId());
        params.setOnResultDataListener(this);
        HcNetWorkTask mTask = new HcNetWorkTask(params);
        switch (v.getId()){
            case R.id.get:
                mTask.doGet();
                break;
            case R.id.post:
                params.setPostData("你要post的内容".getBytes());
                mTask.doPost();
                break;
            case R.id.timer:
                mGetValidate.setEnabled(false);
                mGetValidate.setText("准备倒计时");
                handler.postDelayed(runnable, 1000);
                break;
        }
    }

    @Override
    public void onResult(RequestParams params) throws Exception {
        if(SLog.debug)SLog.v(params.getUrl()+":"+params.result);
        switch (params.eventCode){
            case R.id.get:
                SLog.e("Get 请求完成");
                break;
            case R.id.post:
                SLog.e("Post 请求完成");
                break;
        }
    }
}
