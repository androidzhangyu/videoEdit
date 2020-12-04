package com.forevas.videoeditor.jni;

/**
 * desc 混音操作工具类
 */

public class AudioJniUtils {


    static {
        System.loadLibrary("AudioJniUtils");
    }
    public static native byte[] audioMix(byte[] sourceA,byte[] sourceB,byte[] dst,float firstVol , float secondVol);
}
