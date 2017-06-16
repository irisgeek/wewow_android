package com.wewow.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.wewow.LoginActivity;
import com.wewow.R;
import com.wewow.ShareActivity;
import com.wewow.UserInfo;

import java.io.ByteArrayOutputStream;

/**
 * Created by suncjs on 2017/4/18.
 */

public class ShareUtils {

    private Context context;
    private static final String TAG = "ShareUtils";
    private static final int ANIMATION_DURATION = 500;

    private String content;
    private Bitmap picture;
    private String url;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public enum ShareTypes {
        Manual_Select,
        Weibo,
        Copy_Link,
        Wechat_Circle,
        Wechar_Friend
    }

    private static class beforeAnimInfo {
        public static float scaleX;
        public static float scaleY;
        public static float alpha;
    }

    public ShareUtils(Context cxt) {
        this.context = cxt;
    }


    public void share() {
        this.share(ShareTypes.Manual_Select);
    }

    public void share(ShareTypes type) {
//        if (!UserInfo.isUserLogged(this.context)) {
//            LoginUtils.startLogin((Activity) this.context, LoginActivity.REQUEST_CODE_LOGIN);
//            return;
//        }
        final Intent intent = new Intent(this.context, ShareActivity.class);
        intent.putExtra(ShareActivity.SHARE_TYPE, type.ordinal());
        intent.putExtra(ShareActivity.SHARE_CONTEXT, this.content);
        intent.putExtra(ShareActivity.SHARE_URL, this.url == null ? "" : this.url);
        if (this.picture != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
            intent.putExtra(ShareActivity.SHARE_IMAGE, baos.toByteArray());
        }
        if (!(this.context instanceof Activity)) {
            this.context.startActivity(intent);
            return;
        }
        Activity act = (Activity) context;
        View v = act.findViewById(android.R.id.content);
        if (v != null) {
            Bitmap bm = BlurBuilder.blur(v);
            byte[] buf = Utils.getBitmapBytes(bm);
            intent.putExtra(ShareActivity.BACK_GROUND, buf);
        }

        this.context.startActivity(intent);
        /*AnimatorSet animSet = new AnimatorSet();
        beforeAnimInfo.alpha = v.getAlpha();
        beforeAnimInfo.scaleY = v.getScaleY();
        beforeAnimInfo.scaleX = v.getScaleX();
        ValueAnimator alphava = ObjectAnimator.ofFloat(v, "alpha", beforeAnimInfo.alpha, beforeAnimInfo.alpha / 2f);
        ValueAnimator scaleYva = ObjectAnimator.ofFloat(v, "scaleY", beforeAnimInfo.scaleY, beforeAnimInfo.scaleY * 0.9f);
        ValueAnimator scaleXva = ObjectAnimator.ofFloat(v, "scaleX", beforeAnimInfo.scaleX, beforeAnimInfo.scaleY * 0.9f);
        animSet.play(scaleXva).with(scaleYva);

        animSet.setDuration(ANIMATION_DURATION);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ShareUtils.this.context.startActivity(intent);
//                ActivityOptions ao = ActivityOptions.makeCustomAnimation(ShareUtils.this.context, R.anim.share_start, 0);
//                ShareUtils.this.context.startActivity(intent, ao.toBundle());
                ShareUtils.this.reverse();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                //
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
//
            }
        });
        animSet.start();*/
    }

    private void reverse() {
        Activity act = (Activity) this.context;
        final View v = act.findViewById(android.R.id.content);
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setAlpha(beforeAnimInfo.alpha);
                v.setScaleX(beforeAnimInfo.scaleX);
                v.setScaleY(beforeAnimInfo.scaleY);
                Log.d(TAG, "reversed");
            }
        }, ANIMATION_DURATION * 5);
    }

}
