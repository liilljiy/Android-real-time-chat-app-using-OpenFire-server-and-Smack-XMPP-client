package com.example.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lj.R;

import java.util.List;

// 请确认 Contact 类已在 adapter 包下，且包含 name/account/status 字段和对应的 get 方法
public class ContactAdapter extends BaseAdapter {
    private Context context;
    private List<Contact> data;

    public ContactAdapter(Context context, List<Contact> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.iv_contact_icon);
            holder.tvName = convertView.findViewById(R.id.tv_contact_name);
            holder.tvAccount = convertView.findViewById(R.id.tv_contact_account);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = data.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvAccount.setText(contact.getAccount());
        holder.tvStatus.setText(contact.getStatus());

        // TODO: 你可以根据状态设置头像颜色或状态图标
        // holder.ivIcon.setImageResource(...)

        return convertView;
    }

    public void updateData(List<Contact> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvAccount;
        TextView tvStatus;
    }
}
