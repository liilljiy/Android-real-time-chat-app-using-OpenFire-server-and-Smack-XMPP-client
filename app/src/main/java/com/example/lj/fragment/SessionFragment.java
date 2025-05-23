package com.example.lj.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.lj.R;
import com.example.lj.activity.ChatActivity;
import com.example.lj.adapter.SessionAdapter;
import com.example.lj.dbhelper.SmsOpenHelper;
import com.example.lj.provider.SmsProvider;

public class SessionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 100;
    private ListView lvSession;
    private SessionAdapter sessionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        lvSession = view.findViewById(R.id.lv_session);
        sessionAdapter = new SessionAdapter(getContext(), null);
        lvSession.setAdapter(sessionAdapter);

        lvSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) sessionAdapter.getItem(position);
                String account = cursor.getString(cursor.getColumnIndexOrThrow(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CONTACT_ACCOUNT, account);
                startActivity(intent);
            }
        });

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        return view;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(
                    getContext(),
                    SmsProvider.URI_SESSION, // 注意我们要从会话 URI 读取（需要你建视图）
                    null, null, null,
                    SmsOpenHelper.SmsTable.TIME + " DESC"
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        sessionAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        sessionAdapter.swapCursor(null);
    }
}
