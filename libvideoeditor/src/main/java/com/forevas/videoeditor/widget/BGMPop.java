package com.forevas.videoeditor.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.adapter.BgmAdapter;
import com.forevas.videoeditor.bean.Song;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.db.bean.BGMStatus;
import com.forevas.videoeditor.db.dao.BGMStatusDao;
import com.forevas.videoeditor.event.FileDownloadEvent;
import com.forevas.videoeditor.event.FileProgressEvent;
import com.forevas.videoeditor.service.DownLoadService;
import com.forevas.videoeditor.utils.DensityUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carden.
 */

public class BGMPop implements View.OnClickListener, ViewPager.OnPageChangeListener, BgmAdapter.OnItemSelectListener{
    public static final int MODE_LOCAL = 0;
    public static final int MODE_ONLINE = 1;
    Context mContext;
    LayoutInflater mInflater;
    PopupWindow mPop;
    ImageView ivConfirm;
    ImageView ivBack;
    FrameLayout flContent;
    LinearLayout llFirstLayer;
    FrameLayout flSecondLayer;
    LinearLayout llLocal, llOnline;
    ViewPager vpContent;
    BgmAdapter mLocalAdapter, mOnlineAdapter;
    LinearLayout llDotContainer;

    RelativeLayout rlVolumeContainer;
    VolumeControlView volumeControlView;
    TextView tvAlert;
    ImageView ivSongIcon;
    TextView tvSong, tvAuthor;
    ImageView ivDelete, ivSubmit;

    List<Song> localList;
    List<Song> onlineList;
    int curMode;//0 local 1 online
    int curIndex;
    Song curSong;
    Handler handler = new Handler(Looper.getMainLooper());
    OnBgmSelectedListener mListener;
    Animation rotation;
    BGMStatusDao bgmStatusDao;


    public BGMPop(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    public void setOnBgmSelectedListener(OnBgmSelectedListener listener) {
        mListener = listener;
    }

    private void initView() {
        View content = mInflater.inflate(R.layout.editor_pop_bgm, null);
        ivConfirm = (ImageView) content.findViewById(R.id.iv_confirm);
        ivBack = (ImageView) content.findViewById(R.id.iv_back);
        flContent = (FrameLayout) content.findViewById(R.id.fl_content);
        llFirstLayer = (LinearLayout) content.findViewById(R.id.ll_first_layer);
        flSecondLayer = (FrameLayout) content.findViewById(R.id.fl_secord_layer);
        llLocal = (LinearLayout) content.findViewById(R.id.ll_local);
        llOnline = (LinearLayout) content.findViewById(R.id.ll_online);
        vpContent = (ViewPager) content.findViewById(R.id.vp_content);
        llDotContainer = (LinearLayout) content.findViewById(R.id.ll_dot_container);

        rlVolumeContainer = (RelativeLayout) content.findViewById(R.id.rl_volume_container);
        volumeControlView = (VolumeControlView) content.findViewById(R.id.volume_control);
        ivSongIcon = (ImageView) content.findViewById(R.id.iv_cd_icon);
        tvSong = (TextView) content.findViewById(R.id.tv_song);
        tvAuthor = (TextView) content.findViewById(R.id.tv_author);
        tvAlert = (TextView) content.findViewById(R.id.tv_alert);
        ivDelete = (ImageView) content.findViewById(R.id.iv_delete);
        ivSubmit = (ImageView) content.findViewById(R.id.iv_submit);

        vpContent.addOnPageChangeListener(this);

        ivBack.setOnClickListener(this);
        llLocal.setOnClickListener(this);
        llOnline.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivSubmit.setOnClickListener(this);


        mPop = new PopupWindow(Constants.screenWidth, Constants.screenHeight - Constants.screenWidth);
        mPop.setAnimationStyle(R.style.editor_bgm_pop);
        mPop.setFocusable(true);
        mPop.setOutsideTouchable(true);
        mPop.setBackgroundDrawable(new BitmapDrawable());
        mPop.setContentView(content);
    }

    private void initData() {
        rotation = AnimationUtils.loadAnimation(mContext, R.anim.editor_bgm_cdicon_rotation);
        initOnlineData();
        initLocalData();
    }

    private void initLocalData() {
        localList = new ArrayList<>();
//        Log.e("hero", "--begin read audio data");
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=? or " + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/mpeg", "audio/x-ms-wma", "audio/mp4a-latm"}, null);
        if (cursor.moveToFirst()) {
            Song song;
            do {
                song = new Song();
                song.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                // 文件名
                song.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                // 歌曲名
//                song.setTitle(cursor.getString(2));
                // 时长
                song.setDuration(cursor.getInt(3));
                // 歌手名
                song.setAuthor(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

                // 歌曲格式
                /*if ("audio/mpeg".equals(cursor.getString(5).trim())) {
                    song.setType("mp3");
                } else if ("audio/x-ms-wma".equals(cursor.getString(5).trim())) {
                    song.setType("wma");
                } else if ("audio/mp4a-latm".equals(cursor.getString(5).trim())) {
                    song.setType("aac");
                }*/
                // 文件大小
                /*if (cursor.getString(6) != null) {
                    float size = cursor.getInt(6) / 1024f / 1024f;
                    song.setSize((size + "").substring(0, 4) + "M");
                } else {
                    song.setSize("未知");
                }*/
                // 文件路径
                if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) != null) {
                    song.setLocalPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                }
                song.setLocation(0);
                localList.add(song);
            } while (cursor.moveToNext());

