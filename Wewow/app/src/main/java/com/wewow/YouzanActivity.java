/*
 * author: lachang@youzan.com
 * Copyright (C) 2016 Youzan, Inc. All Rights Reserved.
 */
package com.wewow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.WebAPIHelper;
import com.youzan.sdk.YouzanToken;
import com.youzan.sdk.event.AbsAuthEvent;
import com.youzan.sdk.event.AbsChooserEvent;
import com.youzan.sdk.event.AbsShareEvent;
import com.youzan.sdk.model.goods.GoodsShareModel;
import com.youzan.sdk.web.plugin.YouzanBrowser;
import com.youzan.sdk.web.plugin.YouzanClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class YouzanActivity extends Activity {
    private static final int CODE_REQUEST_LOGIN = 0x101;
    private YouzanBrowser mView;
    private Activity context;
    private YouzanToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mView = new YouzanBrowser(this);

        setContentView(mView);

        setupYouzanView(mView);

        //替换成需要展示入口的链接
        mView.loadUrl(getIntent().getStringExtra("url"));
    }


    private void setupYouzanView(YouzanClient client) {
        //订阅认证事件
        client.subscribe(new AbsAuthEvent() {
            /**
             * 有赞SDK认证回调.
             * 在加载有赞的页面时, SDK相应会回调该方法.
             * <p>
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

                if (needLogin) {
                    if (!UserInfo.isUserLogged(context)) {
                        LoginUtils.startLogin(context, LoginActivity.REQUEST_CODE_LOGIN);
                    } else {
                        loginYouzan(1);
                    }
                } else {
                    loginYouzan(0);
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

        //订阅分享事件
        client.subscribe(new AbsShareEvent() {
            @Override
            public void call(View view, GoodsShareModel data) {
                /**
                 * 在获取数据后, 可以使用其他分享SDK来提高分享体验.
                 * 这里调用系统分享来简单演示分享的过程.
                 */
                String content = String.format("%s %s", data.getDesc(), data.getLink());
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, data.getTitle());
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private void loginYouzan(final int type) {
        List<Pair<String, String>> ps = new ArrayList<>();
        ps.add(new Pair<>("client_id", CommonUtilities.Youzan_AppID));
        ps.add(new Pair<>("client_secret", CommonUtilities.Youzan_AppSecret));
        String url = "https://uic.youzan.com/sso/open/initToken";
        if(type == 1){
            UserInfo ui = UserInfo.getCurrentUser(context);
            ps.add(new Pair<>("open_user_id", ui.getId() + ""));
            url = "https://uic.youzan.com/sso/open/login";
        }
        ArrayList<Pair<String, String>> header = new ArrayList<>();
        header.add(WebAPIHelper.getHttpFormUrlHeader());
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(url, ps),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(context, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") == 0) {
                                JSONObject data =  r.optJSONObject("data");
                                token = new YouzanToken();
                                token.setAccessToken(data.optString("access_token"));
                                if(type == 1){
                                    token.setCookieKey(data.optString("cookie_key"));
                                    token.setCookieValue(data.optString("cookie_value"));
                                }
                                mView.sync(token);
                            } else {
                                Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                WebAPIHelper.buildHttpQuery(ps).getBytes(),
                header
        };
        new HttpAsyncTask().execute(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /**
             * 用户登录成功返回, 从自己的服务器上请求同步认证后组装成{@link com.youzan.sdk.YouzanToken},
             * 调用{code view.sync(token);}同步信息.
             */
            if (CODE_REQUEST_LOGIN == requestCode) {
//                mView.sync(token);
            } else {
                //处理文件上传
                mView.receiveFile(requestCode, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!mView.pageGoBack()) {
            super.onBackPressed();
        }
    }
}
