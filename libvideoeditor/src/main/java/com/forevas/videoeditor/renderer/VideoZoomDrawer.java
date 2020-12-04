package com.forevas.videoeditor.renderer;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.forevas.videoeditor.filter.AFilter;
import com.forevas.videoeditor.filter.EasyGlUtils;
import com.forevas.videoeditor.filter.MatrixUtils;
import com.forevas.videoeditor.filter.NoFilter;
import com.forevas.videoeditor.filter.RotationOESFilter;
import com.forevas.videoeditor.gpufilter.basefilter.GPUImageFilter;
import com.forevas.videoeditor.media.VideoInfo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by carden
 * 对视频进行等比例缩放(仿美拍的模式)
 */

public class VideoZoomDrawer implements GLSurfaceView.Renderer {
    private float[] OM;     //用于后台绘制的变换矩阵
    private int rotation;
    //控件的长宽
    private int viewWidth;
    private int viewHeight;
    //视频的长宽
    private int videoWidth;
    private int videoHeight;
    //第一个视频的长宽
    private int firstVideoRealWidth=-1;
    private int firstVideoRealHeight=-1;
    //投影的位置和大小
    private int x;
    private int y;
    private int width;
    private int height;
    SurfaceTexture surfaceTexture;
    private RotationOESFilter mPreFilter;
    private GPUImageFilter mGpuFilter;
    AFilter mShow;

    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[2];

    public VideoZoomDrawer(Resources res){
        mPreFilter = new RotationOESFilter(res);//旋转相机操作
        mShow=new NoFilter(res);
        //必须传入上下翻转的矩阵
        OM= MatrixUtils.getOriginalMatrix();
        MatrixUtils.flip(OM,false,true);//矩阵上下翻转
        mPreFilter.setMatrix(OM);
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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewWidth=width;
        viewHeight=height;
        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(2,fTexture,0, GLES20.GL_RGBA,viewWidth,viewHeight);

    }
    public void onVideoChanged(VideoInfo info){
        setRotation(info.rotation);
        setVideoWidthAndHeight(info.width,info.height);
        adjustVideoPosition();
    }
    public void setGpuFilter(GPUImageFilter filter){
        if(mGpuFilter!=null){
            mGpuFilter.destroy();
        }
        mGpuFilter=filter;
        if(filter!=null){
            mGpuFilter.init();
            mGpuFilter.onDisplaySizeChanged(videoWidth,videoHeight);
            mGpuFilter.onInputSizeChanged(videoWidth,videoHeight);
        }
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        surfaceTexture.updateTexImage();
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
        GLES20.glViewport(0,0,viewWidth,viewHeight);
        mPreFilter.draw();
        EasyGlUtils.unBindFrameBuffer();

        if(mGpuFilter!=null){
            EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[1]);
            GLES20.glViewport(0,0,viewWidth,viewHeight);
            mGpuFilter.onDrawFrame(fTexture[0]);
            EasyGlUtils.unBindFrameBuffer();
        }

        GLES20.glViewport(x,y,width,height);
        mShow.setTextureId(fTexture[mGpuFilter==null?0:1]);
        mShow.draw();
    }
    private void adjustVideoPosition(){
        if(firstVideoRealWidth==-1&&firstVideoRealHeight==-1){
            float w = (float) viewWidth / videoWidth;
            float h = (float)viewHeight / videoHeight;
            if(w<h){
                width=viewWidth;
                height= (int) ((float)videoHeight*w);
            }else{
                width= (int) ((float)videoWidth*h);
                height=viewHeight;
            }
            x=(viewWidth-width)/2;
            y=(viewHeight-height)/2;
            firstVideoRealWidth=width;
            firstVideoRealHeight=height;
        }else{
            float w = (float) firstVideoRealWidth / videoWidth;
            float h = (float)firstVideoRealHeight / videoHeight;
            if(w<h){
                width=firstVideoRealWidth;
                height= (int) ((float)videoHeight*w);
            }else{
                width= (int) ((float)videoWidth*h);
                height=firstVideoRealHeight;
            }
            x=(viewWidth-firstVideoRealWidth)/2+(firstVideoRealWidth-width)/2;
            y=(viewHeight-firstVideoRealHeight)/2+(firstVideoRealHeight-height)/2;
        }
    }
    public void setVideoWidthAndHeight(int videoWidth,int videoHeight){
        if(rotation==0||rotation==180){
            this.videoWidth=videoWidth;
            this.videoHeight=videoHeight;
        }else{
            this.videoWidth=videoHeight;
            this.videoHeight=videoWidth;
        }
    }
    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(2, fTexture, 0);
    }

    public void setRotation(int rotation){
        this.rotation=rotation;
        if(mPreFilter!=null){
            mPreFilter.setRotation(this.rotation);
        }
    }
}
