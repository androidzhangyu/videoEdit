package com.forevas.videoeditor.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by carden
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="video_editor.db";
    public static final int DB_VERSION=1;
    public DBHelper(ExDatabaseContext context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table_config="create table if not exists "+DBConstants.RecordConfig.TABLE_NAME+ "("+
                DBConstants.RecordConfig.COLUMN_UID+" text primary key,"+
                DBConstants.RecordConfig.COLUMN_MODE+" integer,"+
                DBConstants.RecordConfig.COLUMN_DUR+" integer,"+
                DBConstants.RecordConfig.COLUMN_SEG+" integer)";
        String table_bgm="create table if not exists "+DBConstants.BGMStatus.TABLE_NAME+"("+
                DBConstants.BGMStatus.COLUMN_ID+" text primary key,"+
                DBConstants.BGMStatus.COLUMN_URL+" text,"+
                DBConstants.BGMStatus.COLUMN_LOCALPATH+" text,"+
                DBConstants.BGMStatus.COLUMN_STATUS+" integer,"+
                DBConstants.BGMStatus.COLUMN_PROGRESS+" integer)";
        String table_save="create table if not exists "+DBConstants.RecordSave.TABLE_NAME+ "("+
                DBConstants.RecordSave.COLUMN_UID+" text primary key,"+
                DBConstants.RecordSave.COLUMN_MODE+" integer,"+
                DBConstants.RecordSave.COLUMN_DUR+" integer,"+
                DBConstants.RecordSave.COLUMN_SEG+" integer,"+
                DBConstants.RecordSave.COLUMN_DATA+" text)";
        db.execSQL(table_config);
        db.execSQL(table_bgm);
        db.execSQL(table_save);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
