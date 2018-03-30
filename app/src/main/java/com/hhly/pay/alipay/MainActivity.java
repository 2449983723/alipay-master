package com.hhly.pay.alipay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.Random;

import static com.hhly.pay.alipay.VersionParam.ALIPAY_PACKAGE_NAME;
import static com.hhly.pay.alipay.VersionParam.BeiZu;
import static com.hhly.pay.alipay.VersionParam.JinEr;

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
//        Intent intent = new Intent();
//        intent.setClassName(ALIPAY_PACKAGE_NAME, "com.eg.android.AlipayGphone.AlipayLogin");
//        startActivity(intent);
        Random random = new Random(100);
        BeiZu = "测试";
        JinEr = String.valueOf(random.nextInt());
        startActivity(this.getPackageManager().getLaunchIntentForPackage(ALIPAY_PACKAGE_NAME));
    }
}