            cursor.close();
        }
        mLocalAdapter = new BgmAdapter(mContext, localList);
        mLocalAdapter.setOnItemSelectListener(this);
    }

    private void initOnlineData() {
        try {
            onlineList = new ArrayList<>();
            bgmStatusDao = new BGMStatusDao(mContext.getApplicationContext());
            final List<Song> onlineSongs=new ArrayList<>();
            for (int i = 0; i < Constants.onlineSongsUrl.length; i++) {
                String url = Constants.onlineSongsUrl[i];
                Song song = new Song();
                song.url=url;
                song.name=Constants.onlineSongsName[i];
                song.author=Constants.onlineSongsAuthor[i];
                BGMStatus bgmStatus = bgmStatusDao.getBGMStatusByUrl(url);
                if (bgmStatus != null) {
                    if (!new File(bgmStatus.localPath).exists()) {
                        song.location = 1;
                        continue;
                    }
                    song.localPath = bgmStatus.localPath;
                    song.location = 0;
                } else {
                    song.location = 1;
                }
                onlineSongs.add(song);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onlineList.addAll(onlineSongs);
                    if (mPop.isShowing() && curMode == MODE_ONLINE) {
                        mOnlineAdapter.notifyDataSetChanged();
                    }
                }
            });

            mOnlineAdapter = new BgmAdapter(mContext, onlineList);
            mOnlineAdapter.setOnItemSelectListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initDot() {
        int count = 0;
        if (curMode == MODE_LOCAL) {
            count = localList.size() % BgmAdapter.PAGE_SIZE == 0 ? localList.size() / BgmAdapter.PAGE_SIZE : localList.size() / BgmAdapter.PAGE_SIZE + 1;
        } else if (curMode == MODE_ONLINE) {
            count = onlineList.size() % BgmAdapter.PAGE_SIZE == 0 ? onlineList.size() / BgmAdapter.PAGE_SIZE : onlineList.size() / BgmAdapter.PAGE_SIZE + 1;
        }
        llDotContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(mContext);
            dot.setImageResource(R.drawable.editor_dot_selector);
            if (i == 0) {
                dot.setSelected(true);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = DensityUtils.dp2px(mContext, 3);
            lp.rightMargin = DensityUtils.dp2px(mContext, 3);
            llDotContainer.addView(dot, lp);
        }
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        mPop.setOnDismissListener(onDismissListener);
    }

    public void show(ViewGroup viewGroup) {
        if (curSong != null) {
            rotation.reset();
            ivSongIcon.startAnimation(rotation);
        }
        mPop.showAtLocation(viewGroup, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.confirm) {
            mPop.dismiss();
        } else if (id == R.id.iv_back) {
            flSecondLayer.setVisibility(View.INVISIBLE);
            llFirstLayer.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.INVISIBLE);
            if (curSong != null) {
                ivDelete.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.ll_local) {
            flSecondLayer.setVisibility(View.VISIBLE);
            llFirstLayer.setVisibility(View.INVISIBLE);
            ivBack.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.INVISIBLE);
            vpContent.setAdapter(mLocalAdapter);
            curMode = 0;
            initDot();
        } else if (id == R.id.ll_online) {
            flSecondLayer.setVisibility(View.VISIBLE);
            llFirstLayer.setVisibility(View.INVISIBLE);
            ivBack.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.INVISIBLE);
            vpContent.setAdapter(mOnlineAdapter);
            curMode = 1;
            initDot();
        } else if (id == R.id.iv_delete) {
            curSong.setSelected(false);
            curSong = null;
            if (mListener != null) {
                mListener.onBgmSelected(curSong);
            }
            ivDelete.setVisibility(View.INVISIBLE);
            hideBgmVolumeControl();
        } else if (id == R.id.iv_submit) {
            mPop.dismiss();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < llDotContainer.getChildCount(); i++) {
            View dot = llDotContainer.getChildAt(i);
            if (i == position) {
                dot.setSelected(true);
            } else {
                dot.setSelected(false);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemSelect(int position) {
        if (curMode == MODE_LOCAL) {
            Song temp = localList.get(position);
            if (temp.equals(curSong)) {
                return;
            }
            if (curSong != null) {
                curSong.setSelected(false);
            }
            curMode = MODE_LOCAL;
            curIndex = position;
            curSong = temp;
            curSong.setSelected(true);
            mLocalAdapter.notifyPager();
        } else if (curMode == MODE_ONLINE) {
            Song temp = onlineList.get(position);
            if (temp.equals(curSong)) {
                return;
            }
            if (temp.location == 1) {
                if (temp.status == 1) {
                    return;
                }
                String baseFolder = Constants.getBaseFolder();
                String dir = baseFolder + "video/bgm/";
                File fdir = new File(dir);
                if (!fdir.exists()) {
                    fdir.mkdirs();
                }
                String file = "bgm" + System.currentTimeMillis();
                DownLoadService.downloadFile(temp.url, dir, file);
                curMode = MODE_ONLINE;
                curIndex = position;
                if (curSong != null) {
                    curSong.setSelected(false);
                }
                temp.setSelected(true);
                curSong = temp;
                mOnlineAdapter.notifyPager();
                if (mListener != null) {
                    mListener.onBgmSelected(null);
                }
                return;
            }
            if (curSong != null) {
                curSong.setSelected(false);
            }
            curMode = MODE_ONLINE;
            curIndex = position;
            curSong = temp;
            curSong.setSelected(true);
            mOnlineAdapter.notifyPager();
        }
        if (mListener != null) {
            mListener.onBgmSelected(curSong);
            showBgmVolumeControl(curSong);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FileDownloadEvent event) {
        String url = event.getUrl();
        for (int i = 0; i < onlineList.size(); i++) {
            Song song = onlineList.get(i);
            if (url.equals(song.url)) {
                song.status = 2;
                song.location = 0;
                song.localPath = event.getDestPath() + event.getDestFileName();
                BGMStatus bgm = new BGMStatus();
                bgm.id = song.id;
                bgm.url = song.url;
                bgm.localPath = song.localPath;
                bgm.progress = 100;
                bgm.status = 2;
                bgmStatusDao.replaceBGMStatus(bgm);
                if (curMode == MODE_ONLINE && curIndex == i) {
                    song.setSelected(true);
                    if (mListener != null) {
                        mListener.onBgmSelected(song);
                        showBgmVolumeControl(curSong);
                    }
                }
                mOnlineAdapter.notifyPager();
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileProgressEvent event) {
        String url = event.getUrl();
        for (int i = 0; i < onlineList.size(); i++) {
            Song song = onlineList.get(i);
            if (url.equals(song.url)) {
                song.status = 1;
                song.totalSize = event.getTotalSize();
                if (Math.abs(event.getProgress() - song.progress) > 5) {
                    song.progress = event.getProgress();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mOnlineAdapter.notifyPager();
                        }
                    });
                }
                break;
            }
        }
    }

    private void showBgmVolumeControl(Song song) {
        tvSong.setText(song.getName());
        tvAuthor.setText(song.getAuthor());
        rotation.reset();
        ivSongIcon.startAnimation(rotation);
        tvAlert.setVisibility(View.INVISIBLE);
        rlVolumeContainer.setVisibility(View.VISIBLE);
    }

    private void hideBgmVolumeControl() {
        rotation.reset();
        rlVolumeContainer.setVisibility(View.INVISIBLE);
        tvAlert.setVisibility(View.VISIBLE);
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
        rotation.cancel();
    }

    public void setOnVolumeChangeListener(VolumeControlView.OnVolumeChangeListener onVolumeChangeListener) {
        volumeControlView.setOnVolumeChangeListener(onVolumeChangeListener);
    }

    public interface OnBgmSelectedListener {
        void onBgmSelected(Song song);
    }
}
