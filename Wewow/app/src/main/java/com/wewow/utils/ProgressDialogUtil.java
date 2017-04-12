package com.wewow.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;

/**
 * Created by suncjs on 2017/4/12.
 */

public class ProgressDialogUtil {

    private Context context = null;
    private static ProgressDialogUtil instance = null;
    private DialogSettings settings = new DialogSettings();
    private ProgressDialog processDialog;

    private ProgressDialogUtil() {

    }

    public static ProgressDialogUtil getInstance(Context cxt) {
        if (instance == null) {
            instance = new ProgressDialogUtil();
        }
        instance.cleanup();
        instance.context = cxt;
        return instance;
    }

    private void cleanup() {
        this.settings.clean();
    }

    public void showProgressDialog() {
        this.processDialog = new ProgressDialog(this.context);
        if (this.settings.text != null) {
            this.processDialog.setTitle(this.settings.text);
        }
        this.processDialog.setOnCancelListener(this.settings.onCancelListener);
        this.processDialog.setOnDismissListener(this.settings.onDismissListener);
        this.processDialog.setCancelable(this.settings.cancellable);
        this.processDialog.setIndeterminate(true);
        this.processDialog.show();
    }

    public void finishProgressDialog() {
        if ((this.processDialog != null) && this.processDialog.isShowing()) {
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
