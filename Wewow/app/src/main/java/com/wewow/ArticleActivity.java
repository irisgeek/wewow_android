package com.wewow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wewow.dto.Article;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

public class ArticleActivity extends AppCompatActivity {

    public static final String ARTICLE_ID = "ARTICLE_ID";
    private static final String TAG = "ArticleActivity";
    private TextView title;
    private WebView content;
    private ImageView logo;
    private LinearLayout discuzContainer;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_article);
        this.setupUI();
        Intent i = this.getIntent();
        int id = i.getIntExtra(ARTICLE_ID, -1);
        Object[] params = new Object[]{
                String.format("%s/article_detail?article_id=%d", CommonUtilities.WS_HOST, id),
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

    private void setupUI() {
        this.findViewById(R.id.article_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArticleActivity.this.finish();
            }
        });
        this.title = (TextView) this.findViewById(R.id.article_title);
        this.content = (WebView) this.findViewById(R.id.article_content);
        this.logo = (ImageView) this.findViewById(R.id.article_logo);
        this.discuzContainer = (LinearLayout) this.findViewById(R.id.article_discuss_container);
        this.setupFeedback();
        this.findViewById(R.id.share_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ArticleActivity.this, ShareActivity.class);
                i.putExtra(ShareActivity.SHARE_TYPE, ShareActivity.SHARE_TYPE_WEIBO);
                i.putExtra(ShareActivity.SHARE_CONTEXT, ArticleActivity.this.data.optString("title", "no title"));
                i.putExtra(ShareActivity.SHARE_URL, ArticleActivity.this.data.optString("share_link", "no link"));
                BitmapDrawable bdr = (BitmapDrawable) ArticleActivity.this.logo.getDrawable();
                if (bdr != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bdr.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos);
                    i.putExtra(ShareActivity.SHARE_IMAGE, baos.toByteArray());
                }
                ArticleActivity.this.startActivity(i);
            }
        });
    }

    private void fillContent(JSONObject article) {
        this.data = article;
        this.title.setText(article.optString("title", "No title"));
        this.content.loadUrl(article.optString("content", "No content"));
        new RemoteImageLoader(this, article.optString("image_750_1112"), new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                ArticleActivity.this.logo.setImageDrawable(dr);
            }
        });
        this.fillComments(article.optJSONArray("comment_list"));
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
                itemView.setBackgroundColor(i % 2 == 0 ? Color.argb(255, 216, 219, 223) : Color.WHITE);
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
        CharSequence cs = tv.getText();
        String link = this.getString(R.string.article_feedback_link);
        SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
        SpannableString ss = new SpannableString(link);
        ss.setSpan(new UnderlineSpan(), 0, link.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ssb.append(ss);
        tv.setText(ssb);
    }
}
