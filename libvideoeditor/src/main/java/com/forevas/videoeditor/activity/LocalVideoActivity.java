package com.forevas.videoeditor.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.codec.VideoCliper;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.gpufilter.helper.FilterTypeHelper;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;
import com.forevas.videoeditor.media.MediaPlayerWrapper;
import com.forevas.videoeditor.media.VideoInfo;
import com.forevas.videoeditor.widget.FilterNameView;
import com.forevas.videoeditor.widget.LoadingView;
import com.forevas.videoeditor.widget.RecordConfigPop;
import com.forevas.videoeditor.widget.VideoClipView;
import com.forevas.videoeditor.widget.VideoPreviewView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * Created by carden
 */

public class LocalVideoActivity extends Activity implements View.OnClickListener, View.OnTouchListener, SlideGpufilterGroup.OnFilterChangeListener, VideoClipView.OnStartPointChangeListener, MediaPlayerWrapper.IMediaCallback {
    ImageView ivClose, ivBack, ivConfirm;
    VideoPreviewView videoView;
    FilterNameView filterName;
    VideoClipView clipView;
    LoadingView loadingView;
    MagicFilterType type=MagicFilterType.NONE;
    int curMode = Constants.MODE_POR_9_16;
    int curDur = RecordConfigPop.DUR_1;//秒级
    int curSeg = RecordConfigPop.SEG_2;
    int maxTime;//毫秒级
    int startPoint;
    int clipDur = Integer.MAX_VALUE;
    String path;
    String outputPath;
    private boolean resumed;
    static final int VIDEO_PREPARE = 0;
    static final int VIDEO_START = 1;
    static final int VIDEO_UPDATE = 2;
    static final int VIDEO_PAUSE = 3;
    static final int VIDEO_CUT_FINISH=4;
    boolean isPlaying = false;
    boolean isDestory = false;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_PREPARE:
                    Executors.newSingleThreadExecutor().execute(update);
                    break;
                case VIDEO_START:
                    isPlaying = true;
                    break;
                case VIDEO_UPDATE:
                    int curDuration = videoView.getCurDuration();
                    if (curDuration > startPoint + clipDur) {
                        videoView.seekTo(startPoint);
                        videoView.start();
                    }
                    break;
                case VIDEO_PAUSE:
                    isPlaying = false;
                    break;
                case VIDEO_CUT_FINISH:
                    loadingView.hideLoading();
                    ((FrameLayout)getWindow().getDecorView()).removeView(loadingView);
                    Intent intent=new Intent(LocalVideoActivity.this,RecordActivity.class);
                    intent.putExtra("from","preview");
                    intent.putExtra("outputPath",outputPath);
                    intent.putExtra("clipDur",clipDur);
                    intent.putExtra("filter", getResources().getText(FilterTypeHelper.FilterType2Name(type)).toString());
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity_local_video);
        initView();
        initData();
    }

    private void initView() {
        ivClose = (ImageView) findViewById(R.id.iv_close);
        videoView = (VideoPreviewView) findViewById(R.id.videoView);
        filterName = (FilterNameView) findViewById(R.id.filter_name);
        clipView = (VideoClipView) findViewById(R.id.video_clip);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivConfirm = (ImageView) findViewById(R.id.iv_confirm);
        loadingView=new LoadingView(this);
        loadingView.hideLoading();
        videoView.setOnFilterChangeListener(this);
        ivClose.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivConfirm.setOnClickListener(this);
        videoView.setOnTouchListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        curMode = intent.getIntExtra("curMode", Constants.MODE_POR_9_16);
        curDur = intent.getIntExtra("curDur", 10);
        curSeg = intent.getIntExtra("curSeg", 2);
        maxTime = intent.getIntExtra("maxTime", 5);
        path = intent.getStringExtra("path");

        filterName.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        RelativeLayout.LayoutParams filterParams = (RelativeLayout.LayoutParams) filterName.getLayoutParams();
        switch (curMode) {
            case Constants.MODE_POR_9_16:
                layoutParams.height = Constants.mode_por_height_9_16;
                filterParams.topMargin = Constants.screenHeight / 3 - filterName.getMeasuredHeight() / 2;
                break;
            case Constants.MODE_POR_1_1:
                layoutParams.height = Constants.mode_por_height_1_1;
                layoutParams.topMargin = (Constants.screenHeight - Constants.mode_por_height_1_1) / 2;
                filterParams.topMargin = (Constants.screenHeight - filterName.getMeasuredHeight()) / 2;
                break;
            case Constants.MODE_POR_16_9:
                layoutParams.height = Constants.mode_por_height_16_9;
                layoutParams.topMargin = (Constants.screenHeight - Constants.mode_por_height_16_9) / 2;
                filterParams.topMargin = (Constants.screenHeight - filterName.getMeasuredHeight()) / 2;
                break;
        }
        videoView.setLayoutParams(layoutParams);
        filterName.setLayoutParams(filterParams);

        ArrayList<String> srcList = new ArrayList<>();
        srcList.add(path);
        videoView.setVideoSrc(srcList);
        videoView.setIMediaCallback(this);
        clipView.setMode(curSeg == -1 ? VideoClipView.MODE_FREE : VideoClipView.MODE_SEG);
//        clipView.setMode(curSeg == -1 ? VideoClipView.MODE_SEG : VideoClipView.MODE_FREE);
        clipView.setClipDur(maxTime);
        clipView.setVideoPath(path);
        clipView.setOnStartPointChangeListener(this);
        clipDur=maxTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumed) {
            videoView.start();
        }
        resumed = true;
    }

    @Override
    public void onBackPressed() {
        if(!loadingView.isLoading()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        isDestory = true;
        videoView.onDestory();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close || id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_confirm) {
            try {
                if(loadingView.isLoading()){
                    return;
                }
                videoView.pause();
                loadingView.showLoading();
                ((FrameLayout)getWindow().getDecorView()).addView(loadingView);
                VideoCliper cliper=new VideoCliper();
                cliper.setInputVideoPath(path);
                outputPath=Constants.getPath("video/clip/",System.currentTimeMillis()+"");
                cliper.setOutputVideoPath(outputPath);
                cliper.setScreenMode(curMode);
                cliper.setFilterType(type);
                cliper.setOnVideoCutFinishListener(new VideoCliper.OnVideoCutFinishListener() {
                    @Override
                    public void onFinish() {
                        mHandler.sendEmptyMessage(VIDEO_CUT_FINISH);
                    }
                });
                cliper.clipVideo(startPoint*1000,clipDur*1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        videoView.onTouch(event);
        return true;
    }

    @Override
    public void onFilterChange(final MagicFilterType type) {
        this.type=type;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filterName.setFilterType(type);
            }
        });
    }

    @Override
    public void onStartPointChange(int startPoint) {
        if (this.startPoint != startPoint) {
            this.startPoint = startPoint;
            videoView.seekTo(startPoint);
            videoView.start();
        }
    }

    @Override
    public void onClipDurChange(int clipDur) {
        this.clipDur = clipDur;
    }

    @Override
    public void onVideoPrepare() {
        mHandler.sendEmptyMessage(VIDEO_PREPARE);
    }

    @Override
    public void onVideoStart() {
        mHandler.sendEmptyMessage(VIDEO_START);
    }

    @Override
    public void onVideoPause() {
        mHandler.sendEmptyMessage(VIDEO_PAUSE);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        videoView.seekTo(startPoint);
        videoView.start();
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            while (!isDestory) {
                if (!isPlaying) {
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                mHandler.sendEmptyMessage(VIDEO_UPDATE);
                try {
                    Thread.currentThread().sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
