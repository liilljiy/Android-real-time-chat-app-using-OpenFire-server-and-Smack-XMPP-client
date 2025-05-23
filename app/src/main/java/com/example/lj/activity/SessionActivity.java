// SessionActivity.java
package com.example.lj.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lj.R;
import com.example.lj.adapter.SessionAdapter;
import com.example.lj.dbhelper.SmsOpenHelper;
import com.example.lj.provider.SmsProvider;

public class SessionActivity extends AppCompatActivity {
    private ListView lvSessions;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        lvSessions = findViewById(R.id.lv_sessions);
        sessionAdapter = new SessionAdapter(this, null);
        lvSessions.setAdapter(sessionAdapter);

        lvSessions.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) sessionAdapter.getItem(position);
            String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
            Intent intent = new Intent(SessionActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONTACT_ACCOUNT, account);
            startActivity(intent);
        });

        loadSessionData();
    }

    private void loadSessionData() {
        // 查询所有会话（按联系人分组，每个联系人只保留最后一条消息）
        String sql = "SELECT * FROM sms WHERE _id IN (" +
                "SELECT MAX(_id) FROM sms GROUP BY session_account" +
                ") ORDER BY time DESC";
        Cursor cursor = getContentResolver().query(
                SmsProvider.URI_SMS,
                null,
                null,
                null,
                null
        );

        sessionAdapter.swapCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionAdapter != null) sessionAdapter.swapCursor(null);
    }
}
