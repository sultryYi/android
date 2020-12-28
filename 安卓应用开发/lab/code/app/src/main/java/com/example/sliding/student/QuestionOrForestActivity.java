package com.example.sliding.student;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.R;
import com.google.gson.Gson;

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

public class QuestionOrForestActivity extends AppCompatActivity {
    private static final String TAG = "QuestionOrForestActivity";
    private final String strUrl = "http://49.234.213.234/getForest";
    private static final String currentUserTable = "currentUserTable";
    private static final String databaseName = "database";
    private Cursor cursor;
    private SQLiteDatabase db;
    private String className;
    private float startY;
    private float currentY;
    private float moveY;
    private float windowY;
    private ViewGroup questionSlideView;
    private ViewGroup forestSlideView;
    private LinearLayout question;
    private LinearLayout forest;
    private long backTime;
    private long twoBackTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_questionorforest);

        backTime = 0;
        twoBackTime = 0;
        question = (LinearLayout) this.findViewById(R.id.questionTextView);
        forest = (LinearLayout) this.findViewById(R.id.forestTextView);
        startY = 0;
        currentY = 0;
        moveY = 0;
        Point point = new Point();
        this.getWindowManager().getDefaultDisplay().getRealSize(point);
        windowY = point.y;
        questionSlideView = (ViewGroup) question;
        forestSlideView = (ViewGroup) forest;
        db = openOrCreateDatabase(databaseName, Context.MODE_PRIVATE,null);
        cursor = db.rawQuery("SELECT * FROM "+currentUserTable,null);
        cursor.moveToFirst();
        className = cursor.getString(2);

        //measure the height of forest
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        forestSlideView.measure(w, h);
        int height = forestSlideView.getMeasuredHeight();

        questionSlideView.setPivotX(getWindowManager().getDefaultDisplay().getWidth()/2);
        questionSlideView.setPivotY(0);
        forestSlideView.setPivotX(getWindowManager().getDefaultDisplay().getWidth()/2);
        forestSlideView.setPivotY(height);

        initStatusBar();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        Intent intent = null;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                currentY = event.getY();
                moveY = currentY - startY;
                if(moveY > 0 && (1 + 6 * moveY / windowY) * forestSlideView.getPivotY() <= windowY){//move down side, control the question
                    questionSlideView.bringToFront();
                    questionSlideView.setScaleX(1 + 6 * moveY / windowY);
                    questionSlideView.setScaleY(1 + 6 * moveY / windowY);
                }else if(moveY < 0 && (1 - 6 * moveY / windowY) * forestSlideView.getPivotY() <= windowY){//move up side
                    forestSlideView.bringToFront();
                    forestSlideView.setScaleX(1 - 6 * moveY / windowY);
                    forestSlideView.setScaleY(1 - 6 * moveY / windowY);
                }else {
                    break;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(moveY > 0) {
                    question.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(200);
                    if(moveY/windowY >= 0.3){
                        intent = new Intent(this, QuestionActivity.class);
                        intent.putExtra("username",this.getIntent().getStringExtra("username"));
                        intent.putExtra("password",this.getIntent().getStringExtra("password"));
                        this.startActivity(intent);
                    }
                }else if(moveY < 0){
                    forest.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(200);
                    if(moveY/windowY <= -0.3){
                        String str = getForest(strUrl, className);
                        if(str.equals("Forest未开启")){
                            forest.animate()
                                    .scaleX(1)
                                    .scaleY(1)
                                    .setDuration(200);
                            Toast.makeText(QuestionOrForestActivity.this,"Forest未开启",Toast.LENGTH_SHORT).show();
                        }else {
                            Log.w(TAG, str);
                            Gson gson = new Gson();
                            Forest forest = gson.fromJson(str, Forest.class);

                            cursor = db.rawQuery("SELECT * FROM "+currentUserTable,null);
                            cursor.moveToFirst();
                            String stuName = cursor.getString(0);

                            intent = new Intent(this, ForestActivity.class);
                            intent.putExtra("second",Long.parseLong(forest.time)*1000+System.currentTimeMillis());
                            intent.putExtra("question",forest.content);
                            intent.putExtra("name",stuName);
                            this.startActivity(intent);
                        }
                    }
                }else {
                    break;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
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

    private String getForest(final String strUrl, final String className){
        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl + "?class=" + className);// 根据自己的服务器地址填写
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

    class Forest{
        String time;
        String content;
    }
}
