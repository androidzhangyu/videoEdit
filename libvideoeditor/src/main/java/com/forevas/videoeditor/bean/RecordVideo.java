package com.forevas.videoeditor.bean;

/**
 * Created by carden
 */

public class RecordVideo {
    //视频本身的信息
    public String path;
    public long videoDur;//毫秒级
    //拍摄配置
    public int mode;
    public long dur;
    public int seg;
    //视频格式,用于数据埋点统计
    public String src;//本地或者录制
    public String filter;//滤镜
    public int beauty;//否0 是1
    public int camera;//前置0 后置1 前后组合2 本地视频默认0
}
