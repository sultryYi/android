package com.example.sliding.student;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.sliding.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ForestService extends Service {
    private static final String heartBeatUrl = "http://49.234.213.234/submitForest";
    private static final String TAG = "ForestService";
    public static String stuName;
    private ServiceBinder binder = new ServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stuName = intent.getStringExtra("name");
        Log.w(TAG,stuName);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        stuName = intent.getStringExtra("name");
        Log.w(TAG,stuName);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        binder.stopHeartBeat();
        return super.onUnbind(intent);
    }


    class ServiceBinder extends Binder {
        private Handler logicHandler;
        private Runnable runnable;
        private static final String TAG = "ServiceBinder";

        public void monitorForest(){
            final long timeSpan = 3000;
            Log.w(TAG,"monitor");
            logicHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    logicHandler.postDelayed(runnable,timeSpan);
                    heartBeat(heartBeatUrl,stuName);
                }
            };
            logicHandler.postDelayed(runnable,timeSpan);
        }

        public void stopHeartBeat(){
            logicHandler.removeCallbacks(runnable);
        }

        public Service getService(){
            return ForestService.this;
        }

        private void heartBeat(final String strUrl, final String name){

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.w(TAG,"here is in the heartbeat");
                    String temp;
                    StringBuffer sb = new StringBuffer();
                    try {
                        //做连接，以及各连接参数的设置
                        URL url = new URL(strUrl+"?name="+name);// 根据自己的服务器地址填写
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        Log.w(TAG, url.toString());
                        conn.setRequestMethod("GET");
                        //发起请求
                        conn.connect();
                        //接收响应信息
                        InputStream is = conn.getInputStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));// 获取输入流
                        while ((temp = in.readLine()) != null) {
                            sb.append(temp);
                        }
                        in.close();
                        String res = sb.toString();
                    } catch (MalformedURLException me) {
                        Log.w(TAG,"你输入的URL格式有问题！");
                        me.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }
}
