package com.example.lj.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SmsOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "im.db";
    public static final int VERSION = 1;

    public static class SmsTable {
        public static final String TABLE_NAME = "sms";
        public static final int TYPE_RECEIVE = 1;
        public static final int TYPE_SEND = 2;
        public static final String FROM_ACCOUNT = "from_account";
        public static final String TO_ACCOUNT = "to_account";
        public static final String SESSION_ACCOUNT = "session_account";
        public static final String _ID = BaseColumns._ID;
        public static final String BODY = "body";
        public static final String TIME = "time";
        public static final String TYPE = "type";
    }

    public SmsOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + SmsTable.TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SmsTable.FROM_ACCOUNT + " TEXT,"
                + SmsTable.TO_ACCOUNT + " TEXT,"
                + SmsTable.SESSION_ACCOUNT + " TEXT,"
                + SmsTable.BODY + " TEXT,"
                + SmsTable.TIME + " LONG,"
                + SmsTable.TYPE + " INTEGER"
                + ")";
        db.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SmsTable.TABLE_NAME);
        onCreate(db);
    }
}
