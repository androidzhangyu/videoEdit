package com.forevas.videoeditor.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.bean.AudioSettingInfo;
import com.forevas.videoeditor.bean.EditConfigBean;
import com.forevas.videoeditor.bean.Song;
import com.forevas.videoeditor.codec.MediaMuxerRunnable;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.event.EditorFinishEvent;
import com.forevas.videoeditor.media.MediaPlayerWrapper;
import com.forevas.videoeditor.media.VideoInfo;
import com.forevas.videoeditor.widget.BGMPop;
import com.forevas.videoeditor.widget.LoadingView;
import com.forevas.videoeditor.widget.VideoEditView;
import com.forevas.videoeditor.widget.VolumeControlView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * Created by carden
 */

public class EditActivity extends Activity implements View.OnClickListener, PopupWindow.OnDismissListener, BGMPop.OnBgmSelectedListener, VolumeControlView.OnVolumeChangeListener, MediaPlayerWrapper.IMediaCallback {
    private LinearLayout llTopContainer;
    private FrameLayout flBottomContainer;
    private FrameLayout flTitle;
    private ImageView ivClose, ivBgm, ivBack;
    private VideoEditView videoView;
    private BGMPop mPop;
    private View confirm;
    private LoadingView loadingView;
    private int curMode;
    private List<String> pathList;
    private String outputPath;
    private AudioSettingInfo audioSetting;
    private boolean resumed;
    private MediaPlayer bgmPlayer;
    public static final int BGM_PLAYING = 0;
    public static final int BGM_PAUSING = 0;
    private int bgmStatus;
    private Song curSong;
    private EditConfigBean configBean;
    private String videoFormat;
    private long endTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity_edit);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    private void initView() {
        ivClose = (ImageView) findViewById(R.id.iv_close);
        ivBgm = (ImageView) findViewById(R.id.iv_bgm);
        videoView = (VideoEditView) findViewById(R.id.videoView);
        llTopContainer = (LinearLayout) findViewById(R.id.ll_top_container);
        flBottomContainer = (FrameLayout) findViewById(R.id.fl_bottom_container);
        flTitle = (FrameLayout) findViewById(R.id.fl_title);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        confirm = findViewById(R.id.confirm);

        mPop = new BGMPop(this);
        mPop.setOnDismissListener(this);
        mPop.setOnBgmSelectedListener(this);
        mPop.setOnVolumeChangeListener(this);

        loadingView = new LoadingView(this);
        loadingView.hideLoading();

        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.height = Constants.screenWidth;
        videoView.setLayoutParams(layoutParams);

        videoView.setIMediaCallback(this);

        ivClose.setOnClickListener(this);
        ivBgm.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        pathList = intent.getStringArrayListExtra("path");
        curMode = intent.getIntExtra("curMode", Constants.MODE_POR_9_16);
        videoFormat=intent.getStringExtra("videoFormat");
        endTime=intent.getLongExtra("time",endTime);
        videoView.setVideoSrc(pathList);
        audioSetting = new AudioSettingInfo();
        audioSetting.volFirst = 1;
        audioSetting.volSecond = 1;
        bgmPlayer = new MediaPlayer();
        bgmPlayer.setLooping(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumed) {
            videoView.start();
        }
        if (bgmStatus == BGM_PAUSING) {
            bgmPlayer.start();
            bgmStatus = BGM_PLAYING;
        }
        resumed = true;
    }

    @Override
    public void onBackPressed() {
        if (!loadingView.isLoading()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
        if (bgmStatus == BGM_PLAYING) {
            bgmPlayer.pause();
            bgmStatus = BGM_PAUSING;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        videoView.onDestory();
        mPop.onDestroy();
        bgmPlayer.release();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EditorFinishEvent event) {
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close) {
            finish();
        } else if (id == R.id.iv_bgm) {
            try {
                startTopContainerAnim(true);
                mPop.show((ViewGroup) getWindow().getDecorView());
            }catch (Exception e){
                e.printStackTrace();
            }

        } else if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.confirm) {
            if(loadingView.isLoading()){
                return;
            }
            if(curSong==null&&pathList.size()==1){//不加背景音乐,不需要编解码
                if(outputPath==null||(configBean!=null&&configBean.song!=null)){
                    loadingView.showLoading();
                    ((FrameLayout) getWindow().getDecorView()).addView(loadingView);
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            File file=Constants.createExternalDir("/happyvideo/video/record/");
                            File outFile= new File(file, "VID" + "_"
                                    + new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
                                    + "_"+ SystemClock.currentThreadTimeMillis() + ".mp4");
                            outputPath = outFile.getAbsolutePath();
                            try {
                                FileInputStream inputStream=new FileInputStream(pathList.get(0));
                                FileOutputStream outputStream=new FileOutputStream(outputPath);
                                byte[] buffer=new byte[8*1024];
                                int size;
                                while((size=inputStream.read(buffer))!=-1){
                                    outputStream.write(buffer,0,size);
                                }
                                inputStream.close();
                                outputStream.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingView.hideLoading();
                                        ((FrameLayout) getWindow().getDecorView()).removeView(loadingView);
                                        startPublishActivity();
                                        if(configBean!=null){
                                            configBean.song=null;
                                            configBean.settingInfo=null;
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    startPublishActivity();
                }
                return;
            }
            if(configBean!=null&&configBean.equals(curSong,audioSetting)){//配置没有什么变化,也不用重新编解码
                startPublishActivity();
                return;
            }
            videoView.pause();
            if (bgmStatus == BGM_PLAYING) {
                bgmPlayer.pause();
                bgmStatus = BGM_PAUSING;
            }
            loadingView.showLoading();
            ((FrameLayout) getWindow().getDecorView()).addView(loadingView);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
//                    outputPath = Constants.getPath("video/output/", System.currentTimeMillis() + ".mp4");
                    try {
                        File file=Constants.createExternalDir("/happyvideo/video/record/");
                        File outFile= new File(file, "VID" + "_"
                                + new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
                                + "_"+ SystemClock.currentThreadTimeMillis() + ".mp4");
                        outputPath = outFile.getAbsolutePath();
                        final long startTime = System.currentTimeMillis();
                        MediaMuxerRunnable instance = new MediaMuxerRunnable();
                        instance.setVideoInfo(videoView.getVideoInfo(), outputPath);
                        instance.setCurMode(curMode);
                        if (curSong != null) {
                            instance.setAudioSetting(audioSetting);
                        }
                        instance.addMuxerListener(new MediaMuxerRunnable.MuxerListener() {
                            @Override
                            public void onStart() {
//                            Log.e("hero", "===muxer  onStart====");
                            }

                            @Override
                            public void onFinish() {
//                            Log.e("hero", "===muxer  onFinish====");
                                long endTime = System.currentTimeMillis();
//                            Log.e("timee", "---视频编辑消耗的时间===" + (endTime - startTime));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingView.hideLoading();
                                        ((FrameLayout) getWindow().getDecorView()).removeView(loadingView);
                                        startPublishActivity();
                                        configBean=new EditConfigBean();
                                        configBean.setSong(curSong);
                                        configBean.setAudioSettingInfo(audioSetting);
                                    }
                                });
                            }
                        });
                        instance.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    private void startPublishActivity(){
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("path", outputPath);
        intent.putExtra("curMode", curMode);
        intent.putExtra("timeVideo",endTime);
        startActivity(intent);
        finish();
//        EventBus.getDefault().post(new EditorFinishEvent(outputPath,endTime*1000));
//        finish();
    }

    /**
     * @param flag true向上偏移 false向下偏移
     */
    private void startTopContainerAnim(final boolean flag) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fra = animation.getAnimatedFraction();
                fra = flag ? fra : 1 - fra;
                llTopContainer.scrollTo(0, (int) (fra * flTitle.getHeight()));
                flBottomContainer.setAlpha(1 - fra);
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onDismiss() {
        startTopContainerAnim(false);
    }

    @Override
    public void onBgmSelected(Song song) {
        if (song == null) {
            bgmPlayer.reset();
            curSong = null;
            return;
        }
        if ((song.location == 0 && TextUtils.isEmpty(song.localPath)) || song.location == 1 || song.equals(curSong)) {
            return;
        }

        curSong = song;
        audioSetting.filePath = song.getLocalPath();
        try {
            bgmPlayer.reset();
            bgmPlayer.setDataSource(song.getLocalPath());
            bgmPlayer.prepare();
            bgmPlayer.start();
            bgmStatus = BGM_PLAYING;
            videoView.seekTo(0);
            videoView.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVolumeChange(float rat) {
        if (rat <= 0.5f) {
            float v = rat / 0.5f;
            videoView.setVolume(v);
            audioSetting.volFirst = v;
        }
        if (rat >= 0.5f) {
            float v = (1 - rat) / 0.5f;
            bgmPlayer.setVolume(v, v);
            audioSetting.volSecond = v;
        }
    }

    @Override
    public void onVideoPrepare() {

    }

    @Override
    public void onVideoStart() {

    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (bgmStatus == BGM_PLAYING) {
            bgmPlayer.seekTo(0);
        }
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }
}
