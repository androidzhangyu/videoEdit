package com.forevas.videoeditor.event;

import java.io.File;

/**
 * Created by carden
 */
public class FileDownloadEvent {

    private final String url;
    private final String destPath;
    private final String destFileName;

    public FileDownloadEvent(String url, String destPath, String destFileName) {
        this.url = url;
        this.destPath = destPath;
        this.destFileName = destFileName;
    }

    public String getUrl() {
        return url;
    }

    public String getDestPath() {
        return destPath;
    }

    public String getDestFileName() {
        return destFileName;
    }

    public String getAbsolutePath() {
        return new File(destPath, destFileName).getAbsolutePath();
    }
}
