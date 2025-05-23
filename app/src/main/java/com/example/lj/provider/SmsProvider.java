package com.example.lj.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lj.dbhelper.SmsOpenHelper;

public class SmsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.lj.provider.SmsProvider";
    public static final Uri URI_SMS = Uri.parse("content://" + AUTHORITY + "/" + SmsOpenHelper.SmsTable.TABLE_NAME);
    public static final Uri URI_SESSION = Uri.parse("content://" + AUTHORITY + "/session");

    public static final int TYPE_RECEIVE = SmsOpenHelper.SmsTable.TYPE_RECEIVE;
    public static final int TYPE_SEND = SmsOpenHelper.SmsTable.TYPE_SEND;

    private static final int SMS = 1;
    private static final int SESSION = 2; // 新增支持 session 查询

    private static final UriMatcher sUriMatcher;
    private static final String TAG = "SmsProvider";

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SmsOpenHelper.SmsTable.TABLE_NAME, SMS);
        sUriMatcher.addURI(AUTHORITY, "session", SESSION); // 注册 session 路径
    }

    private SmsOpenHelper helper;

    @Override
    public boolean onCreate() {
        helper = new SmsOpenHelper(getContext());
        return helper != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        Cursor cursor;
        SQLiteDatabase db = helper.getReadableDatabase();

        switch (match) {
            case SMS:
                cursor = db.query(SmsOpenHelper.SmsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SESSION:
                // 示例 SQL：每个会话(session_id)取最新一条消息
                String sql = "SELECT * FROM " + SmsOpenHelper.SmsTable.TABLE_NAME +
                        " WHERE _id IN (SELECT MAX(_id) FROM " + SmsOpenHelper.SmsTable.TABLE_NAME +
                        " GROUP BY session_id) ORDER BY time DESC";
                cursor = db.rawQuery(sql, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for query: " + uri);
        }

        if (cursor != null) {
            Log.i(TAG, "query Success!!!!!");
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                long id = db.insert(SmsOpenHelper.SmsTable.TABLE_NAME, null, values);
                if (id > 0) {
                    Log.i(TAG, "insert Success!!!!!");
                    returnUri = ContentUris.withAppendedId(uri, id);
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.e(TAG, "insert failed for URI: " + uri);
                    returnUri = null;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for insert: " + uri);
        }
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int updateCount;
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                updateCount = db.update(SmsOpenHelper.SmsTable.TABLE_NAME, values, selection, selectionArgs);
                if (updateCount > 0) {
                    Log.i(TAG, "update Success!!!!!");
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.w(TAG, "update affected 0 rows for URI: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for update: " + uri);
        }
        return updateCount;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int deleteCount;
        switch (match) {
            case SMS:
                SQLiteDatabase db = helper.getWritableDatabase();
                deleteCount = db.delete(SmsOpenHelper.SmsTable.TABLE_NAME, selection, selectionArgs);
                if (deleteCount > 0) {
                    Log.i(TAG, "delete Success!!!!!");
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    Log.w(TAG, "delete affected 0 rows for URI: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for delete: " + uri);
        }
        return deleteCount;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
