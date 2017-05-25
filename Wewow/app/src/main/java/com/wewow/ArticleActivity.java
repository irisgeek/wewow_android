package com.wewow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.utils.BlurBuilder;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ARTICLE_ID = "ARTICLE_ID";
    private static final String TAG = "ArticleActivity";
    private TextView title;
    private WebView content;
    private ImageView logo;
    private ImageView like;
    private TextView article_fav_count;
    private LinearLayout discuzContainer;
    CircleProgressBar progressBar;
    View layout_content;
    private JSONObject data;
    private ArrayList<String> pictures = new ArrayList<>();
    private int id;
    private int likedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_article);
        this.setupUI();
        Intent i = this.getIntent();
        this.id = i.getIntExtra(ARTICLE_ID, -1);
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("article_id", String.valueOf(this.id)));
        if (UserInfo.isUserLogged(this)) {
            fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(this).getId().toString()));
        }
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/article_detail", CommonUtilities.WS_HOST), fields),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        ProgressDialogUtil.getInstance(ArticleActivity.this).finishProgressDialog();
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj != null) {
                            try {
                                ArticleActivity.this.fillContent(jobj.getJSONObject("result").getJSONObject("data"));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON error");
                            }
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET,
        };
        ProgressDialogUtil.getInstance(this).showProgressDialog();
        new HttpAsyncTask().execute(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_more_discuss:
            case R.id.iv_more_discuss:
                Intent intent = new Intent(ArticleActivity.this, AllCommentActivity.class);
                intent.putExtra("articleId", id + "");
                startActivity(intent);
                break;
        }
    }

    private class ArticleJS {
        private Pattern imgPattern = Pattern.compile("<img\\s+src=\"(.+?)\"");

        @JavascriptInterface
        public void onGetPage(String html) {
            //Log.d(TAG, String.format("onGetPage: %d", html.length()));
            Matcher m = this.imgPattern.matcher(html);
            while (m.find()) {
                //Log.d(TAG, String.format("found image: %s", html.substring(m.start(1), m.end(1))));
                ArticleActivity.this.pictures.add(html.substring(m.start(1), m.end(1)));
            }
        }
    }

    private ArticleJS js = new ArticleJS();

    private void setupUI() {
        this.findViewById(R.id.article_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArticleActivity.this.finish();
            }
        });
        this.title = (TextView) this.findViewById(R.id.article_title);
        this.content = (WebView) this.findViewById(R.id.article_content);
        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        layout_content = findViewById(R.id.layout_content);
        WebSettings ws = this.content.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setAllowContentAccess(true);
        this.content.addJavascriptInterface(this.js, "articlejs");
        this.content.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //Log.d(TAG, "onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    URI u = new URI(url);
                    if (u.getScheme().equals("wewow")) {
                        //Log.d(TAG, "shouldOverrideUrlLoading: " + u.getHost() + "  " + u.getQuery());
                        String[] queries = u.getQuery().split("\\?");
                        //Log.d(TAG, "clicked: " + queries[0]);
                        int i = ArticleActivity.this.pictures.indexOf(queries[0].replace("url=", ""));
                        Intent intent = new Intent(ArticleActivity.this, ShowImageActivity.class);
                        intent.putExtra(ShowImageActivity.IMAGE_LIST, ArticleActivity.this.pictures);
                        intent.putExtra(ShowImageActivity.IMAGE_INDEX, i);
                        ArticleActivity.this.startActivity(intent);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                } catch (URISyntaxException e) {
                    return true;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:articlejs.onGetPage(document.documentElement.innerHTML);");
                layout_content.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.scrollview).scrollTo(0, 0);
            }
        });
        this.logo = (ImageView) this.findViewById(R.id.article_logo);
        this.discuzContainer = (LinearLayout) this.findViewById(R.id.article_discuss_container);
        this.setupFeedback();
        this.findViewById(R.id.article_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ArticleActivity.this.data == null) {
                    return;
                }
                Intent intent = new Intent(ArticleActivity.this, ShareActivity.class);
                intent.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_TOSELECT);
                intent.putExtra(ShareActivity.SHARE_CONTEXT, data.optString("title"));
                intent.putExtra(ShareActivity.SHARE_URL, data.optString("share_link"));
                BitmapDrawable bd = (BitmapDrawable) ArticleActivity.this.logo.getDrawable();
                if (bd != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bd.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos);
                    intent.putExtra(ShareActivity.SHARE_IMAGE, baos.toByteArray());
                }
                View v = findViewById(android.R.id.content);
                if (v != null) {
                    Bitmap bm = BlurBuilder.blur(v);
                    byte[] buf = Utils.getBitmapBytes(bm);
                    intent.putExtra(ShareActivity.BACK_GROUND, buf);
                }
                startActivity(intent);
            }
        });
        this.like = (ImageView) this.findViewById(R.id.article_fav);
        article_fav_count = (TextView) findViewById(R.id.article_fav_count);
        findViewById(R.id.layout_article_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserInfo.isUserLogged(ArticleActivity.this)) {
                    LoginUtils.startLogin(ArticleActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                    return;
                }
                Drawable.ConstantState notliked = ArticleActivity.this.getResources().getDrawable(R.drawable.mark_b).getConstantState();
                Drawable.ConstantState currentlike = ArticleActivity.this.like.getDrawable().getConstantState();
                final Integer like = notliked.equals(currentlike) ? 1 : 0;
                ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
                UserInfo ui = UserInfo.getCurrentUser(ArticleActivity.this);
                fields.add(new Pair<String, String>("user_id", ui.getId().toString()));
                fields.add(new Pair<String, String>("token", ui.getToken()));
                fields.add(new Pair<String, String>("item_type", "article"));
                fields.add(new Pair<String, String>("item_id", String.valueOf(ArticleActivity.this.id)));
                fields.add(new Pair<String, String>("like", like.toString()));
                ArrayList<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
                headers.add(WebAPIHelper.getHttpFormUrlHeader());
                Object[] params = new Object[]{
                        String.format("%s/like", CommonUtilities.WS_HOST),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                try {
                                    int i = jobj.getJSONObject("result").getInt("code");
                                    if (i != 0) {
                                        throw new Exception(String.valueOf(i));
                                    }
                                    ArticleActivity.this.like.setImageDrawable(ArticleActivity.this.getResources().getDrawable(like == 1 ? R.drawable.marked_b : R.drawable.mark_b));
                                    String s;
                                    if (like == 1) {
                                        likedCount += 1;
                                        s = ArticleActivity.this.getString(R.string.fav_succeed);
                                    } else {
                                        likedCount -= 1;
                                        s = ArticleActivity.this.getString(R.string.unfav_succeed);
                                    }
                                    article_fav_count.setText(likedCount + "");
                                    MessageBoxUtils.messageBoxWithNoButton(ArticleActivity.this, true, s, 2500);
                                } catch (Exception e) {
                                    Log.e(TAG, String.format("favourite fail: %s", e.getMessage()));
                                    //Toast.makeText(ArticleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    String s = ArticleActivity.this.getString(like == 1 ? R.string.fav_fail : R.string.unfav_fail);
                                    MessageBoxUtils.messageBoxWithNoButton(ArticleActivity.this, true, s, 2500);
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.POST,
                        WebAPIHelper.buildHttpQuery(fields).getBytes(),
                        headers
                };
                new HttpAsyncTask().execute(params);
            }
        });
        this.findViewById(R.id.share_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ArticleActivity.this, ShareActivity.class);
                i.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_WEIBO);
                i.putExtra(ShareActivity.SHARE_CONTEXT, ArticleActivity.this.data.optString("title", "no title"));
                i.putExtra(ShareActivity.SHARE_URL, ArticleActivity.this.data.optString("share_link", "no link"));
                BitmapDrawable bdr = (BitmapDrawable) ArticleActivity.this.logo.getDrawable();
                if (bdr != null) {
                    byte[] buf = Utils.getBitmapBytes(bdr.getBitmap());
                    i.putExtra(ShareActivity.SHARE_IMAGE, buf);
                }
                ArticleActivity.this.startActivity(i);
            }
        });
        this.findViewById(R.id.share_copylink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ArticleActivity.this, ShareActivity.class);
                i.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_COPY_LINK);
                i.putExtra(ShareActivity.SHARE_URL, ArticleActivity.this.data.optString("share_link", "no link"));
                ArticleActivity.this.startActivity(i);
            }
        });
        this.findViewById(R.id.share_wechat_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ArticleActivity.this, ShareActivity.class);
                i.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_WECHAT_CIRCLE);
                i.putExtra(ShareActivity.SHARE_CONTEXT, ArticleActivity.this.data.optString("title", "no title"));
                i.putExtra(ShareActivity.SHARE_URL, ArticleActivity.this.data.optString("share_link", "no link"));
                BitmapDrawable bdr = (BitmapDrawable) ArticleActivity.this.logo.getDrawable();
                if (bdr != null) {
                    byte[] buf = Utils.getBitmapBytes(bdr.getBitmap());
                    i.putExtra(ShareActivity.SHARE_IMAGE, buf);
                }
                ArticleActivity.this.startActivity(i);
            }
        });
        this.findViewById(R.id.share_wechat_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ArticleActivity.this, ShareActivity.class);
                i.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_WECHAT_FRIEND);
                i.putExtra(ShareActivity.SHARE_CONTEXT, ArticleActivity.this.data.optString("title", "no title"));
                i.putExtra(ShareActivity.SHARE_URL, ArticleActivity.this.data.optString("share_link", "no link"));
                BitmapDrawable bdr = (BitmapDrawable) ArticleActivity.this.logo.getDrawable();
                if (bdr != null) {
                    byte[] buf = Utils.getBitmapBytes(bdr.getBitmap());
                    i.putExtra(ShareActivity.SHARE_IMAGE, buf);
                }
                ArticleActivity.this.startActivity(i);
            }
        });
        findViewById(R.id.share_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                String content = data.optString("title", "no title") + " " + data.optString("share_link", "no link");
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(sendIntent, "选择分享方式"));
            }
        });
        findViewById(R.id.iv_more_discuss).setOnClickListener(this);
        findViewById(R.id.tv_more_discuss).setOnClickListener(this);
    }

    private void fillContent(JSONObject article) {
        this.data = article;
        this.title.setText(article.optString("title", "No title"));
        this.like.setImageDrawable(this.getResources().getDrawable(article.optInt("liked", 0) == 1 ? R.drawable.marked_b : R.drawable.mark_b));
        likedCount = article.optInt("liked_count");
        article_fav_count.setText(likedCount + "");
        article_fav_count.setVisibility(likedCount == 0 ? View.GONE : View.VISIBLE);
        this.content.loadUrl(article.optString("content", "No content"));
        new RemoteImageLoader(this, article.optString("image_750_1112"), new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                ArticleActivity.this.logo.setImageDrawable(dr);
            }
        });
        this.fillComments(article.optJSONObject("rel_data").optJSONArray("comment_list"));
    }

    private void fillComments(JSONArray comments) {
        if (comments == null) {
            return;
        }
        int cc = comments.length() > 2 ? 2 : comments.length();
        try {
            for (int i = 0; i < cc; i++) {
                JSONObject comment = comments.getJSONObject(i);
                View itemView = View.inflate(this, R.layout.article_comment, null);
//                itemView.setBackgroundColor(i % 2 == 0 ? Color.argb(255, 216, 219, 223) : Color.WHITE);
                TextView tv = (TextView) itemView.findViewById(R.id.article_comment_author);
                tv.setText(comment.optString("user", "no author"));
                tv = (TextView) itemView.findViewById(R.id.article_comment_date);
                tv.setText(comment.optString("time", "no time"));
                tv = (TextView) itemView.findViewById(R.id.article_comment_content);
                tv.setText(comment.optString("content", "no content"));
                tv = (TextView) itemView.findViewById(R.id.article_comment_liked_count);
                tv.setText(String.format("%d", comment.optInt("liked_count", 0)));
                this.discuzContainer.addView(itemView);
            }
        } catch (JSONException e) {
            Log.e(TAG, "fillComments error");
        }
    }

    private void setupFeedback() {
        TextView tv = (TextView) this.findViewById(R.id.article_feedback_link);
        tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv.getPaint().setAntiAlias(true);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArticleActivity.this, FeedbackActivity.class));
            }
        });
    }
}
