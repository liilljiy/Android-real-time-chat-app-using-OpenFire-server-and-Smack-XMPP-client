package com.example.lj.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lj.R;

import java.util.ArrayList;
import java.util.List;

public class ToolBarUtil {

    private List<LinearLayout> buttons = new ArrayList<>();
    private List<ImageView> icons = new ArrayList<>();
    private List<TextView> texts = new ArrayList<>();

    public void createToolBar(LinearLayout bottom, String[] titles, int[] iconSelectors, View.OnClickListener listener) {
        for (int i = 0; i < titles.length; i++) {
            View item = LayoutInflater.from(bottom.getContext()).inflate(R.layout.inflate_toobarbtn, bottom, false);

            ImageView icon = item.findViewById(R.id.toolbar_icon);
            TextView text = item.findViewById(R.id.toolbar_text);

            icon.setImageResource(iconSelectors[i]);
            text.setText(titles[i]);

            item.setTag(i);
            item.setOnClickListener(listener);

            buttons.add((LinearLayout) item);
            icons.add(icon);
            texts.add(text);

            bottom.addView(item);
        }
    }

    public void changeColor(int selectedIndex) {
        for (int i = 0; i < buttons.size(); i++) {
            ImageView icon = icons.get(i);
            TextView text = texts.get(i);

            buttons.get(i).setSelected(i == selectedIndex);
            icon.setSelected(i == selectedIndex);
            text.setTextColor(i == selectedIndex ? 0xFF6200EE : 0xFF000000); // 可自定义颜色
        }
    }
}
