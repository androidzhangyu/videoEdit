package com.forevas.videoeditor.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forevas.videoeditor.db.DBConstants;
import com.forevas.videoeditor.db.DBHelper;
import com.forevas.videoeditor.db.ExDatabaseContext;
import com.forevas.videoeditor.db.bean.RecordConfig;

/**
 * Created by carden
 */

public class RecordConfigDao {
    DBHelper dbHelper;

    public RecordConfigDao(Context context) {
        dbHelper = new DBHelper(new ExDatabaseContext(context));
    }

    public RecordConfig getRecordConfigByUid(String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        RecordConfig config = null;
        Cursor cursor = db.query(DBConstants.RecordConfig.TABLE_NAME, null, DBConstants.RecordConfig.COLUMN_UID + "=?", new String[]{uid}, null, null, null);
        if (cursor.moveToFirst()) {
            config = new RecordConfig();
            config.uid = cursor.getString(cursor.getColumnIndex(DBConstants.RecordConfig.COLUMN_UID));
            config.mode = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordConfig.COLUMN_MODE));
            config.dur = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordConfig.COLUMN_DUR));
            config.seg = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordConfig.COLUMN_SEG));
        }
        cursor.close();
        db.close();
        return config;
    }

    public void replaceRecordConfig(RecordConfig config) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DBConstants.RecordConfig.COLUMN_UID,config.uid);
        values.put(DBConstants.RecordConfig.COLUMN_MODE,config.mode);
        values.put(DBConstants.RecordConfig.COLUMN_DUR,config.dur);
        values.put(DBConstants.RecordConfig.COLUMN_SEG,config.seg);
        db.replace(DBConstants.RecordConfig.TABLE_NAME,null,values);
        db.close();
    }
}
