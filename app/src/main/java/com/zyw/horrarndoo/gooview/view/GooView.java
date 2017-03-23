package com.zyw.horrarndoo.gooview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.zyw.horrarndoo.gooview.utils.GeometryUtils;
import com.zyw.horrarndoo.gooview.utils.Utils;

/**
 * GooView
 * Created by Administrator on 2017/3/20.
 */

public class GooView extends View {
    protected static final String TAG = "GooView";

    private PointF mDragCenter;
    private PointF mStickCenter;
    float dragCircleRadius = 0;
    float stickCircleRadius = 0;
    float stickCircleMinRadius = 0;
    float stickCircleTempRadius = stickCircleRadius;

    String mNumberText = "";

    private Paint mPaintRed;
    private Paint mTextPaint;
    private ValueAnimator mAnim;
    private boolean isOutOfRange = false;

    private OnDisappearListener mListener;
    private int mStatusBarHeight;

    /**
     * dragPoint拖出mMaxDistance范围后，重置dragPoint范围，超出消失，否则重置
     */
    private float mResetDistance;
    /**
     * 绘制连接线最大范围，超出不绘制贝塞尔曲线部分以及stickPoint
     */
    private float mMaxDistance = 0;

    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        stickCircleRadius = Utils.dip2Dimension(10.0f, context);
        dragCircleRadius = Utils.dip2Dimension(10.0f, context);
        stickCircleMinRadius = Utils.dip2Dimension(3.0f, context);
        mMaxDistance = Utils.dip2Dimension(70.0f, context);
        mResetDistance = Utils.dip2Dimension(40.0f, getContext());

        mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRed.setColor(Color.RED);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(dragCircleRadius * 1.2f);
    }

    /**
     * 设置拖拽圆的半径
     *
     * @param r
     */
    public void setDargCircleRadius(float r) {
        dragCircleRadius = r;
        invalidate();
    }

    /**
     * 设置固定圆的半径
     *
     * @param r
     */
    public void setStickCircleRadius(float r) {
        stickCircleRadius = r;
        invalidate();
    }

    /**
     * 设置数字
     *
     * @param num
     */
    public void setNumber(int num) {
        mNumberText = String.valueOf(num);
    }

    /**
     * 初始化圆的圆心坐标
     *
     * @param x
     * @param y
     */
    public void initCenter(float x, float y) {
        initDragCenter(x, y);
        initStickCenter(x, y);
        invalidate();
    }

    public void initDragCenter(float x, float y) {
        mDragCenter = new PointF(x, y);
    }

    public void initStickCenter(float x, float y) {
        mStickCenter = new PointF(x, y);
    }

    /**
     * 更新拖拽圆的圆心坐标，重绘View
     *
     * @param x
     * @param y
     */
    private void updateDragPointCenter(float x, float y) {
        this.mDragCenter.x = x;
        this.mDragCenter.y = y;
        invalidate();
    }

    /**
     * 根据距离获得当前固定圆的半径
     *
     * @param distance
     * @return
     */
    private float getCurrentRadius(float distance) {
        distance = Math.min(distance, mMaxDistance);
        //20%-80%
        float fraction = 0.2f + 0.8f * distance / mMaxDistance;

        // Distance -> mMaxDistance
        // stickCircleRadius -> stickCircleMinRadius
        float evaluateValue = (float) GeometryUtils.evaluateValue(fraction, stickCircleRadius, stickCircleMinRadius);
        return evaluateValue;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isAnimRunning()) {
            return false;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                isOutOfRange = false;
                updateDragPointCenter(event.getRawX(), event.getRawY());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //如果两圆间距大于最大距离mMaxDistance，执行拖拽结束动画
                PointF p0 = new PointF(mDragCenter.x, mDragCenter.y);
                PointF p1 = new PointF(mStickCenter.x, mStickCenter.y);
                if (GeometryUtils.getDistanceBetween2Points(p0, p1) > mMaxDistance) {
                    isOutOfRange = true;
                    updateDragPointCenter(event.getRawX(), event.getRawY());
                    return false;
                }
                updateDragPointCenter(event.getRawX(), event.getRawY());
                break;
            }
            case MotionEvent.ACTION_UP: {
                handleActionUp();
                break;
            }
            default: {
                isOutOfRange = false;
                break;
            }
        }
        return true;
    }

    /**
     * 判断动画是否正在执行
     *
     * @return
     */
    private boolean isAnimRunning() {
        if (mAnim != null && mAnim.isRunning()) {
            return true;
        }
        return false;
    }

    /**
     * 清除小红点
     */
    private void disappeared() {
        invalidate();

        if (mListener != null) {
            mListener.onDisappear(mDragCenter);
        }
    }

    /**
     * 手势抬起动作
     */
    private void handleActionUp() {
        if (isOutOfRange) {
            // 当拖动dragPoint范围已经超出mMaxDistance，然后又将dragPoint拖回mResetDistance范围内时
            if (GeometryUtils.getDistanceBetween2Points(mDragCenter, mStickCenter) < mResetDistance) {
                if (mListener != null)
                    mListener.onReset(isOutOfRange);
                return;
            }
            // dragPoint > mResetDistance，删除这个点
            disappeared();
        } else {
            //手指抬起时，弹回动画
            mAnim = ValueAnimator.ofFloat(1.0f);
            mAnim.setInterpolator(new OvershootInterpolator(5.0f));

            final PointF startPoint = new PointF(mDragCenter.x, mDragCenter.y);
            final PointF endPoint = new PointF(mStickCenter.x, mStickCenter.y);
            mAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    PointF pointByPercent = GeometryUtils.getPointByPercent(startPoint, endPoint, fraction);
                    updateDragPointCenter((float) pointByPercent.x, (float) pointByPercent.y);
                }
            });
            mAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mListener != null)
                        mListener.onReset(isOutOfRange);
                }
            });

            if (GeometryUtils.getDistanceBetween2Points(startPoint, endPoint) < 10) {
                mAnim.setDuration(100);
            } else {
                mAnim.setDuration(300);
            }
            mAnim.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //去除状态栏高度偏差
        canvas.translate(0, -mStatusBarHeight);
        if (!isOutOfRange) {
            drawGooPath(canvas);
        }
        // 画拖拽圆
        canvas.drawCircle(mDragCenter.x, mDragCenter.y, dragCircleRadius, mPaintRed);
        // 画数字
        canvas.drawText(mNumberText, mDragCenter.x, mDragCenter.y + dragCircleRadius / 2f, mTextPaint);
        canvas.restore();
    }

    /**
     * 绘制贝塞尔曲线部分以及固定圆
     *
     * @param canvas
     */
    private void drawGooPath(Canvas canvas) {
        Path path = new Path();
        //1. 根据当前两圆圆心的距离计算出固定圆的半径
        float distance = (float) GeometryUtils.getDistanceBetween2Points(mDragCenter, mStickCenter);
        stickCircleTempRadius = getCurrentRadius(distance);

        //2. 计算出经过两圆圆心连线的垂线的dragLineK（对边比临边）。求出四个交点坐标
        float xDiff = mStickCenter.x - mDragCenter.x;
        Double dragLineK = null;
        if (xDiff != 0) {
            dragLineK = (double) ((mStickCenter.y - mDragCenter.y) / xDiff);
        }

        //分别获得经过两圆圆心连线的垂线与圆的交点（两条垂线平行，所以dragLineK相等）。
        PointF[] dragPoints = GeometryUtils.getIntersectionPoints(mDragCenter, dragCircleRadius, dragLineK);
        PointF[] stickPoints = GeometryUtils.getIntersectionPoints(mStickCenter, stickCircleTempRadius, dragLineK);

        //3. 以两圆连线的0.618处作为 贝塞尔曲线 的控制点。（选一个中间点附近的控制点）
        PointF pointByPercent = GeometryUtils.getPointByPercent(mDragCenter, mStickCenter, 0.618f);

        // 绘制两圆连接闭合
        path.moveTo((float) stickPoints[0].x, (float) stickPoints[0].y);
        path.quadTo((float) pointByPercent.x, (float) pointByPercent.y,
                (float) dragPoints[0].x, (float) dragPoints[0].y);
        path.lineTo((float) dragPoints[1].x, (float) dragPoints[1].y);
        path.quadTo((float) pointByPercent.x, (float) pointByPercent.y,
                (float) stickPoints[1].x, (float) stickPoints[1].y);
        canvas.drawPath(path, mPaintRed);
        // 画固定圆
        canvas.drawCircle(mStickCenter.x, mStickCenter.y, stickCircleTempRadius, mPaintRed);
    }

    public OnDisappearListener getOnDisappearListener() {
        return mListener;
    }

    public void setOnDisappearListener(OnDisappearListener mListener) {
        this.mListener = mListener;
    }

    public void setStatusBarHeight(int statusBarHeight) {
        this.mStatusBarHeight = statusBarHeight;
    }

    interface OnDisappearListener {
        /**
         * GooView Disapper
         *
         * @param mDragCenter
         */
        void onDisappear(PointF mDragCenter);

        /**
         * GooView onReset
         *
         * @param isOutOfRange
         */
        void onReset(boolean isOutOfRange);
    }
}
