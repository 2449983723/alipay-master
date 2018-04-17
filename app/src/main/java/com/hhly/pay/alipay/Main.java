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
    private static LoadPackageParam m_lpparam = null;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;

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

            // hook 支付宝主界面的onCreate方法，获得主界面对象并注册广播
            findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.quinox.LauncherActivity onCreated" + "\n");
                    launcherActivity = (Activity) param.thisObject;
                    alipayBroadcast = new AlipayBroadcast();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(AlipayBroadcast.CONSULT_SET_AMOUNT_RES_STRING_INTENT_FILTER_ACTION);
                    intentFilter.addAction(AlipayBroadcast.COOKIE_STR_INTENT_FILTER_ACTION);
                    launcherActivity.registerReceiver(alipayBroadcast, intentFilter);
                }
            });

            // hook 支付宝的主界面的onDestory方法，销毁广播
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

            // hook设置金额和备注的onCreate方法，自动填写数据并点击
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

            // hook获得二维码url的回调方法
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", lpparam.classLoader, "a",
                    findClass("com.alipay.transferprod.rpc.result.ConsultSetAmountRes", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity a" + "\n");
                    String cookieStr = getCookieStr();
                    Object consultSetAmountRes = param.args[0];
                    String consultSetAmountResString = "";
                    if (consultSetAmountRes != null) {
                        consultSetAmountResString = (String) callMethod(consultSetAmountRes, "toString");
                    }
                    Intent broadCastIntent = new Intent();
                    broadCastIntent.putExtra("consultSetAmountResString", consultSetAmountResString);
                    broadCastIntent.putExtra("cookieStr", cookieStr);
                    broadCastIntent.setAction(PluginBroadcast.CONSULT_SET_AMOUNT_RES_STRING_INTENT_FILTER_ACTION);
                    Activity activity = (Activity) param.thisObject;
                    activity.sendBroadcast(broadCastIntent);
                    log("consultSetAmountResString:" + consultSetAmountResString + "\n");
                    log("cookieStr:" + cookieStr + "\n");
                }
            });
        }
    }

    public static String getCookieStr() {
        String cookieStr = "";
        // 获得cookieStr
        if (m_lpparam != null) {
            callStaticMethod(findClass("com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", Main.m_lpparam.classLoader), "getSessionid");
            Context context = (Context) callStaticMethod(findClass("com.alipay.mobile.common.transportext.biz.shared.ExtTransportEnv", Main.m_lpparam.classLoader), "getAppContext");
            if (context != null) {
                Object readSettingServerUrl = callStaticMethod(findClass("com.alipay.mobile.common.helper.ReadSettingServerUrl", Main.m_lpparam.classLoader), "getInstance");
                if (readSettingServerUrl != null) {
                    String gWFURL = (String) callMethod(readSettingServerUrl, "getGWFURL", context);
                    cookieStr = (String) callStaticMethod(findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", Main.m_lpparam.classLoader), "getCookie", gWFURL);
                }
            }
        }
        return cookieStr;
    }

    // 解决支付宝的反hook
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
