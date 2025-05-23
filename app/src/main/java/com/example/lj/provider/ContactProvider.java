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

import com.example.lj.dbhelper.ContactOpenHelper;

public class ContactProvider extends ContentProvider {

    public static final String AUTHORITIES = ContactProvider.class.getCanonicalName();
    public static final Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");

    public static final int CONTACT = 1;

    private static final String TAG = "ContactProvider";
    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES, "contact", CONTACT);
    }

    private ContactOpenHelper helper;

    @Override
    public boolean onCreate() {
        helper = new ContactOpenHelper(getContext());
        return helper != null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues cv) {
        int match = uriMatcher.match(uri);
        Uri returnUri = null;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = helper.getWritableDatabase();
                long id = db.insert(ContactOpenHelper.T_CONTACT, null, cv);
                if (id > 0) {
                    Log.i(TAG, "insert Success!!!!!");
                    returnUri = ContentUris.withAppendedId(uri, id);
                    // 通知数据改变
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);
                }
                break;
            default:
                break;
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int deleteCount = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = helper.getWritableDatabase();
                deleteCount = db.delete(ContactOpenHelper.T_CONTACT, selection, selectionArgs);
                if (deleteCount > 0) {
                    Log.i(TAG, "delete Success!!!!!");
                    // 通知数据改变
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);
                }
                break;
            default:
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int updateCount = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = helper.getWritableDatabase();
                updateCount = db.update(ContactOpenHelper.T_CONTACT, values, selection, selectionArgs);
                if (updateCount > 0) {
                    Log.i(TAG, "update Success!!!!!");
                    // 通知数据改变
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);
                }
                break;
            default:
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        int match = uriMatcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = helper.getReadableDatabase();
                cursor = db.query(ContactOpenHelper.T_CONTACT, projection, selection, selectionArgs,
                        null, null, sortOrder);
                if (cursor != null) {
                    Log.i(TAG, "query Success!!!!!");
                    // 设置通知 URI，使得数据变更可监听
                    cursor.setNotificationUri(getContext().getContentResolver(), URI_CONTACT);
                }
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
