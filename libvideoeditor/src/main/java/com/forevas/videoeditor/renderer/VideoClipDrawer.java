package com.forevas.videoeditor.renderer;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.forevas.videoeditor.filter.AFilter;
import com.forevas.videoeditor.filter.EasyGlUtils;
import com.forevas.videoeditor.filter.MatrixUtils;
import com.forevas.videoeditor.filter.NoFilter;
import com.forevas.videoeditor.filter.RotationOESFilter;
import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.media.VideoInfo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by carden
 * 对视频画幅进行裁剪(仿VUE的模式)
 */

public class VideoClipDrawer implements GLSurfaceView.Renderer {
    private float[] OM;     //用于后台绘制的变换矩阵
    private float[] SM = new float[16];     //用于显示的变换矩阵
    private int rotation;
    //控件的长宽
    private int viewWidth;
    private int viewHeight;
    SurfaceTexture surfaceTexture;
    private RotationOESFilter mPreFilter;
    private SlideGpufilterGroup mSlideFilter;
    AFilter mShow;

    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    public VideoClipDrawer(Resources res){
        mPreFilter = new RotationOESFilter(res);//旋转相机操作
        mShow=new NoFilter(res);
        //必须传入上下翻转的矩阵
        OM= MatrixUtils.getOriginalMatrix();
        MatrixUtils.flip(OM,false,true);//矩阵上下翻转
        mShow.setMatrix(OM);
        mSlideFilter=new SlideGpufilterGroup();
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int texture[]=new int[1];
        GLES20.glGenTextures(1,texture,0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        surfaceTexture = new SurfaceTexture(texture[0]);
        mPreFilter.create();
        mPreFilter.setTextureId(texture[0]);
        mShow.create();
        mSlideFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewWidth=width;
        viewHeight=height;
        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,fTexture,0, GLES20.GL_RGBA,viewWidth,viewHeight);
        mSlideFilter.onSizeChanged(width,height);

    }
    public void onVideoChanged(VideoInfo info){
        setRotation(info.rotation);
        if(info.rotation==0||info.rotation==180){
            MatrixUtils.getShowMatrix(SM,info.width,info.height,viewWidth,viewHeight);
        }else{
            MatrixUtils.getShowMatrix(SM,info.height,info.width,viewWidth,viewHeight);
        }

        mPreFilter.setMatrix(SM);
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        surfaceTexture.updateTexImage();
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
        GLES20.glViewport(0,0,viewWidth,viewHeight);
        mPreFilter.draw();
        EasyGlUtils.unBindFrameBuffer();

        mSlideFilter.onDrawFrame(fTexture[0]);

        GLES20.glViewport(0,0,viewWidth,viewHeight);
        mShow.setTextureId(mSlideFilter.getOutputTexture());
        mShow.draw();
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
    }

    public void setRotation(int rotation){
        this.rotation=rotation;
        if(mPreFilter!=null){
            mPreFilter.setRotation(this.rotation);
        }
    }
    public void onTouch(MotionEvent event){
        mSlideFilter.onTouchEvent(event);
    }

    public void setOnFilterChangeListener(SlideGpufilterGroup.OnFilterChangeListener listener){
        mSlideFilter.setOnFilterChangeListener(listener);
    }
}
