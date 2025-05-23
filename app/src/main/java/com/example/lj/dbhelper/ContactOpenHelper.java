package com.example.lj.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

public class ContactOpenHelper extends SQLiteOpenHelper {

    public static final String T_CONTACT = "t_contact";

    public class ContactTable implements BaseColumns {
        public static final String ACCOUNT = "account";
        public static final String STATUS = "status";
        public static final String NICKNAME = "nickname";
        public static final String AVATAR = "avatar";
        public static final String PINYIN = "pinyin";
    }

    public ContactOpenHelper(@Nullable Context context) {
        super(context, "contact.db", null, 2); // 版本号改为2，触发升级
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + T_CONTACT + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ContactTable.ACCOUNT + " TEXT,"
                + ContactTable.NICKNAME + " TEXT,"
                + ContactTable.STATUS + " INTEGER,"     // 加入status字段
                + ContactTable.AVATAR + " TEXT,"
                + ContactTable.PINYIN + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + T_CONTACT + " ADD COLUMN " + ContactTable.STATUS + " INTEGER DEFAULT 0");
        }
    }
}
