package com.wewow;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements IWXAPIEventHandler {

    public static final int REQUEST_CODE_LOGIN = 1;

    public static final int RESPONSE_CODE_MOILE = 1;
    public static final int RESPONSE_CODE_WECHAT = 2;
    public static final int RESPONSE_CODE_WEIBO = 3;
    public static final int RESPONSE_CODE_HUAWEI = 4;

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

    /**
     * register event handlers
     */
    private void initView() {
        this.edtPhoneNo = (EditText) this.findViewById(R.id.login_phone_number);
        this.imWechat = (ImageButton) this.findViewById(R.id.login_btn_wechat);
        this.imWeibo = (ImageButton) this.findViewById(R.id.login_btn_weibo);
        this.imHuawei = (ImageButton) this.findViewById(R.id.login_btn_huawei);
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
                        new TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                LoginActivity.this.progressDlg.dismiss();
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
                LoginActivity.this.progressDlg = ProgressDialog.show(LoginActivity.this, null, null, false, true);
                new LoginAyncTask().execute(params);
            }
        });
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
                        new TaskDelegate() {
                            private static final String TAG = "ReqCodeTaskDeletegate";

                            @Override
                            public void taskCompletionResult(byte[] result) {
                                try {
                                    LoginActivity.this.progressDlg.dismiss();
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
                LoginActivity.this.progressDlg = ProgressDialog.show(LoginActivity.this, null, null, true, false);
                new LoginAyncTask().execute(params);
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

    private interface TaskDelegate {
        public void taskCompletionResult(byte[] result);
    }

    private class LoginAyncTask extends AsyncTask<Object, Integer, byte[]> {

        public TaskDelegate delegate;

        @Override
        protected byte[] doInBackground(Object... objects) {
            String url = (String) objects[0];
            this.delegate = objects.length > 1 ? (TaskDelegate) objects[1] : null;
            WebAPIHelper.HttpMethod method = objects.length > 2 ? (WebAPIHelper.HttpMethod) objects[2] : WebAPIHelper.HttpMethod.GET;
            byte[] buf = objects.length > 3 ? (byte[]) objects[3] : null;
            WebAPIHelper wpi = WebAPIHelper.getWewowWebAPIHelper();
            return wpi.callWebAPI(url, method, buf);
        }

        @Override
        protected void onPostExecute(byte[] result) {
            this.delegate.taskCompletionResult(result);
        }
    }

    private void setupWeibo() {
        AuthInfo authInfo = new AuthInfo(this, CommonUtilities.Weibo_AppKey, CommonUtilities.Weibo_Redirect_URL, CommonUtilities.Weibo_Scope);
        final SsoHandler ssohandler = new SsoHandler(this, authInfo);
        this.imWeibo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ssohandler.authorize(new WeiboAuthListener() {
                    @Override
                    public void onComplete(Bundle bundle) {
                        // todo parse login result and login
                        Toast.makeText(LoginActivity.this, R.string.login_weibo_ok, Toast.LENGTH_LONG).show();
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
        this.imWechat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                IWXAPI api = WXAPIFactory.createWXAPI(LoginActivity.this, CommonUtilities.WX_AppID, false);
                api.registerApp(CommonUtilities.WX_AppID);
                if (!api.isWXAppInstalled()) {
                    Toast.makeText(LoginActivity.this, R.string.login_wechat_not_install, Toast.LENGTH_LONG).show();
                    return;
                }
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = LoginActivity.this.getPackageName();
                api.sendReq(req);
            }
        });
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, String.format("WX req"));
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, String.format("WX resp"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onActivityResult: %d", requestCode));
        switch (requestCode) {
            case 0x101: {
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}

