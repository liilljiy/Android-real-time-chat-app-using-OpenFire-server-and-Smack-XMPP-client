package com.example.lj.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.lj.R;
import com.example.lj.adapter.ChatMessageAdapter;
import com.example.lj.dbhelper.SmsOpenHelper;
import com.example.lj.provider.SmsProvider;
import com.example.lj.service.IMService;
import com.example.lj.utils.ThreadUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ChatActivity";
    public static final String EXTRA_CONTACT_ACCOUNT = "contact_account";
    private static final int LOADER_ID = 1;

    private ListView lvChatMessages;
    private EditText etMessageInput;
    private Button btnSendMessage;

    private String contactAccount;
    private String myAccount;
    private ChatMessageAdapter chatMessageAdapter;
    private Chat chat;
    private ChatManager chatManager;
    private IncomingChatMessageListener incomingListener;



    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        contactAccount = getIntent().getStringExtra(EXTRA_CONTACT_ACCOUNT);
        if (TextUtils.isEmpty(contactAccount)) {
            Toast.makeText(this, "无效的联系人账号", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        myAccount = IMService.currentAccount;
        setTitle(contactAccount);

        initViews();
        initListener();
        initChat();
        initLoader();
    }

    private void initViews() {
        lvChatMessages = findViewById(R.id.lv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);

        chatMessageAdapter = new ChatMessageAdapter(this, null);
        lvChatMessages.setAdapter(chatMessageAdapter);
    }

    private void initListener() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void initChat() {
        if (IMService.conn == null || !IMService.conn.isConnected() || !IMService.conn.isAuthenticated()) {
            Toast.makeText(this, "XMPP 连接未就绪或未认证", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            chatManager = ChatManager.getInstanceFor(IMService.conn);
            EntityBareJid contactJid = JidCreate.entityBareFrom(contactAccount);
            chat = chatManager.chatWith(contactJid);

            // 创建监听器
            incomingListener = (from, message, chat) -> {
                if (from.asBareJid().equals(contactJid) && message.getBody() != null) {
                    mainHandler.post(this::loadMessages);
                }
            };

            // 添加监听器，正确的方法名
            chatManager.addIncomingListener(incomingListener);

        } catch (XmppStringprepException e) {
            e.printStackTrace();
            Toast.makeText(this, "联系人JID格式错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendMessage() {
        String content = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "消息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (chat == null) {
            Toast.makeText(this, "聊天对象未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        ThreadUtils.runInThread(() -> {
            try {
                chat.send(content);

                ContentValues values = new ContentValues();
                values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, myAccount);
                values.put(SmsOpenHelper.SmsTable.BODY, content);
                values.put(SmsOpenHelper.SmsTable.TYPE, 0); // 将发送消息的类型改为 0，与 ChatMessageAdapter 中的 VIEW_TYPE_SENT 一致
                values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
                values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, contactAccount);

                getContentResolver().insert(SmsProvider.URI_SMS, values);

                mainHandler.post(() -> {
                    etMessageInput.setText("");
                    loadMessages();
                });

            } catch (SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(ChatActivity.this, "消息发送失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initLoader() {
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }

    private void loadMessages() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    // ... existing code ...
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            String selection = SmsOpenHelper.SmsTable.SESSION_ACCOUNT + "=?";
            String[] selectionArgs = new String[]{contactAccount};
            // 将排序方式改为降序 (DESC)，以便最新的消息在最上面
            String sortOrder = SmsOpenHelper.SmsTable.TIME + " ASC";
            return new CursorLoader(this, SmsProvider.URI_SMS, null, selection, selectionArgs, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ID) {
            chatMessageAdapter.swapCursor(data);
//          lvChatMessages.setSelection(chatMessageAdapter.getCount() - 1);
            // 移除滚动到最后一项的代码，因为最新的消息已经在顶部了
        }
    }
// ... existing code ...

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID) {
            chatMessageAdapter.swapCursor(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除监听，避免内存泄漏
        if (chatManager != null && incomingListener != null) {
            chatManager.removeIncomingListener(incomingListener);
        }
    }
}
