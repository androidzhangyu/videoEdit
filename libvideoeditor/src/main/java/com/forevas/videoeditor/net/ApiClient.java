package com.forevas.videoeditor.net;


import com.forevas.videoeditor.constants.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 *Created by carden
 */

public class ApiClient {
    public static OkHttpClient okHttpClient;
    static{
        okHttpClient=new OkHttpClient.Builder().
                connectTimeout(15*1000, TimeUnit.MILLISECONDS)
                .readTimeout(15*1000, TimeUnit.MILLISECONDS)
                .writeTimeout(15*1000, TimeUnit.MILLISECONDS)
                .build();
    }
    public static OkHttpClient getHttpClient(){
        return okHttpClient;
    }
}
