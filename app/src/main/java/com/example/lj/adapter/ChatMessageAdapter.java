package com.example.lj.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.lj.R;
import com.example.lj.dbhelper.SmsOpenHelper; // 导入 SmsOpenHelper
import com.example.lj.service.IMService; // 导入 IMService

public class ChatMessageAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_SENT = 0; // 发送的消息类型
    private static final int VIEW_TYPE_RECEIVED = 1; // 接收的消息类型

    public ChatMessageAdapter(Context context, Cursor c) {
        super(context, c, 0); // 使用 flags = 0 (不需要注册 ContentObserver，因为 LoaderManager 会处理)
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // 根据消息类型选择不同的布局
        int type = cursor.getInt(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TYPE));
        View view;
        if (type == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
        } else { // VIEW_TYPE_RECEIVED
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_received, parent, false);
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // 根据消息类型找到对应的 TextView 并设置消息内容
        int type = cursor.getInt(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TYPE));
        String messageBody = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));

        if (type == VIEW_TYPE_SENT) {
            TextView tvSentMessageBody = view.findViewById(R.id.tv_chat_sent_message_body);
            if (tvSentMessageBody != null) {
                tvSentMessageBody.setText(messageBody);
            }
        } else { // VIEW_TYPE_RECEIVED
            TextView tvReceivedMessageBody = view.findViewById(R.id.tv_chat_received_message_body);
            if (tvReceivedMessageBody != null) {
                tvReceivedMessageBody.setText(messageBody);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 根据当前行的消息类型返回视图类型
        Cursor cursor = (Cursor) getItem(position);
        int type = cursor.getInt(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TYPE));
        return type == 0 ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @Override
    public int getViewTypeCount() {
        // 返回视图类型总数
        return 2;
    }
}