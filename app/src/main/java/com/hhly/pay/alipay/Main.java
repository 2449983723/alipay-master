package com.hhly.pay.alipay;

import android.app.Activity;
import android.content.Intent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.hhly.pay.alipay.VersionParam.ALIPAY_PACKAGE_NAME;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class Main implements IXposedHookLoadPackage {
    private static Activity launcherUiActivity = null;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(ALIPAY_PACKAGE_NAME)) {
            // hook 个人收取的onActivityResult方法
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRActivity onCreated" + "\n");
                    Intent intent = (Intent) param.args[2];
                    String qr_money = intent.getStringExtra("qr_money");
                    String beiZhu = intent.getStringExtra("beiZhu");
                    String qrCodeUrl = intent.getStringExtra("qrCodeUrl");
                    String qrCodeUrlOffline = intent.getStringExtra("qrCodeUrlOffline");
                    log("qr_money:" + qr_money + "\n");
                    log("beiZhu:" + beiZhu + "\n");
                    log("qrCodeUrl:" + qrCodeUrl + "\n");
                    log("qrCodeUrlOffline:" + qrCodeUrlOffline + "\n");
                }
            });
        }

    }
}
