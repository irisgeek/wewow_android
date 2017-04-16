package com.wewow.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;

/**
 * Created by suncjs on 2017/4/12.
 */

public class ProgressDialogUtil {

    private Context context = null;
    private static ProgressDialogUtil instance = null;
    private DialogSettings settings = new DialogSettings();
    private ProgressDialog processDialog;
    private static final String TAG = "ProgressDialogUtil";

    private ProgressDialogUtil() {

    }

    public static ProgressDialogUtil getInstance(Context cxt) {
        if (instance == null) {
            instance = new ProgressDialogUtil();
        }
        instance.cleanup();
        if (cxt != instance.context) {
            instance.finishProgressDialog();
            instance.processDialog = null;
            instance.context = cxt;
            Log.d(TAG, "getInstance: context change");
        }
        return instance;
    }

    private void cleanup() {
        this.settings.clean();
    }

    public void showProgressDialog() {
        if (this.processDialog == null) {
            this.processDialog = new ProgressDialog(this.context);
            this.processDialog.setIndeterminate(true);
            Log.d(TAG, "showProgressDialog: new");
        }
//        if (this.settings.text != null) {
//            this.processDialog.setTitle(this.settings.text);
//        }
//        this.processDialog.setOnCancelListener(this.settings.onCancelListener);
//        this.processDialog.setOnDismissListener(this.settings.onDismissListener);
//        this.processDialog.setCancelable(this.settings.cancellable);
        if (!this.processDialog.isShowing()) {
            Log.d(TAG, "showProgressDialog: show");
            this.processDialog.show();
        }
    }

    public void finishProgressDialog() {
        if ((this.processDialog != null) && this.processDialog.isShowing()) {
            Log.d(TAG, "showProgressDialog: dismiss");
            this.processDialog.dismiss();
        }
    }

    public DialogSettings getSettings() {
        return this.settings;
    }

    public class DialogSettings {
        public CharSequence text;
        public boolean cancellable;
        public DialogInterface.OnCancelListener onCancelListener;
        public DialogInterface.OnDismissListener onDismissListener;
        public AnimationDrawable animationDrawable;

        public void clean() {
            this.text = null;
            this.cancellable = false;
            this.onCancelListener = null;
            this.onDismissListener = null;
            this.animationDrawable = null;
        }
    }
}
