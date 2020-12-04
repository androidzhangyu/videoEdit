package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.constants.Constants;

/**
 * Created by carden
 */

public class RecordConfigPop implements View.OnClickListener {
    public static final int DUR_1=10;
    public static final int DUR_2=20;
    public static final int DUR_3=30;
    public static final int DUR_4=60;
    public static final int DUR_5=120;
    public static final int DUR_6=180;
    public static final int SEG_1=1;
    public static final int SEG_2=2;
    public static final int SEG_3=3;
    public static final int SEG_4=4;
    public static final int SEG_5=5;
    public static final int SEG_ANY=-1;
    ImageView icon9_16, icon1_1, icon16_9;
    TextView tvDurStatus;
    TextView tvDur1, tvDur2, tvDur3, tvDur4, tvDur5, tvDur6;
    TextView tvSegStatus;
    TextView tvSeg1, tvSeg2, tvSeg3, tvSeg4, tvSeg5, tvSegAny;
    RelativeLayout rlDurStatus, rlSegStatus;
    LinearLayout llDurContainer, llSegContainer;
    private PopupWindow mPop;
    private Context mContext;
    private LayoutInflater mInflater;
    private int curMode=Integer.MAX_VALUE;
    private int curDur=Integer.MAX_VALUE;
    private int curSeg=Integer.MAX_VALUE;
    private OnConfigChangeListener mListener;

    public RecordConfigPop(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        init();
    }

