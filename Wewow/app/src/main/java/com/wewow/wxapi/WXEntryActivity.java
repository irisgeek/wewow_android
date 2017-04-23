package com.wewow.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wewow.LoginActivity;
import com.wewow.R;
import com.wewow.UserInfo;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final int WX_ENTRY_RESULT_CODE = 5000;
    public static final int WX_OK = 5000;
    public static final int WX_USER_CANCEL = 5001;
    public static final int WX_USER_DENIED = 5002;
    public static final int WX_USER_OTHER = 5003;
    public static final String WX_AUTH_CODE = "WX_AUTH_CODE";
    public static final String WX_STATUS_CODE = "WX_STATUS_CODE";

    private static final String TAG = "WXEntryActivity";

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Utils.setActivityToBeFullscreen(this);
        this.api = WXAPIFactory.createWXAPI(this, CommonUtilities.WX_AppID, true);
        this.api.registerApp(CommonUtilities.WX_AppID);
        this.api.handleIntent(this.getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "onReq: ");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, String.format("onResp: %d %d %s", baseResp.getType(), baseResp.errCode, baseResp.errStr));
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_SENDAUTH:
                this.onAuthResp((SendAuth.Resp) baseResp);
                break;
        }
    }

    private void onAuthResp(SendAuth.Resp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                SendAuth.Resp sresp = (SendAuth.Resp) resp;
                ProgressDialogUtil.getInstance(this).showProgressDialog();
                Object[] params = new Object[]{
                        String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                                CommonUtilities.WX_AppID, CommonUtilities.WX_AppSecret, sresp.code),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                if (!jobj.has("access_token")) {
                                    ProgressDialogUtil.getInstance(WXEntryActivity.this).finishProgressDialog();
                                    Toast.makeText(WXEntryActivity.this, R.string.login_wechat_other, Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Wechar fail in access_token");
                                    WXEntryActivity.this.finish();
                                    return;
                                }
                                String token = jobj.optString("access_token");
                                String openid = jobj.optString("openid");
                                WXEntryActivity.this.onWecharAuthorized(openid, token);
                            }
                        },
                        WebAPIHelper.HttpMethod.GET
                };
                new HttpAsyncTask().execute(params);
                return;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                this.setResult(WX_USER_DENIED);
                Toast.makeText(this, R.string.login_wechat_deny, Toast.LENGTH_LONG).show();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                this.setResult(WX_USER_CANCEL);
                Toast.makeText(this, R.string.login_wechat_cancel, Toast.LENGTH_LONG).show();
                break;
            default:
                this.setResult(WX_USER_OTHER);
                Toast.makeText(this, R.string.login_wechat_other, Toast.LENGTH_LONG).show();
                break;
        }
        Log.d(TAG, "onResp: finish");
        this.finish();
    }

    private void onWecharAuthorized(final String openid, String token) {
        Object[] params = new Object[]{
                String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s", token, openid),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        String nickname = jobj.optString("nickname", null);
                        if (nickname == null) {
                            ProgressDialogUtil.getInstance(WXEntryActivity.this).finishProgressDialog();
                            Toast.makeText(WXEntryActivity.this, R.string.login_wechat_other, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Wechar fail in userinfo");
                            WXEntryActivity.this.finish();
                            return;
                        }
                        List<Pair<String, String>> fields = new ArrayList<>();
                        fields.add(new Pair<String, String>("open_id", openid));
                        fields.add(new Pair<String, String>("nickname", nickname));
                        List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
                        headers.add(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
                        Object[] params = new Object[]{
                                String.format("%s/reg-wx", CommonUtilities.WS_HOST),
                                new HttpAsyncTask.TaskDelegate() {
                                    @Override
                                    public void taskCompletionResult(byte[] result) {
                                        ProgressDialogUtil.getInstance(WXEntryActivity.this).finishProgressDialog();
                                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                        try {
                                            JSONObject ui = jobj.getJSONObject("result").getJSONObject("user");
                                            UserInfo userinfo = UserInfo.getUserInfo(ui);
                                            userinfo.saveUserInfo(WXEntryActivity.this);
                                        } catch (JSONException e) {
                                            Log.e(TAG, "reg-wx fail");
                                            Toast.makeText(WXEntryActivity.this, R.string.login_wechat_other, Toast.LENGTH_LONG).show();
                                        } finally {
                                            WXEntryActivity.this.finish();
                                        }
                                    }
                                },
                                WebAPIHelper.HttpMethod.POST,
                                WebAPIHelper.buildHttpQuery(fields).getBytes(),
                                headers
                        };
                        new HttpAsyncTask().execute(params);
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        this.api.handleIntent(intent, this);
    }

}
