package com.wewow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by suncjs on 2017/4/8.
 */

public class RemoteImageLoader {
    private static final String TAG = "RemoteImageLoader";

    private String url;
    private RemoteImageListener listener;
    private Context context;

    public RemoteImageLoader(Context cxt, String url, RemoteImageListener listener) {
        this.url = url;
        this.listener = listener;
        this.context = cxt;
        this.run();
    }

    public interface RemoteImageListener {
        void onRemoteImageAcquired(Drawable dr);
    }

    public void run() {
        if (this.isImageSaved(url)) {
            byte[] buf = this.getImageFile(url);
            Drawable dr = this.getDrawableByBuffer(buf);
            if (this.listener != null) {
                this.listener.onRemoteImageAcquired(dr);
            }
        } else {
            Object[] params = new Object[]{
                    this.url,
                    new HttpAsyncTask.TaskDelegate() {
                        @Override
                        public void taskCompletionResult(byte[] result) {
                            if ((result != null) && (RemoteImageLoader.this.listener != null)) {
                                Drawable bd = RemoteImageLoader.this.getDrawableByBuffer(result);
                                if (bd != null) {
                                    Log.d(TAG, String.format("Succeed loading url: %s", RemoteImageLoader.this.url));
                                    RemoteImageLoader.this.saveImage(RemoteImageLoader.this.url, result);
                                    RemoteImageLoader.this.listener.onRemoteImageAcquired(bd);
                                } else {
                                    Log.w(TAG, String.format("Fail loading url: %s", RemoteImageLoader.this.url));
                                }
                            }
                        }
                    },
                    WebAPIHelper.HttpMethod.GET,
                    null,
                    null,
                    true
            };
            new HttpAsyncTask().execute(params);
        }
    }

    private boolean isImageSaved(String url) {
        String fn = this.getImageFilePath(url);
        File f = new File(fn);
        return f.exists() && f.isFile() && (System.currentTimeMillis() - f.lastModified() < 1000 * 3600);
//        return false;
    }

    private void saveImage(String url, byte[] buf) {
        Log.d(TAG, String.format("saveImage %s %d bytes", url, buf.length));
        String fn = this.getImageFilePath(url);
        try {
            FileOutputStream fos = new FileOutputStream(fn);
            fos.write(buf, 0, buf.length);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, String.format("write file error for %s", url));
        }
    }

    private byte[] getImageFile(String url) {
        String path = this.getImageFilePath(url);
        try {
            File f = new File(path);
            byte[] buf = new byte[(int) f.length()];
            FileInputStream fis = new FileInputStream(path);
            fis.read(buf, 0, buf.length);
            fis.close();
            return buf;
        } catch (FileNotFoundException e) {
            Log.e(TAG, String.format("getImageFile: fail %s", url));
            return null;
        } catch (IOException e) {
            Log.e(TAG, String.format("getImageFile: IO error %s", e.getMessage()));
            return null;
        }
    }

    private String getImageFilePath(String url) {
        File root = this.context.getCacheDir();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] in = url.getBytes("utf-8");
            md.update(in);
            byte[] out = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : out) {
                sb.append(String.format("%02X", b));
            }
            String fn = String.format("%s/%s", root.getAbsolutePath(), sb.toString());
            Log.d(TAG, String.format("file %s for url %s", fn, url));
            return fn;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private Drawable getDrawableByBuffer(byte[] buf) {
        Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
        if (bm == null) {
            return null;
        }
        BitmapDrawable bd = new BitmapDrawable(this.context.getResources(), bm);
        return bd;
    }

}
