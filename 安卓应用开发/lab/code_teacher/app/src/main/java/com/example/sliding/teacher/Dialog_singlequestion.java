package com.example.sliding.teacher;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.sliding.R;

public class Dialog_singlequestion extends Dialog {

    Activity context;
    String content;
    int num;
    String test = "佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气佛说不生气";

    public Dialog_singlequestion(Activity context,String content, int num,int theme){
        super(context,theme);
        this.context=context;
        this.content=content;
        this.num=num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.dialog_single_question);

        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);

        TextView contentView = findViewById(R.id.question_content);
        TextView agreeView = findViewById(R.id.question_agree);
        contentView.setText(content+test);
        contentView.setMovementMethod(ScrollingMovementMethod.getInstance());
        agreeView.setText("Agree    "+num);

        this.setCancelable(true);
    }
}
