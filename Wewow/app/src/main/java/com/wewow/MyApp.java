package com.wewow;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.growingio.android.sdk.collection.Configuration;
import com.growingio.android.sdk.collection.GrowingIO;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.Utils;
import com.youzan.sdk.YouzanSDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iris on 17/4/24.
 */
public class MyApp extends Application implements Thread.UncaughtExceptionHandler {

    private final static String TAG = "WewowAPP";
    private String[] channels = {"wewow_android", "360", "baidu", "yingyongbao", "sougou", "xiaomi", "lenovo", "huawei", "vivo",
            "meizu", "chuizi", "oppo", "pp", "taobao", "aliyun", "wandoujia", "UC", "yingyonghui", "anzhi", "mumayi", "ifanr",
            "appso", "zuimei", "shaoshupai", "haoqixin", "36kr", "apipi"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        String channel=channels[Integer.parseInt(BuildConfig.AUTO_TYPE)];
        GrowingIO.startWithConfiguration(this, new Configuration()
                .useID()
                .trackAllFragments()
                .setChannel(channel));
        setUpLeanCloud();
        YouzanSDK.init(this, CommonUtilities.Youzan_AppID);

    }

    private void setUpLeanCloud() {
        AVOSCloud.initialize(this, CommonUtilities.Leancloud_AppID,
                CommonUtilities.Leancloud_AppKey);
        // 启用崩溃错误统计
//        AVAnalytics.enableCrashReport(this.getApplicationContext(), true);
        AVOSCloud.setLastModifyEnabled(true);
        AVOSCloud.setDebugLogEnabled(true);
        if (!FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_NOTIFICATION_INSTALLATION_ID, this)) {

            AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                public void done(AVException e) {
                    if (e == null) {
                        // 保存成功
                        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                        FileCacheUtil.setCache(installationId, MyApp.this, CommonUtilities.CACHE_FILE_NOTIFICATION_INSTALLATION_ID, 0);
                        // 关联  installationId 到用户表等操作……
                    } else {
                        // 保存失败，输出错误信息
                    }
                }
            });
        }

        PushService.setDefaultPushCallback(this, MainActivity.class);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG, String.format("uncaughtException: %s", e.getMessage()));
        try {
            File root = new File(Environment.getExternalStorageDirectory() + "/wewow");
            if (!root.exists()) {
                root.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //String fname = String.format("%s%s.txt", TAG, sdf.format(new Date()));
            String fname = String.format("%s/%s%s.txt", root.getAbsolutePath(), TAG, sdf.format(new Date()));
            Log.e(TAG, fname);
            //FileOutputStream fos = this.openFileOutput(fname, MODE_APPEND);
            FileOutputStream fos = new FileOutputStream(fname, true);
            fos.write(e.getMessage().getBytes());
            fos.write("\r\n".getBytes());
            for (StackTraceElement ste : e.getStackTrace()) {
                fos.write(ste.toString().getBytes());
                fos.write("\r\n".getBytes());
            }
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (FileNotFoundException e1) {
            Log.e(TAG, String.format("uncaughtException: %S", e1.getMessage()));
        } catch (IOException e1) {
            Log.e(TAG, String.format("uncaughtException: %s", e1.getMessage()));
        }
        System.exit(-1);
    }
}