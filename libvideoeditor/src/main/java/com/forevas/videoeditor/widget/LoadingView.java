package com.forevas.videoeditor.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;

/**
 * Created by carden
 */

public class LoadingView extends FrameLayout {
    View root;
    View loading;//加载
    ImageView iv_loading;//加载页面动画1
    ImageView iv_loading_probar;//加载页面动画2
    TranslateAnimation tranAnim;//加载动画
    boolean isLoading;
    public LoadingView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        //加载中页面
        initLoading(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }
    private void initLoading(Context context) {
        root = View.inflate(context, R.layout.editor_pop_loading, this);
        loading = root.findViewById(R.id.rl_loading);
        iv_loading = (ImageView) root.findViewById(R.id.iv_loading);
        iv_loading_probar = (ImageView) root.findViewById(R.id.iv_loading_probar);
        initAnim();
    }
    //初始化动画
    private void initAnim() {
        tranAnim = new TranslateAnimation(1, 0, 1, 1.2f, 1, 0, 1, 0);
        tranAnim.setDuration(500);//设置动画持续时间
        tranAnim.setRepeatCount(-1);//设置重复次数
        tranAnim.setRepeatMode(Animation.REVERSE);//设置反方向执行
        // 适配手机
        ((AnimationDrawable) iv_loading.getDrawable()).start();
    }
    public void showLoading() {
        isLoading=true;
        setVisibility(VISIBLE);
        iv_loading_probar.startAnimation(tranAnim);
    }
    public void hideLoading(){
        isLoading=false;
        tranAnim.reset();
        setVisibility(INVISIBLE);
    }
    public boolean isLoading(){
        return isLoading;
    }
}
