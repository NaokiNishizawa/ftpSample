package com.example.ftpsample;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Ftpによりファイル操作を行うUtilクラス
 */
public class FtpFileUtil {
    private String ReceiveTag = "receiveDebug";

    private static FtpFileUtil sInstance;

    private static final String IP = "192.168.11.12";
    private static final int PORT  = 21;
    private static final String USER = "work";
    private static final String PASS = "work";
    private static int DEF_INTERVAL = 5000; // 5秒

    private Context mContext;

    // 受信用変数
    private  Handler mReceiveHandler;
    private  Timer mReceiveTimer;
    private  ReceiveTimerTask mReceiveTimerTask;


    /** 隠蔽 */
    private FtpFileUtil() {}

    public static FtpFileUtil getInstance() {
        if(sInstance == null) {
            sInstance = new FtpFileUtil();
        }
        return sInstance;
    }

    /**
     * 受信開始
     *
     * @param context
     */
    public void ReceiveStart(Context context) {
        mContext = context;
        mReceiveHandler = new Handler();

        if(mReceiveTimer != null) {
            ReceiveStop();
        }

        mReceiveTimer = new Timer();

         mReceiveTimerTask = new ReceiveTimerTask();

        // 5秒に一回受信する
        mReceiveTimer.schedule(mReceiveTimerTask, 0, DEF_INTERVAL);
    }

    /**
     * 受信終了
     */
    public  void ReceiveStop() {
        if(null != mReceiveTimer) {
            mReceiveTimer.cancel();
            mReceiveTimer = null;
        }
    }


    // インナークラス
    private class ReceiveTimerTask extends TimerTask {

        @Override
        public void run() {
            // 受信処理開始
            Log.d(ReceiveTag, "+++++ start receiveTask +++++");

            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(5000);
            try {
                // 接続開始
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.connect(IP, PORT);

                int code = ftpClient.getReplyCode();

                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // 接続失敗
                    Log.d(ReceiveTag, "connect failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(ReceiveTag ,"connect success");

                // ログイン
                ftpClient.login(USER, PASS);
                code = ftpClient.getReplyCode();
                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // ログイン失敗
                    Log.d(ReceiveTag, "login failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(ReceiveTag, "login success");


                code = ftpClient.pasv();
                ftpClient.setSoTimeout(1000);
                // 転送モードはバイナリーモードを設定する
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setDataTimeout(1000);


                Log.d(ReceiveTag ,"転送モード設定完了");

                // ダウンロード開始
                Log.d(ReceiveTag ,"ダウンロード開始");
                FileOutputStream outputStream = mContext.openFileOutput("aaa.txt", Context.MODE_PRIVATE);
                if(!ftpClient.retrieveFile("aaa.txt", outputStream)) {
                    Log.d(ReceiveTag ,"ダウンロード失敗しました。");
                }
                outputStream.close();
                Log.d(ReceiveTag ,"ダウンロード終了");

            } catch (IOException e) {
                Log.d(ReceiveTag, "Receive failed");
                e.printStackTrace();
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(ReceiveTag, "+++++ end receiveTask +++++");
        }
    }

}
