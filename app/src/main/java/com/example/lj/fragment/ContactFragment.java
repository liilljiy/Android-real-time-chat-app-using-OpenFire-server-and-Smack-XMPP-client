package com.example.lj.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lj.R;
import com.example.lj.activity.ChatActivity;
import com.example.lj.adapter.Contact;
import com.example.lj.adapter.ContactAdapter;
import com.example.lj.dbhelper.ContactOpenHelper;
import com.example.lj.provider.ContactProvider;
import com.example.lj.service.IMService;

import org.jivesoftware.smack.roster.Roster;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private EditText etContact;
    private Button btnAdd;
    private ListView lvContacts;
    private ContactAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();  // 改为 List<Contact>
    private ContentResolver resolver;

    private final ContentObserver contactObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            loadContacts();  // 数据变化时重新加载
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        etContact = view.findViewById(R.id.et_contact);
        btnAdd = view.findViewById(R.id.btn_add_contact);
        lvContacts = view.findViewById(R.id.lv_contacts);
        resolver = requireContext().getContentResolver();

        adapter = new ContactAdapter(getContext(), contacts);
        lvContacts.setAdapter(adapter);

        // 注册内容观察者监听联系人数据库变化
        resolver.registerContentObserver(ContactProvider.URI_CONTACT, true, contactObserver);

        loadContacts();

        btnAdd.setOnClickListener(v -> addContact());

        lvContacts.setOnItemClickListener((parent, v, position, id) -> {
            Contact contact = contacts.get(position);
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONTACT_ACCOUNT, contact.getAccount());
            startActivity(intent);
        });

        lvContacts.setOnItemLongClickListener((parent, v, position, id) -> {
            Contact contact = contacts.get(position);
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("删除联系人")
                    .setMessage("确定要删除联系人 \"" + contact.getAccount() + "\" 吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteContact(contact.getAccount()))
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        return view;
    }

    private void loadContacts() {
        contacts.clear();
        Cursor cursor = resolver.query(ContactProvider.URI_CONTACT,
                new String[]{
                        ContactOpenHelper.ContactTable.NICKNAME,
                        ContactOpenHelper.ContactTable.ACCOUNT,
                        ContactOpenHelper.ContactTable.STATUS // 如果你的数据库没有状态字段，请移除这一列
                },
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String account = cursor.getString(1);
                String statusStr = null;

                try {
                    String rawStatus = cursor.getString(2); // 获取原始状态
                    if ("0".equals(rawStatus)) {
                        statusStr = "在线";
                    } else {
                        statusStr = "离线";
                    }
                } catch (Exception e) {
                    statusStr = "离线"; // 默认状态
                }

                contacts.add(new Contact(name, account, statusStr));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void addContact() {
        String account = etContact.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(getContext(), "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME, account);
        values.put(ContactOpenHelper.ContactTable.PINYIN, account.toLowerCase());
        // 如果有状态字段，可以添加默认状态，比如：
        // values.put(ContactOpenHelper.ContactTable.STATUS, "离线");

        Uri uri = resolver.insert(ContactProvider.URI_CONTACT, values);
        if (uri != null) {
            Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
            etContact.setText("");
        } else {
            Toast.makeText(getContext(), "添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteContact(String account) {
        int count = resolver.delete(ContactProvider.URI_CONTACT,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{account});

        if (count > 0) {
            Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resolver.unregisterContentObserver(contactObserver);
    }
}
