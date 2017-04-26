package com.wewow.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.wewow.ShareActivity;

import java.io.ByteArrayOutputStream;

/**
 * Created by suncjs on 2017/4/18.
 */

public class ShareUtils {

    private Context context;
    private static final String TAG = "ShareUtils";

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

    public ShareUtils(Context cxt) {
        this.context = cxt;
    }


    public void share() {
        this.share(ShareTypes.Manual_Select);
    }

    public void share(ShareTypes type) {
        Intent intent = new Intent(this.context, ShareActivity.class);
        intent.putExtra(ShareActivity.SHARE_TYPE, type.ordinal());
        intent.putExtra(ShareActivity.SHARE_CONTEXT, this.content);
        intent.putExtra(ShareActivity.SHARE_URL, this.url);
        if (this.picture != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
            intent.putExtra(ShareActivity.SHARE_IMAGE, baos.toByteArray());
        }
        this.context.startActivity(intent);
    }


}
