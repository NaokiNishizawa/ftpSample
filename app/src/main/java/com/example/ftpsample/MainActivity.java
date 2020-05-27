package com.example.ftpsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mReceiveBtn;
    private Button mReceiveStopBtn;
    private Button mSendBtn;
    private Button mSendStopBtn;
    private FtpFileUtil ftpFileUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初期化
        init();
    }

    private void init() {
        ftpFileUtil = FtpFileUtil.getInstance();

        mReceiveBtn = findViewById(R.id.receive_btn);
        mReceiveStopBtn = findViewById(R.id.receive_stop_btn);

        mSendBtn = findViewById(R.id.send_btn);
        mSendStopBtn = findViewById(R.id.send_stop_btn);

        mReceiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 受信開始
                ftpFileUtil.ReceiveStart(getApplicationContext());
            }
        });

        mReceiveStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //受信終了
                ftpFileUtil.ReceiveStop();
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 送信処理
                ftpFileUtil.SendStart(getApplicationContext());
            }
        });

        mSendStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 送信終了処理
                ftpFileUtil.SendStop();
            }
        });
    }
}
