package com.wewow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.sina.weibo.sdk.openapi.models.User;
import com.vansuita.pickimage.img.ImageHandler;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.Utils;
import com.youzan.sdk.YouzanToken;
import com.youzan.sdk.event.AbsAuthEvent;
import com.youzan.sdk.event.AbsChooserEvent;
import com.youzan.sdk.tool.Preference;
import com.youzan.sdk.web.plugin.YouzanBrowser;
import com.youzan.sdk.web.plugin.YouzanClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/4/16.
 */
public class WebPageActivity extends Activity {

    private YouzanBrowser mView;
    private YouzanToken token=new YouzanToken();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_page);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        Intent intent=getIntent();

        String url=intent.getStringExtra("url");
//
//        //WebView
//        WebView webView=(WebView)findViewById(R.id.Toweb);
//        webView.setWebChromeClient(new WebChromeClient());
//        webView.setWebViewClient(new WebViewClient());
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.loadUrl(url);
//
//        //设置可自由缩放网页
//        webView.getSettings().setSupportZoom(true);
//        webView.getSettings().setBuiltInZoomControls(true);
//
//        // 如果页面中链接，如果希望点击链接继续在当前browser中响应，
//        // 而不是新开Android的系统browser中响应该链接，必须覆盖webview的WebViewClient对象
//        webView.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
//                view.loadUrl(url);
//                return true;
//            }
//        });
        LinearLayout view=(LinearLayout)findViewById(R.id.Toweb);
//
        mView = new YouzanBrowser(this);
        view.addView(mView);
//        setContentView(mView);
        setupYouzanView(mView);
//        String url="https://h5.youzan.com/v2/showcase/homepage?alias=mrs9kuj1";
        mView.loadUrl(url);
//        if(UserInfo.isUserLogged(WebPageActivity.this))
//        {
            youZanLogin();
//        }
//        else {
//            LoginUtils.startLogin(WebPageActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
//        }
        setUpToolBar();
    }

    private void setupYouzanView(YouzanClient client) {
        //订阅认证事件
        client.subscribe(new AbsAuthEvent() {
            /**
             * 有赞SDK认证回调.
             * 在加载有赞的页面时, SDK相应会回调该方法.
             * <p/>
             * 从自己的服务器上请求同步认证后组装成{@link com.youzan.sdk.YouzanToken}, 调用{code view.sync(token);}同步信息.
             *
             * @param view      发起回调的视图
             * @param needLogin 表示当下行为是否需要需要用户角色的认证信息, True需要.
             */
            @Override
            public void call(View view, boolean needLogin) {
                /**
                 * <pre>
                 *     处理逻辑
                 *
                 *     1. 判断是否需要需要用户角色的认证信息?
                 *     2. 是(needLogin=True) : 判断App内的用户是否登录? 已登录:  向服务端请求带用户角色的认证信息, 并同步给SDK; 未登录: 唤起App内登录界面.
                 *     3. 否(needLogin=False): 向服务端请求不需要登录态的认证信息, 并同步给SDK.
                 * </pre>
                 */


                //实现代码略...
                Toast.makeText(WebPageActivity.this,"called",Toast.LENGTH_LONG).show();
                if(needLogin)
                {
//                    if(UserInfo.isUserLogged(WebPageActivity.this))
//                    {
                      youZanLogin();
//                    }
//                    else {
//                        LoginUtils.startLogin(WebPageActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
//                    }

                }


            }
        });

        //订阅文件选择事件
        client.subscribe(new AbsChooserEvent() {
            @Override
            public void call(View view, Intent intent, int requestCode) throws ActivityNotFoundException {
                //调用系统图片选择器
                startActivity(intent);
            }
        });
    }

    //go back
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        WebView browser=(WebView)findViewById(R.id.Toweb);
//        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mView.canGoBack()) {
            mView.goBack();
            return true;
        }
//        //  return true;
//        // If it wasn't the Back key or there's no web page history, bubble up to the default
//        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void setUpToolBar() {
        ImageView imageViewBack=(ImageView) findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
//            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//            layout.setVisibility(View.VISIBLE);
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void youZanLogin() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId="0";

        if(UserInfo.isUserLogged(WebPageActivity.this))
        {
            userId = UserInfo.getCurrentUser(WebPageActivity.this).getId().toString();
        }


        iTask.youzanLogin(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    String data = new JSONObject(realData).getJSONObject("result").getString("data");
                    if (!data.contains("cookie_value")) {
                        Toast.makeText(WebPageActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject jsonObject=new JSONObject(realData).getJSONObject("result").getJSONObject("data");
                        token.setAccessToken(jsonObject.getString("access_token"));
                        token.setCookieKey(jsonObject.getString("cookie_key"));
                        token.setCookieValue(jsonObject.getString("cookie_value"));
                        mView.sync(token);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(WebPageActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(WebPageActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(WebPageActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /**
             * 用户登录成功返回, 从自己的服务器上请求同步认证后组装成{@link com.youzan.sdk.YouzanToken},
             * 调用{code view.sync(token);}同步信息.
             */
            if (LoginActivity.REQUEST_CODE_LOGIN == requestCode&&resultCode!=0) {
                youZanLogin();
            } else {
                //处理文件上传
                mView.receiveFile(requestCode, data);
            }
        }
    }


}
