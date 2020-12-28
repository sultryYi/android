package com.example.sliding.student;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.Question;
import com.example.sliding.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static java.lang.Math.abs;

//TODO
//保存自己添加的问题，只有一个
//在添加问题的dialog里，如果已经添加了问题，edittext中显示该问题，否则为空，通过这个上传可以修改自己的问题
//textview下拉同意，上滑反对
//textview中视觉上表示同意人数以及自己是否同意
//下拉刷新（选做）


public class QuestionActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "QuestionActivity";
    static final String databaseName = "database";
    static final String userDatabaseName = "userDatabaseName";
    static final String submitAnswerURL = "http://49.234.213.234/submitAnswer";
    static final String currentQuestionTable = "currentQuestionTable";
    static final String questionTable = "questionTable";
    SQLiteDatabase db;
    SQLiteDatabase userDb;
    private Cursor cursor;
    private Button mButton;
    private SeekBar mSeekBar;
    private HorizontalScrollView mScrollView;
    private LinearLayout mLayout;
    private List<Question> questions;
    private List<TextView> textViews;
    private LinearLayout.LayoutParams textViewLayoutParams;
    private LinearLayout.LayoutParams viewLayoutParams;
    private String stuName;
    private String stuClass;
    private int length;
    private float startY;
    private float currentY;
    private float moveY;
    private float actStartY;
    private float actCurrentY;
    private float actMoveY;
    private ViewGroup mSlideView;
    private int movement;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_question);
        mButton = (Button) findViewById(R.id.addQuestion);
        mSeekBar = (SeekBar) findViewById(R.id.SeekBar);
        mScrollView = (HorizontalScrollView) findViewById(R.id.mScrollView);
        mLayout = (LinearLayout) findViewById(R.id.textViews);
        questions = new ArrayList<>();
        textViews = new ArrayList<>();
        stuName= this.getIntent().getStringExtra("username");
        stuClass = getStudentClassById("http://49.234.213.234/getStudentClassById", stuName);
        questions = getQuestionByClass("http://49.234.213.234/getQuestionByClass", stuClass);
        if(questions == null || questions.size()==0){
            questions.add(new Question(99999,"SE1","实在是没题目🌶，先看点书歇歇⑧",0,0,"99999"));
        }

        db = openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        userDb = openOrCreateDatabase(userDatabaseName,Context.MODE_PRIVATE,null);

        length = 0;
        mSlideView = (ViewGroup) this.getWindow().getDecorView();
        Collections.reverse(questions);

