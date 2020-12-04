package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.utils.DensityUtils;


/**
 * Description:
 */
public class CircularProgressView extends AppCompatImageView {
    public static final int MODE_SEG=0;//分段模式
    public static final int MODE_FREE=1;//自由模式
    private int recordMode=MODE_SEG;
    private int mBorderStroke=3;
    private int mTextSize=14;
    private int mProcess=0;
    private int mTotal=100;
    private int mBorderColor=0xFFFFFFFF;
    private int mCenterColor=0xFFFDD915;
//    private int mBorderColor=0x007799;
//    private int mCenterColor=0xCC00CC;
    private int mStartAngle=-90;
    private RectF mRectF,mRectF1;

    private Paint mCenterPaint;
    private Paint mBorderPaint;
    private Paint mTextPaint;

    private Drawable mDrawable;
    private Bitmap confirm;
    private boolean isConfirmMode;

    public CircularProgressView(Context context) {
        this(context,null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mCenterPaint=new Paint();
        mCenterPaint.setColor(mCenterColor);
        mCenterPaint.setStrokeWidth(1);
        mCenterPaint.setStyle(Paint.Style.FILL);
        mCenterPaint.setAntiAlias(true);

        mBorderStroke= DensityUtils.dp2px(getContext(),mBorderStroke);
        mBorderPaint=new Paint();
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderStroke);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);

        mTextPaint=new Paint();
        mTextPaint.setColor(mBorderColor);
        mTextPaint.setTextSize(DensityUtils.dp2px(getContext(),mTextSize));
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mDrawable=new Progress();
        setImageDrawable(mDrawable);
        confirm= BitmapFactory.decodeResource(getResources(),R.mipmap.editor_record_confirm_center);
    }

    /**
     * 设置总时长
     * @param total 毫秒级
     */
    public void setTotal(int total){
        this.mTotal=total;
        post(new Runnable() {
            @Override
            public void run() {
                mDrawable.invalidateSelf();
            }
        });

    }

    /**
     * 设置当前进度
     * @param process 毫秒级
     */
    public void setProcess(int process){
        this.mProcess=process;
        post(new Runnable() {
            @Override
            public void run() {
                mDrawable.invalidateSelf();
            }
        });
    }

    public int getProcess(){
        return mProcess;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getLayoutParams().width== ViewGroup.LayoutParams.WRAP_CONTENT){
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }else{
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    public void finishProcess() {
        mProcess=0;
        post(new Runnable() {
            @Override
            public void run() {
                mDrawable.invalidateSelf();
            }
        });
    }

    /**
     * 显示拍摄完成的标志
     * @param isConfirmMode
     */
    public void confirmMode(boolean isConfirmMode){
        this.isConfirmMode=isConfirmMode;
        post(new Runnable() {
            @Override
            public void run() {
                mDrawable.invalidateSelf();
            }
        });
    }
    public void setRecordMode(int recordMode){
        this.recordMode=recordMode;
        post(new Runnable() {
            @Override
            public void run() {
                mDrawable.invalidateSelf();
            }
        });
    }
    public int getRecordMode(){
        return recordMode;
    }
    private class Progress extends Drawable {
        @Override
        public void draw(Canvas canvas) {
            if(recordMode==MODE_SEG){
                drawModeSeg(canvas);
            }else if(recordMode==MODE_FREE){
                drawModeFree(canvas);
            }

        }
        private void drawModeSeg(Canvas canvas){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int width=getWidth();
            int pd=mBorderStroke/2+1;
            if(mRectF==null){
                mRectF=new RectF(pd,pd,width-pd,width-pd);
            }
            if(mProcess==0){
                canvas.drawCircle(width/2,width/2,width/2-pd,mCenterPaint);
                canvas.drawArc(mRectF,mStartAngle,360,false,mBorderPaint);
                if(isConfirmMode){
                    Rect srcR=new Rect(0,0,confirm.getWidth(),confirm.getHeight());
                    Rect destR=new Rect(getWidth()/4,getHeight()/4,getWidth()*3/4,getHeight()*3/4);
                    canvas.drawBitmap(confirm,srcR,destR,null);
                }
            }else{
                canvas.drawArc(mRectF,mStartAngle,mProcess*360/(float)mTotal,false,mBorderPaint);
                float  sec= (float) (mTotal-mProcess)/1000;
                String format = String.format("%.1f", sec);
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float top = fontMetrics.top;
                float bottom = fontMetrics.bottom;
                canvas.drawText(format,getWidth()/2,(getHeight()-top-bottom)/2,mTextPaint);
            }
        }
        private void drawModeFree(Canvas canvas){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int width=getWidth();
            int pd=mBorderStroke/2+1;
            if(mRectF==null){
                mRectF=new RectF(pd,pd,width-pd,width-pd);
            }
            if(mProcess==0){
                canvas.drawCircle(width/2,width/2,width/2-pd,mCenterPaint);
                canvas.drawArc(mRectF,mStartAngle,360,false,mBorderPaint);
                if(isConfirmMode){
                    Rect srcR=new Rect(0,0,confirm.getWidth(),confirm.getHeight());
                    Rect destR=new Rect(getWidth()/4,getHeight()/4,getWidth()*3/4,getHeight()*3/4);
                    canvas.drawBitmap(confirm,srcR,destR,null);
                }
            }else{
                canvas.drawArc(mRectF,mStartAngle,mProcess*360/(float)mTotal,false,mBorderPaint);
                if(mRectF1==null){
                    mRectF1=new RectF(width/3,width/3,width*2/3,width*2/3);
                }
                canvas.drawRoundRect(mRectF1,10,10,mCenterPaint);
            }
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

}
