package com.hhly.pay.alipay;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.widget.Button;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.hhly.pay.alipay.VersionParam.ALIPAY_PACKAGE_NAME;
import static com.hhly.pay.alipay.VersionParam.BeiZu;
import static com.hhly.pay.alipay.VersionParam.JinEr;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class Main implements IXposedHookLoadPackage {
    private static Activity launcherActivity = null;
    private static LoadPackageParam m_lpparam;
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;

        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            findAndHookMethod(BuildConfig.APPLICATION_ID + ".MainActivity", lpparam.classLoader, "launcherShouQianActivity", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("MainActivity launcherShouQianActivity" + "\n");
                    launcherShouQianActivity();
                }
            });
        }

        if (packageName.equals(ALIPAY_PACKAGE_NAME)) {
            m_lpparam = lpparam;
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

            // hook 微信主界面的onCreate方法，获得主界面对象
            findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.quinox.LauncherActivity onCreated" + "\n");
                    launcherActivity = (Activity) param.thisObject;
                    if (launcherActivity != null) {
                        log("launcherActivity != null" + "\n");
                    }
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
                    log("JinEr:" + JinEr + "\n");
                    log("BeiZu:" + BeiZu + "\n");
                    XposedHelpers.callMethod(jinErView, "setText", "10");
                    XposedHelpers.callMethod(beiZhuView, "setText", "测试");

                    Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
                    final Button quRenButton = (Button) quRenField.get(param.thisObject);
                    quRenButton.performClick();
                }
            });

            // hook 个人收取的onActivityResult方法
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRActivity onActivityResult" + "\n");
                    if (launcherActivity != null) {
                        log("launcherActivity != null" + "\n");
                    }
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

    public static void launcherShouQianActivity() {
//        if (launcherActivity != null) {
//            log("start launcherShouQianActivity" + "\n");
//            Intent intent = new Intent(launcherActivity, findClass("com.eg.android.AlipayGphone.AlipayLogin", m_lpparam.classLoader));
//            launcherActivity.startActivity(intent);
//        } else {
//            log("launcherActivity == null" + "\n");
//        }
    }
}
