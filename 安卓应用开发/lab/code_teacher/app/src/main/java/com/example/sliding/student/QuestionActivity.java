package com.example.sliding.student;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sliding.R;

import java.util.ArrayList;
import java.util.List;


public class QuestionActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mButton;
    private HorizontalScrollView mScrollView;
    private LinearLayout mLayout;
    private List<TextView> textViews;
    private TextView t1;
    private TextView t2;
    private TextView t3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_question);
        mButton = (Button) findViewById(R.id.addQuestion);
        mScrollView = (HorizontalScrollView) findViewById(R.id.mScrollView);
        mLayout = (LinearLayout) findViewById(R.id.textViews);
        textViews = new ArrayList();

        t1=(TextView)findViewById(R.id.text1);
        textViews.add(t1);
        t2=(TextView)findViewById(R.id.text2);
        textViews.add(t2);
        t3=(TextView)findViewById(R.id.text3);
        textViews.add(t3);

        for (int i = 0; i < textViews.size(); i++) {
            textViews.get(i).setOnClickListener(this);
            textViews.get(i).setMovementMethod(ScrollingMovementMethod.getInstance());
        }


        initStatusBar();
        mButton.setOnClickListener(this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //自动居中
    private void autoScroll(int i) {
        // Width of the screen
        DisplayMetrics metrics = getResources()
                .getDisplayMetrics();
        int widthScreen = metrics.widthPixels;

        // Width of one child (Button)
        int widthChild = textViews.get(i).getWidth(); // 获取对应位置的子View的宽度

        // Nb children in screen
        int nbChildInScreen = widthScreen / widthChild;

        // Child position left
        int positionLeftChild = textViews.get(i).getLeft(); // 获取对应位置的子View的左边位置 - 坐标

        // Auto scroll to the middle
        mScrollView.smoothScrollTo((positionLeftChild - ((nbChildInScreen * widthChild) / 2) + widthChild / 5), 0);
//        hor.smoothScrollTo((positionLeftChild - (widthScreen / 2) + widthChild / 2), 0);
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
                AddQuestionDialog stuLoginDialog = new AddQuestionDialog(QuestionActivity.this, 0, 0, stuView, R.style.DialogTheme);
                stuLoginDialog.setCancelable(true);
                stuLoginDialog.show();
                break;

            case R.id.text1:
                autoScroll(0);
                break;

            case R.id.text2:
                autoScroll(1);
                break;

            case R.id.text3:
                autoScroll(2);
                break;
        }
    }
}
