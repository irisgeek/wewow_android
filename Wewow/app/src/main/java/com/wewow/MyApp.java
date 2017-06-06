package com.wewow;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.growingio.android.sdk.collection.Configuration;
import com.growingio.android.sdk.collection.GrowingIO;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        GrowingIO.startWithConfiguration(this, new Configuration()
                .useID()
                .trackAllFragments()
                .setChannel(getResources().getString(R.string.growingio_channel)));

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