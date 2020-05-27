package com.example.ftpsample;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Ftpによりファイル操作を行うManagerクラス
 */
public class FtpFileManager {
    private static final String rTAG = "receiveDebug";
    private static final String sTAG = "sendDebug";

    private static FtpFileManager sInstance;

    private static final String IP = "192.168.11.12";
    private static final int PORT  = 21;
    private static final String USER = "work";
    private static final String PASS = "work";
    private static final String FILE_NAME = "aaa.txt";
    private static final String TMP_FILE_NAME = "tmp.txt";
    private static int DEF_INTERVAL = 5000; // 5秒

    private Context mContext;

    // 受信用変数
    private  Timer mReceiveTimer;
    private  ReceiveTimerTask mReceiveTimerTask;

    // 送信用変数
    private Timer mSendTimer;
    private SendTimerTask mSendTimerTask;


    /** 隠蔽 */
    private FtpFileManager() {}

    public static FtpFileManager getInstance() {
        if(sInstance == null) {
            sInstance = new FtpFileManager();
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

    /**
     * 送信開始
     *
     * @param context
     */
    public void SendStart(Context context) {
        mContext = context;

        if(mSendTimer != null) {
            SendStop();
        }

        mSendTimer = new Timer();

        mSendTimerTask = new SendTimerTask();

        // 送信開始
        mSendTimer.schedule(mSendTimerTask, 0, DEF_INTERVAL);
    }

    /**
     * 送信終了
     */
    public void SendStop() {
        if(null != mSendTimer) {
            mSendTimer.cancel();
            mSendTimer = null;
        }
    }

    //---------------------------------------------------------------
    // インナークラス
    private class ReceiveTimerTask extends TimerTask {

        @Override
        public void run() {
            // 受信処理開始
            Log.d(rTAG, "+++++ start receiveTask +++++");

            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(DEF_INTERVAL);
            try {
                // 接続開始
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.connect(IP, PORT);

                int code = ftpClient.getReplyCode();

                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // 接続失敗
                    Log.d(rTAG, "connect failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(rTAG,"connect success");

                // ログイン
                ftpClient.login(USER, PASS);
                code = ftpClient.getReplyCode();
                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // ログイン失敗
                    Log.d(rTAG, "login failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(rTAG, "login success");


                code = ftpClient.pasv();
                ftpClient.setSoTimeout(1000);
                // 転送モードはバイナリーモードを設定する
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setDataTimeout(1000);


                Log.d(rTAG,"転送モード設定完了");

                // ダウンロード開始
                Log.d(rTAG,"ダウンロード開始");
                FileOutputStream outputStream = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                if(!ftpClient.retrieveFile(FILE_NAME, outputStream)) {
                    Log.d(rTAG,"ダウンロード失敗しました。");
                }
                outputStream.close();
                Log.d(rTAG,"ダウンロード終了");

                ftpClient.logout();

            } catch (IOException e) {
                Log.d(rTAG, "Receive failed");
                e.printStackTrace();
            } finally {
                try {
                    ftpClient.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(rTAG, "+++++ end receiveTask +++++");
        }
    }

    //---------------------------------------------------------------
    // インナークラス
    private class SendTimerTask extends TimerTask {

        @Override
        public void run() {
            // 送信処理開始
            Log.d(sTAG, "+++++ start sendTask +++++");

            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(DEF_INTERVAL);

            try {
                // 接続開始
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.connect(IP, PORT);

                int code = ftpClient.getReplyCode();

                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // 接続失敗
                    Log.d(sTAG, "connect failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(sTAG,"connect success");

                // ログイン
                ftpClient.login(USER, PASS);
                code = ftpClient.getReplyCode();
                if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    // ログイン失敗
                    Log.d(sTAG, "login failed");
                    ftpClient.disconnect();
                    return;
                }
                Log.d(sTAG, "login success");

                // まずtmpという名前でファイルを送信する
                code = ftpClient.pasv();
                ftpClient.setSoTimeout(1000);
                // 転送モードはバイナリーモードを設定する
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setDataTimeout(1000);

                Log.d(sTAG,"転送モード設定完了");

                Log.d(sTAG,"書き込み開始");
                FileInputStream inputStream = mContext.openFileInput(FILE_NAME);
                if(!ftpClient.storeFile(TMP_FILE_NAME, inputStream)) {
                    Log.d(sTAG,"書き込み失敗");
                }
                Log.d(sTAG,"書き込み終了");

                // 元のファイルを一度削除し、今回アップロードしたファイルをリネームする
                Log.d(sTAG,"削除開始");
                 if(!ftpClient.deleteFile(FILE_NAME)) {
                     Log.d(sTAG,"削除失敗");
                 } else {
                     Log.d(sTAG,"削除完了");

                     Log.d(sTAG,"リネーム開始");
                     if(!ftpClient.rename(TMP_FILE_NAME, FILE_NAME)) {
                         Log.d(sTAG,"リネーム失敗");
                     }
                     Log.d(sTAG,"リネーム終了");
                 }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ftpClient.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 送信処理終了
            Log.d(sTAG, "+++++ end sendTask +++++");
        }
    }

}
