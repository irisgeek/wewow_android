package com.wewow.utils;

import android.content.Context;

/**
 * Created by suncjs on 2017/4/18.
 */

public class ShareUtils {

    private Context context;
    private IShareCallback delegate;
    private static final String TAG = "ShareUtils";

    public interface IShareCallback {
        void onSuccess();

        void onFail();

        void onCancel();
    }

    public ShareUtils(Context cxt, IShareCallback callback) {
        this.context = cxt;
        this.delegate = callback;
    }



}
