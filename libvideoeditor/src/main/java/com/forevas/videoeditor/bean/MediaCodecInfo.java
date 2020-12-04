package com.forevas.videoeditor.bean;

import android.media.MediaExtractor;

/**
 * desc 音频解码的info类 包含了音频path 音频的MediaExtractor
 * 和本段音频的截取点cutPoint
 * 以及剪切时长 cutDuration
 */

public class MediaCodecInfo {
    public String path;
    public MediaExtractor extractor;
    public int cutPoint;
    public int cutDuration;
    public int duration;
}
