package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.utils.DensityUtils;

import static com.forevas.videoeditor.widget.VideoClipView.MIN_DUR;

/**
 * Created by carden
 */

public class VideoClipFgView extends View {
    Paint paint, centerPaint, textPaint;
    Bitmap icon_left, icon_right;
    Rect leftSrc, leftDest, rightSrc, rightDest, centerRect;
    int curMode = VideoClipView.MODE_SEG;
    int clipDur = 15 * 1000;//预先设定的裁剪时长
    int videoDur;
    int finalClipDur;//最终裁剪的视频时长
    int fgLastX;
    int touchMode = 0;//0 移动 1左拉伸 2右拉伸
    OnFgScrollListener mListener;

    public VideoClipFgView(Context context) {
        super(context);
        init();
    }

    public VideoClipFgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (centerPaint == null) {
            centerPaint = new Paint();
            centerPaint.setStrokeWidth(1);
            centerPaint.setAntiAlias(true);
            centerPaint.setStyle(Paint.Style.FILL);
            centerPaint.setColor(0x3f000000);
        }
        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setTextSize(DensityUtils.dp2px(getContext(), 12));
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
            textPaint.setColor(0xffffffff);
        }
        if (paint == null) {
            paint = new Paint();
            paint.setStrokeWidth(DensityUtils.dp2px(getContext(), 4));
            paint.setAntiAlias(true);
        }
        if (curMode == VideoClipView.MODE_FREE) {
            icon_left = BitmapFactory.decodeResource(getResources(), R.mipmap.editor_icon_video_clip_free_left);
            icon_right = BitmapFactory.decodeResource(getResources(), R.mipmap.editor_icon_video_clip_free_right);
            paint.setColor(0xffffc90e);
        } else {
            icon_left = BitmapFactory.decodeResource(getResources(), R.mipmap.editor_icon_video_clip_static_left);
            icon_right = BitmapFactory.decodeResource(getResources(), R.mipmap.editor_icon_video_clip_static_right);
            paint.setColor(Color.WHITE);
        }
    }

    public void setCurMode(int curMode) {
        this.curMode = curMode;
        init();
    }

    public void setClipDur(int clipDur) {
        this.clipDur = clipDur;
    }

    public void setOnFgScrollListener(OnFgScrollListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        leftSrc = new Rect(0, 0, icon_left.getWidth(), icon_left.getHeight());
        rightSrc = new Rect(0, 0, icon_right.getWidth(), icon_right.getHeight());
        leftDest = new Rect(0, 0, icon_left.getWidth(), getHeight());
        rightDest = new Rect(getWidth() - icon_left.getWidth(), 0, getWidth(), getHeight());
        centerRect = new Rect(icon_left.getWidth(), DensityUtils.dp2px(getContext(), 2), getWidth() - icon_right.getWidth(), getHeight() - DensityUtils.dp2px(getContext(), 2));

        canvas.drawBitmap(icon_left, leftSrc, leftDest, null);
        canvas.drawBitmap(icon_right, rightSrc, rightDest, null);
        canvas.drawLine(icon_left.getWidth(), 0, getWidth() - icon_right.getWidth(), 0, paint);
        canvas.drawLine(icon_left.getWidth(), getHeight(), getWidth() - icon_right.getWidth(), getHeight(), paint);
        canvas.drawRect(centerRect, centerPaint);

        float sec = 0;
        if (curMode == VideoClipView.MODE_SEG) {
            if (videoDur > clipDur) {
                sec = (float) clipDur / 1000;
            } else {
                sec = (float) videoDur / 1000;
            }
        } else if (curMode == VideoClipView.MODE_FREE) {
            FrameLayout parent = (FrameLayout) getParent();
            if (videoDur > clipDur) {
                sec = (float) clipDur / 1000 * getWidth() / parent.getWidth();
            } else {
                sec = (float) videoDur / 1000 * getWidth() / parent.getWidth();
            }
        }
        Log.e("--->Sli", sec + "");
        finalClipDur = (int) (sec * 1000);

        String format = String.format("%.1f", sec);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        canvas.drawText(format, getWidth() / 2, (getHeight() - top - bottom) / 2, textPaint);
    }

    public int getLeftBarWidth() {
        return icon_left.getWidth();
    }

    public int getRightBarWidth() {
        return icon_right.getWidth();
    }


    public void onTouchE(MotionEvent event) {
        FrameLayout parent = (FrameLayout) getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fgLastX = (int) event.getX();

                if (fgLastX < getLeftBarWidth() * 2) {
                    touchMode = 1;
                } else if (fgLastX > getWidth() - getRightBarWidth() * 2) {
                    touchMode = 2;
                } else {
                    touchMode = 0;
                }
                fgLastX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getRawX();
                int offset = curX - fgLastX;
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
                if (touchMode == 0) {
                    if (layoutParams.leftMargin + offset < 0) {
                        layoutParams.leftMargin = 0;
                    } else if (layoutParams.leftMargin + offset + layoutParams.width > parent.getWidth()) {
                        layoutParams.leftMargin = parent.getWidth() - layoutParams.width;
                    } else {
                        layoutParams.leftMargin += offset;
                    }
                } else if (touchMode == 1) {
                    if (layoutParams.leftMargin + offset < 0) {
                        layoutParams.width += layoutParams.leftMargin;
                        layoutParams.leftMargin = 0;
                    } else if (layoutParams.width - offset < MIN_DUR / clipDur * parent.getWidth()) {
                        layoutParams.leftMargin += (layoutParams.width - MIN_DUR / clipDur * parent.getWidth());
                        layoutParams.width = (int) (MIN_DUR / clipDur * parent.getWidth());
                    } else {
                        layoutParams.width -= offset;
                        layoutParams.leftMargin += offset;
                    }
                } else if (touchMode == 2) {
                    if (layoutParams.width + offset < MIN_DUR / clipDur * parent.getWidth()) {
                        layoutParams.width = (int) (MIN_DUR / clipDur * parent.getWidth());
                    } else if (layoutParams.leftMargin + layoutParams.width + offset > parent.getWidth()) {
                        layoutParams.width = parent.getWidth() - layoutParams.leftMargin;
                    } else {
                        layoutParams.width += offset;
                    }
                }
                setLayoutParams(layoutParams);
                if (mListener != null) {
                    mListener.onFgScroll(layoutParams.leftMargin);
                }
                fgLastX = curX;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
    }

    public void setVideoDur(int videoDur) {
        this.videoDur = videoDur;
    }

    public int getFinalClipDur() {
        return finalClipDur;
    }

    public interface OnFgScrollListener {
        void onFgScroll(int leftMargin);
    }
}
