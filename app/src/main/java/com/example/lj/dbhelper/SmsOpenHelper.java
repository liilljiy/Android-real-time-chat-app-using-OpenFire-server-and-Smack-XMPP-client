package com.example.lj.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "im.db";
    public static final int VERSION = 2;  // 升级版本号

    public static class SmsTable {
        public static final String TABLE_NAME = "sms";
        public static final String _ID = "_id";
        public static final String FROM_ACCOUNT = "from_account";
        public static final String TO_ACCOUNT = "to_account";
        public static final String BODY = "body";
        public static final String TIME = "time";
        public static final String TYPE = "type";
        public static final String SESSION_ID = "session_id"; // 如果需要此字段
        // SESSION_ACCOUNT 不作为数据库字段，用TO_ACCOUNT代替
        public static final int TYPE_RECEIVE = 0;  // 收到的消息
        public static final int TYPE_SEND = 1;     // 发送的消息
        public static final String SESSION_ACCOUNT = TO_ACCOUNT;
    }

    public SmsOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + SmsTable.TABLE_NAME + " ("
                + SmsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SmsTable.FROM_ACCOUNT + " TEXT, "
                + SmsTable.TO_ACCOUNT + " TEXT, "
                + SmsTable.BODY + " TEXT, "
                + SmsTable.TIME + " LONG, "
                + SmsTable.TYPE + " INTEGER, "
                + SmsTable.SESSION_ID + " TEXT"  // 明确写出session_id字段
                + ")";
        db.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 升级时增加session_id字段
            db.execSQL("ALTER TABLE " + SmsTable.TABLE_NAME + " ADD COLUMN " + SmsTable.SESSION_ID + " TEXT");
        }
    }
}
