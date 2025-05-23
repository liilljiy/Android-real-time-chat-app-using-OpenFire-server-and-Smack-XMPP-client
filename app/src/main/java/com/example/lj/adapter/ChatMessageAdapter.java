package com.example.lj.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.lj.R;
import com.example.lj.dbhelper.SmsOpenHelper;
// 导入 IMService 以便获取当前用户账号
import com.example.lj.service.IMService;


public class ChatMessageAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_SENT = 0; // 发送的消息类型
    private static final int VIEW_TYPE_RECEIVED = 1; // 接收的消息类型

    private LayoutInflater inflater;
    private String myAccount; // 添加当前用户账号字段

    public ChatMessageAdapter(Context context, Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
        myAccount = IMService.currentAccount; // 在构造函数中获取当前用户账号
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        // 确保getColumnIndexOrThrow能找到列，否则会抛出异常，这里使用get方法
        int fromAccountColumnIndex = cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT);
        String fromAccount = "";
        if (fromAccountColumnIndex != -1) {
            fromAccount = cursor.getString(fromAccountColumnIndex);
        }

        // 根据发送者账号判断消息类型
        if (myAccount != null && fromAccount.equals(myAccount)) {
            return VIEW_TYPE_SENT; // 当前用户发送的消息
        } else {
            return VIEW_TYPE_RECEIVED; // 接收到的消息
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType == VIEW_TYPE_SENT) ? R.layout.item_chat_sent : R.layout.item_chat_received;
        return inflater.inflate(layoutId, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int viewType = getItemViewType(cursor.getPosition());

        TextView tvChatMessage;
        if (viewType == VIEW_TYPE_SENT) {
            // 发送的消息（当前用户）
            tvChatMessage = view.findViewById(R.id.tv_chat_sent_message_body);
        } else {
            // 接收的消息（对方）
            tvChatMessage = view.findViewById(R.id.tv_chat_message);
        }

        if (tvChatMessage != null) {
            int bodyIndex = cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY);
            String body = "";
            if (bodyIndex != -1) {
                body = cursor.getString(bodyIndex);
            }
            tvChatMessage.setText(body);
        }
    }


    // 您可能需要一个将时间戳格式化为可读字符串的方法
    // private String formatTime(long timestamp) { ... }
}