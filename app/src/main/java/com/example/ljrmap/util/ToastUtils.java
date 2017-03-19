package com.example.ljrmap.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by user on 2017/3/16.
 */
public class ToastUtils {
    public static void showToast(Context context, CharSequence text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}
