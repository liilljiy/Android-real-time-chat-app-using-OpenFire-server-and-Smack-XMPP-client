package com.example.lj.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.lj.dbhelper.ContactOpenHelper;
import com.example.lj.dbhelper.SmsOpenHelper;
import com.example.lj.provider.ContactProvider;
import com.example.lj.provider.SmsProvider;
import com.example.lj.utils.MyPinyinHelper;
import com.example.lj.utils.ThreadUtils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IMService extends Service {

    public static AbstractXMPPConnection conn;
    public static String currentAccount;

    private static final String TAG = "IMService";

    private Roster roster;
    private ChatManager chatManager;
    private Map<String, Chat> activeChats = new ConcurrentHashMap<>();
    private MyRosterListener rosterListenerInstance;
    private IncomingChatMessageListener incomingChatMessageListenerInstance;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "IMService created");

        ThreadUtils.runInThread(() -> {
            long startTime = System.currentTimeMillis();
            long timeout = 30000;

            while ((conn == null || !conn.isConnected() || !conn.isAuthenticated())
                    && (System.currentTimeMillis() - startTime < timeout)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Waiting for connection interrupted", e);
                    return;
                }
                Log.d(TAG, "Waiting for XMPP connection...");
            }

            if (conn == null || !conn.isConnected() || !conn.isAuthenticated()) {
                Log.e(TAG, "XMPP Connection is not ready after waiting. Cannot proceed.");
                return;
            }

            try {
                roster = Roster.getInstanceFor(conn);
                Collection<RosterEntry> entries = roster.getEntries();
                Log.i(TAG, "Found " + entries.size() + " entries in roster.");
                for (RosterEntry entry : entries) {
                    saveOrUpdate(entry);
                }

                Presence presence = new Presence(Presence.Type.available);
                presence.setPriority(24);
                conn.sendStanza(presence);

                rosterListenerInstance = new MyRosterListener(roster);
                roster.addRosterListener(rosterListenerInstance);
                Log.i(TAG, "MyRosterListener added.");

                conn.addAsyncStanzaListener(stanza -> {
                    if (stanza instanceof Message) {
                        Message msg = (Message) stanza;
                        Log.i(TAG, "StanzaListener received message from: " + msg.getFrom() + " to: " + msg.getTo() + " body: " + msg.getBody());
                    }
                }, stanza -> stanza instanceof Message);



                chatManager = ChatManager.getInstanceFor(conn);
                incomingChatMessageListenerInstance = new MyIncomingChatMessageListener();
                chatManager.addIncomingListener(incomingChatMessageListenerInstance);
                Log.i(TAG, "MyIncomingChatMessageListener added.");
                Log.i(TAG, "Connected as: " + conn.getUser());

            } catch (Exception e) {
                Log.e(TAG, "Error during IMService initialization", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conn != null && conn.isConnected()) {
            if (roster != null && rosterListenerInstance != null) {
                roster.removeRosterListener(rosterListenerInstance);
            }
            if (chatManager != null && incomingChatMessageListenerInstance != null) {
                chatManager.removeIncomingListener(incomingChatMessageListenerInstance);
            }
            conn.disconnect();
        }
        conn = null;
        currentAccount = null;
        Log.i(TAG, "IMService destroyed and XMPP connection disconnected.");
    }

    private void saveOrUpdate(RosterEntry entry) {
        ContentValues cv = new ContentValues();
        String account = entry.getJid().asBareJid().toString();
        String nickname = entry.getName();
        if (nickname == null || nickname.isEmpty()) {
            nickname = account.contains("@") ? account.substring(0, account.indexOf('@')) : account;
        }

        Presence presence = roster.getPresence(entry.getJid());

        cv.put(ContactOpenHelper.ContactTable.STATUS, presence.isAvailable()?"0":"1");
        cv.put(ContactOpenHelper.ContactTable.ACCOUNT, account);
        cv.put(ContactOpenHelper.ContactTable.NICKNAME, nickname);
        cv.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        cv.put(ContactOpenHelper.ContactTable.PINYIN, MyPinyinHelper.toPinyin(nickname));

        int update = getContentResolver().update(ContactProvider.URI_CONTACT, cv,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
        if (update <= 0) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(ContactProvider.URI_CONTACT,
                        new String[]{ContactOpenHelper.ContactTable._ID},
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                        new String[]{account}, null);

                if (cursor == null || cursor.getCount() == 0) {
                    getContentResolver().insert(ContactProvider.URI_CONTACT, cv);
                    Log.i(TAG, "Inserted new contact: " + account);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting contact", e);
            } finally {
                if (cursor != null) cursor.close();
            }
        } else {
            Log.i(TAG, "Updated contact: " + account);
        }
    }

    class MyRosterListener implements RosterListener {
        private final Roster roster;

        public MyRosterListener(Roster roster) {
            this.roster = roster;
        }

        @Override
        public void entriesAdded(Collection<Jid> addresses) {
            for (Jid jid : addresses) {
                BareJid bareJid = jid.asBareJid();
                RosterEntry entry = roster.getEntry(bareJid);
                if (entry != null) saveOrUpdate(entry);
            }
        }

        @Override
        public void entriesUpdated(Collection<Jid> addresses) {
            entriesAdded(addresses);
        }

        @Override
        public void entriesDeleted(Collection<Jid> addresses) {
            for (Jid jid : addresses) {
                String account = jid.asBareJid().toString();
                getContentResolver().delete(ContactProvider.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
                Log.i(TAG, "Deleted contact: " + account);
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            Log.d(TAG, "Presence changed: " + presence.getFrom() + " -> " + presence.getStatus());
        }
    }

    class MyIncomingChatMessageListener implements IncomingChatMessageListener {
        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
            Log.i(TAG, "Received message from " + from + ": " + message.getBody());
            if (message.getBody() == null || message.getBody().trim().isEmpty()) return;

            String senderAccount = from.toString();
            ContentValues values = new ContentValues();
            values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, senderAccount);
            values.put(SmsOpenHelper.SmsTable.BODY, message.getBody());
            values.put(SmsOpenHelper.SmsTable.TYPE, SmsProvider.TYPE_RECEIVE);
            values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
            values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, senderAccount);

            getContentResolver().insert(SmsProvider.URI_SMS, values);
            Log.i(TAG, "Message saved to DB: " + senderAccount);

            // 你可以在这里发送广播或通知 UI
        }
    }
}
