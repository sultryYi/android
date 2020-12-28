package com.example.sliding.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class timerTask extends Activity {
    private int recLen = 0;
    private TextView txtView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(runnable, 1000);
    }

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recLen++;
            handler.postDelayed(this, 1000);
        }
    };
}
