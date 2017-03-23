package com.zyw.horrarndoo.gooview.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

/**
 * Utils
 * Created by Horrarndoo on 2017/3/20.
 */

public class Utils {
    public static Toast mToast;

    public static void showToast(Context mContext, String msg) {
        if (mToast == null) {
            synchronized (Utils.class){
                if(mToast == null){
                    mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
                }
            }
        }
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * dip 转换成 px
     * @param dip
     * @param context
     * @return
     */
    public static float dip2Dimension(float dip, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }

    /** 获取状态栏高度
     * @param v
     * @return
     */
    public static int getStatusBarHeight(View v) {
        if (v == null) {
            return 0;
        }
        Rect frame = new Rect();
        v.getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
}
