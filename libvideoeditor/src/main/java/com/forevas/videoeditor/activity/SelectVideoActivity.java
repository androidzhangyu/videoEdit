package com.forevas.videoeditor.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.forevas.videoeditor.R;
import com.forevas.videoeditor.adapter.VideoAdapter;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.widget.RecordConfigPop;

import java.io.IOException;

/**
 * 选择视频
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SelectVideoActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, VideoAdapter.OnVideoSelectListener {
    ImageView ivClose;
    GridView gridview;
    VideoAdapter mMediaAdapter;
    public static final String PROJECT_VIDEO = MediaStore.MediaColumns._ID;
    int max_size = -1;//  最大size
    private int curMode = Constants.MODE_POR_9_16;
    private int curDur = RecordConfigPop.DUR_1;//秒级
    private int curSeg = RecordConfigPop.SEG_2;
    private int maxTime;//毫秒级
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity_select_video);
        initView();
        initData();
    }

    private void initView() {
        ivClose= (ImageView) findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridview=(GridView)findViewById(R.id.gridview_media_video);
    }

    private void initData() {
        Intent intent=getIntent();
        curMode=intent.getIntExtra("curMode", Constants.MODE_POR_9_16);
        curDur=intent.getIntExtra("curDur", 10);
        curSeg=intent.getIntExtra("curSeg", 2);
        maxTime=intent.getIntExtra("maxTime", 5);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String order = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        return new CursorLoader(getApplicationContext(), videoUri, new String[]{MediaStore.Video.Media.DATA, PROJECT_VIDEO}, null, null, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() <= 0) {
            return;
        }
        if (mMediaAdapter == null) {
            mMediaAdapter = new VideoAdapter(getApplicationContext(), data);
            mMediaAdapter.setMediaSelectVideoActivity(this);
            mMediaAdapter.setOnSelectChangedListener(this);
            mMediaAdapter.setMaxSize(max_size);
        } else {
            mMediaAdapter.swapCursor(data);
        }


        if (gridview.getAdapter() == null) {
            gridview.setAdapter(mMediaAdapter);
        }
        mMediaAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mMediaAdapter != null)
            mMediaAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(0);
        Glide.get(this).clearMemory();
        super.onDestroy();
    }

    @Override
    public void onSelect(String path,String cover) {
        int videoTrack=-1;
        int audioTrack=-1;
        MediaExtractor extractor=new MediaExtractor();
        try {
            extractor.setDataSource(path);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    videoTrack=i;
                    String videoMime = format.getString(MediaFormat.KEY_MIME);
                    if(!"video/avc".equals(videoMime)){
                        Toast.makeText(this,"Sorry,The video format is not supported",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continue;
                }
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioTrack=i;
                    String audioMime = format.getString(MediaFormat.KEY_MIME);
                    if(!"audio/mp4a-latm".equals(audioMime)){
                        Toast.makeText(this,"Sorry,The video format is not supported",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continue;
                }
            }
            extractor.release();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"Sorry,The video format is not supported",Toast.LENGTH_SHORT).show();
            extractor.release();
            return;
        }
        if(videoTrack==-1||audioTrack==-1){
            Toast.makeText(this,"Sorry,The video format is not supported",Toast.LENGTH_SHORT).show();
            return;
        }
        if(path!=null){
            Intent intent=new Intent(this,LocalVideoActivity.class);
            intent.putExtra("curMode",curMode);
            intent.putExtra("curDur",curDur);
            intent.putExtra("curSeg",curSeg);
            intent.putExtra("maxTime",maxTime);
            intent.putExtra("path",path);
            startActivity(intent);
        }
    }
}