//        //init the question list by myself
//        for(int i = 0; i<5; i++){
//            String temp = ""+i;
//            String question = "";
//            for(int j = 0; j<50;j++)
//                question+=temp;
//            questions.add(question);
//        }

        //init the TextView list with the list of question
        View mView = getLayoutInflater().inflate(R.layout.view_empty, null);
        mLayout.addView(mView);
        viewLayoutParams = (LinearLayout.LayoutParams) mView.getLayoutParams();
        viewLayoutParams.width = 53;
        viewLayoutParams.height = 1365;
        mView.setLayoutParams(viewLayoutParams);
        length = length + 53;

        Iterator<Question> iterator = questions.iterator();
        while(iterator.hasNext()){
            String question = iterator.next().questionContent;
            TextView questionTextView;
            questionTextView = (TextView)getLayoutInflater().inflate(R.layout.textview_student_question,null);
//            questionTextView = (TextView) getLayoutInflater().inflate(R.layout.textview_student_question,mLayout,true);
            questionTextView.setText(question);     //set the context of every textView
            questionTextView.setOnClickListener(this);      //set the onClickListener for textView
            questionTextView.setOnTouchListener(this);      //set the onTouchListener for textView
            questionTextView.setFocusable(true);        //set the textView focusable
            questionTextView.setMovementMethod(ScrollingMovementMethod.getInstance());      //set can move in scrollView for the textView

            mLayout.addView(questionTextView);
            textViewLayoutParams = (LinearLayout.LayoutParams) questionTextView.getLayoutParams();
            textViewLayoutParams.width = 709;
            textViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            textViewLayoutParams.setMargins(0,210,0,0);
            questionTextView.setLayoutParams(textViewLayoutParams);
            length = length + 709;

            mView = getLayoutInflater().inflate(R.layout.view_empty, null);
            mLayout.addView(mView);
            viewLayoutParams = (LinearLayout.LayoutParams) mView.getLayoutParams();
            viewLayoutParams.width = 53;
            viewLayoutParams.height = 1365;
            mView.setLayoutParams(viewLayoutParams);
            length = length + 53;

//            getLayoutInflater().inflate(R.layout.view_empty, mLayout,true);

            textViews.add(questionTextView);
            textInit(questionTextView);
        }
        updateQuestionTable(questions,null,"-1");
        updateTextViewColor();

        mSeekBar.setMax(length - mScrollView.getWidth());
        mSeekBar.setProgress(mScrollView.getScrollX());
        mSeekBar.setEnabled(false);

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            private int lastX = 0;
            private int touchEventId = -9983761;

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mSeekBar.setProgress(mScrollView.getScrollX());
                    View scroller = (View)msg.obj;
                    if(msg.what==touchEventId) {
                        if(lastX ==scroller.getScrollX()) {
                            handleStop(scroller);
                        }else {
                            handler.sendMessageDelayed(handler.obtainMessage(touchEventId,scroller), 5);
                            lastX = scroller.getScrollX();
                        }
                    }
                }
            };
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    handler.sendMessageDelayed(handler.obtainMessage(touchEventId,v), 5);
                }
                return false;
            }
            //这里写真正的事件
            private void handleStop(Object view) {
                HorizontalScrollView scroller = (HorizontalScrollView) view;
                int maxScroll = length - scroller.getWidth();
                autoScroll(textViews.get(((textViews.size()-1) * scroller.getScrollX()/maxScroll)));
            }
        });

        mScrollView.setOnClickListener(new View.OnClickListener() {
            private int lastX = 0;
            private int clickEventId = -9983762;
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mSeekBar.setProgress(mScrollView.getScrollX());
                    View scroller = (View)msg.obj;
                    if(msg.what==clickEventId) {
                        if(lastX ==scroller.getScrollX()) {
                            handleStop(scroller);
                        }else {
                            handler.sendMessageDelayed(handler.obtainMessage(clickEventId,scroller), 50);
                            lastX = scroller.getScrollX();
                        }
                    }
                }
            };

            @Override
            public void onClick(View v) {
                handler.sendMessageDelayed(handler.obtainMessage(clickEventId,v), 50);
            }

            //这里写真正的事件
            private void handleStop(Object view) {
                HorizontalScrollView scroller = (HorizontalScrollView) view;
                int maxScroll = length - scroller.getWidth();
                mSeekBar.setProgress(mScrollView.getScrollX());
            }
        });

        initStatusBar();
        mButton.setOnClickListener(this);
    }

    private void updateTextViewColor() {
        Cursor cursor = userDb.rawQuery("SELECT * FROM " + stuName,null);
        if(cursor.moveToFirst()){
            do{
                String questionId = cursor.getString(1);
                String isAgree = cursor.getString(2);
                Question question = new Question(Integer.parseInt(questionId),"1","1",-2,-2,"1");
                if(questions.contains(question)){
                    int index = questions.indexOf(question);
                    TextView textView = textViews.get(index);
                    if(isAgree.equals("1")){
                        textAgree(textView);
                    }else if(isAgree.equals("0")){
                        textDisagree(textView);
                    }else{
                        textInit(textView);
                    }
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //自动居中
    private void autoScroll(TextView view) {
        final int initX = mScrollView.getScrollX();

        // Width of the screen
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthScreen = metrics.widthPixels;

        // Width of one child (Button)
        int widthChild = view.getWidth(); // 获取对应位置的子View的宽度

        // Nb children in screen
        int nbChildInScreen = widthScreen / widthChild;

        // Child position left
        int positionLeftChild = view.getLeft(); // 获取对应位置的子View的左边位置 - 坐标

        // Auto scroll to the middle
//        final int movement = (positionLeftChild - ((nbChildInScreen * widthChild) / 2) + widthChild / 5) - mScrollView.getScrollX();
        mScrollView.smoothScrollTo((positionLeftChild - ((nbChildInScreen * widthChild) / 2) + widthChild / 5), 0);
//        mScrollView.smoothScrollBy(-positionLeftChild+widthScreen/2-widthChild/2,0);
//        hor.smoothScrollTo((positionLeftChild - (widthScreen / 2) + widthChild / 2), 0);
        mSeekBar.setProgress(mScrollView.getScrollX());
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addQuestion:
                View stuView = getLayoutInflater().inflate(R.layout.dialog_addquestion, null);
                AddQuestionDialog stuLoginDialog = new AddQuestionDialog(QuestionActivity.this, 0, 0, stuView, R.style.DialogTheme, db, new AddQuestionDialog.PriorityListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void refreshPriorityUI(int res) {
                        if(res <= 1) {
                            if(res == 1) {
                                Toast.makeText(QuestionActivity.this, "添加成功！", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(QuestionActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                            }

                            length  = 0;
                            mLayout.removeAllViews();
                            textViews = new ArrayList<>();
                            questions = getQuestionByClass("http://49.234.213.234/getQuestionByClass", stuClass);
                            Collections.reverse(questions);

                            View mView = getLayoutInflater().inflate(R.layout.view_empty, null);
                            mLayout.addView(mView);
                            viewLayoutParams = (LinearLayout.LayoutParams) mView.getLayoutParams();
                            viewLayoutParams.width = 53;
                            viewLayoutParams.height = 1365;
                            mView.setLayoutParams(viewLayoutParams);
                            length = length + 53;

                            Iterator<Question> iterator = questions.iterator();
                            while(iterator.hasNext()){
                                String question = iterator.next().questionContent;
                                TextView questionTextView;
                                questionTextView = (TextView)getLayoutInflater().inflate(R.layout.textview_student_question,null);
                                questionTextView.setText(question);
                                questionTextView.setOnClickListener(QuestionActivity.this);
                                questionTextView.setOnTouchListener(QuestionActivity.this);      //set the onTouchListener for textView
                                questionTextView.setFocusable(true);        //set the textView focusable
                                questionTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

                                mLayout.addView(questionTextView);
                                textViewLayoutParams = (LinearLayout.LayoutParams) questionTextView.getLayoutParams();
                                textViewLayoutParams.width = 709;
                                textViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                textViewLayoutParams.setMargins(0,210,0,0);
                                questionTextView.setLayoutParams(textViewLayoutParams);
                                length = length + 709;

                                mView = getLayoutInflater().inflate(R.layout.view_empty, null);
                                mLayout.addView(mView);
                                viewLayoutParams = (LinearLayout.LayoutParams) mView.getLayoutParams();
                                viewLayoutParams.width = 53;
                                viewLayoutParams.height = 1365;
                                mView.setLayoutParams(viewLayoutParams);
                                length = length +53;

                                textInit(questionTextView);
                                textViews.add(questionTextView);
                            }
                            updateQuestionTable(questions,null,"-1");
                            updateTextViewColor();


                            mSeekBar.setMax(length - mScrollView.getWidth());
                            mSeekBar.setProgress(mScrollView.getScrollX());

                            getWindow().getDecorView().requestLayout();
                            getWindow().getDecorView().invalidate();
                        }else {
                            Toast.makeText(QuestionActivity.this, "添加失败~~~", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                stuLoginDialog.setCancelable(true);
                stuLoginDialog.show();
                break;
            default:
                autoScroll((TextView) view);
                mScrollView.callOnClick();
                break;
        }
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

    public static List<Question> getQuestionByClass(final String strUrl, final String stuClass) {
        FutureTask<List<Question>> task = new FutureTask<>(new Callable<List<Question>>() {
            @Override
            public List<Question> call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl + "?class=" + stuClass);// 根据自己的服务器地址填写
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
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Question>>(){}.getType();
                    List<Question> resList = gson.fromJson(res, listType);
                    return resList;
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

    private Boolean submitAnswer(final String strUrl, final String studentId, final String questionId, final String isAgree) {
        FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String temp;
                StringBuffer sb = new StringBuffer();
                try {
                    //做连接，以及各连接参数的设置
                    URL url = new URL(strUrl + "?studentId=" + studentId + "&questionId=" + questionId + "&isAgree=" + isAgree);// 根据自己的服务器地址填写
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
                    if(res.equals("提交成功")){
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float windowY = QuestionActivity.this.getWindowManager().getDefaultDisplay().getHeight();
        float ratio = 0.3f;
        float enough = 0.07f;
        float firstX;
        float firstY;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                movement = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if(movement == 0){
                    firstX = event.getX();
                    firstY = event.getY();
                    if (firstX > firstY){
                        movement = -1;
                        break;
                    }else {
                        movement = 1;
                    }
                }else if(movement == -1){
                    break;
                }
                currentY = event.getY();
                moveY = currentY - startY;
                if(abs(moveY/windowY) >= enough){
                    //TODO
                    //submit the answer agree
                    String studentId = stuName;
                    String questionId = questions.get(textViews.indexOf(view)).questionId+"";
                    String isAgree = String.valueOf(moveY > 0 ? 1 : 0);
                    boolean flag = false;

//                    Cursor userCursor = userDb.rawQuery("SELECT * FROM " + stuName + " WHERE questionId = " + questionId,null);
                    Cursor userCursor = userDb.rawQuery("SELECT * FROM " + stuName,null);
                    if(userCursor.moveToFirst()){
                        do{
                            if(userCursor.getString(1).equals(questionId)){
                                String myIsAgree = userCursor.getString(2);
                                if(myIsAgree.equals("-1")){

                                }else if (myIsAgree.equals(isAgree)){
                                    flag = true;
                                    Toast.makeText(this,moveY > 0 ? "已经赞过啦~" : "已经踩过啦~",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }while (userCursor.moveToNext());
                    }
                    if(!flag){
                        flag = false;
                        submitAnswer(submitAnswerURL,studentId,questionId,isAgree);
                        Toast.makeText(this,moveY > 0 ? "已赞" : "已踩",Toast.LENGTH_SHORT).show();
                        if(moveY>0){
                            textAgree(textViews.get(textViews.indexOf(view)));
                        }else {
                            textDisagree(textViews.get(textViews.indexOf(view)));
                        }
                        updateQuestionTable(questions, questions.get(textViews.indexOf(view)),isAgree);
                        updateTextViewColor();
                    }
                    userCursor.close();
                    break;
                }
                if(moveY > 0){//move down side
                    view.setTranslationY(ratio*moveY);
                    view.postInvalidate();
                }else if(moveY < 0){// move up side
                    view.setTranslationY(ratio*moveY);
                    view.postInvalidate();
                }
                break;
            case  MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(abs(moveY/windowY) >= enough){
                    int single = moveY/windowY >= 0 ? -1 : 1;
                    int epoch = 1000;
                    for(int i = 0; i < epoch; i++){
                        view.setTranslationY(ratio*enough*windowY*single/epoch);
                        view.postInvalidate();
                    }
                }else {
                    int epoch = 1000;
                    for (int i = 0; i < epoch; i++) {
                        view.setTranslationY(ratio * moveY / epoch);
                        view.postInvalidate();
                    }
                }
                break;
        }
        return false;
    }


    private void updateQuestionTable(List<Question> questions,Question conQuestion, String isAgree){
        Cursor userCursor = userDb.rawQuery("SELECT * FROM " + stuName,null);
        Cursor questionCursor = db.rawQuery("SELECT * FROM " + questionTable,null);
        if(!userCursor.moveToFirst()){
            if(questionCursor.moveToFirst()){
                db.execSQL("DELETE FROM " + questionTable);
            }
            Iterator<Question> itr = questions.iterator();
            while (itr.hasNext()) {
                Question question = itr.next();

                ContentValues questionCv = new ContentValues(7);
                questionCv.put("questionId", question.questionId + "");
                questionCv.put("questionClassId", question.questionClassId);
                questionCv.put("questionContent", question.questionContent);
                questionCv.put("agreeNum", question.agreeNum + "");
                questionCv.put("disagreeNum", question.disagreeNum + "");
                questionCv.put("publishTime", question.publishTime);
                questionCv.put("isAgree", "-1");
                db.insert(questionTable, null, questionCv);

                ContentValues userCv = new ContentValues(3);
                userCv.put("name", stuName);
                userCv.put("questionId", question.questionId + "");
                userCv.put("isAgree", "-1");
                userDb.insert(stuName, null, userCv);
            }
        }else {
            if(questionCursor.moveToFirst()){
                db.execSQL("DELETE FROM " + questionTable);
            }
            do{
                Question temp = new Question(Integer.parseInt(userCursor.getString(1)),"0","0",0,0,"0");
                Question question = questions.get(questions.indexOf(temp));

                ContentValues questionCv = new ContentValues(7);
                questionCv.put("questionId", question.questionId + "");
                questionCv.put("questionClassId", question.questionClassId);
                questionCv.put("questionContent", question.questionContent);
                questionCv.put("agreeNum", question.agreeNum + "");
                questionCv.put("disagreeNum", question.disagreeNum + "");
                questionCv.put("publishTime", question.publishTime);
                questionCv.put("isAgree",userCursor.getString(2));
                db.insert(questionTable,null,questionCv);
            }while (userCursor.moveToNext());
        }

        userCursor.close();
        questionCursor.close();

        if(!isAgree.equals("-1")){
            String questionId = String.valueOf(conQuestion.questionId);
            db.execSQL("UPDATE " + questionTable + " SET isAgree = " + isAgree + " WHERE questionId = " + questionId);
            userDb.execSQL("UPDATE " + stuName + " SET isAgree = " + isAgree + " WHERE questionId = " + questionId);
        }
    }

    private void textAgree(TextView textView){
        textView.setShadowLayer(0,0,0,Color.BLACK);
        textView.setTextColor(Color.WHITE);
    }

    private void textDisagree(TextView textView){
        textView.setShadowLayer(0,0,0,Color.BLACK);
        textView.setTextColor(Color.BLACK);
    }

    private void textInit(TextView textView){
        textView.setShadowLayer(10,5,5,Color.BLACK);
        textView.setTextColor(Color.WHITE);
    }
}
