package com.hhly.pay.alipay.boradcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hhly.pay.alipay.Main;

import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by dell on 2018/4/4.
 */

public class AlipayBroadcast extends BroadcastReceiver{
    public static String CONSULT_SET_AMOUNT_RES_STRING_INTENT_FILTER_ACTION = "com.hhly.pay.alipay.info.consultSetAmountResString";

    public static String COOKIE_STR_INTENT_FILTER_ACTION = "com.hhly.pay.alipay.info.cookieStr";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(CONSULT_SET_AMOUNT_RES_STRING_INTENT_FILTER_ACTION)) {
            String qr_money = intent.getStringExtra("qr_money");
            String beiZhu = intent.getStringExtra("beiZhu");
            log("AlipayBroadcast onReceive " + qr_money + " " + beiZhu + "\n");
            if (!qr_money.contentEquals("")) {
                Intent launcherIntent = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", Main.launcherActivity.getApplicationContext().getClassLoader()));
                launcherIntent.putExtra("qr_money", qr_money);
                launcherIntent.putExtra("beiZhu", beiZhu);
                Main.launcherActivity.startActivity(launcherIntent);
            }
        } else if (intent.getAction().contentEquals(COOKIE_STR_INTENT_FILTER_ACTION)) {
            String cookieStr = Main.getCookieStr();
            log("AlipayBroadcast onReceive getCookieStr " + cookieStr + "\n");
            Intent broadCastIntent = new Intent();
            broadCastIntent.putExtra("cookieStr", cookieStr);
            broadCastIntent.setAction(PluginBroadcast.COOKIE_STR_INTENT_FILTER_ACTION);
            context.sendBroadcast(broadCastIntent);
        }
    }
}
