package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.Vector;

/**
 * Created by carden
 */

public class VideoClipBgView extends View {
    Bitmap bufferBitmap;
    Canvas bufferCanvas;
    Paint grayPaint;
    String videoPath;
    Thread frameThread;
    FrameLayout.LayoutParams layoutParams;
    Vector<Bitmap> frameList=new Vector<>();
    int frameCount;
    Rect srcRect,destRect;
    int offset;
    boolean destoryed;
    public VideoClipBgView(Context context) {
        super(context);
    }

    public VideoClipBgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public void setVideoPath(String videoPath) {
        this.videoPath=videoPath;
        layoutParams= (FrameLayout.LayoutParams) getLayoutParams();
        frameCount=layoutParams.width/layoutParams.height+1;//加1防止view不能完全填充的问题
        init();
        frameThread=new Thread(frameRunnable);
        frameThread.start();
    }
    private void init() {
        bufferBitmap = Bitmap.createBitmap(layoutParams.width,layoutParams.height, Bitmap.Config.ARGB_8888);
        bufferCanvas = new Canvas(bufferBitmap);
        srcRect=new Rect(0,0,bufferBitmap.getWidth(),bufferBitmap.getHeight());
        destRect=new Rect(0,0,layoutParams.width,layoutParams.height);

        grayPaint=new Paint();
        grayPaint.setStrokeWidth(1);
        grayPaint.setAntiAlias(true);
        grayPaint.setStyle(Paint.Style.FILL);
        grayPaint.setColor(0x3f000000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        preDraw();
        canvas.drawBitmap(bufferBitmap,srcRect,destRect,null);
    }
    private void preDraw(){
        while(frameList.size()>0){
            Bitmap bitmap = frameList.remove(0);
            Rect srcR=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
            Rect destR=new Rect(offset,0,offset+layoutParams.height,layoutParams.height);
            bufferCanvas.drawBitmap(bitmap,srcR,destR,null);
            bufferCanvas.drawRect(destR,grayPaint);
            offset+=layoutParams.height;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destoryed=true;
    }

    private Runnable frameRunnable=new Runnable() {
        @Override
        public void run() {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            String duration=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int videoDur=Integer.parseInt(duration);
            int perStepDur=videoDur/frameCount;
            for(int i=0;i<frameCount&&!destoryed;i++){
                Bitmap frame = retriever.getFrameAtTime(i * perStepDur * 1000);

                int frameWidth=frame.getWidth();
                int frameHeight=frame.getHeight();

                if(frameWidth<frameHeight){
                    float scaleWidth = ((float) layoutParams.height) / frameWidth;
                    float scaleHeight = ((float) layoutParams.height) / frameWidth;
                    // 取得想要缩放的matrix参数
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap square = Bitmap.createBitmap(frame, 0, (frameHeight - frameWidth) / 2, frameWidth, frameWidth,matrix,true);
                    frameList.add(square);
                }else{
                    float scaleWidth = ((float) layoutParams.height) / frameHeight;
                    float scaleHeight = ((float) layoutParams.height) / frameHeight;
                    // 取得想要缩放的matrix参数
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap square = Bitmap.createBitmap(frame, (frameWidth-frameHeight)/2, 0, frameHeight, frameHeight,matrix,true);
                    frameList.add(square);
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
                frame.recycle();
            }
        }
    };
}
