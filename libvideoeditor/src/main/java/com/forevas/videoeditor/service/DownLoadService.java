package com.forevas.videoeditor.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.forevas.videoeditor.VideoEditorSDK;
import com.forevas.videoeditor.event.FileDownloadEvent;
import com.forevas.videoeditor.event.FileProgressEvent;
import com.forevas.videoeditor.net.ApiClient;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadService extends Service {
    private static final String TAG = "DownLoadService";
    private static final String INTENT_KEY_REMOTE_URL = "remote_url";
    private static final String INTENT_KEY_DEST_DIR = "dest_dir";
    private static final String INTENT_KEY_DEST_FILE_NAME = "dest_file_name";
    private OkHttpClient httpClient;
    private final List<String> tags = new ArrayList<>();

    public DownLoadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
//        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (intent != null) {
            String fileName = intent.getStringExtra(INTENT_KEY_DEST_FILE_NAME);
            String url = intent.getStringExtra(INTENT_KEY_REMOTE_URL);
            String destDir = intent.getStringExtra(INTENT_KEY_DEST_DIR);
            if (httpClient == null) {
                httpClient = ApiClient.getHttpClient();
            }
            submitDownloadFileTask(url, destDir, fileName);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (httpClient != null) {
            List<Call> runningCalls = httpClient.dispatcher().runningCalls();
            for (Call call : runningCalls) {
                Object tag = call.request().tag();
                if (tag instanceof String) {
                    String tagUrl = (String) tag;
                    if (tags.contains(tagUrl)) {
                        call.cancel();
                        tags.remove(tagUrl);
                    }
                }
            }
        }

//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private void submitDownloadFileTask(final String url, final String destPath, final String destFileName) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(destPath) || TextUtils.isEmpty(destFileName)) {
            Log.e(TAG, "downloadFile: Illegal argument");
            return;
        }
        /*if (new File(destPath, destFileName).exists()) {
            EventBus.getDefault().post(new FileDownloadEvent(url, destPath, destFileName));
        }*/
        List<Call> runningCalls = httpClient.dispatcher().runningCalls();
        for (Call call : runningCalls) {
            if (url.equals(call.request().tag())) {
                Log.d(TAG, "downloadFile: file is downloading" + url);
                return;
            }
        }
        Request request = new Request.Builder().url(url).tag(url).build();
        tags.add(url);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                tags.remove(url);
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File dir = new File(destPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, destFileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        is.skip(file.length());
                    }
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        EventBus.getDefault().post(new FileProgressEvent(url, destPath, destFileName, progress,total));
                        Log.d(TAG, "progress=" + progress);
                    }
                    fos.flush();
                    EventBus.getDefault().post(new FileDownloadEvent(url, destPath, destFileName));
                    Log.d(TAG, "文件下载成功");
                } catch (Exception e) {
                    Log.e(TAG, "文件下载失败", e);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException ignored) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException ignored) {
                    }
                    tags.remove(url);
                }
            }
        });
    }


    public static void downloadFile(String url, String destDir, String destFileName) {
        Context context = VideoEditorSDK.getInstance().getContext();
        Intent intent = new Intent(context, DownLoadService.class);
        intent.putExtra(INTENT_KEY_REMOTE_URL, url);
        intent.putExtra(INTENT_KEY_DEST_DIR, destDir);
        intent.putExtra(INTENT_KEY_DEST_FILE_NAME, destFileName);
        context.startService(intent);
    }

}
