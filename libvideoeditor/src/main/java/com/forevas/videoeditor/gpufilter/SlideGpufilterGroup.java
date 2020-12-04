package com.forevas.videoeditor.gpufilter;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.forevas.videoeditor.VideoEditorSDK;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.filter.EasyGlUtils;
import com.forevas.videoeditor.gpufilter.basefilter.GPUImageFilter;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterFactory;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;

/**
 *Created by carden
 */

public class SlideGpufilterGroup {
    private MagicFilterType[]types= Constants.types;
    private GPUImageFilter curfilter;
    private GPUImageFilter filterL;
    private GPUImageFilter filterR;
    private int width,height;
    private int[]fFrame=new int[1];
    private int[]fTexture=new int[1];
    private int curIndex=0;
    private Scroller scroller;
    private OnFilterChangeListener mListener;

    public SlideGpufilterGroup(){
        initFilter();
        scroller=new Scroller(VideoEditorSDK.getInstance().getContext());
    }
    public void initFilter(){
        curfilter= getFilter(getCurIndex());
        filterL=getFilter(getLeftIndex());
        filterR=getFilter(getRightIndex());
    }
    public GPUImageFilter getFilter(int index){
        GPUImageFilter filter = MagicFilterFactory.initFilters(types[index]);
        if(filter==null){
            filter=new GPUImageFilter();
        }
        return filter;
    }
    public void setFilterType(MagicFilterType type){
        destory();
        for(int i=0;i<types.length;i++){
            if(type==types[i]){
                curIndex=i;
                break;
            }
        }
        initFilter();
        init();
        onFilterSizeChanged(width,height);
    }
    public void init(){
        curfilter.init();
        filterL.init();
        filterR.init();
    }
    public void onSizeChanged(int width,int height){
        this.width=width;
        this.height=height;
        GLES20.glGenFramebuffers(1, fFrame, 0);
        EasyGlUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height);
        onFilterSizeChanged(width,height);
    }
    private void onFilterSizeChanged(int width,int height){
        curfilter.onInputSizeChanged(width,height);
        filterL.onInputSizeChanged(width,height);
        filterR.onInputSizeChanged(width,height);
        curfilter.onDisplaySizeChanged(width,height);
        filterL.onDisplaySizeChanged(width,height);
        filterR.onDisplaySizeChanged(width,height);
    }
    public int getOutputTexture(){
        return fTexture[0];
    }
    public void onDrawFrame(int textureId){
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0]);
        if(direction==0&&offset==0){
            curfilter.onDrawFrame(textureId);
        }else if(direction==1){
            onDrawSlideLeft(textureId);
        }else if(direction==-1){
            onDrawSlideRight(textureId);
        }
        EasyGlUtils.unBindFrameBuffer();
    }
    private void onDrawSlideLeft(int textureId){
        if(locked&&scroller.computeScrollOffset()){
            offset=scroller.getCurrX();
            drawSlideLeft(textureId);
        }else{
            drawSlideLeft(textureId);
            if(locked){
                if(needSwitch){
                    reCreateRightFilter();
                    if(mListener!=null){
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset=0;
                direction=0;
                locked=false;
            }
        }
    }
    private void onDrawSlideRight(int textureId){
        if(locked&&scroller.computeScrollOffset()){
            offset=scroller.getCurrX();
            drawSlideRight(textureId);
        }else{
            drawSlideRight(textureId);
            if(locked){
                if(needSwitch){
                    reCreateLeftFilter();
                    if(mListener!=null){
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset=0;
                direction=0;
                locked=false;
            }
        }
    }
    private void drawSlideLeft(int textureId){
        GLES20.glViewport(0,0,width,height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0,0,offset,height);
        filterL.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0,0,width,height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(offset,0,width-offset,height);
        curfilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }
    private void drawSlideRight(int textureId){
        GLES20.glViewport(0,0,width,height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0,0,width-offset,height);
        curfilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0,0,width,height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(width-offset,0,offset,height);
        filterR.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }
    private void reCreateRightFilter(){
        decreaseCurIndex();
        filterR.destroy();
        filterR=curfilter;
        curfilter=filterL;
        filterL=getFilter(getLeftIndex());
        filterL.init();
        filterL.onDisplaySizeChanged(width,height);
        filterL.onInputSizeChanged(width,height);
        needSwitch=false;
    }
    private void reCreateLeftFilter(){
        increaseCurIndex();
        filterL.destroy();
        filterL=curfilter;
        curfilter=filterR;
        filterR=getFilter(getRightIndex());
        filterR.init();
        filterR.onDisplaySizeChanged(width,height);
        filterR.onInputSizeChanged(width,height);
        needSwitch=false;
    }
    public void destory(){
        curfilter.destroy();
        filterL.destroy();
        filterR.destroy();
    }
    private int getLeftIndex(){
        int leftIndex=curIndex-1;
        if(leftIndex<0){
            leftIndex=types.length-1;
        }
        return leftIndex;
    }
    private int getRightIndex(){
        int rightIndex=curIndex+1;
        if(rightIndex>=types.length){
            rightIndex=0;
        }
        return rightIndex;
    }
    private int getCurIndex(){
        return curIndex;
    }
    private void increaseCurIndex(){
        curIndex++;
        if(curIndex>=types.length){
            curIndex=0;
        }
    }
    private void decreaseCurIndex(){
        curIndex--;
        if(curIndex<0){
            curIndex=types.length-1;
        }
    }
    int downX;
    int direction;//0为静止,-1为向左滑,1为向右滑
    int offset;
    boolean locked;
    boolean needSwitch;
    public void onTouchEvent(MotionEvent event){
        if(locked){
            return;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX= (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if(downX==-1){
                    return;
                }
                int curX= (int) event.getX();
                if(curX>downX){
                    direction=1;
                }else{
                    direction=-1;
                }
                offset=Math.abs(curX-downX);
                break;
            case MotionEvent.ACTION_UP:
                if(downX==-1){
                    return;
                }
                if(offset==0){
                    return;
                }
                locked=true;
                downX=-1;
                if(offset> Constants.screenWidth/3){
                    scroller.startScroll(offset,0,Constants.screenWidth-offset,0,100*(1-offset/Constants.screenWidth));
                    needSwitch=true;
                }else{
                    scroller.startScroll(offset,0,-offset,0,100*(offset/Constants.screenWidth));
                    needSwitch=false;
                }
                break;
        }
    }

    public void setOnFilterChangeListener(OnFilterChangeListener listener){
        this.mListener=listener;
    }

    public interface OnFilterChangeListener{
        void onFilterChange(MagicFilterType type);
    }
}
