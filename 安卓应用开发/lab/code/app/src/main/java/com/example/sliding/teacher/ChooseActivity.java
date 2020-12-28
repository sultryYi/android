package com.example.sliding.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.sliding.MainActivity;
import com.example.sliding.R;

public class ChooseActivity extends AppCompatActivity implements View.OnTouchListener{
    private TextView Bubble;
    private TextView q,f;
    private int sx;
    private int sy;
    private float qx;
    private float qy;
    private float fx;
    private float fy;
    private float len;
    private String forest_content = null;
    private Boolean forest_state = false;
    private int forest_time = 0;
    private Intent intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_choose);

        q = findViewById(R.id.question_choose);
        f = findViewById(R.id.forest_choose);
        Bubble = findViewById(R.id.bubble);
        Bubble.setAlpha((float) 0.4);
        Bubble.bringToFront();
        Bubble.setOnTouchListener(this);

        intent = new Intent(this, com.example.sliding.teacher.ForestActivity.class);
        intent.putExtra("content",forest_content);
        initStatusBar();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            // 如果手指放在imageView上拖动
            case R.id.bubble:
                // event.getRawX(); //获取手指第一次接触屏幕在x方向的坐标
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 获取手指第一次接触屏幕
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
//                        Bubble.setImageResource(R.drawable.t);
                        break;
                    case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动对应的事件
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        // 获取手指移动的距离
                        int dx = x - sx;
                        int dy = y - sy;
                        // 得到imageView最开始的各顶点的坐标
                        int l = Bubble.getLeft();
                        int r = Bubble.getRight();
                        int t = Bubble.getTop();
                        int b = Bubble.getBottom();
                        // 更改imageView在窗体的位置
                        Bubble.layout(l + dx, t + dy, r + dx, b + dy);
                        // 获取移动后的位置
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:// 手指离开屏幕对应事件
                        // 记录最后图片在窗体的位置
                        int lasty = Bubble.getTop();
                        int lastx = Bubble.getLeft();

                        len = q.getWidth();
                        qx = q.getX();qy = q.getY();
                        fx = f.getX();fy = f.getY();
                        System.out.println(lastx+","+lasty);
                        System.out.println(qx+","+qy+"-"+(qx+len)+","+(qy+len));
                        if (onit(lastx,lasty,"question")){
                            startActivity(new Intent(this, com.example.sliding.teacher.QuestionActivity.class));
                        }
                        if (onit(lastx,lasty,"forest")){
                            startActivityForResult(intent, 1000);
                        }
//                        Bubble.setImageResource(R.drawable.next);
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putInt("lasty", lasty);
//                        editor.putInt("lastx", lastx);
//                        editor.commit();
                        update();
                        break;
                }
                break;
        }
        return true;// 不会中断触摸事件的返回
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1001){
            assert data != null;
            forest_content = data.getStringExtra("content");
            forest_state = data.getBooleanExtra("state", false);
            forest_time = data.getIntExtra("time", 0);
            intent.putExtra("content",forest_content);
            intent.putExtra("state", forest_state);
            intent.putExtra("time",forest_time);
        }
    }

    private void update(){
        setContentView(R.layout.activity_teacher_choose);

        q = findViewById(R.id.question_choose);
        f = findViewById(R.id.forest_choose);
        Bubble = findViewById(R.id.bubble);
        Bubble.setAlpha((float) 0.4);
        Bubble.bringToFront();
        Bubble.setOnTouchListener(this);
    }

    private boolean onit(int x,int y,String str){
        if (str==null)  return false;
        if (str.equals("question")){
            if (x > qx && x < qx+len && y > qy && y < qy+len)   return true;
        }
        if (str.equals("forest")){
            if (x > fx && x < fx+len && y > fy && y < fy+len)   return true;
        }
        return false;
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
