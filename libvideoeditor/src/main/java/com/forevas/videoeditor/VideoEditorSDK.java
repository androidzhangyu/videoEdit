package com.forevas.videoeditor;

import android.content.Context;

import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.event.EditorFinishEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;


/**
 * Created by carden
 */

public class VideoEditorSDK {
    private static final VideoEditorSDK INSTANCE=new VideoEditorSDK();
    private Context mContext;
    private VideoEditorSDK(){}
    public static VideoEditorSDK getInstance(){
        return INSTANCE;
    }
    public void init(Context context){
        mContext=context;
        Constants.init(context);
    }
    public Context getContext(){
        return mContext;
    }
    public void finishEditor(){
        EventBus.getDefault().post(new EditorFinishEvent());
        clearCache();
    }
    public void clearCache(){
        String baseFolder = Constants.getBaseFolder();
        File recordDir=new File(baseFolder+"video/record/");
        File clipDir=new File(baseFolder+"video/clip/");
        File tempDir=new File(baseFolder+"video/temp/");
        if(recordDir.exists()){
            File[] files = recordDir.listFiles();
            for(File file:files){
                if(file.exists()&&!file.isDirectory()){
                    file.delete();
                }
            }
        }
        if(clipDir.exists()){
            File[] files = clipDir.listFiles();
            for(File file:files){
                if(file.exists()&&!file.isDirectory()){
                    file.delete();
                }
            }
        }
        if(tempDir.exists()){
            File[] files = tempDir.listFiles();
            for(File file:files){
                if(file.exists()&&!file.isDirectory()){
                    file.delete();
                }
            }
        }
    }
}
