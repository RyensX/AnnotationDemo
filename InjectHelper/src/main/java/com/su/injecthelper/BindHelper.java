package com.su.injecthelper;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;

public class BindHelper {

    private final static String bindViewClassName = "$BindViewInjector";

    public static void inject(Activity activity) {
        String classFullName = activity.getClass().getName() + bindViewClassName;
        try {
            Class proxy = Class.forName(classFullName);
            Constructor constructor = proxy.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            Log.d(bindViewClassName, "实例化失败");
            e.printStackTrace();
        }
    }
}