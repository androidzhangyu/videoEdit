package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.media.MediaPlayerWrapper;
import com.forevas.videoeditor.media.VideoInfo;
import com.forevas.videoeditor.renderer.VideoClipDrawer;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by carden
 */

public class VideoPreviewView extends GLSurfaceView implements GLSurfaceView.Renderer,MediaPlayer.OnCompletionListener,MediaPlayerWrapper.IMediaCallback{
    private MediaPlayerWrapper mMediaPlayer;
    private VideoClipDrawer mDrawer;
    private MediaPlayerWrapper.IMediaCallback callback;
    public VideoPreviewView(Context context) {
        super(context,null);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setPreserveEGLContextOnPause(false);
        setCameraDistance(100);
        mDrawer=new VideoClipDrawer(getResources());

        mMediaPlayer=new MediaPlayerWrapper();
        mMediaPlayer.setOnCompletionListener(this);
    }
    public void setVideoSrc(List<String> path){
        mMediaPlayer.setDataSource(path);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mDrawer.onSurfaceCreated(gl,config);
        SurfaceTexture surfaceTexture = mDrawer.getSurfaceTexture();
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayer.setSurface(surface);
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        surface.release();
        mMediaPlayer.start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawer.onSurfaceChanged(gl,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mDrawer.onDrawFrame(gl);
    }

    public void onDestory(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
    }

    @Override
    public void onVideoPrepare() {
        if(callback!=null){
            callback.onVideoPrepare();
        }
    }

    @Override
    public void onVideoStart() {
        if(callback!=null){
            callback.onVideoStart();
        }
    }

    @Override
    public void onVideoPause() {
        if(callback!=null){
            callback.onVideoPause();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(callback!=null){
            callback.onCompletion(mp);
        }
    }

    @Override
    public void onVideoChanged(final VideoInfo info) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.onVideoChanged(info);
            }
        });
        if(callback!=null){
            callback.onVideoChanged(info);
        }
    }
    public void onTouch(final MotionEvent event){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.onTouch(event);
            }
        });
    }
    public void setOnFilterChangeListener(SlideGpufilterGroup.OnFilterChangeListener listener){
        mDrawer.setOnFilterChangeListener(listener);
    }
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }
    public void pause(){
        mMediaPlayer.pause();
    }
    public void start(){
        mMediaPlayer.start();
    }

    /**
     * this must be called after {@link #setVideoSrc(List)}
     * @return
     */
    public int getVideoDuration(){
        return mMediaPlayer.getVideoDuration();
    }

    public int getCurDuration(){
        return mMediaPlayer.getCurPosition();
    }
    public void seekTo(int time){
        mMediaPlayer.seekTo(time);
    }
    public List<VideoInfo> getVideoInfo(){
        return mMediaPlayer.getVideoInfo();
    }
    public void setIMediaCallback(MediaPlayerWrapper.IMediaCallback callback){
        this.callback=callback;
    }

}
