package com.hhly.pay.alipay;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.widget.Button;

import com.hhly.pay.alipay.boradcast.AlipayBroadcast;
import com.hhly.pay.alipay.boradcast.PluginBroadcast;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.hhly.pay.alipay.VersionParam.ALIPAY_PACKAGE_NAME;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class Main implements IXposedHookLoadPackage {
    public static Activity launcherActivity = null;
    private static AlipayBroadcast alipayBroadcast = null;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;

        if (packageName.equals(ALIPAY_PACKAGE_NAME)) {
            XposedHelpers.findAndHookMethod(Application.class,
                    "attach",
                    Context.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Context context = (Context) param.args[0];
                            ClassLoader appClassLoader = context.getClassLoader();
                            securityCheckHook(appClassLoader);
                        }
                    });

            // hook 支付宝主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.quinox.LauncherActivity onCreated" + "\n");
                    launcherActivity = (Activity) param.thisObject;
                    alipayBroadcast = new AlipayBroadcast();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(AlipayBroadcast.INTENT_FILTER_ACTION);
                    launcherActivity.registerReceiver(alipayBroadcast, intentFilter);
                }
            });

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", lpparam.classLoader, "onDestroy", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.quinox.LauncherActivity onDestroy" + "\n");
                    if (alipayBroadcast != null) {
                        ((Activity) param.thisObject).unregisterReceiver(alipayBroadcast);
                    }
                    launcherActivity = null;
                }
            });

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity onCreated" + "\n");
                    Field jinErField = XposedHelpers.findField(param.thisObject.getClass(), "b");
                    final Object jinErView = jinErField.get(param.thisObject);
                    Field beiZhuField = XposedHelpers.findField(param.thisObject.getClass(), "c");
                    final Object beiZhuView = beiZhuField.get(param.thisObject);
                    Intent intent = ((Activity) param.thisObject).getIntent();
                    String jinEr = intent.getStringExtra("qr_money");
                    String beiZu = intent.getStringExtra("beiZhu");
                    log("JinEr:" + jinEr + "\n");
                    log("BeiZu:" + beiZu + "\n");
                    XposedHelpers.callMethod(jinErView, "setText", jinEr);
                    XposedHelpers.callMethod(beiZhuView, "setText", beiZu);

                    Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
                    final Button quRenButton = (Button) quRenField.get(param.thisObject);
                    quRenButton.performClick();
                }
            });

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRActivity onCreated" + "\n");
                    Intent intent = ((Activity) param.thisObject).getIntent();
                    if (intent != null) {
                        String qr_money = intent.getStringExtra("qr_money");
                        String beiZhu = intent.getStringExtra("beiZhu");
                        if (qr_money != null) {
                            log("JinEr:" + qr_money + "\n");
                            log("BeiZu:" + beiZhu + "\n");
                            Intent launcherIntent = new Intent((Activity) param.thisObject, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", lpparam.classLoader));
                            launcherIntent.putExtra("qr_money", qr_money);
                            launcherIntent.putExtra("beiZhu", beiZhu);
                            ((Activity) param.thisObject).startActivityForResult(launcherIntent, 10);
                        }
                    }
                }
            });

            // hook 个人收取的onActivityResult方法
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRActivity onActivityResult" + "\n");
                    Intent intent = (Intent) param.args[2];
                    if (intent != null) {
                        String qr_money = intent.getStringExtra("qr_money");
                        String beiZhu = intent.getStringExtra("beiZhu");
                        String qrCodeUrl = intent.getStringExtra("qrCodeUrl");
                        String qrCodeUrlOffline = intent.getStringExtra("qrCodeUrlOffline");
                        Intent broadCastIntent = new Intent();
                        broadCastIntent.putExtra("qr_money", qr_money);
                        broadCastIntent.putExtra("beiZhu", beiZhu);
                        broadCastIntent.putExtra("qrCodeUrl", qrCodeUrl);
                        broadCastIntent.putExtra("qrCodeUrlOffline", qrCodeUrlOffline);
                        broadCastIntent.setAction(PluginBroadcast.INTENT_FILTER_ACTION);
                        Activity activity = (Activity) param.thisObject;
                        activity.sendBroadcast(broadCastIntent);
                        log("qr_money:" + qr_money + "\n");
                        log("beiZhu:" + beiZhu + "\n");
                        log("qrCodeUrl:" + qrCodeUrl + "\n");
                        log("qrCodeUrlOffline:" + qrCodeUrlOffline + "\n");

                        Object appInfo = callStaticMethod(findClass("com.ali.user.mobile.info.AppInfo", lpparam.classLoader), "getInstance");
                        if (appInfo != null) {
                            log("com.ali.user.mobile.info.AppInfo != null");
                            String apdidToken = (String)callMethod(appInfo, "getApdidToken");
                            log("apdidToken " + apdidToken);
                            String apdid = (String)callMethod(appInfo, "getApdid");
                            log("apdid " + apdid);
                            String appKey = (String)callMethod(appInfo, "getAppKey", launcherActivity);
                            log("appKey " + appKey);
                            String channel = (String)callMethod(appInfo, "getChannel");
                            log("getChannel " + channel);
                            String productId = (String)callMethod(appInfo, "getProductId");
                            log("productId " + productId);
                            String umid = (String)callMethod(appInfo, "getUmid");
                            log("umid " + umid);
                            String deviceKeySet = (String)callMethod(appInfo, "getDeviceKeySet");
                            log("deviceKeySet " + deviceKeySet);
                            String deviceId = (String)callMethod(appInfo, "getDeviceId");
                            log("deviceId " + deviceId);
                        }

                        String token = (String)callStaticMethod(findClass("com.alipay.mobile.nebula.log.H5Logger", lpparam.classLoader), "getToken");
                        if (token != null) {
                            log("com.alipay.mobile.nebula.log.H5Logger getToken");
                            log("token " + token);
                        }

                        String sessionid = (String)callStaticMethod(findClass("com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", lpparam.classLoader), "getSessionid");
                        log("sessionid " + sessionid);
                        ((Activity) param.thisObject).finish();
                    }
                }
            });

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", lpparam.classLoader, "getSessionidFromCookiestr", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.common.transportext.biz.appevent getSessionidFromCookiestr" + "\n");
                    String cookieStr = (String)param.args[0];
                    log("cookieStr " + cookieStr);
                }
            });

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.nebulacore.ui.H5Activity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.nebulacore.ui.H5Activity onCreated" + "\n");
                    Intent intent = ((Activity) param.thisObject).getIntent();
                    if (intent != null) {
                        if (intent.getStringExtra("url") != null) {
                            log("url" + intent.getStringExtra("url"));
                        }
                    }
                }
            });
        }
    }

    private void securityCheckHook(ClassLoader classLoader) {
        try {
            Class securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    XposedHelpers.setBooleanField(object, "a", false);
                    param.setResult(object);
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
