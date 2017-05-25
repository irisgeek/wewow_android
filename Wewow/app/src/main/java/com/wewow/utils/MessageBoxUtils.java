package com.wewow.utils;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wewow.R;

/**
 * Created by suncjs on 2017/5/25.
 */

public class MessageBoxUtils {

    public static void messageBoxWithNoButton(Context cxt, boolean state, String text, int duration) {
        View v = View.inflate(cxt, R.layout.dialog_msgbox_nobutton, null);
        TextView tv = (TextView) v.findViewById(R.id.hint_text);
        tv.setText(text);
        ImageView iv = (ImageView) v.findViewById(R.id.hint_image);
        iv.setImageDrawable(cxt.getResources().getDrawable(state ? R.drawable.success : R.drawable.fail));
        final PopupWindow pw = new PopupWindow(v, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //pw.setContentView(v);
        View root = ((Activity) cxt).findViewById(android.R.id.content);
        pw.showAtLocation(root, Gravity.CENTER, 0, 0);
        pw.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pw.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pw.update();
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (pw.isShowing()) {
                        pw.dismiss();
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }, duration);
    }

}
