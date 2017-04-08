package com.wewow.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wewow.utils.CommonUtilities;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.api = WXAPIFactory.createWXAPI(this, CommonUtilities.WX_AppID, true);
        //this.api.registerApp(CommonUtilities.WX_AppID);
        this.api.handleIntent(this.getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "onReq: ");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, String.format("onResp: %d %s", baseResp.errCode, baseResp.errStr));
        SendAuth.Resp sresp = (SendAuth.Resp)baseResp;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        this.api.handleIntent(intent, this);
    }
}
