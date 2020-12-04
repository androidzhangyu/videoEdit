package com.forevas.videoeditor.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.event.EditorFinishEvent;
import com.forevas.videoeditor.media.MediaPlayerWrapper;
import com.forevas.videoeditor.media.VideoInfo;
import com.forevas.videoeditor.widget.VideoPreviewView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

/**
 * 预览界面
 */
public class PreviewActivity extends Activity implements View.OnTouchListener, MediaPlayerWrapper.IMediaCallback, View.OnClickListener {
    VideoPreviewView videoView;
    int curMode = Constants.MODE_POR_9_16;
    String path;
    private ImageView imageViewClear, imageViewSure;
    private long endTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    private void initView() {
        videoView = (VideoPreviewView) findViewById(R.id.videoView);
        imageViewClear = (ImageView) findViewById(R.id.clear);
        imageViewSure = (ImageView) findViewById(R.id.sure);
        videoView.setOnTouchListener(this);
        imageViewSure.setOnClickListener(this);
        imageViewClear.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        curMode = intent.getIntExtra("curMode", Constants.MODE_POR_9_16);
        path = intent.getStringExtra("path");
        endTime = intent.getLongExtra("timeVideo", 0);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        switch (curMode) {
            case Constants.MODE_POR_9_16:
                layoutParams.height = Constants.mode_por_height_9_16;
                break;
            case Constants.MODE_POR_1_1:
                layoutParams.height = Constants.mode_por_height_1_1;
                layoutParams.topMargin = (Constants.screenHeight - Constants.mode_por_height_1_1) / 2;
                break;
            case Constants.MODE_POR_16_9:
                layoutParams.height = Constants.mode_por_height_16_9;
                layoutParams.topMargin = (Constants.screenHeight - Constants.mode_por_height_16_9) / 2;
                break;
        }
        videoView.setLayoutParams(layoutParams);
        ArrayList<String> srcList = new ArrayList<>();
        srcList.add(path);
        videoView.setVideoSrc(srcList);
        videoView.setIMediaCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView!=null){
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.onDestory();
        }
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
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
        videoView.seekTo(0);
        videoView.start();
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }

    @Override
    public void onClick(View v) {
        int ids = v.getId();
        if (ids == R.id.clear) {
            if (path != null) {
                File file = new File(path);
                if (file.exists() && !file.isDirectory()) {
                    file.delete();
                }
                startActivity(new Intent(PreviewActivity.this, RecordActivity.class));
                finish();
            }
        } else if (ids == R.id.sure) {
            EventBus.getDefault().post(new EditorFinishEvent(path, endTime * 1000));
            finish();

        }
    }

}
