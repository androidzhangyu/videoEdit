package com.forevas.videoeditor.renderer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.VideoEditorSDK;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.encoder.video.TextureMovieEncoder;
import com.forevas.videoeditor.filter.AFilter;
import com.forevas.videoeditor.filter.AiyaFilter;
import com.forevas.videoeditor.filter.EasyGlUtils;
import com.forevas.videoeditor.filter.GroupFilter;
import com.forevas.videoeditor.filter.MatrixUtils;
import com.forevas.videoeditor.filter.NoFilter;
import com.forevas.videoeditor.filter.ProcessFilter;
import com.forevas.videoeditor.filter.WaterMarkFilter;
import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.gpufilter.filters.MagicBeautyFilter;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;
import com.forevas.videoeditor.utils.BitMapUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * Description:
 * VUE:1072*592,1072*1072,592*1072
 */
public class CameraDrawer implements GLSurfaceView.Renderer {
    public static final int SCREEN_MODE_FULL = 0;
    public static final int SCREEN_MODE_SQUARE = 1;
    public static final int SCREEN_MODE_RECTANGLE = 2;
    public static final int SCREEN_MODE_THIN = 3;

    private int screenMode = 0;

    private float[] EM = new float[16];     //用于更改大小的变换矩阵
    private float[] SM = new float[16];     //用于显示的变换矩阵
    private float[] OM;     //用于后台绘制的变换矩阵

    private int width, height;//view的宽高
    private SurfaceTexture mSurfaceTexture;
    private AFilter mThinFilter;
    private AFilter mShowFilter;        //用于显示到界面上
    private int recordWidth,recordHeight;

    private AFilter mPreFilter;         //数据准备的Filter,track和保存
    private AFilter mProcessFilter;
    private GroupFilter mBeFilter;
    private GroupFilter mAfFilter;
    private SlideGpufilterGroup mSlideFilter;
    private MagicBeautyFilter mBeautyFilter;

    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[3];

    private int mPreviewWidth = 0, mPreviewHeight = 0;//预览数据的宽高

    private TextureMovieEncoder videoEncoder;
    private boolean recordingEnabled;
    private int recordingStatus;
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final int RECORDING_PAUSE = 3;
    private static final int RECORDING_RESUME = 4;
    private static final int RECORDING_PAUSED = 5;
    private String savePath;
    private int showWidth,showHeight,xOffset,yOffset;//显示数据的宽高
    private boolean isSurfaceChanged;

    public CameraDrawer(Resources res) {
        mPreFilter = new AiyaFilter(res);//旋转相机操作
        mProcessFilter = new ProcessFilter(res);

        mShowFilter = new NoFilter(res);//当前显示的Filter
        mThinFilter=new NoFilter(res);

        mBeFilter = new GroupFilter(res);
        mAfFilter = new GroupFilter(res);
        //TODO WaterMaker
        //必须传入上下翻转的矩阵
        OM = MatrixUtils.getOriginalMatrix();

        MatrixUtils.flip(OM, false, true);//矩阵上下翻转
        mThinFilter.setMatrix(OM);
        mBeautyFilter = new MagicBeautyFilter();
        mShowFilter.setMatrix(OM);
        mSlideFilter=new SlideGpufilterGroup();
        recordingEnabled = false;
        WaterMarkFilter mWaterMarkFilter = new WaterMarkFilter(res);
//        mWaterMarkFilter.setWaterMark(BitMapUtils.drawable2Bitmap(VideoEditorSDK.getInstance().getContext().getDrawable(R.mipmap.editor_loading_01)));
        mWaterMarkFilter.setWaterMark(BitmapFactory.decodeResource(res,R.mipmap.watermark));
        mWaterMarkFilter.setPosition(30,30,0,0);
//        mWaterMarkFilter.setPosition(0,-70,0,0);
        mWaterMarkFilter.setMatrix(OM);
        mBeFilter.addFilter(mWaterMarkFilter);
    }

    public  int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//    public int px2dip(Context context, float pxValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (pxValue / scale + 0.5f);
//    }
//    public  int getScreenSize(int mSize,float density){
//return (int)(mSize/density);
//
//    }
//
//    public  int getScreenWidth(){
//        DisplayMetrics displayMetrics=new DisplayMetrics();
//        getWindowManger().getDefaultDisplay().getMetrics(displayMetrics);
//        return displayMetrics.widthPixels;
//
//    }
//    public  int getScreenHeight(){
//        DisplayMetrics displayMetrics=new DisplayMetrics();
//        getWindowManger().getDefaultDisplay().getMetrics(displayMetrics);
//        return displayMetrics.heightPixels;
//
//    }
//    public WindowManager getWindowManger(){
//
//        if (manager==null){
//            manager=(WindowManager) VideoEditorSDK.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);
//        }
//        return  manager;
//
//    }

    public void addFilter(AFilter filter, boolean beforeTrack) {
        filter.setMatrix(OM);//为了抵消颠倒的操作(抵消本身的颠倒操作)
        if (beforeTrack) {
            mBeFilter.addFilter(filter);
        } else {
            mAfFilter.addFilter(filter);
        }
    }

