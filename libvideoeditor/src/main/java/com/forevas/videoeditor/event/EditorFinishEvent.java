package com.forevas.videoeditor.event;

/**
 *Created by carden
 */

public class EditorFinishEvent {
    public   String path;
public  long endTime;
    public EditorFinishEvent(String path,long endTime) {
        this.path = path;
        this.endTime=endTime;
    }

    public EditorFinishEvent() {
    }
}
