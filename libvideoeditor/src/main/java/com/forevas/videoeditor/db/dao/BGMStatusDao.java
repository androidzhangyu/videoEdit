package com.forevas.videoeditor.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forevas.videoeditor.db.DBConstants;
import com.forevas.videoeditor.db.DBHelper;
import com.forevas.videoeditor.db.ExDatabaseContext;
import com.forevas.videoeditor.db.bean.BGMStatus;

/**
 * Created by carden
 */

public class BGMStatusDao {
    private DBHelper dbHelper;
    public BGMStatusDao(Context context){
        dbHelper=new DBHelper(new ExDatabaseContext(context));
    }
    public BGMStatus getBGMStatusByUrl(String url){
        if(url==null){
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BGMStatus bgm = null;
        Cursor cursor = db.query(DBConstants.BGMStatus.TABLE_NAME, null, "url=?", new String[]{url}, null, null, null);
        if(cursor.moveToFirst()){
            bgm=new BGMStatus();
            bgm.id=cursor.getString(cursor.getColumnIndex(DBConstants.BGMStatus.COLUMN_ID));
            bgm.url=cursor.getString(cursor.getColumnIndex(DBConstants.BGMStatus.COLUMN_URL));
            bgm.localPath=cursor.getString(cursor.getColumnIndex(DBConstants.BGMStatus.COLUMN_LOCALPATH));
            bgm.status=cursor.getInt(cursor.getColumnIndex(DBConstants.BGMStatus.COLUMN_STATUS));
            bgm.progress=cursor.getInt(cursor.getColumnIndex(DBConstants.BGMStatus.COLUMN_PROGRESS));
        }
        cursor.close();
        db.close();
        return bgm;
    }
    public void replaceBGMStatus(BGMStatus bgm){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DBConstants.BGMStatus.COLUMN_ID,bgm.id);
        values.put(DBConstants.BGMStatus.COLUMN_URL,bgm.url);
        values.put(DBConstants.BGMStatus.COLUMN_LOCALPATH,bgm.localPath);
        values.put(DBConstants.BGMStatus.COLUMN_STATUS,bgm.status);
        values.put(DBConstants.BGMStatus.COLUMN_PROGRESS,bgm.progress);
        db.replace(DBConstants.BGMStatus.TABLE_NAME,null,values);
        db.close();
    }
}
