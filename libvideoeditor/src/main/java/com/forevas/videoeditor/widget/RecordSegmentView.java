package com.forevas.videoeditor.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.utils.DensityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carden
 */

public class RecordSegmentView extends View {
    public static final int MODE_SEG=0;//分段模式
    public static final int MODE_FREE=1;//自由模式
    private int recordMode=MODE_SEG;
    private int dotCount = 50;
    private Paint paint1, paint2, paint3;
    private int segmentCount = 5;
    private int recordSegment;
    private int dotColor = 0xFFFFFFFF;
    private int lineColor = 0xFFFFFFFF;
    private int segmentColor = 0xFFFDD915;
    private int orientation = 0;
    private int dotSize;
    private int lineSize;
    private int segmentSize;
    private boolean anim = false;
    private float animFra;
    private boolean increase, decrease;
    private boolean lock;
    private int toBeDecrese;

    //for mode free
    private int totalTime;//mills
    private int addedTime;
    private List<Integer> segList;

    public RecordSegmentView(Context context) {
        super(context);
        init();
    }

    public RecordSegmentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
        init();
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordSegmentView);
        if (typedArray != null) {
            dotCount = typedArray.getInt(R.styleable.RecordSegmentView_dot_count, 50);
            segmentCount = typedArray.getInt(R.styleable.RecordSegmentView_segment_count, 5);
            dotColor = typedArray.getColor(R.styleable.RecordSegmentView_dot_color, 0xFFFFFFFF);
            lineColor = typedArray.getColor(R.styleable.RecordSegmentView_line_color, 0xFFFFFFFF);
            segmentColor = typedArray.getColor(R.styleable.RecordSegmentView_segment_color, 0xFFFDD915);
            orientation = typedArray.getInt(R.styleable.RecordSegmentView_orientation, 0);
            dotSize = (int) typedArray.getDimension(R.styleable.RecordSegmentView_dot_size, DensityUtils.dp2px(getContext(), 1));
            lineSize = (int) typedArray.getDimension(R.styleable.RecordSegmentView_line_size, DensityUtils.dp2px(getContext(), 1));
            segmentSize = (int) typedArray.getDimension(R.styleable.RecordSegmentView_segment_size, -1);
            typedArray.recycle();
        }
    }

    private void init() {
        paint1 = new Paint();
        paint1.setAntiAlias(true);
        paint1.setColor(dotColor);
        if (dotSize == 0) {
            paint1.setStrokeWidth(DensityUtils.dp2px(getContext(), 1));
        } else {
            paint1.setStrokeWidth(dotSize);
        }

        paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setColor(segmentColor);

        paint3 = new Paint();
        paint3.setAntiAlias(true);
        paint3.setColor(lineColor);
        if (lineSize == 0) {
            paint3.setStrokeWidth(DensityUtils.dp2px(getContext(), 1));
        } else {
            paint3.setStrokeWidth(lineSize);
        }
        segList=new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(recordMode==MODE_SEG){
            if (orientation == 0) {
                drawHorizontal(canvas);
            } else {
                drawVertical(canvas);
            }
        }else if(recordMode==MODE_FREE){
            if (orientation == 0) {
                drawFreeHorizontal(canvas);
            } else {
                drawFreeVertical(canvas);
            }
        }

    }

    /**
     * 横向分段模式绘制
     * @param canvas
     */
    private void drawHorizontal(Canvas canvas) {
        int linePerStep = getWidth() / segmentCount;
        int perStepDotCount = dotCount / segmentCount;
        int dotPerStep = linePerStep / (perStepDotCount + 1);
        for (int i = 0; i < segmentCount; i++) {
            for (int j = 0; j < perStepDotCount; j++) {
                canvas.drawPoint((j + 1) * dotPerStep + i * linePerStep, getHeight() / 2, paint1);
            }
        }
        if (segmentSize == 0) {
            paint2.setStrokeWidth(getHeight() * 2 / 3);
        } else {
            paint2.setStrokeWidth(segmentSize);
        }

        for (int i = 0; i < recordSegment; i++) {
            if (anim && i == recordSegment - 1) {
                if (increase) {
                    canvas.drawLine(i * linePerStep + 2, getHeight() / 2, ((i + 1) * linePerStep - 2) * animFra, getHeight() / 2, paint2);
                    if (animFra == 1) {
                        anim = false;
                        increase = false;
                        lock = false;
                    }
                } else if (decrease) {
                    if (animFra == 1) {
                        anim = false;
                        decrease = false;
                        recordSegment--;
                        lock = false;
                        if(toBeDecrese!=0){
                            toBeDecrese--;
                            decreaseRecordCount();
                        }
                    } else {
                        canvas.drawLine(i * linePerStep + 2, getHeight() / 2, ((i + 1) * linePerStep - 2) * (1 - animFra), getHeight() / 2, paint2);
                    }
                }
            } else {
                canvas.drawLine(i * linePerStep + 2, getHeight() / 2, (i + 1) * linePerStep - 2, getHeight() / 2, paint2);
            }
        }
        for (int i = 0; i < segmentCount - 1; i++) {
            canvas.drawLine((i + 1) * linePerStep, 0, (i + 1) * linePerStep, getHeight(), paint3);
        }
    }

    /**
     * 纵向分段模式绘制
     * @param canvas
     */
    private void drawVertical(Canvas canvas) {
        int linePerStep = getHeight() / segmentCount;
        int perStepDotCount = dotCount / segmentCount;
        int dotPerStep = linePerStep / (perStepDotCount + 1);
        for (int i = 0; i < segmentCount; i++) {
            for (int j = 0; j < perStepDotCount; j++) {
                canvas.drawPoint(getWidth() / 2, (j + 1) * dotPerStep + i * linePerStep, paint1);
            }
        }
        if (segmentSize == 0) {
            paint2.setStrokeWidth(getWidth() * 2 / 3);
        } else {
            paint2.setStrokeWidth(segmentSize);
        }

        for (int i = 0; i < recordSegment; i++) {
            if (anim && i == recordSegment - 1) {
                if (increase) {
                    canvas.drawLine(getWidth() / 2, i * linePerStep + 2, getWidth() / 2, ((i + 1) * linePerStep - 2) * animFra, paint2);
                    if (animFra == 1) {
                        anim = false;
                        increase = false;
                        lock = false;
                    }
                } else if (decrease) {
                    if (animFra == 1) {
                        anim = false;
                        decrease = false;
                        recordSegment--;
                        lock = false;
                        if(toBeDecrese!=0){
                            toBeDecrese--;
                            decreaseRecordCount();
                        }
                    } else {
                        canvas.drawLine(getWidth() / 2, i * linePerStep + 2, getWidth() / 2, ((i + 1) * linePerStep - 2) * (1 - animFra), paint2);
                    }
                }
            } else {
                canvas.drawLine(getWidth() / 2, i * linePerStep + 2, getWidth()/2, (i + 1) * linePerStep - 2, paint2);
            }
        }
        for (int i = 0; i < segmentCount - 1; i++) {
            canvas.drawLine(0, (i + 1) * linePerStep, getWidth(), (i + 1) * linePerStep, paint3);
        }
    }

    /**
     * 横向自由模式绘制
     * @param canvas
     */
    private void drawFreeHorizontal(Canvas canvas) {
        int dotPerStep = getWidth() / (dotCount + 1);
        for (int i = 0; i < dotCount; i++) {
            canvas.drawPoint((i+1)*dotPerStep, getHeight() / 2, paint1);
        }
        if (segmentSize == 0) {
            paint2.setStrokeWidth(getHeight() * 2 / 3);
        } else {
            paint2.setStrokeWidth(segmentSize);
        }
        int startTime = 0;
        for (int i = 0; i < segList.size(); i++) {
            int startPosition=getWidth()*startTime/totalTime;
            int length=getWidth()*segList.get(i)/totalTime;
            if (anim && i == segList.size() - 1) {
                if (increase) {
                    canvas.drawLine(startPosition + 2, getHeight() / 2, (startPosition+length - 2) * animFra, getHeight() / 2, paint2);
                    if (animFra == 1) {
                        anim = false;
                        increase = false;
                        lock = false;
                    }
                } else if (decrease) {
                    if (animFra == 1) {
                        anim = false;
                        decrease = false;
                        int removed=segList.remove(segList.size()-1);
                        addedTime-=removed;
                        lock = false;
                        if(toBeDecrese!=0){
                            toBeDecrese--;
                            decreaseRecordCount();
                        }
                    } else {
                        canvas.drawLine(startPosition + 2, getHeight() / 2, (startPosition+length - 2) * (1 - animFra), getHeight() / 2, paint2);
                    }
                }
            } else {
                canvas.drawLine(startPosition + 2, getHeight() / 2, startPosition+length - 2, getHeight() / 2, paint2);
            }
            if(i<segList.size()){
                startTime+=segList.get(i);
            }
        }
        startTime=0;
        for (int i = 0; i < segList.size(); i++) {
            startTime+=segList.get(i);
            int linePosition=getWidth()*startTime/totalTime;
            if(i!=segList.size()-1){
                canvas.drawLine(linePosition, 0, linePosition, getHeight(), paint3);
            }else{
                if(!anim){
                    canvas.drawLine(linePosition, 0, linePosition, getHeight(), paint3);
                }
            }
        }
    }

    /**
     * 纵向自由模式绘制
     * @param canvas
     */
    private void drawFreeVertical(Canvas canvas) {
        int dotPerStep = getHeight() / (dotCount + 1);
        for (int i = 0; i < dotCount; i++) {
            canvas.drawPoint(getWidth() / 2 ,(i+1)*dotPerStep, paint1);
        }
        if (segmentSize == 0) {
            paint2.setStrokeWidth(getWidth() * 2 / 3);
        } else {
            paint2.setStrokeWidth(segmentSize);
        }
        int startTime = 0;
        for (int i = 0; i < segList.size(); i++) {
            int startPosition=getHeight()*startTime/totalTime;
            int length=getHeight()*segList.get(i)/totalTime;
            if (anim && i == segList.size() - 1) {
                if (increase) {
                    canvas.drawLine(getWidth() / 2, startPosition + 2, getWidth() / 2, (startPosition+length - 2) * animFra, paint2);
                    if (animFra == 1) {
                        anim = false;
                        increase = false;
                        lock = false;
                    }
                } else if (decrease) {
                    if (animFra == 1) {
                        anim = false;
                        decrease = false;
                        int removed=segList.remove(segList.size()-1);
                        addedTime-=removed;
                        lock = false;
                        if(toBeDecrese!=0){
                            toBeDecrese--;
                            decreaseRecordCount();
                        }
                    } else {
                        canvas.drawLine(getWidth() / 2, startPosition + 2, getWidth() / 2, (startPosition+length - 2) * (1 - animFra), paint2);
                    }
                }
            } else {
                canvas.drawLine(getWidth() / 2, startPosition + 2, getWidth() / 2, startPosition+length - 2, paint2);
            }
            if(i<segList.size()){
                startTime+=segList.get(i);
            }
        }
        startTime=0;
        for (int i = 0; i < segList.size(); i++) {
            startTime+=segList.get(i);
            int linePosition=getHeight()*startTime/totalTime;
            if(i!=segList.size()-1){
                canvas.drawLine(0, linePosition, getWidth(), linePosition, paint3);
            }else{
                if(!anim){
                    canvas.drawLine(0, linePosition, getWidth(), linePosition, paint3);
                }
            }
        }
    }

    /**
     * 分段模式设置最大段数
     * @param segmentCount
     */
    public void setSegmentCount(int segmentCount) {
        this.segmentCount = segmentCount;
        invalidate();
    }

    /**
     * 设置当前已拍摄的段数,无动画
     * @param recordSegment
     */
    public void setRecordCount(int recordSegment) {
        this.recordSegment = recordSegment;
        invalidate();
    }

    /**
     * 分段模式增加一段
     */
    public void increaseRecordCount() {
        if (recordSegment >= segmentCount || lock) {
            return;
        }
        lock = true;
        recordSegment++;
        anim = true;
        increase = true;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animFra = animation.getAnimatedFraction();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 删除分段模式的最后一段
     */
    public void decreaseRecordCount() {
        if(lock){
            toBeDecrese++;
        }
        if (recordSegment <= 0 || lock) {
            return;
        }
        lock = true;
        anim = true;
        decrease = true;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animFra = animation.getAnimatedFraction();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 添加自由模式数据集
     * @param segList
     */
    public void addSegList(List<Integer> segList){
        this.segList=segList;
        for(int i=0;i<segList.size();i++){
            addedTime+=segList.get(i);
        }
        invalidate();
    }

    /**
     * 增加自由模式的一段
     * @param segLength 时长
     */
    public void addFreeRecordCount(int segLength){
        if (addedTime+segLength > totalTime || lock) {
            return;
        }
        addedTime+=segLength;
        segList.add(segLength);
        lock = true;
        anim = true;
        increase = true;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animFra = animation.getAnimatedFraction();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 删除自由模式中的最后一段
     */
    public void subFreeRecordCount(){
        if(segList.size()==0||lock){
            return;
        }
        lock = true;
        anim = true;
        decrease = true;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animFra = animation.getAnimatedFraction();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 返回已录制的时间总和
     * @return
     */
    public int getAddedTime(){
        if(anim&&decrease&&lock){
            if(segList.size()>0){
                return addedTime-segList.get(segList.size()-1);
            }
        }
        return addedTime;
    }

    /**
     * 设置拍摄模式
     * @param recordMode
     */
    public void setRecordMode(int recordMode){
        this.recordMode=recordMode;
        invalidate();
    }

    /**
     * 自由模式最大时长
     * @param totalTime 最大时长,毫秒级
     */
    public void setTotalTime(int totalTime){
        this.totalTime=totalTime;
    }

}
