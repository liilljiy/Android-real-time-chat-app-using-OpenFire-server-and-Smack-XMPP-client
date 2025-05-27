package com.example.lj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lj.databinding.ActivityLoginBinding;
import com.example.lj.service.IMService;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    public static final String HOST = "10.176.133.153";
    //public static final String HOST = "192.168.150.71";
    public static final int PORT = 5222;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(v -> onLoginButtonClick());

        binding.register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void onLoginButtonClick() {
        String userName = binding.username.getText().toString().trim();
        String passWord = binding.password.getText().toString().trim();

        if (TextUtils.isEmpty(userName)) {
            binding.username.setError("用户名不能为空！");
            return;
        }
        if (TextUtils.isEmpty(passWord)) {
            binding.password.setError("密码不能为空！");
            return;
        }

        new Thread(() -> {
            AbstractXMPPConnection connection = null;
            try {
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setHost(HOST)
                        .setPort(PORT)
                        .setXmppDomain(HOST)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .build();

                connection = new XMPPTCPConnection(config);

                connection.connect();

                connection.login(userName, passWord);

                // 登录成功，赋值IMService的静态连接和账号
                IMService.conn = connection;
                IMService.currentAccount = userName + "@" + HOST;

                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    // 启动IMService
                    Intent imServiceIntent = new Intent(LoginActivity.this, IMService.class);
                    startService(imServiceIntent);

                    // 跳转主页
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (org.jxmpp.stringprep.XmppStringprepException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "域名格式错误：" + e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (SmackException | IOException | XMPPException | InterruptedException e) {
                e.printStackTrace();
                String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
                // 出错断开连接
                if (connection != null && connection.isConnected()) {
                    connection.disconnect();
                }
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败：" + errorMsg, Toast.LENGTH_LONG).show());
            }
            // 这里不用再断开连接，连接保留给IMService使用
        }).start();
    }
}
