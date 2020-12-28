package com.example.sliding.teacher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.sliding.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TeacherLoginDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "LoginDialog";
    private TextView ok;
    private TextView cancel;
    private EditText username;
    private EditText password;
    private Handler mHandler;

    public TeacherLoginDialog(Context context, int width, int height, View layout, int style, SQLiteDatabase database, SQLiteDatabase userDatabase){
        super(context,style);
        setContentView(layout);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }


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
                        Toast toast = Toast.makeText(TeacherLoginDialog.this.getContext(),"用户名或密码错误",Toast.LENGTH_LONG);
                        break;
                    case 4://teacher
                        TeacherLoginDialog.this.dismiss();
                        intent = new Intent(TeacherLoginDialog.this.getOwnerActivity(), ChooseActivity.class);
                        intent.putExtra("username",msg.getData().get("name").toString());
                        intent.putExtra("password",msg.getData().get("pwd").toString());
                        TeacherLoginDialog.this.getOwnerActivity().startActivity(intent);
                        TeacherLoginDialog.this.dismiss();
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
                loginUrl("http://49.234.213.234/teacher_login", this.username.getText().toString().trim(), this.password.getText().toString().trim(),mHandler);
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
                        msg.what = 4;   //标志消息的标志
                        Bundle bundle = new Bundle();
                        bundle.putString("name",name);
                        bundle.putString("pwd",pwd);
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
}
