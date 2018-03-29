package com.hhly.pay.alipay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mShouQianButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShouQianButton = (Button) findViewById(R.id.shouqian);
        mShouQianButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcherShouQianActivity();
            }
        });
    }

    public void launcherShouQianActivity() {

    }
}
