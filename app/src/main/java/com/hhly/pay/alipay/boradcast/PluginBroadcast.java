package com.hhly.pay.alipay.boradcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hhly.pay.alipay.App;

/**
 * Created by dell on 2018/4/4.
 */

public class PluginBroadcast extends BroadcastReceiver{
    public static String INTENT_FILTER_ACTION = "com.eg.android.AlipayGphone.info";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(INTENT_FILTER_ACTION)) {
            App.dealAlipayInfo(context, intent);
        }
    }
}
