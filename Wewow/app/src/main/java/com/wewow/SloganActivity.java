package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.jaeger.library.StatusBarUtil;

/**
 * Date: 2017/6/5
 * Description:
 */
public class SloganActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slogan);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        new Handler().postDelayed(new Runnable() {

            public void run() {
                Intent intent = new Intent(SloganActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);

    }
}
