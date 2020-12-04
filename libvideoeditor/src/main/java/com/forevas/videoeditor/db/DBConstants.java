package com.forevas.videoeditor.db;

/**
 * Created by carden
 */

public class DBConstants {
    public static class RecordConfig{
        public static String TABLE_NAME="Record_Config";
        public static String COLUMN_UID="uid";
        public static String COLUMN_MODE="mode";
        public static String COLUMN_DUR="dur";
        public static String COLUMN_SEG="seg";
    }
    public static class BGMStatus{
        public static String TABLE_NAME="BGM_Status";
        public static String COLUMN_ID="id";
        public static String COLUMN_URL="url";
        public static String COLUMN_LOCALPATH="localpath";
        public static String COLUMN_STATUS="status";
        public static String COLUMN_PROGRESS="progress";
    }
    public static class RecordSave{
        public static String TABLE_NAME="Record_Save";
        public static String COLUMN_UID="uid";
        public static String COLUMN_MODE="mode";
        public static String COLUMN_DUR="dur";
        public static String COLUMN_SEG="seg";
        public static String COLUMN_DATA="data";
    }
}
