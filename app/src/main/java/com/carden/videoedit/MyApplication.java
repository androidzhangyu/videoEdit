package com.carden.videoedit;

import android.app.Application;

import com.forevas.videoeditor.VideoEditorSDK;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoEditorSDK.getInstance().init(this);
    }
}
