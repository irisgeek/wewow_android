package com.wewow;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareActivity extends Activity implements IWeiboHandler.Response {

    private static final String TAG = "ShareActivity";
    public static final String SHARE_TYPE = "SHARE_TYPE";
    public static final int SHARE_TYPE_UNKNOWN = -1;
    public static final int SHARE_TYPE_TOSELECT = 0;
    public static final int SHARE_TYPE_WEIBO = 1;
    public static final int SHARE_TYPE_COPY_LINK = 2;
    public static final int SHARE_TYPE_WECHAT_CIRCLE = 3;
    public static final int SHARE_TYPE_WECHAT_FRIEND = 4;
    public static final String SHARE_URL = "SHARE_URL";
    public static final String SHARE_CONTEXT = "SHARE_CONTEXT";
    public static final String SHARE_IMAGE = "SHARE_IMAGE";
    public static final String BACK_GROUND = "BACK_GROUND";
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
            case SHARE_TYPE_COPY_LINK:
                this.shareLink();
                break;
            case SHARE_TYPE_WECHAT_CIRCLE:
                this.shareWechatCircle();
                break;
            case SHARE_TYPE_WECHAT_FRIEND:
                this.shareWechatFriend();
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
        this.setContentView(R.layout.activity_share);
        this.findViewById(R.id.share_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareActivity.this.finish();
            }
        });
        this.findViewById(R.id.share_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareActivity.this.shareWeibo();
            }
        });
        this.findViewById(R.id.share_copylink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareActivity.this.shareLink();
            }
        });
        final IWXAPI api = WXAPIFactory.createWXAPI(ShareActivity.this, CommonUtilities.WX_AppID, true);
        ImageView wf = (ImageView) this.findViewById(R.id.share_wechat_friend);
        ImageView wc = (ImageView) this.findViewById(R.id.share_wechat_circle);
        if (!api.isWXAppInstalled()) {
            wf.setImageDrawable(this.getResources().getDrawable(R.drawable.sharewechatfriend_grey));
            wc.setImageDrawable(this.getResources().getDrawable(R.drawable.sharewechatcircle_grey));
        }
        wf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (api.isWXAppInstalled()) {
                    ShareActivity.this.shareWechatFriend();
                } else {
                    Toast.makeText(ShareActivity.this, R.string.login_wechat_not_install, Toast.LENGTH_LONG).show();
                }
            }
        });
        wc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (api.isWXAppInstalled()) {
                    ShareActivity.this.shareWechatCircle();
                } else {
                    Toast.makeText(ShareActivity.this, R.string.login_wechat_not_install, Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.share_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
//                if (intent.hasExtra(SHARE_IMAGE)){
//                    sendIntent.setType("image/*");
//                    intent.putExtra(Intent.EXTRA_STREAM, uri);
//                }else{
                sendIntent.setType("text/plain");
//                }
                String content = intent.getStringExtra(SHARE_CONTEXT);
                if (intent.hasExtra(SHARE_URL)) {
                    content = intent.getStringExtra(SHARE_CONTEXT) + " " + intent.getStringExtra(SHARE_URL);
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(sendIntent, "选择分享方式"));
            }
        });
        /*if (this.getIntent().hasExtra(BACK_GROUND)) {
            byte[] buf = this.getIntent().getByteArrayExtra(BACK_GROUND);
            Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            this.findViewById(android.R.id.content).setBackground(new BitmapDrawable(bm));
        }*/
        View sv = this.findViewById(R.id.share_area);
        sv.setY(Utils.getScreenHeightPx(this));
        this.findViewById(R.id.share_area).post(new Runnable() {
            @Override
            public void run() {
                ShareActivity.this.animShareArea();
            }
        });
    }

    private void animShareArea() {
        View sv = this.findViewById(R.id.share_area);
        View nv = this.findViewById(R.id.non_share_area);
        int sh = Utils.getScreenHeightPx(this);
        float h = sv.getHeight();
        ValueAnimator va = ObjectAnimator.ofFloat(sv, "y", sh, sh - h);
        va.setDuration(300);
        ValueAnimator va1 = ObjectAnimator.ofFloat(nv, "alpha", 0, 0.7f);
        va1.setDuration(300);
        va.start();
        va1.start();
    }

    private void shareWeibo() {
        if (this.api.isWeiboAppInstalled()) {
            this.api.registerApp();
            WeiboMultiMessage msg = new WeiboMultiMessage();
            String url = this.intent.hasExtra(SHARE_URL) ? this.intent.getStringExtra(SHARE_URL) : "";
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
        } else {
            this.shareWeiboH5();
        }
    }

    private void shareWeiboH5() {
        AuthInfo authInfo = new AuthInfo(this, CommonUtilities.Weibo_AppKey, CommonUtilities.Weibo_Redirect_URL, CommonUtilities.Weibo_Scope);
        SsoHandler sso = new SsoHandler(this, authInfo);
        sso.authorize(new WeiboAuthListener() {

            private RequestListener reql = new RequestListener() {
                @Override
                public void onComplete(String s) {
                    String ss = ShareActivity.this.getString(R.string.share_weibo_result, ShareActivity.this.getString(R.string.share_result_succeed));
                    MessageBoxUtils.messageBoxWithButtons(ShareActivity.this, ss,
                            new String[]{ShareActivity.this.getString(R.string.userinfo_comfirm)},
                            null,
                            new MessageBoxUtils.MsgboxButtonListener[]{
                                    new MessageBoxUtils.MsgboxButtonListener() {
                                        @Override
                                        public boolean shouldCloseMessageBox(Object tag) {
                                            return true;
                                        }

                                        @Override
                                        public void onClick(Object tag) {
                                            ShareActivity.this.finish();
                                        }
                                    }
                            }
                    );
                }

                @Override
                public void onWeiboException(WeiboException e) {
                    String s = ShareActivity.this.getString(R.string.share_weibo_error, e.getMessage());
                    MessageBoxUtils.messageBoxWithNoButton(ShareActivity.this, false, s, 1000);
                }
            };

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(bundle);
                Intent x = ShareActivity.this.intent;
                StatusesAPI sapi = new StatusesAPI(ShareActivity.this, CommonUtilities.Weibo_AppKey, token);
                String url = x.hasExtra(SHARE_URL) ? x.getStringExtra(SHARE_URL) : "";
                String text = String.format("%s %s", x.getStringExtra(SHARE_CONTEXT), url);
                if (x.hasExtra(SHARE_IMAGE)) {
                    byte[] buf = x.getByteArrayExtra(SHARE_IMAGE);
                    Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                    sapi.upload(text, bm, null, null, this.reql);
                } else {
                    sapi.update(text, null, null, this.reql);
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                String s = ShareActivity.this.getString(R.string.share_weibo_error, e.getMessage());
                MessageBoxUtils.messageBoxWithNoButton(ShareActivity.this, false, s, 1000);
            }

            @Override
            public void onCancel() {
                String s = ShareActivity.this.getString(R.string.share_weibo_result, ShareActivity.this.getString(R.string.share_result_cancel));
                MessageBoxUtils.messageBoxWithNoButton(ShareActivity.this, false, s, 1000);
            }
        });
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

    private void shareLink() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String url = this.intent.getStringExtra(SHARE_URL);
        ClipData clip = ClipData.newPlainText("", url);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, this.getString(R.string.share_copylink_result, this.getString(R.string.share_result_succeed)), Toast.LENGTH_LONG).show();
        this.finish();
    }

    private void shareWechatCircle() {
        this.shareWechat(1);
    }

    private void shareWechatFriend() {
        this.shareWechat(0);
    }

    private void shareWechat(int type) {
        String content = this.intent.getStringExtra(SHARE_CONTEXT);
        String url = this.intent.hasExtra(SHARE_URL) ? this.intent.getStringExtra(SHARE_URL) : "";
        WXMediaMessage.IMediaObject iobj = null;
        if (!url.isEmpty()) {
            WXWebpageObject wpobj = new WXWebpageObject();
            wpobj.webpageUrl = url;
            iobj = wpobj;
        } else {
            WXTextObject tobj = new WXTextObject(content);
            iobj = tobj;
        }
        WXMediaMessage msg = new WXMediaMessage();
        msg.title = content;
        msg.description = content;
        msg.mediaObject = iobj;
        if (this.intent.hasExtra(SHARE_IMAGE)) {
            byte[] buf = this.intent.getByteArrayExtra(SHARE_IMAGE);
            while (buf.length > WXMediaMessage.THUMB_LENGTH_LIMIT) {
                double times = Math.sqrt(Math.ceil((double) buf.length / WXMediaMessage.THUMB_LENGTH_LIMIT));
                Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                Bitmap x = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() / times), (int) (bm.getHeight() / times), false);
                bm.recycle();
                bm = x;
                buf = Utils.getBitmapBytes(bm);
            }
            msg.thumbData = buf;
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = this.genWXTransaction();
        req.message = msg;
        req.scene = type == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        IWXAPI api = WXAPIFactory.createWXAPI(this, CommonUtilities.WX_AppID);
        api.registerApp(CommonUtilities.WX_AppID);
        api.sendReq(req);
        this.finish();
    }

    private String genWXTransaction() {
        SimpleDateFormat timefmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return timefmt.format(now);
    }

}
