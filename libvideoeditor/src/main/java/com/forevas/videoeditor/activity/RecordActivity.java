package com.forevas.videoeditor.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.filter.WaterMarkFilter;
import com.forevas.videoeditor.utils.BitMapUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.forevas.videoeditor.R;
import com.forevas.videoeditor.bean.RecordVideo;
import com.forevas.videoeditor.camera.SensorControler;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.db.bean.RecordConfig;
import com.forevas.videoeditor.db.bean.RecordSave;
import com.forevas.videoeditor.db.dao.RecordConfigDao;
import com.forevas.videoeditor.db.dao.RecordSaveDao;
import com.forevas.videoeditor.event.EditorFinishEvent;
import com.forevas.videoeditor.event.RecordSegFinishEvent;
import com.forevas.videoeditor.gpufilter.SlideGpufilterGroup;
import com.forevas.videoeditor.gpufilter.helper.FilterTypeHelper;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;
import com.forevas.videoeditor.renderer.CameraDrawer;
import com.forevas.videoeditor.utils.SharedPreferenceUtils;
import com.forevas.videoeditor.utils.TimeFormatUtils;
import com.forevas.videoeditor.widget.CameraView;
import com.forevas.videoeditor.widget.CircularProgressView;
import com.forevas.videoeditor.widget.FilterNameView;
import com.forevas.videoeditor.widget.FocusImageView;
import com.forevas.videoeditor.widget.LoadingView;
import com.forevas.videoeditor.widget.RecordConfigPop;
import com.forevas.videoeditor.widget.RecordSegmentView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * carden
 */

public class RecordActivity extends Activity implements View.OnClickListener, View.OnTouchListener, RecordConfigPop.OnConfigChangeListener, SensorControler.CameraFocusListener, SlideGpufilterGroup.OnFilterChangeListener, CameraView.OnSurfaceSizeChangeListener {
    ImageView ivCameraBeauty, ivCameraSwitch, ivClose, ivAddLocal, ivBack;
    RecordSegmentView segmentView;
    TextView tvConfig;
    RecordConfigPop configPop;
    CameraView mCameraView;
    FocusImageView mFocusImageView;
    FilterNameView mFilterName;
    CircularProgressView mCapture;
    TextView tvCancel, tvFinish, tvFreeTime;
    FrameLayout flGuide;
    TextView tvGuideSubmit;
    LoadingView loadingView;
    private SensorControler mSensorControler;

    private int curMode = Constants.MODE_POR_9_16;
    private int curDur = RecordConfigPop.DUR_1;
    private int curSeg = RecordConfigPop.SEG_2;

    public static final String GUIDE_KEY = "isFirstRecord";

