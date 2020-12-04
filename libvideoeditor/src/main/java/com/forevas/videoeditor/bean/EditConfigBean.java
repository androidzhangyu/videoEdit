package com.forevas.videoeditor.bean;

public class EditConfigBean {
    public Song song;
    public AudioSettingInfo settingInfo;
    public void setSong(Song song){
        if(song==null){
            this.song=null;
        }else{
            this.song=new Song();
            this.song.id=song.id;
            this.song.name=song.name;
            this.song.url=song.url;
            this.song.location=song.location;
            this.song.localPath=song.localPath;
        }
    }
    public void setAudioSettingInfo(AudioSettingInfo settingInfo){
        if(settingInfo==null){
            this.settingInfo=null;
        }else{
            this.settingInfo=new AudioSettingInfo();
            this.settingInfo.filePath=settingInfo.filePath;
            this.settingInfo.volFirst=settingInfo.volFirst;
            this.settingInfo.volSecond=settingInfo.volSecond;
        }
    }
    public boolean equals(Song song,AudioSettingInfo settingInfo){
        boolean songEquals=false;
        if(this.song==null&&song==null){
            songEquals=true;
        }else if(this.song==null||song==null){
            songEquals=false;
        }else if(this.song.id==song.id&&this.song.name==song.name&&this.song.url==song.url
                &&this.song.location==song.location&&this.song.localPath==song.localPath){
            songEquals=true;
        }
        boolean audioEquals=false;
        if(this.settingInfo==null&&settingInfo==null){
            audioEquals=true;
        }else if(this.settingInfo==null||settingInfo==null){
            audioEquals=false;
        }else if(this.settingInfo.filePath==settingInfo.filePath&&this.settingInfo.volFirst==settingInfo.volFirst&&this.settingInfo.volSecond==settingInfo.volSecond){
            audioEquals=true;
        }
        return songEquals&&audioEquals;
    }
}
