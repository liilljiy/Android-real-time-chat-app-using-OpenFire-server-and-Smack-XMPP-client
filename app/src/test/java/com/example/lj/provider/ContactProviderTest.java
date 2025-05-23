package com.example.lj.provider;

import android.content.Context;

import junit.framework.TestCase;

public class ContactProviderTest extends TestCase {
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();


    // ... existing code ...
    @Test
    public void insert() {
        ContentValues cv = new ContentValues();
        cv.put(ContactOpenHelper.ContactTable.ACCOUNT, "test@localhost");
        cv.put(ContactOpenHelper.ContactTable.NICKNAME, "测试");
        cv.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        cv.put(ContactOpenHelper.ContactTable.PINYIN, "ceshi");
        appContext.getContentResolver().insert(ContactProvider.URI_CONTACT, cv);
    }

    @Test
    public void update() {
        ContentValues cv = new ContentValues();
        cv.put(ContactOpenHelper.ContactTable.ACCOUNT, "test@localhost");
        cv.put(ContactOpenHelper.ContactTable.NICKNAME, "测试用户");
        cv.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        cv.put(ContactOpenHelper.ContactTable.PINYIN, "ceshi");
        appContext.getContentResolver().update(ContactProvider.URI_CONTACT, cv, "account=?", new String[]{"test@localhost"});
    }

    @Test
    public void delete() {
        appContext.getContentResolver().delete(ContactProvider.URI_CONTACT, "account=?", new String[]{"test@localhost"});
    }

    @Test
    public void query() {
        Cursor cursor = appContext.getContentResolver().query(ContactProvider.URI_CONTACT, projection: null, selection: null, selectionArgs: null, sortOrder: null);
        String str = "";
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                str += cursor.getColumnName(i) + ":" + cursor.getString(i) + ";";
            }
            Log.i(tag: "ContactProviderTest", str);
        }
    }
// ... existing code ...
}