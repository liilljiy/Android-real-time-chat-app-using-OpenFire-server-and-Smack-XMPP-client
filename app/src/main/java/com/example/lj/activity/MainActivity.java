package com.example.lj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.lj.R;
import com.example.lj.adapter.MyViewPagerAdapter;
import com.example.lj.databinding.ActivityMainBinding;
import com.example.lj.fragment.ContactFragment;
import com.example.lj.service.IMService;
import com.example.lj.utils.ToolBarUtil;
import com.example.lj.fragment.SessionFragment;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Fragment> fragments = new ArrayList<>();
    private MyViewPagerAdapter myViewPagerAdapter;
    private ToolBarUtil toolBarUtil = new ToolBarUtil();

    private String[] titles = {"会话", "联系人"};
    private int[] icons = {R.drawable.icon_message_selector, R.drawable.icon_contact_selector}; // 你的icon顺序按实际调

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 启动IMService监听联系人（确保IMService.conn已经连接认证）
        Intent intent = new Intent(this, IMService.class);
        startService(intent);

        initData();
        initBottomBar();
        initListener();
    }

    private void initData() {
        fragments.add(new SessionFragment());
        fragments.add(new ContactFragment());

        myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager(), fragments);
        binding.mainViewPage.setAdapter(myViewPagerAdapter);
    }

    private void initBottomBar() {
        LinearLayout bottomBar = findViewById(R.id.bottomBar); // 确保activity_main.xml里有这个ID
        toolBarUtil.createToolBar(bottomBar, titles, icons, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                binding.mainViewPage.setCurrentItem(index);
                toolBarUtil.changeColor(index);
                binding.mainTitleTv.setText(titles[index]);
            }
        });
        // 默认选中第一个
        toolBarUtil.changeColor(0);
    }

    private void initListener() {
        binding.mainViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                binding.mainTitleTv.setText(titles[position]);
                toolBarUtil.changeColor(position);
            }

            @Override public void onPageScrollStateChanged(int state) {}
        });
    }
}
