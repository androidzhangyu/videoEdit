package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.forevas.videoeditor.camera.AiyaCamera;
import com.forevas.videoeditor.camera.IAiyaCamera;
import com.forevas.videoeditor.filter.AFilter;
import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;
import com.forevas.videoeditor.renderer.CameraDrawer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Description:
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener {



    private AiyaCamera mCamera;

    private CameraDrawer mDrawer;

    private boolean isSetParm=false;
    private int dataWidth=0,dataHeight=0;

    private int cameraId;
    private OnSurfaceSizeChangeListener mListener;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void setOnSurfaceSizeChangeListener(OnSurfaceSizeChangeListener listener){
        this.mListener=listener;
    }
    private void init(){
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        setCameraDistance(100);//相机距离
        mDrawer=new CameraDrawer(getResources());
        mCamera=new AiyaCamera();
        IAiyaCamera.Config mConfig=new IAiyaCamera.Config();
        mConfig.minPreviewWidth=720;
        mConfig.minPictureWidth=720;
        mConfig.rate=1.778f;
        mCamera.setConfig(mConfig);
    }

    /**
     * 每次Activity onResume时被调用,第一次不会打开相机
     */
    @Override
    public void onResume() {
        super.onResume();
        if(isSetParm){
            open(cameraId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.close();
    }

    /**
     * 相机初始化操作
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mDrawer.onSurfaceCreated(gl, config);
        if(!isSetParm){
            try {
                open(cameraId);
                stickerInit();
            }catch (Exception e){
                e.printStackTrace();
                Log.e("CameraView-->",e.getMessage());
            }

        }
        mDrawer.setPreviewSize(dataWidth,dataHeight);
    }

    private void open(final int cameraId){
        mCamera.close();
        mDrawer.setCameraId(cameraId);
        mCamera.open(cameraId);

        final Point previewSize=mCamera.getPreviewSize();
        dataWidth=previewSize.x;
        dataHeight=previewSize.y;
        SurfaceTexture texture = mDrawer.getTexture();
        texture.setOnFrameAvailableListener(this);
        mCamera.setPreviewTexture(texture);
        mCamera.preview();
    }

    public void switchCamera(){
        mDrawer.switchCamera();
        //0 back 1 font
        cameraId=cameraId==0?1:0;
        open(cameraId);
    }
    public int getCameraId(){
        return cameraId;
    }
    public void setCameraListener(AiyaCamera.CameraListener listener){
        mCamera.setCameraListener(listener);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawer.onSurfaceChanged(gl,width,height);
        if(mListener!=null){
            mListener.onSurfaceSizeChange();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(isSetParm){
            mDrawer.onDrawFrame(gl);
        }
    }

    public void onDestroy(){
        setPreserveEGLContextOnPause(false);
        onPause();
    }

    public void startRecord(){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.startRecord();
            }
        });
    }

    public void stopRecord(){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.stopRecord();
            }
        });
    }


    /**
     * 增加自定义滤镜
     * @param filter   自定义滤镜
     * @param isBeforeSticker 是否增加在贴纸之前
     */
    public void addFilter(AFilter filter, boolean isBeforeSticker){
        mDrawer.addFilter(filter,isBeforeSticker);
    }
    public void setGpuFilterType(final MagicFilterType filterType){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.setGpuFilter(filterType);
            }
        });
    }
    public void changeBeautyLevel(final int level){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.changeBeautyLevel(level);
            }
        });
    }
    public int getBeautyLevel(){
        return mDrawer.getBeautyLevel();
    }

    private void stickerInit(){
        if(!isSetParm&&dataWidth>0&&dataHeight>0) {
            isSetParm = true;
        }
    }

    public void setSavePath(String path) {
        mDrawer.setSavePath(path);
    }
    public void cancel() {
    }

    public void resume(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.onResume(auto);
            }
        });

    }

    public void pause(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.onPause(auto);
            }
        });

    }

    /**
     * 只有在摄像头打开的时候这里才会回调,onPause()之后停止回调
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        mCamera.onFocus(point,callback);
    }

    public AiyaCamera getCamera() {
        return mCamera;
    }

    public void setFlashMode(AiyaCamera.FlashMode mode) {
        mCamera.setFlashMode(mode);
    }
    public AiyaCamera.FlashMode getFlashMode(){
        return mCamera.getFlashMode();
    }
    public void setScreenMode(final int screenMode, final int width, final int height, final float offset, final float fraction){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDrawer.setScreenMode(screenMode,width,height,offset,fraction);
            }
        });
        requestRender();
    }
    public void preSetScreenMode(final int screenMode, final int width, final int height, final float offset, final float fraction){
        mDrawer.setScreenMode(screenMode,width,height,offset,fraction);
    }
    public int getScreenMode(){
        return mDrawer.getScreenMode();
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
    public interface OnSurfaceSizeChangeListener{
        void onSurfaceSizeChange();
    }
}
