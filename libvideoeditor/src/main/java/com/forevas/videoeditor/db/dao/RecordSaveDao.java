package com.forevas.videoeditor.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forevas.videoeditor.db.DBConstants;
import com.forevas.videoeditor.db.DBHelper;
import com.forevas.videoeditor.db.ExDatabaseContext;
import com.forevas.videoeditor.db.bean.RecordSave;

/**
 * Created by carden
 */

public class RecordSaveDao {
    DBHelper dbHelper;

    public RecordSaveDao(Context context) {
        dbHelper = new DBHelper(new ExDatabaseContext(context));
    }

    public RecordSave getRecordSaveByUid(String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        RecordSave save = null;
        Cursor cursor = db.query(DBConstants.RecordSave.TABLE_NAME, null, DBConstants.RecordSave.COLUMN_UID + "=?", new String[]{uid}, null, null, null);
        if (cursor.moveToFirst()) {
            save = new RecordSave();
            save.uid = cursor.getString(cursor.getColumnIndex(DBConstants.RecordSave.COLUMN_UID));
            save.mode = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordSave.COLUMN_MODE));
            save.dur = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordSave.COLUMN_DUR));
            save.seg = cursor.getInt(cursor.getColumnIndex(DBConstants.RecordSave.COLUMN_SEG));
            save.data=cursor.getString(cursor.getColumnIndex(DBConstants.RecordSave.COLUMN_DATA));
        }
        cursor.close();
        db.close();
        return save;
    }

    public void replaceRecordSave(RecordSave save) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DBConstants.RecordSave.COLUMN_UID,save.uid);
        values.put(DBConstants.RecordSave.COLUMN_MODE,save.mode);
        values.put(DBConstants.RecordSave.COLUMN_DUR,save.dur);
        values.put(DBConstants.RecordSave.COLUMN_SEG,save.seg);
        values.put(DBConstants.RecordSave.COLUMN_DATA,save.data);
        db.replace(DBConstants.RecordSave.TABLE_NAME,null,values);
        db.close();
    }
    public void deleteRecordSaveByUid(String uid){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBConstants.RecordSave.TABLE_NAME,DBConstants.RecordSave.COLUMN_UID+"=?",new String[]{uid});
        db.close();
    }
}
