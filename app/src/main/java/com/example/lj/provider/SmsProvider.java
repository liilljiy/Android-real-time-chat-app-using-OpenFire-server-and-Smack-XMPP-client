package com.example.lj.provider;

import android.content.ContentProvider;
import android.content.ContentUris; // 导入 ContentUris
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log; // 导入 Log

import androidx.annotation.NonNull; // 导入 NonNull
import androidx.annotation.Nullable; // 导入 Nullable

import com.example.lj.dbhelper.SmsOpenHelper;

public class SmsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.lj.provider.SmsProvider";
//    public static final String AUTHORITIES = ContactProvider.class.getCanonicalName();
    public static final Uri URI_SMS = Uri.parse("content://" + AUTHORITY + "/" + SmsOpenHelper.SmsTable.TABLE_NAME); // 使用 SmsTable.TABLE_NAME 构建 Uri

    // 引用 SmsOpenHelper 中的类型常量
    public static final int TYPE_RECEIVE = SmsOpenHelper.SmsTable.TYPE_RECEIVE;
    public static final int TYPE_SEND = SmsOpenHelper.SmsTable.TYPE_SEND;

    private static final int SMS = 1;
    private static final UriMatcher sUriMatcher;

    private static final String TAG = "SmsProvider"; // 添加 TAG

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SmsOpenHelper.SmsTable.TABLE_NAME, SMS); // 使用 SmsTable.TABLE_NAME
    }

    private SmsOpenHelper helper;

    @Override
    public boolean onCreate() {
        helper = new SmsOpenHelper(getContext());
        return helper != null; // onCreate 必须返回 true 表示成功创建
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri); // 使用 match 变量
        Cursor cursor = null; // 初始化 cursor
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getReadableDatabase();
                cursor = db.query(SmsOpenHelper.SmsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                if (cursor != null) {
                    Log.i(TAG, "query Success!!!!!");
                    // ！！重要：设置通知 URI ！！
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for query: " + uri); // 抛出异常
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri); // 使用 match 变量
        Uri returnUri = null; // 初始化 returnUri
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                long id = db.insert(SmsOpenHelper.SmsTable.TABLE_NAME, null, values);
                if (id > 0) {
                    Log.i(TAG, "insert Success!!!!!");
                    returnUri = ContentUris.withAppendedId(uri, id);
                    // ！！重要：通知数据改变 ！！
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.e(TAG, "insert failed for URI: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for insert: " + uri); // 抛出异常
        }
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int updateCount = 0; // 初始化 updateCount
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                updateCount = db.update(SmsOpenHelper.SmsTable.TABLE_NAME, values, selection, selectionArgs);
                if (updateCount > 0) {
                    Log.i(TAG, "update Success!!!!!");
                    // ！！重要：通知数据改变 ！！
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.w(TAG, "update affected 0 rows for URI: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for update: " + uri); // 抛出异常
        }
        return updateCount;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int deleteCount = 0; // 初始化 deleteCount
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                deleteCount = db.delete(SmsOpenHelper.SmsTable.TABLE_NAME, selection, selectionArgs);
                if (deleteCount > 0) {
                    Log.i(TAG, "delete Success!!!!!");
                    // ！！重要：通知数据改变 ！！
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.w(TAG, "delete affected 0 rows for URI: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for delete: " + uri); // 抛出异常
        }
        return deleteCount;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // 根据 URI 返回 MIME 类型，这里简单返回 null
        return null;
    }
}