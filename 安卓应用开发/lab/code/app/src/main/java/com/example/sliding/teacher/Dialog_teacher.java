package com.example.sliding.teacher;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.sliding.R;

public class Dialog_teacher extends Dialog {
    /**
     * 上下文对象 *
     */
    Activity context;

    private Button btn_send;

    private View.OnClickListener mClickListener;

    private String content;

    public Dialog_teacher(Activity context) {
        super(context);
        this.context = context;
    }

    public Dialog_teacher(Activity context,View.OnClickListener clickListener, int theme) {
        super(context,theme);
        this.context = context;
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.dialog_addquestion_teacher);

        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);

        EditText editText = findViewById(R.id.teacher_edit);
        if (content!=null){
            editText.setText(content);
        }

        // 根据id在布局中找到控件对象
        btn_send = (Button) findViewById(R.id.send_button);

        // 为按钮绑定点击事件监听器
        btn_send.setOnClickListener(mClickListener);

        this.setCancelable(true);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent(){
        EditText editText = findViewById(R.id.teacher_edit);
        return editText.getText().toString();
    }
}
