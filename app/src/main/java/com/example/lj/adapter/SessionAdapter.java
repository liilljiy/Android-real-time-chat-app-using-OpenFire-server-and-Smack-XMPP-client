// SessionAdapter.java
package com.example.lj.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lj.R;
import com.example.lj.dbhelper.SmsOpenHelper;

import androidx.cursoradapter.widget.CursorAdapter;

public class SessionAdapter extends CursorAdapter {
    public SessionAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvAccount = view.findViewById(R.id.tv_session_account);
        TextView tvLastMsg = view.findViewById(R.id.tv_last_message);

        String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TO_ACCOUNT));
        String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));

        tvAccount.setText(account + " : ");
        tvLastMsg.setText(body);
    }
}
