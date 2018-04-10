package com.hhly.pay.alipay.boradcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.hhly.pay.alipay.Main;

import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.log;

/**
 * Created by dell on 2018/4/4.
 */

public class AlipayBroadcast extends BroadcastReceiver{
    public static String INTENT_FILTER_ACTION = "com.hhly.pay.alipay.info";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(INTENT_FILTER_ACTION)) {
            String qr_money = intent.getStringExtra("qr_money");
            String beiZhu = intent.getStringExtra("beiZhu");
            log("AlipayBroadcast onReceive " + qr_money + " " + beiZhu + "\n");
            if (!qr_money.contentEquals("")) {
                Intent launcherIntent = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", Main.launcherActivity.getApplicationContext().getClassLoader()));
                launcherIntent.putExtra("qr_money", qr_money);
                launcherIntent.putExtra("beiZhu", beiZhu);
                Main.launcherActivity.startActivity(launcherIntent);
            }
        }
    }
}
