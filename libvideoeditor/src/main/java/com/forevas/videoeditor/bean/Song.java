package com.forevas.videoeditor.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * desc 歌曲的bean
 */

public class Song implements Parcelable {
    @SerializedName("seqId")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("duration")
    public int duration;
    @SerializedName("author")
    public String author;
    @SerializedName("album")
    public String album;
    @SerializedName("language")
    public String language;
    public String genre;
    @SerializedName("url")
    public String url;
    @SerializedName("extension")
    public String extension;
    public int location;//0本地,1在线
    public int status;//0未下载,1,下载中,2已下载
    public String localPath;
    public int progress;
    public long totalSize;
    public boolean selected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public String getLocalPath(){
        return localPath;
    }
    public void setLocalPath(String localPath){
        this.localPath=localPath;
    }
    public int getProgress(){
        return progress;
    }
    public void setProgress(int progress){
        this.progress=progress;
    }
    public long getTotalSize(){
        return totalSize;
    }
    public void setTotalSize(long totalSize){
        this.totalSize=totalSize;
    }
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.duration);
        dest.writeString(this.author);
        dest.writeString(this.album);
        dest.writeString(this.language);
        dest.writeString(this.genre);
        dest.writeString(this.url);
        dest.writeString(this.extension);
        dest.writeInt(this.location);
        dest.writeInt(this.status);
        dest.writeString(this.localPath);
        dest.writeInt(this.progress);
        dest.writeLong(this.totalSize);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    public Song() {
    }

    protected Song(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.duration = in.readInt();
        this.author = in.readString();
        this.album = in.readString();
        this.language = in.readString();
        this.genre = in.readString();
        this.url = in.readString();
        this.extension = in.readString();
        this.location = in.readInt();
        this.status = in.readInt();
        this.localPath=in.readString();
        this.progress=in.readInt();
        this.totalSize=in.readLong();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
