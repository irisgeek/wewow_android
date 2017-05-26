package com.wewow.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

    public static void messageBoxWithButtons(Context cxt, String text, String[] cmds, Object[] tags, MsgboxButtonListener[] btnHandlers) {
        View v = View.inflate(cxt, R.layout.dialog_msgbox_btns, null);
        TextView tv = (TextView) v.findViewById(R.id.hint_text);
        tv.setText(text);
        final PopupWindow pw = new PopupWindow(v, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int max = cmds.length > 2 ? 2 : cmds.length;
        Resources rcs = cxt.getResources();
        for (int i = 0; i < max; i++) {
            String btnid = String.format("msgbox_btn%d", i);
            int resid = rcs.getIdentifier(btnid, "id", cxt.getPackageName());
            Button b = (Button) v.findViewById(resid);
            b.setText(cmds[i]);
            Object obj = tags.length > i ? tags[i] : null;
            MsgboxButtonListener l = btnHandlers.length > i ? btnHandlers[i] : null;
            b.setTag(new Object[]{pw, obj, l});
            b.setOnClickListener(btnClick);
            b.setVisibility(View.VISIBLE);
        }
        View root = ((Activity) cxt).findViewById(android.R.id.content);
        pw.showAtLocation(root, Gravity.CENTER, 0, 0);
        pw.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pw.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pw.setFocusable(true);
        pw.update();
    }

    private static View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object[] p = (Object[]) v.getTag();
            PopupWindow pw = (PopupWindow) p[0];
            Object id = p[1];
            MsgboxButtonListener l = (MsgboxButtonListener) p[2];
            if (l != null) {
                l.onClick(id);
            } else {
                pw.dismiss();
                return;
            }
            if (l.shouldCloseMessageBox(id)) {
                pw.dismiss();
            }
        }
    };

    public interface MsgboxButtonListener {
        boolean shouldCloseMessageBox(Object tag);

        void onClick(Object tag);
    }
}
