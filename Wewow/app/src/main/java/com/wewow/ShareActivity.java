package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.Utils;

public class ShareActivity extends AppCompatActivity implements IWeiboHandler.Response {

    private static final String TAG = "ShareActivity";
    public static final String SHARE_TYPE = "SHARE_TYPE";
    public static final int SHARE_TYPE_UNKNOWN = -1;
    public static final int SHARE_TYPE_TOSELECT = 0;
    public static final int SHARE_TYPE_WEIBO = 1;
    public static final String SHARE_URL = "SHARE_URL";
    public static final String SHARE_CONTEXT = "SHARE_CONTEXT";
    public static final String SHARE_IMAGE = "SHARE_IMAGE";
    private IWeiboShareAPI api;


    private int shareType;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        this.intent = this.getIntent();
        this.shareType = this.intent.getIntExtra(SHARE_TYPE, SHARE_TYPE_TOSELECT);
        this.setContentView(this.shareType == SHARE_TYPE_TOSELECT ? R.layout.activity_share : R.layout.activity_share_empty);
        this.api = WeiboShareSDK.createWeiboAPI(this, CommonUtilities.Weibo_AppKey);
        switch (this.shareType) {
            case SHARE_TYPE_TOSELECT:
                this.setupUI();
                break;
            case SHARE_TYPE_WEIBO:
                this.shareWeibo();
                break;
            case SHARE_TYPE_UNKNOWN:
            default:
                Toast.makeText(this, this.getString(R.string.share_unknown, this.shareType), Toast.LENGTH_LONG).show();
                this.finish();
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.api.handleWeiboResponse(intent, this); //当前应用唤起微博分享后，返回当前应用
    }

    private void setupUI() {


    }

    private void shareWeibo() {
        this.api.registerApp();
        WeiboMultiMessage msg = new WeiboMultiMessage();
        String url = this.intent.getStringExtra(SHARE_URL);
        if (this.intent.hasExtra(SHARE_IMAGE)) {
            byte[] buf = this.intent.getByteArrayExtra(SHARE_IMAGE);
            Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            ImageObject iobj = new ImageObject();
            iobj.setImageObject(bm);
            msg.imageObject = iobj;
        }
        msg.textObject = new TextObject();
        msg.textObject.text = String.format("%s %s", this.intent.getStringExtra(SHARE_CONTEXT), url);
        SendMultiMessageToWeiboRequest req = new SendMultiMessageToWeiboRequest();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.multiMessage = msg;
        this.api.sendRequest(this, req);
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        Log.d(TAG, String.format("Weibo share onResponse: %d %s", baseResponse.errCode, baseResponse.errMsg));
        String resultmsg = null;
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                resultmsg = this.getString(R.string.share_result_succeed);
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                resultmsg = this.getString(R.string.share_result_cancel);
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                resultmsg = this.getString(R.string.share_result_fail);
                break;
        }
        String msg = this.getString(R.string.share_weibo_result, resultmsg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        this.finish();
    }
}
