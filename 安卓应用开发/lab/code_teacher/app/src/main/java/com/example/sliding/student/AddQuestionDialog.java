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

public class AddQuestionDialog extends Dialog{

    public AddQuestionDialog(Context context, int width, int height, View layout, int style){
        super(context,style);
        setContentView(layout);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }


        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }
}
