package com.zyw.horrarndoo.gooview.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zyw.horrarndoo.gooview.R;
import com.zyw.horrarndoo.gooview.utils.Utils;
import com.zyw.horrarndoo.gooview.view.GooView.OnDisappearListener;

/**
 * Created by Horrarndoo on 2017/3/20.
 */

public class GooViewListener implements OnTouchListener, OnDisappearListener {

    private WindowManager mWm;
    private WindowManager.LayoutParams mParams;
    private GooView mGooView;
    private View pointLayout;
    private int number;
    private final Context mContext;

    private Handler mHandler;

    public GooViewListener(Context mContext, View pointLayout) {
        this.mContext = mContext;
        this.pointLayout = pointLayout;
        this.number = (Integer) pointLayout.getTag();

        mGooView = new GooView(mContext);

        mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;//使窗口支持透明度

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        mHandler = new Handler(mContext.getMainLooper());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        // 当按下时，将自定义View添加到WindowManager中
        if (action == MotionEvent.ACTION_DOWN) {
            ViewParent parent = v.getParent();
            // 请求其父级View不拦截Touch事件
            parent.requestDisallowInterceptTouchEvent(true);

            int[] points = new int[2];
            //获取pointLayout在屏幕中的位置（layout的左上角坐标）
            pointLayout.getLocationInWindow(points);
            //获取初始小红点中心坐标
            int x = points[0] + pointLayout.getWidth() / 2;
            int y = points[1] + pointLayout.getHeight() / 2;
            // 初始化当前点击的item的信息，数字及坐标
            mGooView.setStatusBarHeight(Utils.getStatusBarHeight(v));
            mGooView.setNumber(number);
            mGooView.initCenter(x, y);
            //设置当前GooView消失监听
            mGooView.setOnDisappearListener(this);
            // 添加当前GooView到WindowManager
            mWm.addView(mGooView, mParams);
            pointLayout.setVisibility(View.INVISIBLE);
        }
        // 将所有touch事件转交给GooView处理
        mGooView.onTouchEvent(event);
        return true;
    }

    @Override
    public void onDisappear(PointF mDragCenter) {
        if (mWm != null && mGooView.getParent() != null) {
            mWm.removeView(mGooView);

            //播放气泡爆炸动画
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.anim_bubble_pop);
            AnimationDrawable mAnimDrawable = (AnimationDrawable) imageView
                    .getDrawable();

            final BubbleLayout bubbleLayout = new BubbleLayout(mContext);
            bubbleLayout.setCenter((int) mDragCenter.x, (int) mDragCenter.y - Utils.getStatusBarHeight(mGooView));

            bubbleLayout.addView(imageView, new FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));

            mWm.addView(bubbleLayout, mParams);

            mAnimDrawable.start();

            // 播放结束后，删除该bubbleLayout
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWm.removeView(bubbleLayout);
                }
            }, 501);
        }

    }

    @Override
    public void onReset(boolean isOutOfRange) {
        // 当dragPoint弹回时，去除该View，等下次ACTION_DOWN的时候再添加
        if (mWm != null && mGooView.getParent() != null) {
            mWm.removeView(mGooView);
        }
    }
}
