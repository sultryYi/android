package com.example.sliding.student;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.sliding.R;

public class ForestActivity extends AppCompatActivity {
    private static final String TAG = "ForestActivity";
    private static final String heartBeatUrl = "http://49.234.213.234/submitForest";
    private TextView timer;
    private TextView question;
    private AlertDialog.Builder builder;
    private Handler handlerUI;
    private Handler handlerThread;
    private Runnable runnable;
    private long millSecond;
    private long remainTime;
    private long lastTime;
    private ServiceConnection connection;
    private ForestService.ServiceBinder serviceBinder;
    private ForestService forestService;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_forest);

        millSecond = this.getIntent().getLongExtra("second",900000+System.currentTimeMillis());

        question = (TextView) findViewById(R.id.stu_forest_question);
        question.setText(this.getIntent().getStringExtra("question"));
        question.setMovementMethod(ScrollingMovementMethod.getInstance());

        lastTime = millSecond -System.currentTimeMillis();
        timer = (TextView) findViewById(R.id.stu_forest_timer);
        timer.setText(secondToTime(lastTime));

        //设置service和activity的关联
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.w(TAG,"onServiceConnected");
                serviceBinder = (ForestService.ServiceBinder) service;
                forestService = (ForestService) serviceBinder.getService();
                serviceBinder.monitorForest();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.w(TAG,"onServiceDisconnected");
            }
        };

//        关联service和activity
//        Intent bindIntent = new Intent(this, ForestService.class);
//        bindIntent.putExtra("name",this.getIntent().getStringExtra("name"));
//        bindService(bindIntent,connection,BIND_AUTO_CREATE);

        initStatusBar();

        handlerUI = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        handlerThread.removeCallbacks(runnable);
                        unbindService(connection);
                        forestService = null;
                        builder = new AlertDialog.Builder(ForestActivity.this)
                                .setCancelable(false)
                                .setTitle("Forest结束")
                                .setMessage("时间到，点击确定按钮退出Forest")
                                .setPositiveButton("确定",
                                        new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Message msg = Message.obtain();
                                                msg.what = 1;
                                                handlerUI.sendMessage(msg);
                                                dialog.dismiss();
                                            }
                                        });
                        builder.create().show();
                        break;
                    case 1:
                        ForestActivity.this.finish();
                        break;
                }
            }
        };

        handlerThread = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                remainTime = millSecond -System.currentTimeMillis();
                if(remainTime <= 0){
                    timer.setText("00 : 00");
                    Message msg = Message.obtain();
                    msg.what = 0;
                    handlerUI.sendMessage(msg);
                }
                if(remainTime <= lastTime){
                    lastTime = remainTime;
                    timer.setText(secondToTime(lastTime));
                }
                handlerThread.postDelayed(this, 200);
            }
        };
        handlerThread.postDelayed(runnable,200);
    }

    @Override
    protected void onStop() {
        Log.w(TAG,"onStop");
        if(forestService!=null){
            unbindService(connection);
            forestService = null;
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG,"onStart");
        Intent bindIntent = new Intent(this, ForestService.class);
        bindIntent.putExtra("name",this.getIntent().getStringExtra("name"));
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private String secondToTime(long second){
        StringBuffer time = new StringBuffer();
        second = second/1000;
        long min = second/60;
        long sec = second%60;
        String strMin;
        String strSec;
        if(min<10){
            strMin="0"+min;
        }else {
            strMin=String.valueOf(min);
        }
        if(sec<10){
            strSec="0"+sec;
        }else {
            strSec=String.valueOf(sec);
        }
        time.append(strMin)
                .append(" : ")
                .append(strSec);
        return time.toString();
    }
}
