package com.wewow;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Pair;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability.OnUpdateListener;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks;
import com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.HuaweiIdStatusCodes;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;
import com.huawei.hms.support.api.hwid.SignInResult;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.HttpManager;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.modelmsg.SendAuth;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements OnConnectionFailedListener, ConnectionCallbacks, OnUpdateListener {

    public static final int REQUEST_CODE_LOGIN = 1;

    public static final int RESPONSE_CODE_MOILE = 1;
    public static final int RESPONSE_CODE_WECHAT = 2;
    public static final int RESPONSE_CODE_WEIBO = 3;
    public static final int RESPONSE_CODE_HUAWEI = 4;

    private static final int HUAWEI_REQUEST_RESOLVE_ERROR = 1001;
    private static final int HUAWEI_REQUEST_UNLOGIN = 1002;
    private static final int HUAWEI_REQUEST_AUTH = 1003;

    private static final String TAG = "LoginActivity";

    private Button btnSendVerifyCode;
    private TextView btnSendVerifyCode2;
    private EditText edtPhoneNo;
    private ImageButton imWechat;
    private ImageButton imWeibo;
    private ImageButton imHuawei;
    private View startView;
    private View verifyView;
    private ArrayList<EditText> edtvcodes = new ArrayList<EditText>();
    private Button btnlogin;
    private TextView tvNumberSent;
    private TextView tvVerifyCountdown;
    private HuaweiApiClient huaweiClient = null;
    private SsoHandler ssohandler;

    private ProgressDialog progressDlg;
    private CountDownTimer verifyTimer = new CountDownTimer(30 * 1000, 1000) {
        @Override
        public void onTick(long l) {
            String s = String.format(LoginActivity.this.getString(R.string.login_wait_verify_countdown), l / 1000);
            LoginActivity.this.tvVerifyCountdown.setText(s);
        }

        @Override
        public void onFinish() {
            LoginActivity.this.btnSendVerifyCode2.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.initView();
    }

    @Override
    protected void onStop() {
        if ((this.huaweiClient != null) && this.huaweiClient.isConnected()) {
            this.huaweiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * register event handlers
     */
    private void initView() {
        this.edtPhoneNo = (EditText) this.findViewById(R.id.login_phone_number);
        this.startView = this.findViewById(R.id.login_mobile_start_view);
        this.verifyView = this.findViewById(R.id.login_mobile_verify_view);
        this.btnlogin = (Button) this.findViewById(R.id.login_btn_login);
        this.tvNumberSent = (TextView) this.findViewById(R.id.login_tv_number_sent);
        this.tvVerifyCountdown = (TextView) this.findViewById(R.id.login_tv_verify_countdown);

        this.edtPhoneNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //don't care
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //don't care
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean validnumber = editable.toString().length() == 11;
                LoginActivity.this.btnSendVerifyCode.setEnabled(validnumber);
                Drawable dr = LoginActivity.this.getResources().getDrawable(validnumber ? R.drawable.roundshapebtnblack : R.drawable.roundshapebtn);
                LoginActivity.this.btnSendVerifyCode.setBackground(dr);
            }
        });

        this.setupReqVerifyCode();
        this.btnSendVerifyCode2 = (TextView) this.findViewById(R.id.login_btn_send_verify_code_2);
        this.btnSendVerifyCode2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });

        this.setupInputVerifyCode();
        this.setupLogin();
        this.setupWeibo();
        this.setupWechat();
        this.setupHuawei();
    }

    /**
     * set up login button of mobile method
     */
    private void setupLogin() {
        this.btnlogin = (Button) this.findViewById(R.id.login_btn_login);
        this.btnlogin.setOnClickListener(new OnClickListener() {
            private static final String TAG = "LoginTaskDeletegate";

            @Override
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder();
                for (EditText edt : LoginActivity.this.edtvcodes) {
                    sb.append(edt.getText().toString());
                }
                if (sb.length() < LoginActivity.this.edtvcodes.size()) {
                    return;
                }
                ArrayList<Pair<String, String>> ups = new ArrayList<Pair<String, String>>();
                ups.add(new Pair<String, String>("code", sb.toString()));
                Object[] params = new Object[]{
                        WebAPIHelper.addUrlParams(String.format("%s/verifycode", CommonUtilities.WS_HOST), ups),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                LoginActivity.this.toggleProgressDialog(false);
                                try {
                                    String x = new String(result, "utf-8");
                                    Log.d(TAG, String.format("login returns: %s", x));
                                    JSONObject jobj = new JSONObject(x).getJSONObject("result");
                                    Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                                    if (jobj.getInt("code") == 0) {
                                        UserInfo user = UserInfo.getUserInfo(jobj.getJSONObject("user_info"));
                                        user.saveUserInfo(LoginActivity.this);
                                        LoginActivity.this.setResult(RESPONSE_CODE_MOILE);
                                        LoginActivity.this.finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    Log.e(TAG, "web response encoding error");
                                } catch (JSONException e) {
                                    Log.e(TAG, "json parse error");
                                }
                            }
                        }
                };
                LoginActivity.this.toggleProgressDialog(true);
                new HttpAsyncTask().execute(params);
            }
        });
    }

    private void toggleProgressDialog(boolean show) {
        if (show) {
            if ((this.progressDlg == null) || !this.progressDlg.isShowing()) {
                this.progressDlg = ProgressDialog.show(LoginActivity.this, null, null, false, true);
            }
        } else {
            if (this.progressDlg != null) {
                this.progressDlg.dismiss();
            }
        }
    }

    /**
     * set up the 4 number input controls for text verification code
     */
    private void setupInputVerifyCode() {
        this.edtvcodes.add((EditText) this.findViewById(R.id.login_txt_code_1));
        this.edtvcodes.add((EditText) this.findViewById(R.id.login_txt_code_2));
        this.edtvcodes.add((EditText) this.findViewById(R.id.login_txt_code_3));
        this.edtvcodes.add((EditText) this.findViewById(R.id.login_txt_code_4));
        for (int i = 0; i < this.edtvcodes.size(); i++) {
            this.edtvcodes.get(i).setTag(i);
        }
        TextWatcher codewatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //don't care
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //don't care
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    this.toggleButtonLogin(false);
                    return;
                }
                EditText ext = (EditText) LoginActivity.this.getCurrentFocus();
                if (ext == null) {
                    return;
                }
                int i = (Integer) ext.getTag();
                if (i < (LoginActivity.this.edtvcodes.size() - 1)) {
                    LoginActivity.this.edtvcodes.get(i + 1).requestFocus();
                }
                for (EditText edt : LoginActivity.this.edtvcodes) {
                    if (edt.getText().toString().isEmpty()) {
                        edt.requestFocus();
                        return;
                    }
                }
                this.toggleButtonLogin(true);
            }

            private void toggleButtonLogin(boolean status) {
                LoginActivity.this.btnlogin.setEnabled(status);
                Drawable dr = LoginActivity.this.getResources().getDrawable(status ? R.drawable.roundshapebtnblack : R.drawable.roundshapebtn);
                LoginActivity.this.btnlogin.setBackground(dr);
            }
        };
        for (EditText edt : this.edtvcodes) {
            edt.addTextChangedListener(codewatcher);
        }
    }

    /**
     * set up the button of requesting text verification code
     */
    private void setupReqVerifyCode() {
        this.btnSendVerifyCode = (Button) this.findViewById(R.id.login_btn_send_verify_code);
        this.btnSendVerifyCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] params = new Object[]{
                        String.format("%s/getcode?phone=%s", CommonUtilities.WS_HOST, LoginActivity.this.edtPhoneNo.getText().toString()),
                        new HttpAsyncTask.TaskDelegate() {
                            private static final String TAG = "ReqCodeTaskDeletegate";

                            @Override
                            public void taskCompletionResult(byte[] result) {
                                try {
                                    LoginActivity.this.toggleProgressDialog(false);
                                    String x = new String(result, "utf-8");
                                    Log.d(TAG, String.format("return: %s", x));
                                    JSONObject jobj = new JSONObject(x).getJSONObject("result");
                                    if (jobj.getInt("code") == 0) {
                                        LoginActivity.this.switchVerifyCodeView(false);
                                    } else {
                                        Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                                        LoginActivity.this.switchVerifyCodeView(false);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    Log.e(TAG, String.format("web response encoding error: %s", e.getMessage()));
                                } catch (JSONException e) {
                                    Log.e(TAG, String.format("jsonparse error: %s", e.getMessage()));
                                }
                            }
                        }
                };
                LoginActivity.this.toggleProgressDialog(true);
                new HttpAsyncTask().execute(params);
            }
        });
    }

    private void switchVerifyCodeView(boolean showStart) {
        this.startView.setVisibility(showStart ? View.VISIBLE : View.GONE);
        this.verifyView.setVisibility(showStart ? View.GONE : View.VISIBLE);
        if (!showStart) {
            this.tvNumberSent.setText(String.format(this.getString(R.string.login_verifycode_sent), this.edtPhoneNo.getText().toString()));
            this.verifyTimer.start();
            this.findViewById(R.id.login_txt_code_1).requestFocus();
        } else {
            this.verifyTimer.cancel();
            this.btnSendVerifyCode2.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.startView.getVisibility() == View.GONE) {
                this.switchVerifyCodeView(true);
                return true;
            } else {
                this.setResult(RESULT_CANCELED);
                this.finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setupWeibo() {
        this.imWeibo = (ImageButton) this.findViewById(R.id.login_btn_weibo);
        AuthInfo authInfo = new AuthInfo(this, CommonUtilities.Weibo_AppKey, CommonUtilities.Weibo_Redirect_URL, CommonUtilities.Weibo_Scope);
        this.ssohandler = new SsoHandler(this, authInfo);
        this.imWeibo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ssohandler.authorize(new WeiboAuthListener() {
                    @Override
                    public void onComplete(Bundle bundle) {
                        Log.d(TAG, "Weibo login succeed");
                        final Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(bundle);
                        UsersAPI userapi = new UsersAPI(LoginActivity.this, CommonUtilities.Weibo_AppKey, token);
                        LoginActivity.this.toggleProgressDialog(true);
                        userapi.show(Long.parseLong(token.getUid()), new RequestListener() {
                            @Override
                            public void onComplete(String s) {
                                LoginActivity.this.toggleProgressDialog(false);
                                final User user = User.parse(s);
                                List<Pair<String, String>> urlparams = new ArrayList<Pair<String, String>>();
                                urlparams.add(new Pair<String, String>("open_id", token.getUid()));
                                urlparams.add(new Pair<String, String>("nickname", user.screen_name));
                                urlparams.add(new Pair<String, String>("from", "weibo"));
                                List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
                                headers.add(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
                                Object[] params = new Object[]{
                                        String.format("%s/register", CommonUtilities.WS_HOST),
                                        new HttpAsyncTask.TaskDelegate() {
                                            @Override
                                            public void taskCompletionResult(byte[] result) {
                                                LoginActivity.this.toggleProgressDialog(false);
                                                try {
                                                    String s = new String(result, "utf-8");
                                                    Log.d(TAG, "taskCompletionResult: " + s);
                                                    JSONObject jobj = new JSONObject(s).getJSONObject("result");
                                                    Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                                                    if (jobj.getInt("code") == 0) {
                                                        UserInfo user = UserInfo.getUserInfo(jobj.getJSONObject("user"));
                                                        user.saveUserInfo(LoginActivity.this);
                                                        Toast.makeText(LoginActivity.this, R.string.login_weibo_ok, Toast.LENGTH_LONG).show();
                                                        LoginActivity.this.setResult(RESPONSE_CODE_WEIBO);
                                                        LoginActivity.this.finish();
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                                                    }
                                                } catch (UnsupportedEncodingException e) {
                                                    Log.e(TAG, String.format("register Weibo user %s %s decode fail", user.idstr, user.screen_name));
                                                } catch (JSONException e) {
                                                    Log.e(TAG, String.format("register Weibo user %s %s json fail", user.idstr, user.screen_name));
                                                }
                                            }
                                        },
                                        WebAPIHelper.HttpMethod.POST,
                                        WebAPIHelper.buildHttpQuery(urlparams).getBytes(),
                                        headers
                                };
                                new HttpAsyncTask().execute(params);
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                LoginActivity.this.toggleProgressDialog(false);
                                Log.e(TAG, String.format("onWeiboException: %s", e.getMessage()));
                                Toast.makeText(LoginActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, R.string.login_weibo_cancel, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setupWechat() {
        this.imWechat = (ImageButton) this.findViewById(R.id.login_btn_wechat);
        this.imWechat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                IWXAPI api = WXAPIFactory.createWXAPI(LoginActivity.this, CommonUtilities.WX_AppID, false);
                if (!api.isWXAppInstalled()) {
                    Toast.makeText(LoginActivity.this, R.string.login_wechat_not_install, Toast.LENGTH_LONG).show();
                    return;
                }
                api.registerApp(CommonUtilities.WX_AppID);
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = LoginActivity.this.getPackageName();
                api.sendReq(req);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onActivityResult request:%d  result:%d", requestCode, resultCode));
        switch (requestCode) {
            case HUAWEI_REQUEST_UNLOGIN:
                this.toggleProgressDialog(true);
                if (resultCode == 0) {
                    this.onHuaweiCancelled();
                    return;
                }
                this.startHuaweiLogin();
                break;
            case HUAWEI_REQUEST_AUTH:
                this.toggleProgressDialog(true);
                if (resultCode == 0) {
                    this.onHuaweiCancelled();
                    return;
                }
                SignInResult aresult = HuaweiId.HuaweiIdApi.getSignInResultFromIntent(data);
                Log.d(TAG, String.format("Huawei Auth returned code: %d", aresult.getStatus().getStatusCode()));
                this.onHuaweiAuthorized(aresult);
                break;
            default:
                if (this.ssohandler != null) {
                    this.ssohandler.authorizeCallBack(requestCode, resultCode, data);
                }
                break;
        }
    }

    private void setupHuawei() {
        this.imHuawei = (ImageButton) this.findViewById(R.id.login_btn_huawei);
        HuaweiIdSignInOptions signInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN).build();
        this.huaweiClient = new HuaweiApiClient.Builder(this)
                .addApi(HuaweiId.SIGN_IN_API, signInOptions)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.imHuawei.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.huaweiClient.connect();
            }
        });
    }

    private ResultCallback<SignInResult> huaweiCallback = new ResultCallback<SignInResult>() {
        @Override
        public void onResult(SignInResult signInResult) {
            if (signInResult == null) {
                Log.d(TAG, "signInResult null");
                return;
            }
            Log.d(TAG, String.format("Huawei sign onResult: %b", signInResult.isSuccess()));
            if (signInResult.isSuccess()) {
                LoginActivity.this.onHuaweiAuthorized(signInResult);
            } else {
                int code = signInResult.getStatus().getStatusCode();
                if (code == HuaweiIdStatusCodes.SIGN_IN_UNLOGIN) {
                    Intent i = signInResult.getData();
                    LoginActivity.this.startActivityForResult(i, HUAWEI_REQUEST_UNLOGIN);
                } else if (code == HuaweiIdStatusCodes.SIGN_IN_AUTH) {
                    Intent i = signInResult.getData();
                    LoginActivity.this.startActivityForResult(i, HUAWEI_REQUEST_AUTH);
                }
            }
        }
    };

    private void startHuaweiLogin() {
        if (!this.huaweiClient.isConnected()) {
            this.huaweiClient.connect();
            return;
        }
        PendingResult<SignInResult> signReuslt = HuaweiId.HuaweiIdApi.signIn(this.huaweiClient);
        signReuslt.setResultCallback(new ResultCallback<SignInResult>() {
            @Override
            public void onResult(SignInResult signInResult) {
                if (signInResult == null) {
                    Log.d(TAG, "signInResult null");
                    return;
                }
                Log.d(TAG, String.format("Huawei sign onResult: %b", signInResult.isSuccess()));
                if (signInResult.isSuccess()) {
                    LoginActivity.this.onHuaweiAuthorized(signInResult);
                } else {
                    int code = signInResult.getStatus().getStatusCode();
                    LoginActivity.this.toggleProgressDialog(true);
                    if (code == HuaweiIdStatusCodes.SIGN_IN_UNLOGIN) {
                        Intent i = signInResult.getData();
                        LoginActivity.this.startActivityForResult(i, HUAWEI_REQUEST_UNLOGIN);
                    } else if (code == HuaweiIdStatusCodes.SIGN_IN_AUTH) {
                        Intent i = signInResult.getData();
                        LoginActivity.this.startActivityForResult(i, HUAWEI_REQUEST_AUTH);
                    }
                }
            }
        });
    }

    private void onHuaweiAuthorized(SignInResult result) {
        Log.d(TAG, "onHuaweiAuthorized: ");
        Log.d(TAG, result != null ? "result" : "null");
        final SignInHuaweiId hid = result.getSignInHuaweiId();
        Log.d(TAG, "onHuaweiAuthorized: " + hid.getOpenId() + " " + hid.getDisplayName());
        this.toggleProgressDialog(true);
        List<Pair<String, String>> urlparams = new ArrayList<Pair<String, String>>();
        urlparams.add(new Pair<String, String>("open_id", hid.getOpenId()));
        urlparams.add(new Pair<String, String>("nickname", hid.getDisplayName()));
        urlparams.add(new Pair<String, String>("from", "huawei"));
        Log.d(TAG, WebAPIHelper.buildHttpQuery(urlparams));
        byte[] load;
        try {
            load = WebAPIHelper.buildHttpQuery(urlparams).getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            load = new byte[0];
        }
        List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        headers.add(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
        Object[] params = new Object[]{
                String.format("%s/register", CommonUtilities.WS_HOST),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        LoginActivity.this.toggleProgressDialog(false);
                        try {
                            String s = new String(result, "utf-8");
                            Log.d(TAG, "taskCompletionResult: " + s);
                            JSONObject jobj = new JSONObject(s).getJSONObject("result");
                            Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                            if (jobj.getInt("code") == 0) {
                                UserInfo user = UserInfo.getUserInfo(jobj.getJSONObject("user"));
                                user.saveUserInfo(LoginActivity.this);
                                LoginActivity.this.setResult(RESPONSE_CODE_HUAWEI);
                                LoginActivity.this.finish();
                            } else {
                                Toast.makeText(LoginActivity.this, jobj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, String.format("register Huawei user %s %s decode fail", hid.getDisplayName(), hid.getDisplayName()));
                        } catch (JSONException e) {
                            Log.e(TAG, String.format("register Huawei user %s %s json fail", hid.getDisplayName(), hid.getDisplayName()));
                        }
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                WebAPIHelper.buildHttpQuery(urlparams).getBytes(),
                headers
        };
        new HttpAsyncTask().execute(params);
    }

    private void onHuaweiCancelled() {
        Log.d(TAG, "Huawei cancelled");
        Toast.makeText(LoginActivity.this, R.string.login_huawei_cancel, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpdateFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Huawei client onUpdateFailed");
        Log.d(TAG, String.format("code:%d message:%s", connectionResult.getErrorCode(), connectionResult.toString()));
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "Huawei client onConnected");
        this.startHuaweiLogin();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Huawei client onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Huawei client onConnectionFailed");
        Log.d(TAG, String.format("code:%d", connectionResult.getErrorCode()));
        final int errorCode = connectionResult.getErrorCode();
        final HuaweiApiAvailability availability = HuaweiApiAvailability.getInstance();
        Log.d(TAG, String.format("Huawei API avaiable: %b", availability.isHuaweiMobileServicesAvailable(this)));
        if (availability.isUserResolvableError(errorCode)) {
            AlertDialog adlg = new AlertDialog.Builder(this)
                    .setTitle(R.string.login_huawei_download_service_title)
                    .setMessage(R.string.login_huawei_download_service_description)
                    .setNeutralButton(R.string.login_huawei_prompt_gotit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            availability.resolveError(LoginActivity.this, errorCode, HUAWEI_REQUEST_RESOLVE_ERROR, LoginActivity.this);
                        }
                    }).show();

        }
    }

}

