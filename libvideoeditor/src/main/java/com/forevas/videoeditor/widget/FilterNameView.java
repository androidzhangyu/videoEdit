package com.forevas.videoeditor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.gpufilter.helper.FilterTypeHelper;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;

/**
 *Created by carden
 */

public class FilterNameView extends LinearLayout {
    TextView tvTopName;
    TextView tvBottomName;
    Animation topAnim,bottomAnim,dismissAnim;
    boolean reset;

    public FilterNameView(Context context) {
        super(context);
        init();
    }

    public FilterNameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View content = View.inflate(getContext(), R.layout.editor_filter_name_view, this);
        tvTopName = (TextView) content.findViewById(R.id.tv_filter_top_name);
        tvBottomName = (TextView) content.findViewById(R.id.tv_filter_bottom_name);
        setVisibility(View.INVISIBLE);
        topAnim = AnimationUtils.loadAnimation(getContext(), R.anim.editor_filter_name_fade_in);
        bottomAnim = AnimationUtils.loadAnimation(getContext(), R.anim.editor_filter_name_slide_fade_in);
        dismissAnim = AnimationUtils.loadAnimation(getContext(), R.anim.editor_filter_name_fade_out);
        topAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                postDelayed(dismissRunnable,1000);
                System.out.println("AnimTest topEnd");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dismissAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!reset){
                    setVisibility(View.INVISIBLE);
                }
                System.out.println("AnimTest dissmisEnd");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setFilterType(MagicFilterType type){
        System.out.println("AnimTest set");
        reset=true;
        removeCallbacks(dismissRunnable);
        topAnim.reset();
        bottomAnim.reset();
        dismissAnim.reset();
//        tvTopName.setText(FilterTypeHelper.FilterType2CnName(type));
        tvBottomName.setText(FilterTypeHelper.FilterType2Name(type));
        setVisibility(View.VISIBLE);
        tvTopName.startAnimation(topAnim);
        tvBottomName.startAnimation(bottomAnim);
    }
    private Runnable dismissRunnable=new Runnable() {
        @Override
        public void run() {
            System.out.println("AnimTest startDismiss");
            reset=false;
            startAnimation(dismissAnim);
        }
    };
}
