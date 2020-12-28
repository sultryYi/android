package com.example.sliding.student;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sliding.MainActivity;
import com.example.sliding.R;

public class LoginDialog extends Dialog implements View.OnClickListener {

    private TextView ok;
    private TextView cancel;
    private EditText username;
    private EditText password;

    public LoginDialog(Context context, int width, int height, View layout, int style){
        super(context,style);
        setContentView(layout);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }

        ok = (TextView) findViewById(R.id.ok);
        cancel = (TextView) findViewById(R.id.cancel);
        username = (EditText) findViewById(R.id.usernameEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.ok:
                if(this.username.getText().toString().trim().equals("student") && this.password.getText().toString().trim().equals("123")){
                    this.dismiss();
                    intent = new Intent(this.getOwnerActivity(), QuestionActivity.class);
                    intent.putExtra("username",this.username.getText().toString().trim());
                    intent.putExtra("password",this.password.getText().toString().trim());
                    this.getOwnerActivity().startActivity(intent);
                    this.getOwnerActivity().finish();
                }else if(this.username.getText().toString().trim().equals("teacher") && this.password.getText().toString().trim().equals("123")){
                    this.dismiss();
                    intent = new Intent(this.getOwnerActivity(), MainActivity.class);
                    intent.putExtra("username",this.username.getText().toString().trim());
                    intent.putExtra("password",this.password.getText().toString().trim());
                    this.getOwnerActivity().startActivity(intent);
                    this.getOwnerActivity().finish();
                } else{
                    Toast toast = Toast.makeText(this.getContext(),"用户名或密码错误",Toast.LENGTH_SHORT);
                    toast.show();
                    this.username.setText("");
                    this.password.setText("");
                }
                break;
            case R.id.cancel:
                this.dismiss();
                break;
        }
    }
}
