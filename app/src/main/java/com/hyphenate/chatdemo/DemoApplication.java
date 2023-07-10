package com.hyphenate.chatdemo;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;


import com.hyphenate.chatdemo.common.interfaceOrImplement.UserActivityLifecycleCallbacks;
import com.hyphenate.chatdemo.common.utils.PreferenceManager;
import com.hyphenate.util.EMLog;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DemoApplication extends Application implements Thread.UncaughtExceptionHandler {
    private static DemoApplication instance;
    private UserActivityLifecycleCallbacks mLifecycleCallbacks = new UserActivityLifecycleCallbacks();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initThrowableHandler();
        // 初始化PreferenceManager
        PreferenceManager.init(this);
        registerActivityLifecycleCallbacks();
        closeAndroidPDialog();
        if(PreferenceManager.getInstance().isAgreeAgreement()) {
            initSDK();
        }
    }

    private void initThrowableHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void initHx() {
        // init hx sdk
        if(DemoHelper.getInstance().getAutoLogin()) {
            EMLog.i("DemoApplication", "application initHx");
            DemoHelper.getInstance().init(this);
        }
    }

    public void initSDK() {
        initHx();
    }

    private void registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks);
    }

    public static DemoApplication getInstance() {
        return instance;
    }

    public UserActivityLifecycleCallbacks getLifecycleCallbacks() {
        return mLifecycleCallbacks;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        EMLog.e("demoApp", e.getMessage());

    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗
     * 弹窗内容“detected problems with api ”
     */
    private void closeAndroidPDialog(){
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            try {
                Class aClass = Class.forName("android.content.pm.PackageParser$Package");
                Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
                declaredConstructor.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Class cls = Class.forName("android.app.ActivityThread");
                Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
                declaredMethod.setAccessible(true);
                Object activityThread = declaredMethod.invoke(null);
                Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
                mHiddenApiWarningShown.setAccessible(true);
                mHiddenApiWarningShown.setBoolean(activityThread, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
