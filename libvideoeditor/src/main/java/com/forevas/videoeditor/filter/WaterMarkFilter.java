package com.forevas.videoeditor.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.VideoEditorSDK;
import com.forevas.videoeditor.utils.BitMapUtils;

/**
 * Description:
 */
public class WaterMarkFilter extends NoFilter {

    private Bitmap mBitmap;
    private NoFilter mFilter;
    private int width,height;

    private int x,y,w,h;

    public WaterMarkFilter(Resources mRes) {
        super(mRes);
        mFilter=new NoFilter(mRes){
            @Override
            protected void onClear() {

            }
        };
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mFilter.create();
        createTexture();
    }

    @Override
    protected void onClear() {
        super.onClear();
    }

    @Override
    public void draw() {
        super.draw();
        //水印 颠倒抵消
        if (mBitmap!=null) {
            GLES20.glViewport(x, y, w == 0 ? mBitmap.getWidth() : w, (int) (h == 0 ? mBitmap.getHeight() * heightRatio : h * heightRatio));
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_DST_ALPHA);
            mFilter.draw();
            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glViewport(0, 0, width, height);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        this.width=width;
        this.height=height;
        mFilter.setSize(width,height);
    }

    public void setWaterMark(Bitmap bitmap){
        if(this.mBitmap!=null){
            this.mBitmap.recycle();
        }
        this.mBitmap=bitmap;
    }

    private int[] textures=new int[1];
    private void createTexture(){
        if(mBitmap!=null){
            //生成纹理
            GLES20.glGenTextures(1,textures,0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
//
// MatrixUtils.flip(mFilter.getMatrix(),false,true);

            mFilter.setTextureId(textures[0]);
        }
    }

    public void setPosition(int x,int y,int width,int height){
        this.x=x;
        this.y=y;
        this.w=width;
        this.h=height;
    }
    public void setHeightRatio(float ratio){
        this.heightRatio=ratio;
    }
    private float heightRatio=1.0f;
}
