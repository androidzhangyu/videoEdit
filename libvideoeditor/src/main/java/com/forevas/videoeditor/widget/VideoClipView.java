package com.forevas.videoeditor.widget;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.utils.DensityUtils;

/**
 * Created by carden
 */

public class VideoClipView extends FrameLayout implements View.OnTouchListener, VideoClipBgContainerView.OnBgScrollListener, VideoClipFgView.OnFgScrollListener {
    public static final int MODE_SEG = 0;
    public static final int MODE_FREE = 1;
    public static final float MIN_DUR = 1000f;
    private int fgHeight = 40;//dp
    private int limitTimes = 10;//太长的视频背景限制最多为10倍
    private boolean upToLimit;
    private VideoClipBgContainerView bg_container;
    private FrameLayout fg_container;
    private VideoClipBgView clipBgView;
    private VideoClipFgView clipFgView;
    private TextView tvStart;
    private int curMode = MODE_SEG;
    private String videoPath;
    private int videoDur;
    private int clipDur;//毫秒级
    private int startPoint;//毫秒级
    private OnStartPointChangeListener mListener;

    public VideoClipView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoClipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View content = View.inflate(getContext(), R.layout.editor_video_clip_view, this);
        bg_container = (VideoClipBgContainerView) content.findViewById(R.id.bg_container);
        fg_container = (FrameLayout) content.findViewById(R.id.ll_fg_container);
        clipBgView = (VideoClipBgView) content.findViewById(R.id.clip_bg);
        clipFgView = (VideoClipFgView) content.findViewById(R.id.clip_fg);
        tvStart = (TextView) content.findViewById(R.id.tv_start);

