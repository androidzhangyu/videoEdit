package com.forevas.videoeditor.utils;

public class FileUtils {

    public static String fileSizeFormat(long duration){
        int a = (int) (duration/1000 + 0.5);
        return a+"";
    }
}
