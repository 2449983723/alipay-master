package com.hhly.pay.alipay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hhly.pay.alipay.boradcast.AlipayBroadcast;

import java.util.Random;

import static com.hhly.pay.alipay.VersionParam.ALIPAY_PACKAGE_NAME;

public class MainActivity extends AppCompatActivity {
    private Button mShouQianButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("liunianprint", "MainActivity onCreate!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShouQianButton = (Button) findViewById(R.id.shouqian);
        mShouQianButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(ALIPAY_PACKAGE_NAME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                Intent broadCastIntent = new Intent();
                Random random = new Random();
                broadCastIntent.putExtra("qr_money", String.valueOf(random.nextInt(100) + 1));
                broadCastIntent.putExtra("beiZhu", "测试");
                broadCastIntent.setAction(AlipayBroadcast.INTENT_FILTER_ACTION);
                sendBroadcast(broadCastIntent);
            }
        });
    }

    public void launcherShouQianActivity() {
//        Random random = new Random();
//        PreferencesUtils.setBeiZu(this, "测试");
//        PreferencesUtils.setJinEr(this, String.valueOf(random.nextInt(100) + 1));
//        startActivity(this.getPackageManager().getLaunchIntentForPackage(ALIPAY_PACKAGE_NAME));
//        Intent intent = getPackageManager().getLaunchIntentForPackage(ALIPAY_PACKAGE_NAME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        Log.i("liunianprint:", PreferencesUtils.getBeiZu());
//        Log.i("liunianprint:", PreferencesUtils.getJinEr());
    }

    public void jumpAlipay(){
//        RootShell rootShell = RootShell.open();
//        rootShell.execute("am start -n com.eg.android.AlipayGphone/com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity");
//        rootShell.close();
//        ShellUtils.CommandResult commandResult = ShellUtils.execCmd("am start -n com.eg.android.AlipayGphone/com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", true);
//        Log.i("liunianprint:", commandResult.result + "-------" + commandResult.errorMsg + "-------" + commandResult.successMsg);
//        try{
//            Intent in=new Intent();
////            in.addCategory(Intent.CATEGORY_LAUNCHER);
//            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            ComponentName cn =new ComponentName(ALIPAY_PACKAGE_NAME,"com.eg.android.AlipayGphone.AlipayLogin");
//            in.setComponent(cn);
//            this.startActivity(in);
//        } catch (Exception e){
//        }
    }
}
