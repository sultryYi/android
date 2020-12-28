package com.example.sliding.student;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.MainActivity;
import com.example.sliding.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class LoginDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "LoginDialog";
    static final String currentUserTable = "currentUserTable";
    static final String currentQuestionTable = "currentQuestionTable";

    SQLiteDatabase db;
    SQLiteDatabase userDb;
    private TextView ok;
    private TextView cancel;
    private EditText username;
    private EditText password;
    private Handler mHandler;

    public LoginDialog(Context context, int width, int height, View layout, int style, SQLiteDatabase database, SQLiteDatabase userDatabase){
        super(context,style);
        setContentView(layout);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }

        db = database;
        userDb = userDatabase;
        ok = (TextView) findViewById(R.id.ok);
        cancel = (TextView) findViewById(R.id.cancel);
        username = (EditText) findViewById(R.id.usernameEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent intent = null;
                switch (msg.what){
                    case 0://用户名或密码错误
                        //create a toast
                        Toast toast = Toast.makeText(LoginDialog.this.getContext(),"用户名或密码错误",Toast.LENGTH_LONG);
                        break;
                    case 3://student
                        updateCurrentUser(db, currentUserTable, msg.getData().get("name").toString(), msg.getData().get("pwd").toString(), msg.getData().get("class").toString());
                        createUserDatabase(userDb,msg.getData().get("name").toString());
                        LoginDialog.this.dismiss();
                        intent = new Intent(LoginDialog.this.getOwnerActivity(), QuestionOrForestActivity.class);
                        intent.putExtra("username",msg.getData().get("name").toString());
                        intent.putExtra("password",msg.getData().get("pwd").toString());
                        LoginDialog.this.getOwnerActivity().startActivity(intent);
                        LoginDialog.this.dismiss();
                        break;
                    case 4://teacher
                        updateCurrentUser(db, currentUserTable, msg.getData().get("name").toString(), msg.getData().get("pwd").toString(), msg.getData().get("class").toString());
                        createUserDatabase(userDb,msg.getData().get("name").toString());
                        LoginDialog.this.dismiss();
                        intent = new Intent(LoginDialog.this.getOwnerActivity(), QuestionActivity.class);
                        intent.putExtra("username",msg.getData().get("name").toString());
                        intent.putExtra("password",msg.getData().get("pwd").toString());
                        LoginDialog.this.getOwnerActivity().startActivity(intent);
                        LoginDialog.this.dismiss();
                        break;
                }
            }
        };

        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.ok:
                if(this.username.getText().toString().trim().equals("student") && this.password.getText().toString().trim().equals("123")){
                    this.dismiss();
                    intent = new Intent(this.getOwnerActivity(), QuestionActivity.class);
                    intent.putExtra("username",this.username.getText().toString().trim());
                    intent.putExtra("password",this.password.getText().toString().trim());
                    this.getOwnerActivity().startActivity(intent);
                    this.dismiss();
                }else if(this.username.getText().toString().trim().equals("teacher") && this.password.getText().toString().trim().equals("123")){
                    this.dismiss();
                    intent = new Intent(this.getOwnerActivity(), MainActivity.class);
                    intent.putExtra("username",this.username.getText().toString().trim());
                    intent.putExtra("password",this.password.getText().toString().trim());
                    this.getOwnerActivity().startActivity(intent);
                    this.dismiss();
                }else{
                    loginUrl("http://49.234.213.234/login", this.username.getText().toString().trim(), this.password.getText().toString().trim(),mHandler);
                }
                break;
            case R.id.cancel:
                this.dismiss();
                break;
        }
    }

    public static void loginUrl(final String strUrl, final String name, final String pwd, final Handler myHandler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl+"?name="+name+"&pass="+pwd);// 根据自己的服务器地址填写
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
                    if(res.equals("登陆成功")){
                        //start the student.QuestionActivity
                        Message msg = Message.obtain();
                        msg.what = 3;   //标志消息的标志
                        Bundle bundle = new Bundle();
                        bundle.putString("name",name);
                        bundle.putString("pwd",pwd);
                        bundle.putString("class",getStudentClassById("http://49.234.213.234/getStudentClassById", name));
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    }else {
                        Message msg = Message.obtain();
                        msg.what = 0;
                        myHandler.sendMessage(msg);
                    }
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

    public static String getStudentClassById(final String strUrl, final String name) {
        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl + "?name=" + name);// 根据自己的服务器地址填写
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
                    return res;
                } catch (MalformedURLException me) {
                    Log.w(TAG, "你输入的URL格式有问题！");
                    me.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
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

    public static void updateCurrentUser(SQLiteDatabase db, String table, String name, String pwd, String className){
        ContentValues cv = new ContentValues(3);
        cv.put("name", name);
        cv.put("pwd", pwd);
        cv.put("class", className);

        Cursor cursor = db.rawQuery("SELECT * FROM " + table, null);
        if(cursor.getCount()==0){
            Log.w(TAG, "here is the new user");
            db.insert(table, null ,cv);
        }else {
            Log.w(TAG, "here isn't the new user");
            db.execSQL("DELETE FROM " + table);
//            db.execSQL("DELETE FROM sqlite_sequence WHERE name=" + table);
            db.insert(table, null ,cv);
        }
    }

    public void createUserDatabase(SQLiteDatabase db, String table){
        Log.w(TAG,table);
        String answer = "CREATE TABLE IF NOT EXISTS " +
                table +
                "(name VARCHAR(32), " +
                "questionId VARCHAR(32), " +
                "isAgree VARCHAR(16))";
        db.execSQL(answer);
    }
}
