package com.example.sliding.student;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.Question;
import com.example.sliding.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class AddQuestionDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "AddQuestionDialog";
    static final String  currentUserTable = "currentUserTable";
    static final String currentQuestionTable = "currentQuestionTable";
    final String addQuestionUrl = "http://49.234.213.234/addQuestion";
    final String deleteQuestionUrl = "http://49.234.213.234/deleteQuestion";
    SQLiteDatabase db;
    private Cursor cursor;
    private EditText mEditText;
    private TextView ok;
    private TextView cancel;
    private String questionClassId;
    private PriorityListener listener;

    public AddQuestionDialog(Context context, int width, int height, View layout, int style, SQLiteDatabase database, PriorityListener listener){
        super(context,style);
        setContentView(layout);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
        db = database;
        this.listener = listener;
        mEditText = (EditText) findViewById(R.id.addQuestionEditText);
        ok = (TextView) findViewById(R.id.addQuestionOk);
        cancel = (TextView) findViewById(R.id.addQuestionCancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
        cursor = db.rawQuery("SELECT * FROM " + currentQuestionTable, null);
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            mEditText.setText(cursor.getString(1));
        }
    }

    public interface PriorityListener{
        public void refreshPriorityUI(int res);
    }

    public AddQuestionDialog(Context context, int themeResId, String questionClassId) {
        super(context, themeResId);
        this.questionClassId = questionClassId;
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.addQuestionOk:
                Cursor userCursor = db.rawQuery("SELECT * FROM " + currentUserTable, null);
                Cursor questionCursor = db.rawQuery("SELECT * FROM " + currentQuestionTable, null);
                userCursor.moveToFirst();
                final String time = System.currentTimeMillis()+"";
                final String questionClassId = userCursor.getString(2);
                final String questionContent = this.mEditText.getText().toString().trim();
                if(questionContent.isEmpty()){
                    if(questionCursor.getCount() != 0){
                        questionCursor.moveToFirst();
                        if(deleteQuestion(deleteQuestionUrl,questionCursor.getString(2))){
                            db.execSQL("DELETE FROM " + currentQuestionTable);
                            listener.refreshPriorityUI(0);
                            this.dismiss();
                        }else {
                            Toast.makeText(AddQuestionDialog.this.getOwnerActivity(),"删除失败~~", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(AddQuestionDialog.this.getOwnerActivity(),"请输入问题描述！", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if(questionCursor.getCount() != 0){
                        if(deleteQuestion(deleteQuestionUrl,questionCursor.getString(2))){
                            if(addQuestion(addQuestionUrl, questionClassId, questionContent, time)){
                                db.execSQL("DELETE FROM " + currentQuestionTable);
                                //在数据库中插入题目
                                ContentValues cv = new ContentValues(3);
                                cv.put("questionClassId",questionClassId);
                                cv.put("questionContent",questionContent);
                                cv.put("publishTime",time);
                                db.insert(currentQuestionTable,null,cv);
                                //使用listener.refreshPriorityUI(1)通知activity
                                listener.refreshPriorityUI(1);
                                this.dismiss();
                            }
                        }
                    }else {
                        if (addQuestion(addQuestionUrl, questionClassId, questionContent, time)) {
                            //TODO
                            //在数据库中插入题目
                            ContentValues cv = new ContentValues(3);
                            cv.put("questionClassId",questionClassId);
                            cv.put("questionContent",questionContent);
                            cv.put("publishTime",time);
                            db.insert(currentQuestionTable,null,cv);
                            listener.refreshPriorityUI(1);
                            this.dismiss();
                        } else {
                            listener.refreshPriorityUI(2);
                        }
                    }
                }
                break;
            case R.id.addQuestionCancel:
                this.dismiss();
                break;
        }
    }

    private Boolean addQuestion(final String strUrl, final String questionClassId, final String questionContent, final String time) {
        FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl+"?class="+questionClassId+"&content="+questionContent+"&time="+time);// 根据自己的服务器地址填写
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
                    if(res.equals("添加成功")){
                        return true;
                    }else {
                        return false;
                    }
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

    private Boolean deleteQuestion(final String deleteQuestionUrl, final String time){
        FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                List<Question> questions = new ArrayList<>();
                String questionId = "";
                try {

                    Cursor userCursor = db.rawQuery("SELECT * FROM "+ currentUserTable,null);
                    userCursor.moveToFirst();
                    String stuName = userCursor.getString(0);
                    String classId = QuestionActivity.getStudentClassById("http://49.234.213.234/getStudentClassById", stuName);
                    questions = QuestionActivity.getQuestionByClass("http://49.234.213.234/getQuestionByClass",classId);
                    userCursor.close();

                    for(Question question : questions){
                        if(question.publishTime.equals(time)){
                            questionId = question.questionId+"";
                            break;
                        }
                    }

                    //做连接，以及各连接参数的设置
                    URL url = new URL(deleteQuestionUrl + "?questionId=" + questionId);// 根据自己的服务器地址填写
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
                    if(res.equals("删除成功")){
                        return true;
                    }else {
                        return false;
                    }
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
}
