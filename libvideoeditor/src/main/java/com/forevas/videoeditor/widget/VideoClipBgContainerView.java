package com.forevas.videoeditor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;

/**
 *Created by carden
 */

public class VideoClipBgContainerView extends FrameLayout {
    VelocityTracker velocityTracker;
    Scroller mScroller;
    int bgLastX;
    int leftBorder;
    int rightBorder;
    OnBgScrollListener mListener;
    public VideoClipBgContainerView(Context context) {
        super(context);
    }

    public VideoClipBgContainerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollBorder(int leftBorder,int rightBorder){
        this.leftBorder=leftBorder;
        this.rightBorder=rightBorder;
    }
    public void setOnBgScrollListener(OnBgScrollListener listener){
        this.mListener=listener;
    }
    public void onTouchE(MotionEvent event){
        if(velocityTracker==null){
            velocityTracker = VelocityTracker.obtain();
        }
        if(mScroller==null){
            mScroller=new Scroller(getContext());
        }
        velocityTracker.addMovement(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                bgLastX= (int) event.getX();
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int curX= (int) event.getX();
                int offset = bgLastX-curX;
                if(getScrollX()+offset<leftBorder){
                    scrollTo(leftBorder,0);
                    if(mListener!=null){
                        mListener.onBgScroll(getScrollX(),getScrollY());
                    }
                }else if(getScrollX()+offset>rightBorder){
                    scrollTo(rightBorder,0);
                    if(mListener!=null){
                        mListener.onBgScroll(getScrollX(),getScrollY());
                    }
                }else{
                    scrollBy(offset,0);
                    if(mListener!=null){
                        mListener.onBgScroll(getScrollX(),getScrollY());
                    }
                }
                bgLastX=curX;
                break;
            case MotionEvent.ACTION_UP:
                bgLastX=0;
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = -velocityTracker.getXVelocity();
                mScroller.fling(getScrollX(),0,(int) xVelocity,0,0,rightBorder,0,0);
                invalidate();
                break;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller==null){
            return;
        }
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),0);
            if(mListener!=null){
                mListener.onBgScroll(getScrollX(),getScrollY());
            }
            invalidate();
        }
    }
    public interface OnBgScrollListener{
        void onBgScroll(int scrollX, int ScrollY);
    }
}