    private void init() {
        View contentView = mInflater.inflate(R.layout.editor_pop_record_config, null);
        icon9_16 = (ImageView) contentView.findViewById(R.id.icon_9_16);
        icon1_1 = (ImageView) contentView.findViewById(R.id.icon_1_1);
        icon16_9 = (ImageView) contentView.findViewById(R.id.icon_16_9);
        tvDurStatus = (TextView) contentView.findViewById(R.id.tv_dur_status);
        rlDurStatus = (RelativeLayout) contentView.findViewById(R.id.rl_dur_status);
        tvDur1 = (TextView) contentView.findViewById(R.id.tv_dur_1);
        tvDur2 = (TextView) contentView.findViewById(R.id.tv_dur_2);
        tvDur3 = (TextView) contentView.findViewById(R.id.tv_dur_3);
        tvDur4 = (TextView) contentView.findViewById(R.id.tv_dur_4);
        tvDur5 = (TextView) contentView.findViewById(R.id.tv_dur_5);
        tvDur6 = (TextView) contentView.findViewById(R.id.tv_dur_6);
        llDurContainer = (LinearLayout) contentView.findViewById(R.id.ll_dur_container);
        tvSegStatus = (TextView) contentView.findViewById(R.id.tv_seg_status);
        rlSegStatus = (RelativeLayout) contentView.findViewById(R.id.rl_seg_status);
        tvSeg1 = (TextView) contentView.findViewById(R.id.tv_seg_1);
        tvSeg2 = (TextView) contentView.findViewById(R.id.tv_seg_2);
        tvSeg3 = (TextView) contentView.findViewById(R.id.tv_seg_3);
        tvSeg4 = (TextView) contentView.findViewById(R.id.tv_seg_4);
        tvSeg5 = (TextView) contentView.findViewById(R.id.tv_seg_5);
        tvSegAny = (TextView) contentView.findViewById(R.id.tv_seg_any);
        llSegContainer = (LinearLayout) contentView.findViewById(R.id.ll_seg_container);

        icon9_16.setOnClickListener(this);
        icon1_1.setOnClickListener(this);
        icon16_9.setOnClickListener(this);
        rlDurStatus.setOnClickListener(this);
        tvDur1.setOnClickListener(this);
        tvDur2.setOnClickListener(this);
        tvDur3.setOnClickListener(this);
        tvDur4.setOnClickListener(this);
        tvDur5.setOnClickListener(this);
        tvDur6.setOnClickListener(this);
        llDurContainer.setOnClickListener(this);
        rlSegStatus.setOnClickListener(this);
        tvSeg1.setOnClickListener(this);
        tvSeg2.setOnClickListener(this);
        tvSeg3.setOnClickListener(this);
        tvSeg4.setOnClickListener(this);
        tvSeg5.setOnClickListener(this);
        tvSegAny.setOnClickListener(this);
        llSegContainer.setOnClickListener(this);

        mPop=new PopupWindow(Constants.screenWidth*4/5,ViewGroup.LayoutParams.WRAP_CONTENT);
        mPop.setFocusable(true);
        mPop.setOutsideTouchable(true);
        mPop.setBackgroundDrawable(new BitmapDrawable());
        mPop.setContentView(contentView);
    }
    public void show(ViewGroup viewGroup){
        mPop.showAtLocation(viewGroup, Gravity.CENTER,0,0);
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.icon_9_16) {
            changeScreenMode(Constants.MODE_POR_9_16);
        } else if (id == R.id.icon_1_1) {
            changeScreenMode(Constants.MODE_POR_1_1);
        } else if (id == R.id.icon_16_9) {
            changeScreenMode(Constants.MODE_POR_16_9);
        } else if (id == R.id.rl_dur_status) {
            showOrHideDurContainer(true);
            showOrHideSegContainer(false);
        } else if (id == R.id.tv_dur_1) {
            changeRecordDur(DUR_1);
        } else if (id == R.id.tv_dur_2) {
            changeRecordDur(DUR_2);
        } else if (id == R.id.tv_dur_3) {
            changeRecordDur(DUR_3);
        } else if (id == R.id.tv_dur_4) {
            changeRecordDur(DUR_4);
        } else if (id == R.id.tv_dur_5) {
            changeRecordDur(DUR_5);
        } else if (id == R.id.tv_dur_6) {
            changeRecordDur(DUR_6);
        } else if (id == R.id.ll_dur_container) {
            showOrHideDurContainer(false);
        } else if (id == R.id.rl_seg_status) {
            showOrHideSegContainer(true);
            showOrHideDurContainer(false);
        } else if (id == R.id.tv_seg_1) {
            changeSegCount(SEG_1);
        } else if (id == R.id.tv_seg_2) {
            changeSegCount(SEG_2);
        } else if (id == R.id.tv_seg_3) {
            changeSegCount(SEG_3);
        } else if (id == R.id.tv_seg_4) {
            changeSegCount(SEG_4);
        } else if (id == R.id.tv_seg_5) {
            changeSegCount(SEG_5);
        } else if (id == R.id.tv_seg_any) {
            changeSegCount(SEG_ANY);
        } else if (id == R.id.ll_seg_container) {
            showOrHideSegContainer(false);
        }
    }
    private void changeScreenMode(int mode){
        if(curMode!=mode){
            curMode=mode;
            switch (mode){
                case Constants.MODE_POR_9_16:
                    icon9_16.setSelected(true);
                    icon1_1.setSelected(false);
                    icon16_9.setSelected(false);
                    break;
                case Constants.MODE_POR_1_1:
                    icon9_16.setSelected(false);
                    icon1_1.setSelected(true);
                    icon16_9.setSelected(false);
                    break;
                case Constants.MODE_POR_16_9:
                    icon9_16.setSelected(false);
                    icon1_1.setSelected(false);
                    icon16_9.setSelected(true);
                    break;
            }
            if(mListener!=null){
                mListener.onSreenModeChange(curMode);
            }
        }
    }
    private void changeRecordDur(int dur){
        if(curDur!=dur){
            curDur=dur;
            switch (dur){
                case DUR_1:
                    tvDur1.setSelected(true);
                    tvDur2.setSelected(false);
                    tvDur3.setSelected(false);
                    tvDur4.setSelected(false);
                    tvDur5.setSelected(false);
                    tvDur6.setSelected(false);
                    break;
                case DUR_2:
                    tvDur1.setSelected(false);
                    tvDur2.setSelected(true);
                    tvDur3.setSelected(false);
                    tvDur4.setSelected(false);
                    tvDur5.setSelected(false);
                    tvDur6.setSelected(false);
                    break;
                case DUR_3:
                    tvDur1.setSelected(false);
                    tvDur2.setSelected(false);
                    tvDur3.setSelected(true);
                    tvDur4.setSelected(false);
                    tvDur5.setSelected(false);
                    tvDur6.setSelected(false);
                    break;
                case DUR_4:
                    tvDur1.setSelected(false);
                    tvDur2.setSelected(false);
                    tvDur3.setSelected(false);
                    tvDur4.setSelected(true);
                    tvDur5.setSelected(false);
                    tvDur6.setSelected(false);
                    break;
                case DUR_5:
                    tvDur1.setSelected(false);
                    tvDur2.setSelected(false);
                    tvDur3.setSelected(false);
                    tvDur4.setSelected(false);
                    tvDur5.setSelected(true);
                    tvDur6.setSelected(false);
                    break;
                case DUR_6:
                    tvDur1.setSelected(false);
                    tvDur2.setSelected(false);
                    tvDur3.setSelected(false);
                    tvDur4.setSelected(false);
                    tvDur5.setSelected(false);
                    tvDur6.setSelected(true);
                    break;
            }
            if(curDur<=60){
                tvDurStatus.setText(curDur+"s");
            }else{
                tvDurStatus.setText(curDur/60+"min");
            }

            showOrHideDurContainer(false);
            if(mListener!=null){
                mListener.onRecordDurChange(curDur);
            }
        }
    }
    private void changeSegCount(int seg){
        if(curSeg!=seg){
            curSeg=seg;
            switch (seg){
                case SEG_1:
                    tvSeg1.setSelected(true);
                    tvSeg2.setSelected(false);
                    tvSeg3.setSelected(false);
                    tvSeg4.setSelected(false);
                    tvSeg5.setSelected(false);
                    tvSegAny.setSelected(false);
                    break;
                case SEG_2:
                    tvSeg1.setSelected(false);
                    tvSeg2.setSelected(true);
                    tvSeg3.setSelected(false);
                    tvSeg4.setSelected(false);
                    tvSeg5.setSelected(false);
                    tvSegAny.setSelected(false);
                    break;
                case SEG_3:
                    tvSeg1.setSelected(false);
                    tvSeg2.setSelected(false);
                    tvSeg3.setSelected(true);
                    tvSeg4.setSelected(false);
                    tvSeg5.setSelected(false);
                    tvSegAny.setSelected(false);
                    break;
                case SEG_4:
                    tvSeg1.setSelected(false);
                    tvSeg2.setSelected(false);
                    tvSeg3.setSelected(false);
                    tvSeg4.setSelected(true);
                    tvSeg5.setSelected(false);
                    tvSegAny.setSelected(false);
                    break;
                case SEG_5:
                    tvSeg1.setSelected(false);
                    tvSeg2.setSelected(false);
                    tvSeg3.setSelected(false);
                    tvSeg4.setSelected(false);
                    tvSeg5.setSelected(true);
                    tvSegAny.setSelected(false);
                    break;
                case SEG_ANY:
                    tvSeg1.setSelected(false);
                    tvSeg2.setSelected(false);
                    tvSeg3.setSelected(false);
                    tvSeg4.setSelected(false);
                    tvSeg5.setSelected(false);
                    tvSegAny.setSelected(true);
                    break;
            }
            if(seg!=SEG_ANY){
                tvSegStatus.setText(curSeg+"");
            }else{
                tvSegStatus.setText("free");
            }

            showOrHideSegContainer(false);
            if(mListener!=null){
                mListener.onSegCountChange(curSeg);
            }
        }
    }
    /**
     *
     * @param flag true show false hide
     */
    private void showOrHideDurContainer(boolean flag){
        if(flag){
            rlDurStatus.setVisibility(View.INVISIBLE);
            llDurContainer.setVisibility(View.VISIBLE);
        }else{
            rlDurStatus.setVisibility(View.VISIBLE);
            llDurContainer.setVisibility(View.INVISIBLE);
        }
    }
    /**
     *
     * @param flag true show false hide
     */
    private void showOrHideSegContainer(boolean flag){
        if(flag){
            rlSegStatus.setVisibility(View.INVISIBLE);
            llSegContainer.setVisibility(View.VISIBLE);
        }else{
            rlSegStatus.setVisibility(View.VISIBLE);
            llSegContainer.setVisibility(View.INVISIBLE);
        }
    }
    public void setOnConfigChangeListener(OnConfigChangeListener listener){
        this.mListener=listener;
    }

    public void setDefault(int curMode, int curDur, int curSeg) {
        changeScreenMode(curMode);
        changeRecordDur(curDur);
        changeSegCount(curSeg);
    }

    public interface OnConfigChangeListener{
        void onSreenModeChange(int mode);
        void onRecordDurChange(int dur);
        void onSegCountChange(int seg);
    }
}
