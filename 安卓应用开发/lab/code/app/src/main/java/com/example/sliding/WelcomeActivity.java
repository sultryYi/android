package com.example.sliding;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Scroller;
import android.widget.Toast;

import com.example.sliding.student.LoginDialog;
import com.example.sliding.student.QuestionActivity;
import com.example.sliding.student.QuestionOrForestActivity;
import com.example.sliding.teacher.TeacherLoginDialog;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    static final String databaseName = "database";
    static final String userDatabaseName = "userDatabaseName";
    static final String currentUserTable = "currentUserTable";
    static final String currentQuestionTable = "currentQuestionTable";
    private Cursor cursor;
    SQLiteDatabase db;
    SQLiteDatabase userDb;
    private float downX;
    private float downY;
    private float moveX;
    private float moveY;
    private float movePercent;
    private ViewGroup mSlideView;
    private Scroller mScroller;
    private Handler mHandler;
    private long backTime;
    private long twoBackTime;
    private Button logout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        backTime = 0;
        twoBackTime = 0;
        db = openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        cursor = db.rawQuery("SELECT * FROM "+currentUserTable,null);
        mSlideView = (ViewGroup) findViewById(R.id.welcomeImageView);
        mSlideView.setPivotX(this.getWindowManager().getDefaultDisplay().getWidth()/2);
        mSlideView.setPivotY(this.getWindowManager().getDefaultDisplay().getHeight());
        mScroller = new Scroller(this,new DecelerateInterpolator());
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.getCount()!=0){
                    Log.w(TAG, "here isn't the new user");
                    db.execSQL("DELETE FROM " + currentUserTable);
                    db.execSQL("DELETE FROM " + currentQuestionTable);
//                    db.execSQL("DELETE FROM sqlite_sequence WHERE name=" + currentUserTable);
                    cursor = db.rawQuery("SELECT * FROM "+currentUserTable,null);
                    Toast.makeText(WelcomeActivity.this,"登出成功",Toast.LENGTH_SHORT).show();
                }
            }
        });
        mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                Intent intent;
                super.handleMessage(msg);
                switch (msg.what){
                    case 0://用户名或密码错误
                        //create a toast
                        Toast toast = Toast.makeText(WelcomeActivity.this,"用户名或密码错误",Toast.LENGTH_LONG);
                        break;
                    case 1://student for the first time
                        View stuView = getLayoutInflater().inflate(R.layout.dialog_login, null);
                        LoginDialog stuLoginDialog = new LoginDialog(WelcomeActivity.this, 0, 0, stuView, R.style.DialogTheme, db,openOrCreateDatabase(userDatabaseName,Context.MODE_PRIVATE, null));
                        stuLoginDialog.setCancelable(true);
                        stuLoginDialog.show();
                        break;
                    case 2://teacher for the first time
                        View teaView = getLayoutInflater().inflate(R.layout.dialog_login, null);
                        TeacherLoginDialog teaLoginDialog = new TeacherLoginDialog(WelcomeActivity.this, 0, 0, teaView, R.style.DialogTheme, db,openOrCreateDatabase(userDatabaseName,Context.MODE_PRIVATE, null));
                        teaLoginDialog.setCancelable(true);
                        teaLoginDialog.show();
                        break;
                    case 3://student in the cache
                        LoginDialog.updateCurrentUser(db, currentUserTable, msg.getData().get("name").toString(), msg.getData().get("pwd").toString(), msg.getData().get("class").toString());
                        createUserDatabase(userDb,userDatabaseName,msg.getData().get("name").toString());
                        intent = new Intent(WelcomeActivity.this, QuestionOrForestActivity.class);
                        intent.putExtra("username",msg.getData().get("name").toString());
                        intent.putExtra("password",msg.getData().get("pwd").toString());
                        WelcomeActivity.this.startActivity(intent);
                        break;
                    case 4://teacher in the cache
                        LoginDialog.updateCurrentUser(db, currentUserTable, msg.getData().get("name").toString(), msg.getData().get("pwd").toString(), msg.getData().get("class").toString());
                        createUserDatabase(userDb,userDatabaseName,msg.getData().get("name").toString());
                        intent = new Intent(WelcomeActivity.this, QuestionActivity.class);
                        intent.putExtra("username",msg.getData().get("name").toString());
                        intent.putExtra("password",msg.getData().get("pwd").toString());
                        WelcomeActivity.this.startActivity(intent);
                        break;
                }
            }
        };

        if(cursor.getCount() == 0){
            logout.setVisibility(View.INVISIBLE);
        }else {
            logout.setVisibility(View.VISIBLE);
        }

        initStatusBar();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                Log.w(TAG,"down");
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = ev.getX();
                moveY = ev.getY();
                movePercent = (moveX-downX)/this.getWindowManager().getDefaultDisplay().getWidth();
                mSlideView.setRotation(30*movePercent);
                mSlideView.setTranslationX(120*movePercent);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Thread syncTask = new Thread() {
                    @Override
                    public void run() {
                        if(movePercent>=0.7){
                            //if the percent >= 0.7, slide right and then come back and then start the student login dialog
                            while (movePercent >= 0) {
                                mSlideView.setRotation(30 * movePercent);
                                mSlideView.setTranslationX(120 * movePercent);
                                movePercent = movePercent - 0.001f;
                                mSlideView.postInvalidate();
                                Log.d(TAG, "movePercent");
                            }

                            //start the stuLoginDialog
                            if(cursor.getCount()==0){
                                Message msg =Message.obtain();
                                msg.what = 1;   //标志消息的标志
                                mHandler.sendMessage(msg);
                            }else {
                                cursor.moveToFirst();
                                LoginDialog.loginUrl("http://49.234.213.234/login",cursor.getString(0),cursor.getString(1),mHandler);
                            }

                        } else if(movePercent>=0) {
                            //if the 0<=percent<0.7, slide right and come back to origin
                            while (movePercent >= 0) {
                                mSlideView.setRotation(30 * movePercent);
                                mSlideView.setTranslationX(120 * movePercent);
                                movePercent = movePercent - 0.001f;
                                mSlideView.postInvalidate();
                                Log.d(TAG, "movePercent");
                            }
                        }else if(movePercent<=-0.7){
                            //if the percent<=-0.7, slide left and then come back and then start the teacher login dialog
                            while (movePercent <= 0) {
                                mSlideView.setRotation(30 * movePercent);
                                mSlideView.setTranslationX(120 * movePercent);
                                movePercent = movePercent + 0.001f;
                                mSlideView.postInvalidate();
                                Log.d(TAG, "movePercent");
                            }

                            //start the teaLoginDialog
                            Message msg =Message.obtain();
                            msg.what = 2;   //标志消息的标志
                            mHandler.sendMessage(msg);
                        }else {
                            //if -0.7<percent<0, slide left and come back to origin
                            while (movePercent <= 0) {
                                mSlideView.setRotation(30 * movePercent);
                                mSlideView.setTranslationX(120 * movePercent);
                                movePercent = movePercent + 0.001f;
                                mSlideView.postInvalidate();
                                Log.d(TAG, "movePercent");
                            }
                        }
                    }
                };
                syncTask.start();

                break;

        }

        return super.dispatchTouchEvent(ev);
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

    @Override
    public void onBackPressed(){
        if(twoBackTime == 0) {
            Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
            backTime = System.currentTimeMillis();
            twoBackTime = System.currentTimeMillis() + 5000;
        }else {
            twoBackTime = System.currentTimeMillis();
            if(twoBackTime - backTime <= 2000){
                this.finish();
                System.exit(0);
            }else {
                Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                backTime = System.currentTimeMillis();
                twoBackTime = System.currentTimeMillis() + 5000;
            }
        }
    }

    public void createUserDatabase(SQLiteDatabase db, String databaseName,String table){
        db = openOrCreateDatabase(databaseName,Context.MODE_PRIVATE, null);
        String answer = "CREATE TABLE IF NOT EXISTS " +
                table +
                "(name VARCHAR(32), " +
                "questionId VARCHAR(32), " +
                "isAgree VARCHAR(16))";
        db.execSQL(answer);
    }
}
