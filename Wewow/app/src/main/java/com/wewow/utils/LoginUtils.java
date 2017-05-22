package com.wewow.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import com.wewow.LoginActivity;

import java.io.ByteArrayOutputStream;

/**
 * Created by suncjs on 2017/5/10.
 */

public class LoginUtils {
    public static void startLogin(Activity act, int req_code) {
        Intent intent = new Intent(act, LoginActivity.class);
        /*final View content = act.getWindow().getDecorView();
        if (content.getWidth() > 0) {
            Bitmap image = BlurBuilder.blur(content);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            intent.putExtra(LoginActivity.BACKGROUND, data);
        }*/
        act.startActivityForResult(intent, req_code);
    }
}
