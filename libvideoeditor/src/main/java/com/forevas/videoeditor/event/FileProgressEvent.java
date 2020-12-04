package com.forevas.videoeditor.event;

/**
 * carden
 */
public class FileProgressEvent {
    private final String url;
    private final String destPath;
    private final String destFileName;
    private final int progress;
    private final long totalSize;

    public FileProgressEvent(String url, String destPath, String destFileName, int progress, long totalSize) {
        this.url = url;
        this.destPath = destPath;
        this.destFileName = destFileName;
        this.progress = progress;
        this.totalSize = totalSize;
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

    public int getProgress() {
        return progress;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
