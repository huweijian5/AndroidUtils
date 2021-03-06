package com.junmeng.tools.helper;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

/**
 * apk下载助手
 *
 * @author HWJ
 */

public class DownloadAPKHelper {

    private DownloadManager downloadManager;
    private Request request;
    private Context context;
    private File downloadFile;

    private long myDownloadReference;
    private boolean isUrlVaild = false;//url是否可用

    /**
     * 如果url不合法则无法使用此对象
     *
     * @param context
     * @param appName     应用名
     * @param description 应用描述
     * @param url         下载地址
     */
    public DownloadAPKHelper(Context context, String appName, String description, String url) {
        this.context = context;
        downloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            Uri uri = Uri
                    .parse(url);
            request = new Request(uri);
            request.setTitle(appName);
            request.setDescription(description);
            registerDownloadCompleteReceiver();
            registerNotificationClickedReceiver();
            isUrlVaild = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 如果url不合法则无法使用此对象
     *
     * @param context
     * @param url     下载地址
     */
    public DownloadAPKHelper(Context context, String url) {
        this.context = context;
        downloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            Uri uri = Uri
                    .parse(url);
            request = new Request(uri);
            registerDownloadCompleteReceiver();
            registerNotificationClickedReceiver();
            isUrlVaild = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置的URL是否可用，如果不可用，则其他接口都不能使用
     *
     * @return
     */
    public boolean isUrlVaild() {
        return isUrlVaild;
    }


    /**
     * 返回request，可用于更多参数设置
     *
     * @return
     */
    public Request getRequest() {
        return request;
    }

    /**
     * 只允许在wifi下下载
     */
    public void setOnlyWifi() {
        if (request != null) {

            request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
        }
    }

    /**
     * 用于设置下载时时候在状态栏显示通知信息
     */
    public void setNotificationVisibility(int visibility) {
        if (request != null) {
            request.setNotificationVisibility(visibility);
        }
    }

    /**
     * 指定保存路径
     *
     * @param file
     */
    public void setSavePath(File file) {
        if (request != null) {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            this.downloadFile = file;
            request.setDestinationUri(Uri.fromFile(downloadFile));
        }
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (request != null) {
            request.setTitle(title);
        }
    }

    /**
     * 设置描述
     *
     * @param description
     */
    public void setDescription(String description) {
        if (request != null) {
            request.setDescription(description);
        }
    }

    /**
     * 获得reference,可用于停止下载
     *
     * @return
     */
    public long getMyDownloadReference() {
        return myDownloadReference;
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        if (request != null) {
            myDownloadReference = downloadManager.enqueue(request);
        }
    }


    /**
     * 注册下载完成的监听器
     */
    public void registerDownloadCompleteReceiver() {
        IntentFilter filter = new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    Toast.makeText(context, "下载完成,即将安装", Toast.LENGTH_LONG).show();
                    installAPK();
                }
            }
        };
        context.registerReceiver(receiver, filter);
    }

    /**
     * 注册通知栏被点击事件
     */
    public void registerNotificationClickedReceiver() {
        IntentFilter filter = new IntentFilter(
                DownloadManager.ACTION_NOTIFICATION_CLICKED);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String extraID = DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                long[] references = intent.getLongArrayExtra(extraID);
                for (long reference : references)
                    if (reference == myDownloadReference) {
                        //Toast.makeText(context, "通知栏被点击", Toast.LENGTH_LONG)
                        //		.show();
                    }
            }
        };
        context.registerReceiver(receiver, filter);
    }

    /**
     * 取消下载
     *
     * @param references
     */
    public void cancelDownload(long... references) {
        downloadManager.remove(references);
    }

    /**
     * 安装APK
     */
    public Intent installAPK() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(downloadFile), type);
        context.startActivity(intent);
        return intent;
    }

}