    public void setGpuFilter(MagicFilterType type) {
        mSlideFilter.setFilterType(type);
    }

    public void changeBeautyLevel(int level) {
        mBeautyFilter.setBeautyLevel(level);
    }

    public int getBeautyLevel() {
        return mBeautyFilter.getBeautyLevel();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int texture = createTextureID();
        mSurfaceTexture = new SurfaceTexture(texture);
        mPreFilter.create();
        mPreFilter.setTextureId(texture);//这里指定了PreFilter的TextureId,其它filter的TextureId在draw方法中才会指定
        mProcessFilter.create();
        mShowFilter.create();
        mThinFilter.create();
        mBeFilter.create();
        mAfFilter.create();
        mBeautyFilter.init();
        mSlideFilter.init();
        //TODO WaterMaker
//        mWaterMarkFilter.create();
        if (recordingEnabled) {
            recordingStatus = RECORDING_RESUMED;
        } else {
            recordingStatus = RECORDING_OFF;
        }
    }

    public SurfaceTexture getTexture() {
        return mSurfaceTexture;
    }

    /**
     * surfaceView的大小发生变化时回调
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(!isSurfaceChanged){
            this.width = width;
            this.height = height;
            setScreenMode(SCREEN_MODE_FULL,width,height,0,1);
            deleteFrameBuffer();
            GLES20.glGenFramebuffers(1, fFrame, 0);
            EasyGlUtils.genTexturesWithParameter(3, fTexture, 0, GLES20.GL_RGBA, width, height);
            mPreFilter.setSize(width, height);
            mBeFilter.setSize(width, height);
            mAfFilter.setSize(width, height);
            mProcessFilter.setSize(width, height);
            mBeautyFilter.onDisplaySizeChanged(width, height);
            mBeautyFilter.onInputSizeChanged(width, height);
            mSlideFilter.onSizeChanged(width,height);
            //warterMaker
//            mWaterMarkFilter.setPosition(60,60,width,height);
            isSurfaceChanged=true;
        }
    }

    /**
     * 这个是因为切换前后置摄像头的时候偶尔会出现一帧画面颠倒的情况,数据残留导致,需要跳过一帧
     */
    boolean switchCamera=false;
    int skipFrame;
    public void switchCamera() {
        switchCamera=true;
    }
    /**
     * 感觉没必要修改SurfaceView的大小,使用投影足矣
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();

        if(switchCamera){
            skipFrame++;
            if(skipFrame>1){
                skipFrame=0;
                switchCamera=false;
            }
            return;
        }
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0]);
        GLES20.glViewport(0, 0, width, height);
        mPreFilter.draw();
        EasyGlUtils.unBindFrameBuffer();

        mBeFilter.setTextureId(fTexture[0]);
        mBeFilter.draw();

//TODO WATERmkser
//        mWaterMarkFilter.draw();

        //美颜处理
        if (mBeautyFilter != null && mBeautyFilter.getBeautyLevel() != 0) {
            EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[1]);
            GLES20.glViewport(0, 0, width, height);
            mBeautyFilter.onDrawFrame(mBeFilter.getOutputTexture());
            EasyGlUtils.unBindFrameBuffer();
            mAfFilter.setTextureId(fTexture[1]);
        } else {
            mAfFilter.setTextureId(mBeFilter.getOutputTexture());
        }

        mAfFilter.draw();
        mSlideFilter.onDrawFrame(mAfFilter.getOutputTexture());
        handleEncoderStatus();
        if(screenMode==SCREEN_MODE_THIN&&afterSizeChanged){
            EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[2]);
            mThinFilter.setTextureId(mSlideFilter.getOutputTexture());
            GLES20.glViewport(0, (int) (height/10*thinFraction), width, height-(int) (height/10*thinFraction*2));
            mThinFilter.draw();
            EasyGlUtils.unBindFrameBuffer();

            //显示出刚才绘制的内容
            GLES20.glViewport(xOffset, yOffset, showWidth, showHeight);
            mShowFilter.setTextureId(fTexture[2]);
            mShowFilter.draw();
        }else{
            //显示出刚才绘制的内容
            GLES20.glViewport(xOffset, yOffset, showWidth, showHeight);
            mShowFilter.setTextureId(mSlideFilter.getOutputTexture());
            mShowFilter.draw();
        }
        handleFrame();
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void handleEncoderStatus(){
        if (recordingEnabled) {
            switch (recordingStatus) {
                case RECORDING_OFF:
                    videoEncoder = new TextureMovieEncoder();
                    videoEncoder.setPreviewSize(recordWidth, recordHeight);
//                    videoEncoder.setTextureBuffer(gLTextureBuffer);
//                    videoEncoder.setCubeBuffer(gLCubeBuffer);
                    videoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                            savePath, recordWidth, recordHeight,
                            3000000, EGL14.eglGetCurrentContext(),
                            null));
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    videoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    videoEncoder.resumeRecording();
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_PAUSED:
                case RECORDING_ON:
                    break;
                case RECORDING_PAUSE:
                    videoEncoder.pauseRecording();
                    recordingStatus = RECORDING_PAUSED;
                    break;
                case RECORDING_RESUME:
                    videoEncoder.resumeRecording();
                    recordingStatus = RECORDING_ON;
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        } else {
            switch (recordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                case RECORDING_PAUSE:
                case RECORDING_RESUME:
                case RECORDING_PAUSED:
                    videoEncoder.stopRecording();
                    recordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        }
    }
    private void handleFrame(){
        if (videoEncoder != null && recordingEnabled && recordingStatus == RECORDING_ON) {
            if(screenMode==SCREEN_MODE_THIN){
                videoEncoder.setTextureId(fTexture[2]);
            }else{
                videoEncoder.setTextureId(mSlideFilter.getOutputTexture());
            }
            videoEncoder.frameAvailable(mSurfaceTexture);
        }
    }
    /**
     * 设置当前相机捕获到的数据大小(eg:720*1280),onSurfaceCreated被调用
     * @param width
     * @param height
     */
    public void setPreviewSize(int width, int height) {
        if (this.mPreviewWidth != width || this.mPreviewHeight != height) {
            this.mPreviewWidth = width;
            this.mPreviewHeight = height;
        }
    }

