package com.example.sliding;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    static final String databaseName = "database";
    static final String userDatabaseName = "userDatabaseName";
    static final String userTable = "userTable";
    static final String questionTable = "questionTable";
    static final String currentUserTable = "currentUserTable";
    static final String currentQuestionTable = "currentQuestionTable";
    SQLiteDatabase db;
    SQLiteDatabase userDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initStatusBar();

        //创建用户表，问题表，当前登陆用户表
        userDb = openOrCreateDatabase(userDatabaseName,Context.MODE_PRIVATE,null);
        db = openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        String createUserTable = "CREATE TABLE IF NOT EXISTS " +
                userTable +
                "(name VARCHAR(32), " +
                "pwd VARCHAR(32), " +
                "class VARCHAR(16))";
//        {"questionId": 1223005217, "questionClassId": "SE1", "questionContent": "urltest", "agreeNum": 0, "disagreeNum": 0, "publishTime": "12345"}
        String createQuestionTable = "CREATE TABLE IF NOT EXISTS " +
                questionTable +
                "(questionId VARCHAR(32), " +
                "questionClassId VARCHAR(16), " +
                "questionContent VARCHAR(1000), " +
                "agreeNum VARCHAR(16), " +
                "disagreeNum VARCHAR(16), " +
                "publishTime VARCHAR(32)," +
                "isAgree VARCHAR(16))";
        String createCurrentUserTable = "CREATE TABLE IF NOT EXISTS " +
                currentUserTable +
                "(name VARCHAR(32), " +
                "pwd VARCHAR(32), " +
                "class VARCHAR(16))";
        String createCurrentQuestionTable = "CREATE TABLE IF NOT EXISTS " +
                currentQuestionTable +
                "(questionClassId VARCHAR(16), " +
                "questionContent VARCHAR(1000), " +
                "publishTime VARCHAR(32))";
        db.execSQL(createUserTable);
        db.execSQL(createQuestionTable);
        db.execSQL(createCurrentUserTable);
        db.execSQL(createCurrentQuestionTable);
        db.close();

//        startActivity(new Intent(MainActivity.this, com.example.sliding.student.QuestionActivity.class));
        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
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
