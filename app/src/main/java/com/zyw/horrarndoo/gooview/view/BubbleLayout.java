package com.zyw.horrarndoo.gooview.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Horrarndoo on 2017/3/20.
 * 播放爆炸动画的layout
 */

public class BubbleLayout extends FrameLayout {
    Context context;

    public BubbleLayout(Context context) {
        super(context);
        this.context = context;
    }

    private int mCenterX, mCenterY;

    public void setCenter(int x, int y) {
        mCenterX = x;
        mCenterY = y;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        View child = getChildAt(0);
        // 设置View到指定位置
        if (child != null && child.getVisibility() != GONE) {
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            child.layout((int) (mCenterX - width / 2.0f), (int) (mCenterY - height / 2.0f)
                    , (int) (mCenterX + width / 2.0f), (int) (mCenterY + height / 2.0f));
        }
    }
}
