package com.dqdana.code.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.dqdana.code.ioc.ViewBinder;
import com.dqdana.code.ioc_annotation.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_hello)
    public TextView mTvHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinder.bind(this);
        mTvHello.setText("哇哈哈哈 自定义注解完成~");
    }
}