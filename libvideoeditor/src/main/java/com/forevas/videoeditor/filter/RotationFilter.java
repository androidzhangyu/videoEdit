package com.forevas.videoeditor.filter;

import android.content.res.Resources;

/**
 * Created by carden
 */

public class RotationFilter extends NoFilter {
    public static final int ROT_0 = 0;
    public static final int ROT_90 = 90;
    public static final int ROT_180 = 180;
    public static final int ROT_270 = 270;

    public RotationFilter(Resources mRes) {
        super(mRes);
    }

    /**
     * 旋转视频操作
     *
     * @param rotation
     */
    public void setRotation(int rotation) {
        float[] coord;
        switch (rotation) {
            case ROT_0:
                coord = new float[]{
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                };
                break;
            case ROT_90:
                coord = new float[]{
                        0.0f, 0.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f
                };
                break;
            case ROT_180:
                coord = new float[]{
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                };
                break;
            case ROT_270:
                coord = new float[]{
                        1.0f, 1.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 0.0f
                };
                break;
            default:
                return;
        }
        mTexBuffer.clear();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }
}
