package com.forevas.videoeditor.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**

 */

public class SharedPreferenceUtils {
    public static final String PREFERENCES_NAME="VideoEditor_1";
    public static int getInt(Context context,String key,int defValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        int result = sharedPreferences.getInt(key, defValue);
        return  result;
    }
    public static void setInt(Context context,String key,int value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key,value);
        edit.commit();
    }
}
