package com.example.sliding.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.R;
import com.google.gson.Gson;

public class ForestActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edit;
    private EditText edit_time;
    private String content = null;
    private Boolean state = false;
    private int time;
    private String num;
    private String[] forest_student;
    private TextView textView;
    Button begin_btn;

    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_forest);
        initStatusBar();

        content = getIntent().getStringExtra("content");
        state = getIntent().getBooleanExtra("state", false);
        time = getIntent().getIntExtra("time", 0);

        edit = findViewById(R.id.forest_content);
        if (content != null) edit.setText(content);

        edit_time = findViewById(R.id.forest_time);
        if (time != 0)  edit_time.setText(time+"");

        this.getIntent().getStringExtra("content");
        begin_btn = findViewById(R.id.forest_button_begin);
        begin_btn.setOnClickListener(this);

        textView = findViewById(R.id.forest_number);
        if (state)  begin_btn.setBackgroundResource(R.drawable.button_forest_end);

        String str = Server_Connection.forest_result("SE1");
        handle(str);
        if (forest_student.length != 0)    {
            textView.setText("Last_number:"+forest_student.length+"/30");
        }
        else{
            textView.setText("Last_number:None");
        }
    }

    private void handle(String str){
        Gson gson = new Gson();
        forest_student = gson.fromJson(str,String[].class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forest_button_begin:
                Server_Connection.forest_begin("SE1", 200, 500);
                Toast.makeText(this,"已发送",Toast.LENGTH_LONG ).show();
                begin_btn.setBackgroundResource(R.drawable.button_forest_end);
                state = true;
            default:
                break;
        }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent a = new Intent();
            a.putExtra("content",edit.getText().toString());
            a.putExtra("state", state);
            if (!edit_time.getText().toString().equals(""))  a.putExtra("time", Integer.valueOf(edit_time.getText().toString()));
            else a.putExtra("time","");
            setResult(1001,a);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        content = savedInstanceState.getString("content");
    }
}

class Student{
    String name;
}