    private boolean recordFlag = false;//是否正在录制
    private boolean pausing = false;
    private boolean autoPausing = false;
    private boolean cancel = false;//是否手动取消
    private boolean isFinishFreeMode = false;//是否已经完成自由模式的录制
    private boolean isFinishRecord = false;//是否已经完成录制
    private ExecutorService executorService;
    private ArrayList<String> videoPath = new ArrayList<>();//用于保存每段视频的地址
    private ArrayList<RecordVideo> videoSave = new ArrayList<>();//用于保存拍摄的片段
    private MagicFilterType mType=MagicFilterType.NONE;//记录当前滤镜 用于数据埋点
    private int camera;//用于记录每段视频所用的摄像头 用于数据埋点
    private Map<String, Boolean> recordStatus = Collections.synchronizedMap(new HashMap<String, Boolean>());//用于记录视频是否拍摄完成
    private long uid;
    private RecordConfigDao configDao;
    private RecordSaveDao saveDao;
    private RecordConfig config;
    private RecordSave save;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity_record_video);
        EventBus.getDefault().register(this);
        initView();
        initData();
        mSensorControler = SensorControler.getInstance();
        mSensorControler.registCameraFocusListener(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            String from = intent.getStringExtra("from");
            if (from != null && from.equals("preview")) {
                String outputPath = intent.getStringExtra("outputPath");
                int clipDur = intent.getIntExtra("clipDur", 0);
                String filterName = intent.getStringExtra("filter");
                if (tvFinish.getTag() != null && tvFinish.getTag().equals("saved")) {//说明要删除上一次保存的记录
                    for (RecordVideo video : videoSave) {//删除视频
                        if (!TextUtils.isEmpty(video.path)) {
                            File temp=new File(video.path);
                            if(temp.exists()){
                                temp.delete();
                            }
                        }
                    }
                    save.uid = uid + "";
                    save.mode = curMode;
                    save.dur = curDur;
                    save.seg = curDur;
                    save.data = "";
                    saveDao.replaceRecordSave(save);
                    videoSave.clear();
                    tvFinish.setTag(null);
                }
                videoPath.add(outputPath);
                RecordVideo video = new RecordVideo();
                video.path = outputPath;
                video.videoDur = clipDur;
                video.mode = curMode;
                video.dur = curDur;
                video.seg = curSeg;
                video.src = "bd";
                video.filter = filterName;
                videoSave.add(video);
                if (curSeg != -1) {
                    segmentView.increaseRecordCount();
                    if (videoPath.size() == curSeg) {
                        tvFinish.setText("finish");
                        mCapture.confirmMode(true);
                        isFinishRecord = true;
                        startEditorActivity(curDur);
                    } else {
                        tvFinish.setText("jump it");
                    }
                } else {
                    int maxTime = curDur * 1000 - segmentView.getAddedTime();//毫秒
                    if (clipDur >= maxTime) {
                        //任意模式录制完成
                        segmentView.addFreeRecordCount(maxTime);
                        tvFinish.setText("finish");
                        isFinishFreeMode = true;
                        mCapture.confirmMode(true);
                        isFinishRecord = true;
                        startEditorActivity(curDur);
                    } else {
                        segmentView.addFreeRecordCount(clipDur);
                        tvFinish.setText("jump it");
                    }
                }
                tvConfig.setVisibility(View.INVISIBLE);
                tvFinish.setVisibility(View.VISIBLE);
                ivBack.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initView() {
        ivCameraBeauty = (ImageView) findViewById(R.id.btn_camera_beauty);
        ivCameraSwitch = (ImageView) findViewById(R.id.btn_camera_switch);
        ivClose = (ImageView) findViewById(R.id.iv_close);
        ivAddLocal = (ImageView) findViewById(R.id.btn_add_local);
        ivBack = (ImageView) findViewById(R.id.btn_back);
        segmentView = (RecordSegmentView) findViewById(R.id.record_segment);
        tvConfig = (TextView) findViewById(R.id.tv_record_config);
        mCameraView = (CameraView) findViewById(R.id.mCameraView);
        mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
        mCapture = (CircularProgressView) findViewById(R.id.mCapture);
        mFilterName = (FilterNameView) findViewById(R.id.filter_name);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvFinish = (TextView) findViewById(R.id.tv_finish);
        tvFreeTime=(TextView)findViewById(R.id.tv_free_time);
        flGuide = (FrameLayout) findViewById(R.id.fl_guide);
        tvGuideSubmit = (TextView) findViewById(R.id.tv_guide_submit);
        loadingView = new LoadingView(this);
        loadingView.hideLoading();
        mCameraView.setOnFilterChangeListener(this);
        mCameraView.setOnSurfaceSizeChangeListener(this);
        ivBack.setVisibility(View.INVISIBLE);
        ivCameraBeauty.setSelected(false);
        ivCameraBeauty.setVisibility(View.INVISIBLE);
        ivCameraBeauty.setOnClickListener(this);
        ivCameraSwitch.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivAddLocal.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvConfig.setOnClickListener(this);
        mCapture.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvFinish.setOnClickListener(this);
        flGuide.setOnClickListener(this);
        tvGuideSubmit.setOnClickListener(this);
        mCameraView.setOnTouchListener(this);
    }

    private void initData() {
        try {
            uid = getIntent().getLongExtra("uid", -1);
            configDao = new RecordConfigDao(getApplicationContext());
            saveDao = new RecordSaveDao(getApplicationContext());
            config = configDao.getRecordConfigByUid(uid + "");
            save = saveDao.getRecordSaveByUid(uid + "");
            if (save != null && !TextUtils.isEmpty(save.data)) {//发现有上次未完成的拍摄
                ArrayList<RecordVideo> tempList = new Gson().fromJson(save.data, new TypeToken<ArrayList<RecordVideo>>() {
                }.getType());
                if (tempList != null && tempList.size() != 0) {//视频段数大于0
                    boolean canRestore = true;
                    for (RecordVideo video : tempList) {
                        if (TextUtils.isEmpty(video.path) || !new File(video.path).exists()) {//若是有视频已经被删除了,则不能恢复
                            canRestore = false;
                            break;
                        }
                    }
                    if (canRestore) {//可以恢复
                        tvFinish.setText("Resume last shot");
                        tvFinish.setVisibility(View.VISIBLE);
                        tvFinish.setTag("saved");
                        videoSave = tempList;
                    } else {//不可以恢复要删掉其余的视频
                        for (RecordVideo video : tempList) {
                            if (!TextUtils.isEmpty(video.path)) {//若是有视频已经被删除了,则不能恢复
                                File temp=new File(video.path);
                                if(temp.exists()){
                                    temp.delete();
                                }
                            }
                        }
                        save = new RecordSave();
                    }
                }
            } else {
                save = new RecordSave();
            }
            if (config == null) {
                config = new RecordConfig();
                config.uid = uid + "";
            } else {
                curMode = config.mode;
                curDur = config.dur;
                curSeg = config.seg;
            }
            configPop = new RecordConfigPop(this);
            configPop.setDefault(curMode, curDur, curSeg);
            configPop.setOnConfigChangeListener(this);
            updateTvConfig();

            if (curSeg != RecordConfigPop.SEG_ANY) {
                segmentView.setRecordMode(RecordSegmentView.MODE_SEG);
                segmentView.setSegmentCount(curSeg);
                mCapture.setRecordMode(CircularProgressView.MODE_SEG);
                mCapture.setTotal(curDur * 1000 / curSeg);
            } else {
                segmentView.setRecordMode(RecordSegmentView.MODE_FREE);
                segmentView.setTotalTime(curDur * 1000);
                mCapture.setRecordMode(CircularProgressView.MODE_FREE);
                mCapture.setTotal(curDur * 1000);
            }
            mFilterName.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            RelativeLayout.LayoutParams filterParams = (RelativeLayout.LayoutParams) mFilterName.getLayoutParams();
            switch (curMode) {
                case Constants.MODE_POR_9_16:
                    filterParams.topMargin = Constants.screenHeight / 3 - mFilterName.getMeasuredHeight() / 2;
                    break;
                case Constants.MODE_POR_1_1:
                    filterParams.topMargin = (int) ((Constants.screenHeight - mFilterName.getMeasuredHeight()) / 2 - Constants.screenHeight * 0.11f);
                    break;
                case Constants.MODE_POR_16_9:
                    filterParams.topMargin = (int) ((Constants.screenHeight - mFilterName.getMeasuredHeight()) / 2 - Constants.screenHeight * 0.11f);
                    break;
            }
            mFilterName.setLayoutParams(filterParams);
            int isFirstRecord = SharedPreferenceUtils.getInt(this, GUIDE_KEY, 1);
            if (isFirstRecord == 1) {
                flGuide.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void updateTvConfig() {
        try{
            String config = (curSeg == -1 ? "free" : (curSeg + " section")) + "  •  " + (curDur <= 60 ? curDur + " second" : curDur / 60 + " minutes") + "  •  " + curMode;
            SpannableString sp = new SpannableString(config);
            Drawable d = null;
            if (curMode == Constants.MODE_POR_9_16) {
                d = getResources().getDrawable(R.mipmap.editor_icon_scale_9_16);
            } else if (curMode == Constants.MODE_POR_1_1) {
                d = getResources().getDrawable(R.mipmap.editor_icon_scale_1_1);
            } else if (curMode == Constants.MODE_POR_16_9) {
                d = getResources().getDrawable(R.mipmap.editor_icon_scale_16_9);
            }
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            sp.setSpan(span, config.length() - 1, config.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvConfig.setText(sp);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            pausing = false;
            autoPausing = false;
            cancel = false;
            int timeCount = 0;//毫秒
            final int maxTime = curSeg == RecordConfigPop.SEG_ANY ? (curDur * 1000 - segmentView.getAddedTime()) : curDur * 1000 / curSeg;//毫秒 当前段可录制的最大时长
            int timeStep = 50;
            long time = System.currentTimeMillis();
            final String savePath = Constants.getPath("video/record/", time + "");
            mCapture.setTotal(maxTime);
            mCameraView.setSavePath(savePath);
            mCameraView.startRecord();
            recordStatus.put(savePath, false);
            System.out.println("testRecordStatus addPath" + savePath);
            while (timeCount <= maxTime && recordFlag && !cancel) {
                if (pausing || autoPausing) {
                    continue;
                }
                mCapture.setProcess(timeCount);
                if(curSeg==RecordConfigPop.SEG_ANY){//如果是自由模式,就更新时间显示
                    final int tempTime=timeCount;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String format = TimeFormatUtils.formatMillisecWithoutHours(tempTime);
                            tvFreeTime.setText(format);
                        }
                    });
                }
                try {
                    Thread.sleep(timeStep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeCount += timeStep;
            }
            mCapture.finishProcess();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvCancel.setVisibility(View.INVISIBLE);//隐藏取消按钮
                    tvFreeTime.setText("00:00");
                    tvFreeTime.setVisibility(View.GONE);//隐藏时间显示
                }
            });
            recordFlag = false;
            mCameraView.stopRecord();

            if (!cancel) {//如果不是取消拍摄
                videoPath.add(savePath);
                final RecordVideo video = new RecordVideo();
                video.path = savePath;
                video.videoDur = timeCount;
                video.mode = curMode;
                video.dur = curDur;
                video.seg = curSeg;
                video.src = "lz";
                video.filter = getResources().getText(FilterTypeHelper.FilterType2Name(mType)).toString();
                video.beauty = ivCameraBeauty.isSelected() ? 1 : 0;
                video.camera=camera==2?camera:(1-mCameraView.getCameraId());
                camera=0;
                videoSave.add(video);//保存这段视频信息
                final int finalTimeCount = timeCount;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (curSeg == RecordConfigPop.SEG_ANY) {//自由模式
                            if (finalTimeCount >= maxTime || Math.abs(finalTimeCount - maxTime) < 500) {//如果已经达到最大时长
                                //任意模式录制完成
                                segmentView.addFreeRecordCount(maxTime);//进度条显示这段新拍摄的视频
                                isFinishFreeMode = true;//标记已完成自由模式的拍摄
                                tvFinish.setText("finish");
                                mCapture.confirmMode(true);
                                isFinishRecord = true;//标记已完成拍摄
                                startEditorActivity(curDur);//自动跳转到视频编辑页面
                            } else {//说明只是完成了本段视频的拍摄,还可继续拍摄
                                mCapture.setClickable(true);
                                segmentView.addFreeRecordCount(finalTimeCount);
                                tvFinish.setText("jump it");
                            }
                        } else {//分段模式完成
                            segmentView.increaseRecordCount();//更新进度条
                            mCapture.setClickable(true);//拍摄按钮可点击
                            if (videoPath.size() == curSeg) {//达到最大段数
                                tvFinish.setText("finish");
                                mCapture.confirmMode(true);
                                isFinishRecord = true;
                                startEditorActivity(curDur);//自动跳转
                            } else {
                                tvFinish.setText("jump it");//没有达到最大段数,显示跳过按钮
                            }
                        }
                        //拍摄完成,添加本地按钮,完成按钮,删除按钮都应显示
                        ivAddLocal.setVisibility(View.VISIBLE);
                        tvFinish.setVisibility(View.VISIBLE);
                        ivBack.setVisibility(View.VISIBLE);
                    }
                });
            } else {//说明是放弃了本段拍摄
//                recordStatus.remove(savePath);//这里不删除状态，因为不管是取消还是完成都要等待本段视频完成录制之后再进行下一步操作
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCapture.setClickable(true);
                        if (videoPath.size() == 0) {//没有拍摄任何视频,显示config按钮
                            tvConfig.setVisibility(View.VISIBLE);
                        }
                        if (videoPath.size() > 0) {//存在拍摄完成的视频,显示完成和删除按钮
                            tvFinish.setVisibility(View.VISIBLE);
                            ivBack.setVisibility(View.VISIBLE);
                        }
                        ivAddLocal.setVisibility(View.VISIBLE);//只要不在拍摄中就显示添加本地按钮
                    }
                });
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {//删除本段视频
                        File temp = new File(savePath);
                        if (temp.exists()) {
                            temp.delete();
                        }
                    }
                }, 2000);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        if (recordFlag && autoPausing) {
            mCameraView.resume(true);
            autoPausing = false;
        }
        mSensorControler.onStart();
    }

    @Override
    public void onBackPressed() {
        if (!loadingView.isLoading()) {
//            super.onBackPressed();
            recordCancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordFlag && !pausing) {
            mCameraView.pause(true);
            autoPausing = true;
        }
        mCameraView.onPause();
        mSensorControler.onStop();
        config.mode = curMode;
        config.dur = curDur;
        config.seg = curSeg;
        configDao.replaceRecordConfig(config);
        save.uid = uid + "";
        save.mode = curMode;
        save.dur = curDur;
        save.seg = curSeg;
        if (videoSave.size() != 0) {//保存拍摄信息
            String data = new Gson().toJson(videoSave);
            save.data = data;
        } else {
            save.data = "";
        }
        saveDao.replaceRecordSave(save);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mCameraView.onDestroy();
        mCameraView=null;
        mSensorControler.unregistCameraFocusListener();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EditorFinishEvent event) {
        saveDao.deleteRecordSaveByUid(uid + "");
        Intent intent=new Intent();
        intent.putExtra("path",event.path);
        intent.putExtra("recordTime",event.endTime);
        setResult(1004,intent);
        Log.e("!!@@-->",event.path);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecordSegFinishEvent event) {
        if (recordStatus.containsKey(event.path)) {
            recordStatus.put(event.path, event.flag);
            System.out.println("testRecordStatus receivedPath" + event.path);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_camera_beauty) {
            if (ivCameraBeauty.isSelected()) {
                mCameraView.changeBeautyLevel(0);
                ivCameraBeauty.setSelected(false);
            } else {
                mCameraView.changeBeautyLevel(3);
                ivCameraBeauty.setSelected(true);
            }
        } else if (id == R.id.btn_camera_switch) {
            if(recordFlag){//在拍摄的过程中切换镜头,说明是前后组合
                camera=2;
            }
            if (mCameraView.getCameraId() == 0) {
                ivCameraBeauty.setVisibility(View.VISIBLE);
                ivCameraBeauty.setSelected(true);
                mCameraView.switchCamera();
                mCameraView.changeBeautyLevel(3);
            } else {
                ivCameraBeauty.setVisibility(View.INVISIBLE);
                ivCameraBeauty.setSelected(false);
                mCameraView.switchCamera();
                mCameraView.changeBeautyLevel(0);
            }
        } else if (id == R.id.iv_close) {
            recordCancel();
        } else if (id == R.id.btn_add_local) {
            if (isFinishRecord) {
                Toast.makeText(this, "The maximum time has been reached", Toast.LENGTH_SHORT).show();
                return;
            }
            int maxTime = curSeg == RecordConfigPop.SEG_ANY ? (curDur * 1000 - segmentView.getAddedTime()) : curDur * 1000 / curSeg;//毫秒
            Intent intent = new Intent(this, SelectVideoActivity.class);
            intent.putExtra("curMode", curMode);
            intent.putExtra("curDur", curDur);
            intent.putExtra("curSeg", curSeg);
            intent.putExtra("maxTime", maxTime);
            startActivity(intent);
        } else if (id == R.id.btn_back) {
            ivBack.setClickable(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivBack.setClickable(true);
                }
            }, 500);
            if (videoPath.size() > 0) {
                if (recordStatus.containsKey(videoPath.get(videoPath.size() - 1))) {
                    recordStatus.remove(videoPath.get(videoPath.size() - 1));
                }
                String remove = videoPath.remove(videoPath.size() - 1);
                videoSave.remove(videoSave.size() - 1);
                File temp = new File(remove);
                if (temp.exists()) {
                    temp.delete();
                }
                if (videoPath.size() == 0) {
                    ivBack.setVisibility(View.INVISIBLE);
                    tvConfig.setVisibility(View.VISIBLE);
                    tvFinish.setVisibility(View.INVISIBLE);
                }
            }
            mCapture.setClickable(true);
            mCapture.confirmMode(false);
            tvFinish.setText("jump it");
            isFinishRecord = false;
            if (curSeg != RecordConfigPop.SEG_ANY) {
                segmentView.decreaseRecordCount();
            } else {
                segmentView.subFreeRecordCount();
                isFinishFreeMode = false;
            }
        } else if (id == R.id.tv_record_config) {
            configPop.show((ViewGroup) getWindow().getDecorView());
        } else if (id == R.id.mCapture) {
            if (tvFinish.getTag() != null && tvFinish.getTag().equals("saved")) {//说明要删除上一次保存的记录
                for (RecordVideo video : videoSave) {//删除视频
                    if (!TextUtils.isEmpty(video.path)) {
                        File temp=new File(video.path);
                        if(temp.exists()){
                            temp.delete();
                        }
                    }
                }
                save.uid = uid + "";
                save.mode = curMode;
                save.dur = curDur;
                save.seg = curDur;
                save.data = "";
                saveDao.replaceRecordSave(save);
                videoSave.clear();
                tvFinish.setTag(null);
            }
            if (isFinishRecord) {
                startEditorActivity(curDur);
                return;
            }
            if (!recordFlag) {//如果当前没有录制视频
                if (!isLastRecordFinish()) {//上一段视频没有录制完成(资源还没有释放结束),需等待
                    return;
                }
                startRecord();
            } else if (recordFlag && curSeg == RecordConfigPop.SEG_ANY) {//如果正在录制,并且是自由模式的话,说明是本段视频录制完成的操作
                recordFlag = false;
                mCapture.setClickable(false);
            }
        } else if (id == R.id.tv_cancel) {
            if (recordFlag) {
                cancel = true;
            }
            tvCancel.setVisibility(View.INVISIBLE);
        } else if (id == R.id.tv_finish) {
            if (tvFinish.getTag() != null && tvFinish.getTag().equals("saved")) {//说明要恢复上一次保存的记录
                onSreenModeChange(save.mode);
                onRecordDurChange(save.dur);
                onSegCountChange(save.seg);
                long totalDur = 0;
                List<Integer> segList = new ArrayList<>();
                for (RecordVideo video : videoSave) {
                    videoPath.add(video.path);
                    if (curSeg == -1) {
                        totalDur += video.videoDur;
                        segList.add((int) video.videoDur);
                    }
                }
                if (curSeg != RecordConfigPop.SEG_ANY) {
                    segmentView.setRecordCount(videoSave.size());
                    if (videoSave.size() == curSeg) {//已经拍摄完毕
                        mCapture.confirmMode(true);
                        isFinishRecord = true;
                        tvFinish.setText("finish");
                    } else {
                        tvFinish.setText("jump it");
                    }
                } else {
                    segmentView.addSegList(segList);
                    if (totalDur > curDur * 1000 || Math.abs(totalDur - curDur * 1000) < 500) {
                        mCapture.confirmMode(true);
                        isFinishFreeMode = true;
                        isFinishRecord = true;
                        tvFinish.setText("finish");
                    } else {
                        tvFinish.setText("jump it");
                    }
                }
                tvFinish.setTag(null);
                ivBack.setVisibility(View.VISIBLE);
                tvConfig.setVisibility(View.INVISIBLE);
            } else {
                startEditorActivity(curDur);
            }

        } else if (id == R.id.fl_guide) {

        } else if (id == R.id.tv_guide_submit) {
            flGuide.setVisibility(View.INVISIBLE);
            SharedPreferenceUtils.setInt(this, GUIDE_KEY, 0);
        }
    }

    private void startRecord() {
        //录制的时候应该隐藏config按钮,添加本地按钮和删除按钮
        try{
            tvConfig.setVisibility(View.INVISIBLE);
            ivAddLocal.setVisibility(View.INVISIBLE);
            ivBack.setVisibility(View.INVISIBLE);
            if (curSeg != RecordConfigPop.SEG_ANY) {//如果不是自由模式,显示取消按钮,隐藏跳过按钮,拍摄按钮不可点击
                tvCancel.setVisibility(View.VISIBLE);
                tvFinish.setVisibility(View.INVISIBLE);
                mCapture.setClickable(false);
            } else {
                if (isFinishFreeMode) {
                    return;
                }
                tvFinish.setVisibility(View.INVISIBLE);//自由模式隐藏完成按钮
                tvFreeTime.setVisibility(View.VISIBLE);//自由模式显示当前拍摄时长
            }
            executorService.execute(recordRunnable);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("RecordActivity",e.getMessage());
        }

    }

    private void startEditorActivity(final long recordTime) {
        loadingView.showLoading();
        ((FrameLayout) getWindow().getDecorView()).addView(loadingView);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isLastRecordFinish()) {
                    loadingView.hideLoading();
                    ((FrameLayout) getWindow().getDecorView()).removeView(loadingView);
                    Intent intent = new Intent(RecordActivity.this, EditActivity.class);
                    intent.putStringArrayListExtra("path", videoPath);
                    intent.putExtra("curMode", curMode);
                    String videoFormat=getVideoFormat();
                    intent.putExtra("videoFormat",videoFormat);
                    //todo
                    intent.putExtra("time",recordTime);
                    startActivity(intent);
                } else {
                    mHandler.postDelayed(this, 200);
                }
            }
        });

    }

    /**
     * 获取视频相关信息,用作数据埋点
     * @return
     */
    private String getVideoFormat(){
        String src = "";
        String filter = "";
        int beauty=0;
        int camera=-1;
        int mode=0;
        int dur=0;
        String seg="";
        String videoFormat;
        for(RecordVideo video:videoSave){
            if("lz".equals(video.src)||"lz".equals(src)){
                src="lz";
            }else{
                src="bd";
            }
            if(!TextUtils.isEmpty(video.filter)&&!"Original".equals(video.filter)){
                if(!TextUtils.isEmpty(filter)){
                    if(!filter.contains(video.filter)){
                        filter=filter+"-"+video.filter;
                    }
                }else{
                    filter=video.filter;
                }
            }
            if(video.beauty==1){
                beauty=1;
            }
            if("lz".equals(video.src)){
                if(camera!=-1){
                    if(camera!=video.camera){
                        camera=2;
                    }
                }else{
                    camera=video.camera;
                }
            }
            mode=video.mode;
            dur= (int) video.dur;
            if(video.seg==-1){
                seg="free";
            }else{
                seg=video.seg+"";
            }
        }
        if(camera==-1){
            camera=0;
        }
        videoFormat=src+","+filter+","+beauty+","+camera+","+mode+","+dur+","+seg;
        return videoFormat;
    }
    private boolean isLastRecordFinish() {
        boolean isReady = true;
        Set<String> keys = recordStatus.keySet();
        for (String key : keys) {
            if (!recordStatus.get(key)) {
                isReady = false;
                break;
            }
        }
        return isReady;
    }

    //取消拍摄或者直接退出
    private void recordCancel(){
        if(recordFlag){
            cancel=true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isLastRecordFinish()) {
                        finish();
                    } else {
                        mHandler.postDelayed(this, 200);
                    }
                }
            });
        }else{
            finish();
        }
    }

    @Override
    public void onSreenModeChange(int mode) {
        curMode = mode;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mFilterName.getLayoutParams();
        switch (mode) {
            case Constants.MODE_POR_9_16:
                startAnim(CameraDrawer.SCREEN_MODE_FULL, Constants.mode_por_width_9_16, Constants.mode_por_height_9_16, 0, 200);
                layoutParams.topMargin = Constants.screenHeight / 3 - mFilterName.getHeight() / 2;
                break;
            case Constants.MODE_POR_1_1:
                startAnim(CameraDrawer.SCREEN_MODE_SQUARE, Constants.mode_por_width_1_1, Constants.mode_por_height_1_1, 0.11f, 300);
                layoutParams.topMargin = (int) ((Constants.screenHeight - mFilterName.getHeight()) / 2 - Constants.screenHeight * 0.11f);
                break;
            case Constants.MODE_POR_16_9:
                startAnim(CameraDrawer.SCREEN_MODE_RECTANGLE, Constants.mode_por_width_16_9, Constants.mode_por_height_16_9, 0.11f, 300);
                layoutParams.topMargin = (int) ((Constants.screenHeight - mFilterName.getHeight()) / 2 - Constants.screenHeight * 0.11f);
                break;
        }
        updateTvConfig();
        mFilterName.setLayoutParams(layoutParams);
    }

    int lastScreenWidth = Constants.mode_por_width_9_16, lastScreenHeight = Constants.mode_por_height_9_16;
    float lastOffset = 0;

    private void startAnim(final int mode, final int width, final int height, final float offset, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                int tWidth = (int) ((width - lastScreenWidth) * animatedFraction + lastScreenWidth);
                int tHeight = (int) ((height - lastScreenHeight) * animatedFraction + lastScreenHeight);
                float tOffset = (offset - lastOffset) * animatedFraction + lastOffset;
                mCameraView.setScreenMode(mode, tWidth, tHeight, tOffset, animatedFraction);
                if (animatedFraction == 1.0) {
                    lastScreenWidth = width;
                    lastScreenHeight = height;
                    lastOffset = offset;
                }
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onRecordDurChange(int dur) {
        curDur = dur;
        updateTvConfig();
        if (curSeg != RecordConfigPop.SEG_ANY) {
            mCapture.setTotal(dur * 1000 / curSeg);
        } else {
            segmentView.setTotalTime(curDur * 1000);
            mCapture.setTotal(curDur * 1000);
        }

    }

    @Override
    public void onSegCountChange(int seg) {
        curSeg = seg;
        if (seg != RecordConfigPop.SEG_ANY) {
            segmentView.setRecordMode(RecordSegmentView.MODE_SEG);
            segmentView.setSegmentCount(seg);
            mCapture.setRecordMode(CircularProgressView.MODE_SEG);
            mCapture.setTotal(curDur * 1000 / curSeg);
        } else {
            segmentView.setRecordMode(RecordSegmentView.MODE_FREE);
            segmentView.setTotalTime(curDur * 1000);
            mCapture.setRecordMode(CircularProgressView.MODE_FREE);
            mCapture.setTotal(curDur * 1000);
        }

        updateTvConfig();

    }

    /**
     * 对焦相关
     */
    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
                mFocusImageView.onFocusSuccess();
            } else {
                //聚焦失败显示的图片
                mFocusImageView.onFocusFailed();
            }
        }
    };
    int downX;
    int downY;
    boolean canFocus;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mCameraView.onTouch(event);
        if (mCameraView.getCameraId() == 1) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                canFocus = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - downX) > 100 || Math.abs(event.getY() - downY) > 100) {
                    canFocus = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!canFocus) {
                    return false;
                }
                int topY = 0, bottomY = 0;
                switch (mCameraView.getScreenMode()) {
                    case Constants.MODE_POR_9_16:
                        topY = 0;
                        int[] loc = new int[2];
                        segmentView.getLocationOnScreen(loc);
                        bottomY = loc[1];
                        break;
                    case Constants.MODE_POR_1_1:
                        topY = (Constants.screenHeight - Constants.screenWidth) / 2;
                        bottomY = Constants.screenHeight - topY;
                        topY -= Constants.screenHeight * 0.11f;
                        bottomY -= Constants.screenHeight * 0.11f;
                        break;
                    case Constants.MODE_POR_16_9:
                        topY = (Constants.screenHeight - Constants.screenWidth / 16 * 9) / 2;
                        bottomY = Constants.screenHeight - topY;
                        topY -= Constants.screenHeight * 0.11f;
                        bottomY -= Constants.screenHeight * 0.11f;
                        break;
                }
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                if (sRawY <= topY || sRawY >= bottomY) {
                    return true;
                }
                float rawY = sRawY * Constants.screenWidth / Constants.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (Constants.screenWidth - temp) * Constants.screenHeight / Constants.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mCameraView.onFocus(point, callback);
                mFocusImageView.startFocus(new Point((int) sRawX, (int) sRawY));
        }
        return true;
    }

    @Override
    public void onFocus() {
        if (mCameraView!=null) {
            if (mCameraView.getCameraId() == 1) {
                return;
            }
            Point point = new Point(Constants.screenWidth / 2, Constants.screenHeight / 2);
            mCameraView.onFocus(point, callback);
        }
    }

    @Override
    public void onFilterChange(final MagicFilterType type) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFilterName.setFilterType(type);
                mType = type;
            }
        });
    }

    @Override
    public void onSurfaceSizeChange() {
        switch (curMode) {
            case Constants.MODE_POR_9_16:
                lastScreenWidth = Constants.mode_por_width_9_16;
                lastScreenHeight = Constants.mode_por_height_9_16;
                lastOffset = 0;
                mCameraView.preSetScreenMode(CameraDrawer.SCREEN_MODE_FULL, Constants.mode_por_width_9_16, Constants.mode_por_height_9_16, 0, 1);
                break;
            case Constants.MODE_POR_1_1:
                lastScreenWidth = Constants.mode_por_width_1_1;
                lastScreenHeight = Constants.mode_por_height_1_1;
                lastOffset = 0.11f;
                mCameraView.preSetScreenMode(CameraDrawer.SCREEN_MODE_SQUARE, Constants.mode_por_width_1_1, Constants.mode_por_height_1_1, 0.11f, 1);
                break;
            case Constants.MODE_POR_16_9:
                lastScreenWidth = Constants.mode_por_width_16_9;
                lastScreenHeight = Constants.mode_por_height_16_9;
                lastOffset = 0.11f;
                mCameraView.preSetScreenMode(CameraDrawer.SCREEN_MODE_RECTANGLE, Constants.mode_por_width_16_9, Constants.mode_por_height_16_9, 0.11f, 1);
                break;
        }
    }
}
