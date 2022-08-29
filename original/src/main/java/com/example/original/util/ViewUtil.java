package com.example.original.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ViewUtil {
    //从系统服务中获取输入法管理器
    public static void hideOneInputMethod(Activity act, View v) {
        InputMethodManager imm = (InputMethodManager)
                act.getSystemService(Context.INPUT_METHOD_SERVICE);
        //关闭屏幕上的软键盘
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
