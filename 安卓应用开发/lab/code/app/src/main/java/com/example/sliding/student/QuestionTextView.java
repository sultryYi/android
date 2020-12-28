package com.example.sliding.student;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;


public class QuestionTextView extends AppCompatTextView {
    public QuestionTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}