    //根据摄像头设置纹理映射坐标
    public void setCameraId(int id) {
        mPreFilter.setFlag(id);
    }

    //创建显示摄像头原始数据的OES TEXTURE
    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }


    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(3, fTexture, 0);
    }




    public void startRecord() {
        recordingEnabled = true;
    }

    public void stopRecord() {
        recordingEnabled = false;
    }

    public void setSavePath(String path) {
        this.savePath = path;
    }


    public void onPause(boolean auto) {
        if (auto) {
            videoEncoder.pauseRecording();
            if (recordingStatus == RECORDING_ON) {
                recordingStatus = RECORDING_PAUSED;
            }
            return;
        }
        if (recordingStatus == RECORDING_ON) {
            recordingStatus = RECORDING_PAUSE;
        }

    }

    public void onResume(boolean auto) {
        if (auto) {
            if (recordingStatus == RECORDING_PAUSED) {
                recordingStatus = RECORDING_RESUME;
            }
            return;
        }
        if (recordingStatus == RECORDING_PAUSED) {
            recordingStatus = RECORDING_RESUME;
        }
    }

    public int getScreenMode() {
        return screenMode;
    }

    float thinFraction;//只有切换到最后一个画幅时有用
    boolean afterSizeChanged;
    public void setScreenMode(int screenMode,int width,int height,float offset,float fraction) {
        this.screenMode = screenMode;
        switch (screenMode){
            case CameraDrawer.SCREEN_MODE_FULL:
                recordWidth=Constants.mode_por_encode_width_9_16;
                recordHeight=Constants.mode_por_encode_height_9_16;
                break;
            case CameraDrawer.SCREEN_MODE_SQUARE:
                recordWidth=Constants.mode_por_encode_width_1_1;
                recordHeight=Constants.mode_por_encode_height_1_1;
                break;
            case CameraDrawer.SCREEN_MODE_RECTANGLE:
                recordWidth=Constants.mode_por_encode_width_16_9;
                recordHeight=Constants.mode_por_encode_height_16_9;
                break;
            case CameraDrawer.SCREEN_MODE_THIN:
                recordWidth=Constants.mode_por_encode_width_16_9;
                recordHeight=Constants.mode_por_encode_height_16_9;
                break;
        }
        if(screenMode!=SCREEN_MODE_THIN){
            this.showWidth=width;
            this.showHeight=height;
            this.xOffset=0;
            this.yOffset= (int) ((this.height-height)/2+this.height*offset);
        }else{
            if(height>Constants.screenWidth/16*9){
                this.showWidth=width;
                this.showHeight=height;
                this.xOffset=0;
                this.yOffset=(this.height-height)/2;
                afterSizeChanged=false;
            }else{
                int temp=Constants.screenWidth/16*9-height;
                float r = (float) temp / (Constants.screenWidth / 16 * 9 / 5);
                this.showWidth=Constants.screenWidth;
                this.showHeight=Constants.screenWidth/16*9;
                this.xOffset=0;
                this.yOffset=(this.height-Constants.screenWidth/16*9)/2;
                thinFraction=r;
                afterSizeChanged=true;
            }

        }

        MatrixUtils.getShowMatrixWithOffset(SM, mPreviewWidth, mPreviewHeight, width, height,offset*2);//这里offset要乘2,因为之前是将屏幕高度看作1,而矩阵变换将屏幕高度看作2
        mPreFilter.setMatrix(SM);
    }
    public void onTouch(MotionEvent event){
        mSlideFilter.onTouchEvent(event);
    }

    public void setOnFilterChangeListener(SlideGpufilterGroup.OnFilterChangeListener listener){
        mSlideFilter.setOnFilterChangeListener(listener);
    }
}
