package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.utils.DensityUtils;

/**
 * Created by carden.
 */

public class VolumeControlView extends View {
    Paint paint;
    Point point;
    int radius;
    int bgLineStrokeWidth;
    int circleStorkeWidth;
    int fgLineStrokeWidth;
    int range;
    OnVolumeChangeListener mListener;

    public VolumeControlView(Context context) {
        super(context);
        init();
    }

    public VolumeControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void setOnVolumeChangeListener(OnVolumeChangeListener listener){
        this.mListener=listener;
    }
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        point = new Point(0, 0);
        radius = DensityUtils.dp2px(getContext(), 6);
        bgLineStrokeWidth = 1;
        circleStorkeWidth = DensityUtils.dp2px(getContext(), 1.5f);
        fgLineStrokeWidth = DensityUtils.dp2px(getContext(), 3);
        range = DensityUtils.dp2px(getContext(), 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (point.x == 0) {
            point.set(getWidth() / 2, getHeight() / 2);
        }
        onDrawBg(canvas);
        onDrawFg(canvas);
        onDrawCircle(canvas);
    }

    private void onDrawBg(Canvas canvas) {
        paint.setStrokeWidth(bgLineStrokeWidth);
        paint.setColor(0xFF323232);
        if (point.x <= 2 * radius + circleStorkeWidth) {
            canvas.drawLine(point.x + radius + circleStorkeWidth / 2, getHeight() / 2, getWidth() - radius - circleStorkeWidth / 2, getHeight() / 2, paint);
        } else if (point.x >= getWidth() - 2 * radius - circleStorkeWidth) {
            canvas.drawLine(radius + circleStorkeWidth / 2, getHeight() / 2, point.x - radius - circleStorkeWidth / 2, getHeight() / 2, paint);
        } else {
            canvas.drawLine(radius + circleStorkeWidth / 2, getHeight() / 2, point.x - radius - circleStorkeWidth / 2, getHeight() / 2, paint);
            canvas.drawLine(point.x + radius + circleStorkeWidth / 2, getHeight() / 2, getWidth() - radius - circleStorkeWidth / 2, getHeight() / 2, paint);
        }

    }

    private void onDrawCircle(Canvas canvas) {
        if (point.x == getWidth() / 2) {
            paint.setColor(0xFFFFFFFF);
        } else {
            paint.setColor(0xFFFDD915);
        }
        paint.setStrokeWidth(circleStorkeWidth);
        canvas.drawCircle(point.x, point.y, radius, paint);
    }

    private void onDrawFg(Canvas canvas) {
        paint.setStrokeWidth(fgLineStrokeWidth);
        paint.setColor(0xFFFDD915);
        if (point.x < getWidth() / 2 - radius - circleStorkeWidth / 2) {
            canvas.drawLine(getWidth() / 2, getHeight() / 2, point.x + radius + circleStorkeWidth / 2, getHeight() / 2, paint);
        } else if (point.x > getWidth() / 2 + radius + circleStorkeWidth / 2) {
            canvas.drawLine(getWidth() / 2, getHeight() / 2, point.x - radius - circleStorkeWidth / 2, getHeight() / 2, paint);
        }

    }

    float rawX;
    float accOffset;//记录滑动距离的累加(粘性效果)

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float curRawX = event.getRawX();
                float offset = curRawX - rawX;
                if (point.x == getWidth() / 2) {
                    accOffset += offset;
                    if (Math.abs(accOffset) > range*3) {
                        point.x += accOffset/3;
                        accOffset = 0;
                        invalidate();
                        if(mListener!=null){
                            float rat=(float)(point.x-radius-circleStorkeWidth/2)/(getWidth()-radius*2-circleStorkeWidth);
                            mListener.onVolumeChange(rat);
                        }
                    }
                } else {
                    if (point.x + offset + radius + circleStorkeWidth / 2 > getWidth()) {
                        point.x = getWidth() - radius - circleStorkeWidth / 2;
                    } else if (point.x + offset - radius - circleStorkeWidth / 2 < 0) {
                        point.x = radius + circleStorkeWidth / 2;
                    } else if (point.x > getWidth() / 2 - range && point.x < getWidth() / 2 && curRawX > rawX) {
                        point.x = getWidth() / 2;
                    } else if (point.x < getWidth() / 2 + range && point.x > getWidth() / 2 && curRawX < rawX) {
                        point.x = getWidth() / 2;
                    } else {
                        point.x += offset;
                    }
                    rawX = curRawX;
                    invalidate();
                    if(mListener!=null){
                        float rat=(float)(point.x-radius-circleStorkeWidth/2)/(getWidth()-radius*2-circleStorkeWidth);
                        mListener.onVolumeChange(rat);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
    public interface OnVolumeChangeListener{
        /**
         *
         * @param rat 0.5 时左右均衡
         */
        void onVolumeChange(float rat);
    }
}
