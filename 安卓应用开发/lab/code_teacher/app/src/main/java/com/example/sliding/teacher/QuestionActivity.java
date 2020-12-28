package com.example.sliding.teacher;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener{

    static final String DB_NAME = "teacherDB";
    static final String TB_CLASS = "teacher_class";
    static final String TB_Questions = "class_questions";
    static final String TB_CONTENT = "send_content";
    private String question_get;
    private List<Question> questions;
    private List<Integer> list_question = new ArrayList<Integer>();
    private Dialog_teacher send_dialog;

    SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_question);
        initStatusBar();
        System.out.println("1");
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        db.execSQL(table(TB_CLASS));
        db.execSQL(table(TB_Questions));
        db.execSQL(table(TB_CONTENT));
        //获得问题列表并存在数据库中
        question_get = Server_Connection.get_question(this,"3","SE1");
        handle(question_get);
        System.out.println("2");
        addData_class("SE1","30");
        addData_question("SE1",question_get);
        //
        LinearLayout linearLayout = findViewById(R.id.teacher_textViews);
        linearLayout.addView(classTextview("SE1"));
        for (Question question:questions) {
            TextView textView = questionTextview(question);
            list_question.add(textView.getId());
            linearLayout.addView(textView);
        }
        linearLayout.addView(classTextview("SE2"));
        for (Question question:questions) {
            TextView textView = questionTextview(question);
            list_question.add(textView.getId());
            linearLayout.addView(textView);
        }
        linearLayout.addView(classTextview("SE3"));
        for (Question question:questions) {
            TextView textView = questionTextview(question);
            list_question.add(textView.getId());
            linearLayout.addView(textView);
        }
        getWindow().getDecorView().requestLayout();
        getWindow().getDecorView().invalidate();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @SuppressLint("Recycle")
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.fab:
                Cursor c = db.rawQuery("SELECT content FROM "+TB_CONTENT, null);
                String str = null;
                if (c.moveToFirst()){
                    c.moveToLast();
                    str = c.getString(0);
                }
                send_dialog = new Dialog_teacher(this, this, str, R.style.DialogTheme);
                send_dialog.show();
                break;
            case R.id.send_button:
                Toast.makeText(this,"已发送",Toast.LENGTH_LONG ).show();
                EditText editText = findViewById(R.id.teacher_edit);
                str = send_dialog.getContent();
                addData_content(str);
                Server_Connection.send_question("SE1",str ,System.currentTimeMillis()+"");
            default:
                if (list_question.contains(view.getId())){
                    TextView a = (TextView)view;
                    int agreenum = 0;
                    for (Question question:questions){
                        if (question.getQuestionId() == null)  continue;;
                        if (question.getQuestionId().equals(view.getId()+"")){
                            agreenum = question.getAgreeNum();
                            break;
                        }
                    }
                    Dialog_singlequestion dialog_question = new Dialog_singlequestion(this, a.getText().toString(), agreenum, R.style.DialogTheme);
                    dialog_question.show();
                }
        }

    }

    private TextView classTextview(String classId){
        TextView a = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = dp2px(this,2);
        layoutParams.bottomMargin = dp2px(this,2);
        a.setText(classId);
        a.setTextSize(50);
        a.setTextColor(Color.rgb(0x44,0xBC,0xA8));
        a.setClickable(false);
        a.setLayoutParams(layoutParams);
        a.setTypeface(Typeface.DEFAULT_BOLD,Typeface.BOLD_ITALIC);
        return a;
    }

    private TextView questionTextview(Question question){
        String id = question.getQuestionId();
        String content = question.getContent();
//        String content = "the max length of one line";
        int agreeNum = question.getAgreeNum();
        TextView a = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = dp2px(this,5);
        if (id != null) {
            a.setId(Integer.parseInt(id));
        }
        a.setText(content);
        a.setTextSize(31);
        a.setClickable(true);
        a.setBackgroundResource(R.drawable.item_shape);
        a.setGravity(Gravity.CENTER);
        a.setEllipsize(TextUtils.TruncateAt.END);
        a.setMaxLines(1);
        a.setPadding(dp2px(this,20),dp2px(this,20),dp2px(this,20),dp2px(this,20));
        a.setTextColor(Color.WHITE);
        a.setFocusable(true);
        a.setLayoutParams(layoutParams);
        a.setOnClickListener(this);
        return a;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    private static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    private void handle(String question_get){
        Gson gson = new Gson();
        System.out.println("json:"+question_get);
        questions = gson.fromJson(question_get,new TypeToken<List<Question>>(){}.getType());
        System.out.println("list:"+questions);
    }

    private String table(String tablename){
        String str = "CREATE TABLE IF NOT EXISTS " +
                tablename ;
        if (tablename.equals("teacher_class")) {
            str = str + "(classID VARCHAR(32)," +
                    "class_number VARCHAR(32))" ;
        }
        else if (tablename.equals("class_questions")){
            str = str + "(classID VARCHAR(32)," +
                    "questions VARCHAR(5000))" ;
        }
        else if (tablename.equals("send_content")){
            str = str + "(content VARCHAR(1000))";
        }
        return str;
    }

    private void addData_class(String classID, String num){
        num = "40";
        ContentValues cv = new ContentValues(2);
        cv.put("classID", classID);
        cv.put("class_number", num);
        db.insert(TB_CLASS, null, cv);
    }

    private void addData_question(String classID, String questions){
        ContentValues cv = new ContentValues(2);
        cv.put("classID", classID);
        cv.put("questions", questions);
        db.insert(TB_Questions, null, cv);
    }

    private void addData_content(String content){
        ContentValues cv = new ContentValues(1);
        cv.put("content",content);
        db.insert(TB_CONTENT, null, cv);
    }

//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//            switch (v.getId()) {
//                case R.id.send_button:
//                    Toast.makeText(this,"发送按钮被点击",Toast.LENGTH_LONG ).show();
//                    Server_Connection.send_question("SE1","教师中文测试内容",System.currentTimeMillis()+"");
//                    break;
//            }
//        }
//    };

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

class Question{
    private String questionId;
    private String content;
    private int agreeNum;

    @NonNull
    @Override
    public String toString() {
        return "{"+questionId+","+content+","+agreeNum+"}";
    }

    public int getAgreeNum() {
        return agreeNum;
    }

    public String getContent() {
        return content;
    }

    public String getQuestionId() {
        return questionId;
    }

    public boolean equals(Integer id){
        if (questionId != null) {
            return Integer.parseInt(questionId) == id;
        }
        else {
            return false;
        }
    }
}