        bg_container.setOnTouchListener(this);
        bg_container.setOnBgScrollListener(this);
    }

    public void setOnStartPointChangeListener(OnStartPointChangeListener listener) {
        this.mListener = listener;
    }

    public void setMode(int mode) {
        this.curMode = mode;
        clipFgView.setCurMode(mode);
        if (curMode == MODE_FREE) {
            clipFgView.setOnTouchListener(this);
            clipFgView.setOnFgScrollListener(this);
        }
    }

    public void setClipDur(int clipDur) {
        this.clipDur = clipDur;
        clipFgView.setClipDur(clipDur);
    }

    public void setVideoPath(String path) {
        this.videoPath = path;
        if (curMode == MODE_SEG) {
            initSegBg();
        } else if (curMode == MODE_FREE) {
            initFreeBg();
        }
    }

    /**
     * seg模式下fg一直处于屏幕的中间,不可滑动和拉伸,bg从fg的leftbar的右边开始
     */
    private void initSegBg() {
        LayoutParams clipFgParams = (LayoutParams) clipFgView.getLayoutParams();
        clipFgParams.width = Constants.screenWidth / 3;
        clipFgParams.height = DensityUtils.dp2px(getContext(), fgHeight);
        clipFgParams.leftMargin = Constants.screenWidth / 3 - DensityUtils.dp2px(getContext(), 10);
        clipFgView.setLayoutParams(clipFgParams);

        LayoutParams tvParams = (LayoutParams) tvStart.getLayoutParams();
        tvParams.leftMargin = clipFgParams.leftMargin;
        tvStart.setLayoutParams(tvParams);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        videoDur = Integer.parseInt(duration);
        clipFgView.setVideoDur(videoDur);
        LayoutParams clipBgParams = (LayoutParams) clipBgView.getLayoutParams();
        clipBgParams.height = DensityUtils.dp2px(getContext(), fgHeight - 5);
        clipBgParams.leftMargin = clipFgParams.leftMargin + clipFgView.getLeftBarWidth();
        if (videoDur <= clipDur) {
            clipBgParams.width = Constants.screenWidth / 3 - clipFgView.getLeftBarWidth() - clipFgView.getRightBarWidth();
        } else if (videoDur <= limitTimes * clipDur) {
            clipBgParams.width = (int) ((float) videoDur / clipDur * (Constants.screenWidth / 3 - clipFgView.getLeftBarWidth() - clipFgView.getRightBarWidth()));
        } else {
            upToLimit = true;
            clipBgParams.width = (Constants.screenWidth / 3 - clipFgView.getLeftBarWidth() - clipFgView.getRightBarWidth()) * limitTimes;
        }
        clipBgView.setLayoutParams(clipBgParams);
        clipBgView.setVideoPath(videoPath);
    }

    /**
     * free模式bg从最左边开始,fg宽度为parent的width的时候,选取时间最长
     */
    private void initFreeBg() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        videoDur = Integer.parseInt(duration);
        LayoutParams clipFgParams = (LayoutParams) clipFgView.getLayoutParams();
        LayoutParams clipBgparams = (LayoutParams) clipBgView.getLayoutParams();
        clipFgParams.height = DensityUtils.dp2px(getContext(), fgHeight);
        clipBgparams.height = DensityUtils.dp2px(getContext(), fgHeight - 5);
        if (videoDur <= clipDur) {
            clipFgParams.width = Constants.screenWidth - DensityUtils.dp2px(getContext(), 20);
            clipBgparams.width = Constants.screenWidth - DensityUtils.dp2px(getContext(), 20);
        } else if (videoDur <= limitTimes * clipDur) {
            clipFgParams.width = Constants.screenWidth / 3;
            clipBgparams.width = (int) ((float) videoDur / clipDur * (Constants.screenWidth - DensityUtils.dp2px(getContext(), 20)));
        } else {
            upToLimit = true;
            clipFgParams.width = Constants.screenWidth / 3;
            clipBgparams.width = (Constants.screenWidth - DensityUtils.dp2px(getContext(), 20)) * limitTimes;
        }
        clipFgView.setLayoutParams(clipFgParams);
        clipFgView.setVideoDur(videoDur);

        LayoutParams tvParams = (LayoutParams) tvStart.getLayoutParams();
        tvParams.leftMargin = clipFgParams.leftMargin;
        tvStart.setLayoutParams(tvParams);

        clipBgView.setLayoutParams(clipBgparams);
        clipBgView.setVideoPath(videoPath);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (curMode == MODE_SEG) {
            bg_container.setScrollBorder(0, clipBgView.getWidth() - (clipFgView.getWidth() - clipFgView.getRightBarWidth() - clipFgView.getLeftBarWidth()));
        } else if (curMode == MODE_FREE) {
            bg_container.setScrollBorder(0, clipBgView.getWidth() - bg_container.getWidth());
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.bg_container) {
            bg_container.onTouchE(event);
        } else if (id == R.id.clip_fg) {
            clipFgView.onTouchE(event);
        }
        return true;
    }

    @Override
    public void onBgScroll(int scrollX, int ScrollY) {
        if (curMode == MODE_SEG) {
            if (!upToLimit) {
                startPoint = (int) ((float) scrollX / clipBgView.getWidth() * videoDur);
            } else {
                startPoint = (int) ((float) scrollX / (clipBgView.getWidth() - (clipFgView.getWidth() - clipFgView.getRightBarWidth() - clipFgView.getLeftBarWidth())) * (videoDur - clipDur));
            }
        } else if (curMode == MODE_FREE) {
            if (!upToLimit) {
                startPoint = (int) (((float) scrollX + clipFgView.getLeft()) / clipBgView.getWidth() * videoDur);
            } else {
                if (scrollX + clipFgView.getLeft() <= clipBgView.getWidth() - fg_container.getWidth()) {
                    startPoint = (int) (((float) scrollX + clipFgView.getLeft()) / (clipBgView.getWidth() - fg_container.getWidth()) * (videoDur - clipDur));
                } else {
                    startPoint = (int) (videoDur - ((float) clipBgView.getWidth() - (scrollX + clipFgView.getLeft())) / fg_container.getWidth() * clipDur);
                }

            }
        }
        float sec = (float) startPoint / 1000;
        String format = String.format("%.1f", sec);
        tvStart.setText(format);
        if (mListener != null) {
            mListener.onStartPointChange(startPoint);
            mListener.onClipDurChange(clipFgView.getFinalClipDur());
        }
    }

    @Override
    public void onFgScroll(int leftMargin) {
        if (!upToLimit) {
            startPoint = (int) (((float) bg_container.getScrollX() + clipFgView.getLeft()) / clipBgView.getWidth() * videoDur);
        } else {
            if (bg_container.getScrollX() + clipFgView.getLeft() <= clipBgView.getWidth() - fg_container.getWidth()) {
                startPoint = (int) (((float) bg_container.getScrollX() + clipFgView.getLeft()) / (clipBgView.getWidth() - fg_container.getWidth()) * (videoDur - clipDur));
            }else{
                startPoint = (int) (videoDur - ((float) clipBgView.getWidth() - (bg_container.getScrollX() + clipFgView.getLeft())) / fg_container.getWidth() * clipDur);
            }
        }
        float sec = (float) startPoint / 1000;
        String format = String.format("%.1f", sec);
        LayoutParams params = (LayoutParams) tvStart.getLayoutParams();
        params.leftMargin = leftMargin;
        tvStart.setLayoutParams(params);
        tvStart.setText(format);
        if (mListener != null) {
            mListener.onStartPointChange(startPoint);
            mListener.onClipDurChange(clipFgView.getFinalClipDur());
        }
    }
    public int getClipVideoStartPoint(){
        return startPoint;
    }
    public int getClipVideoDur(){
        return clipFgView.getFinalClipDur();
    }
    public interface OnStartPointChangeListener {
        /**
         * 视频选择的起点 mills
         *
         * @param startPoint
         */
        void onStartPointChange(int startPoint);
        void onClipDurChange(int clipDur);
    }
}
