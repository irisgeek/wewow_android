package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jaeger.library.StatusBarUtil;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iris on 17/4/16.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        getSloganData();
    }

    private void getSloganData() {
        List<Pair<String, String>> ps = new ArrayList<>();
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/slogan", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            skipHome();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                skipHome();
                            } else {
                                JSONObject data = r.getJSONObject("data");
                                String slogan = data.optString("slogan");
                                String image = data.optString("image");
                                LoadSloganImage(slogan, image);
                            }
                        } catch (JSONException e) {
                            skipHome();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void skipHome() {
        new Handler().postDelayed(new Runnable() {

            public void run() {
                //execute the task
                Intent intent=new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 600);
    }

    private void skipSlogan(final String slogan, final String image) {
        new Handler().postDelayed(new Runnable() {

            public void run() {
                //execute the task
                Intent intent = new Intent(SplashActivity.this, SloganActivity.class);
                intent.putExtra("slogan", slogan);
                intent.putExtra("image", image);
                startActivity(intent);
                finish();
            }
        }, 600);
    }

    private void LoadSloganImage(final String slogan, final String image) {
        Glide.with(this)
                .load(image)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        skipSlogan(slogan, resource.getAbsolutePath());
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        skipHome();
                    }
                });
    }

}
