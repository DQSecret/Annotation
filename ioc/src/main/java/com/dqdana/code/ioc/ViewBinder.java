package com.dqdana.code.ioc;

import android.app.Activity;

/**
 * 从生成类中为当前的 Activity/View 中的 View.findViewById
 */
public class ViewBinder {

    private static final String SUFFIX = "$$ViewInjector";

    /**
     * Activity 中调用的方法
     */
    public static void bind(Activity activity) {
        bind(activity, activity);
    }

    /**
     * 1. 寻找对应的代理类
     * 2. 调用接口提供的绑定方法
     */
    private static void bind(final Object host, final Object root) {
        if (host == null || root == null) {
            return;
        }

        Class<?> hostClass = host.getClass();
        String proxyClassFullName = hostClass.getName() + SUFFIX; // 拼接生成类的名称

        try {
            Class<?> proxyClass = Class.forName(proxyClassFullName);
            ViewInjector injector = (ViewInjector) proxyClass.newInstance();
            //noinspection unchecked : 忽略反省问题
            injector.inject(host, root);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}