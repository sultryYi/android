package com.example.sliding.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.sliding.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static android.content.ContentValues.TAG;

public class Server_Connection {
    private String content = "Null";

    static void send_question(final String mclass, final String mcontent, final String mtime){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    String st = "http://49.234.213.234/addQuestion?"+"class="+mclass+"&content="+mcontent+"&time="+mtime;
                    URL url = new URL(st);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.w(TAG, url.toString());
                    conn.setRequestMethod("GET");
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while ((temp = in.readLine()) != null){
                        sb.append(temp);
                    }
                    in.close();
                    String res = sb.toString();
                    if (res.equals("添加成功")) {
                        System.out.println("已发送");
                    }
                    else{
                        System.out.println("Something Error(可能已存在此问题)");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    
    
    static String get_question(final QuestionActivity questionActivity,final String N, final String classId) {

        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String temp,res = null;
                StringBuffer sb = new StringBuffer();
                try {
                    String st = "http://49.234.213.234/getTopAgreeN?"+"N="+N+"&classId="+classId;
                    URL url = new URL(st);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.w(TAG, url.toString());
                    conn.setRequestMethod("GET");
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while ((temp = in.readLine()) != null){
                        sb.append(temp);
                    }
                    in.close();
                    res = sb.toString();

                    //将数据通过message传出
//                    Message msg = Message.obtain();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("questions",res);
//
//                    msg.setData(bundle);
//                    msg.what = 0x11;
//                    mhandler.sendMessage(msg);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return res;
            }
        });
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            return task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void forest_begin(final String classID, final int limitTime, final int lastTime){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    String st = "http://49.234.213.234/addForest?"+"class="+classID+"&limitTime="+limitTime+"&lastTime="+lastTime;
                    URL url = new URL(st);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.w(TAG, url.toString());
                    conn.setRequestMethod("GET");
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while ((temp = in.readLine()) != null){
                        sb.append(temp);
                    }
                    in.close();
                    String res = sb.toString();
                    if (res.equals("设置成功")) {
                        System.out.println("已发送");
                    }
                    else{
                        System.out.println("Something Error");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    static String forest_result(final String classId) {

        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String temp,res = null;
                StringBuffer sb = new StringBuffer();
                try {
                    String st = "http://49.234.213.234/resultForest?"+"&class="+classId;
                    URL url = new URL(st);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.w(TAG, url.toString());
                    conn.setRequestMethod("GET");
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while ((temp = in.readLine()) != null){
                        sb.append(temp);
                    }
                    in.close();
                    res = sb.toString();

                    //将数据通过message传出
//                    Message msg = Message.obtain();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("questions",res);
//
//                    msg.setData(bundle);
//                    msg.what = 0x11;
//                    mhandler.sendMessage(msg);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return res;
            }
        });
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            return task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class mHandler extends Handler {
        private static String data = null;
        WeakReference<QuestionActivity> weakReference;
        public mHandler(QuestionActivity questionActivity){
            weakReference = new WeakReference<QuestionActivity>(questionActivity);
        }
        @Override
        public void handleMessage(Message msg){
            QuestionActivity questionActivity = weakReference.get();
            if (questionActivity != null){
                if (msg.what == 0x11) {
                    Bundle bundle = msg.getData();
                    this.data = bundle.getString("questions");

                }
            }
        }

        public String getMessage(){

            return data;
        }
    }
}
