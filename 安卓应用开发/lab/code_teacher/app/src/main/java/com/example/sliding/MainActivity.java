package com.example.sliding;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_teacher_question);
//
//        initStatusBar();

        startActivity(new Intent(MainActivity.this, com.example.sliding.teacher.ChooseActivity.class));
//        startActivity(new Intent(MainActivity.this, com.example.sliding.teacher.QuestionActivity.class));
//        startActivity(new Intent(MainActivity.this, com.example.sliding.teacher.ForestActivity.class));
//        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
//        startActivity(new Intent(MainActivity.this, com.example.sliding.student.QuestionActivity.class));
//        startActivity(new Intent(MainActivity.this, com.example.sliding.teacher.QuestionActivity.class));
//        startActivity(new Intent(MainActivity.this, com.example.sliding.student.ForestActivity.class));
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
