package com.example.sliding;

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
import android.widget.Scroller;

import com.example.sliding.student.LoginDialog;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    private float downX;
    private float downY;
    private float moveX;
    private float moveY;
    private float movePercent;
    private ViewGroup mSlideView;
    private Scroller mScroller;
    private Handler mHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mSlideView = (ViewGroup) this.getWindow().getDecorView();
        mSlideView.setPivotX(this.getWindowManager().getDefaultDisplay().getWidth()/2);
        mSlideView.setPivotY(this.getWindowManager().getDefaultDisplay().getHeight());
        mScroller = new Scroller(this,new DecelerateInterpolator());
        mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1://student
                        View stuView = getLayoutInflater().inflate(R.layout.dialog_login, null);
                        LoginDialog stuLoginDialog = new LoginDialog(WelcomeActivity.this, 0, 0, stuView, R.style.DialogTheme);
                        stuLoginDialog.setCancelable(true);
                        stuLoginDialog.show();
                        break;
                    case 2://teacher
                        View teaView = getLayoutInflater().inflate(R.layout.dialog_login, null);
                        LoginDialog teaLoginDialog = new LoginDialog(WelcomeActivity.this, 0, 0, teaView, R.style.DialogTheme);
                        teaLoginDialog.setCancelable(true);
                        teaLoginDialog.show();
                        break;
                }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initStatusBar();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
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
                            Message msg =Message.obtain();
                            msg.what = 1;   //标志消息的标志
                            mHandler.sendMessage(msg);

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


}
