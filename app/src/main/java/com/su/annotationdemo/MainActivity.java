package com.su.annotationdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.su.injectprocessor.InjectApi.BindView;
import com.su.injectviewapi.BindHelper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bbb)
    Button bbb;
    @BindView(R.id.textView)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindHelper.inject(this);
        bbb.setText("测试");
        textView.setText("又一个测试");
    }
}