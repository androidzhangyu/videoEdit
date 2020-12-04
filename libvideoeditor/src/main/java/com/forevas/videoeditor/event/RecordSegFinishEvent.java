package com.forevas.videoeditor.event;

/**
 * Created by carden
 * 每段视频拍摄完成都要发送这个event
 */

public class RecordSegFinishEvent {
    public RecordSegFinishEvent(String path, boolean flag) {
        this.path = path;
        this.flag = flag;
    }
    public String path;
    public boolean flag;//录制完成标志
}
