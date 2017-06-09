package com.wewow;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.utils.BlurBuilder;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.ShareUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.wewow.LoginActivity.REQUEST_CODE_LOGIN;

public class LifePostActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "LifePostActivity";
    public static final String POST_ID = "POST_ID";

    private TextView title;
    private TextView desc;
    private View contentView, header, statusbar;
    private ImageView addpost;
    private View layout_title, lifepost_title_shadow;
    private ImageView lifepost_back, lifepost_share;
    private TextView lifepost_title;
    private JSONArray comments = new JSONArray();
    private UserInfo user;
    private int postId;
    private JSONObject daily_topic;
    private ListView listComments;
    private LinearLayout layout_bottom;
    private CircleProgressBar progressBar;
    private final int ADDPOST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_post);
        this.setupUI();
        Intent i = this.getIntent();
        postId = i.getIntExtra(POST_ID, -1);

        getDailyTopic(true);
    }

    private void getDailyTopic(final boolean isFirst) {
        this.user = UserInfo.isUserLogged(this) ? UserInfo.getCurrentUser(this) : UserInfo.getAnonymouUser();
        progressBar.setVisibility(View.VISIBLE);
        Object[] params = new Object[]{
                String.format("%s/daily_topic?user_id=%d&daily_topic_id=%d", CommonUtilities.WS_HOST, LifePostActivity.this.user.getId(), postId),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj != null) {
                            LifePostActivity.this.onDataLoad(jobj.optJSONObject("result").optJSONObject("data"), isFirst);
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void setupUI() {
        this.findViewById(R.id.lifepost_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifePostActivity.this.finish();
            }
        });
        this.contentView = this.findViewById(R.id.lifepost_root);
        this.statusbar = this.findViewById(R.id.statusbar);
        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        this.layout_title = this.findViewById(R.id.layout_title);
        this.lifepost_title_shadow = this.findViewById(R.id.lifepost_title_shadow);
        this.lifepost_back = (ImageView) this.findViewById(R.id.lifepost_back);
        this.lifepost_share = (ImageView) this.findViewById(R.id.lifepost_share);
        this.lifepost_title = (TextView) this.findViewById(R.id.tv_lifepost_title);
        listComments = (ListView) this.findViewById(R.id.lifepost_comments);
        layout_bottom=(LinearLayout)this.findViewById(R.id.layout_bottom);
        header = View.inflate(this, R.layout.header_life_post, null);
        this.title = (TextView) header.findViewById(R.id.lifepost_title);
        this.desc = (TextView) header.findViewById(R.id.lifepost_desc);
        listComments.addHeaderView(header);
        View footer = new View(this);
//        AbsListView.LayoutParams params = new AbsListView.LayoutParams(0, Utils.dipToPixel(this, 80));
//        footer.setLayoutParams(params);
        footer=View.inflate(this,R.layout.layout_bottom_life_post,null);
        listComments.addFooterView(footer);
        listComments.setAdapter(this.adapter);
        listComments.setOnScrollListener(this);
        this.addpost = (ImageView) this.findViewById(R.id.lifepost_newpost);
        this.addpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserInfo.isUserLogged(LifePostActivity.this)) {
                    LoginUtils.startLogin(LifePostActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                }else{
                    Intent i = new Intent(LifePostActivity.this, AddPostActivity.class);
                    BitmapDrawable bdr = (BitmapDrawable) LifePostActivity.this.contentView.getBackground();
                    if (bdr != null) {
                        i.putExtra(AddPostActivity.BACK_GROUND, Utils.getBitmapBytes(bdr.getBitmap()));
                    }
                    i.putExtra(AddPostActivity.TOPIC_ID, LifePostActivity.this.postId);
                    LifePostActivity.this.startActivityForResult(i, ADDPOST);
                }
            }
        });
        this.findViewById(R.id.lifepost_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils su = new ShareUtils(LifePostActivity.this);
                su.setContent(LifePostActivity.this.title.getText().toString());
                BitmapDrawable bd = (BitmapDrawable) LifePostActivity.this.contentView.getBackground();
                if (bd != null) {
                    su.setPicture(bd.getBitmap());
                }
                su.setUrl(LifePostActivity.this.daily_topic.optString("share_link"));
                Log.d(TAG, LifePostActivity.this.daily_topic.optString("share_link"));
                su.share();
            }
        });
    }

    private void onDataLoad(JSONObject jobj, boolean isFirst) {
        if (jobj == null) {
            return;
        }
        try {
            if(isFirst){
                this.daily_topic = jobj.getJSONObject("daily_topic");
                this.title.setText(daily_topic.optString("title"));
                this.desc.setText(daily_topic.optString("content"));
                new RemoteImageLoader(this, daily_topic.optString("image"), new RemoteImageLoader.RemoteImageListener() {
                    @Override
                    public void onRemoteImageAcquired(Drawable dr) {
                        BitmapDrawable bdr = (BitmapDrawable) dr;
                        Bitmap bm = bdr.getBitmap();
                        Bitmap blurMap = BlurBuilder.blur(LifePostActivity.this, bm);
                        bm.recycle();
                        LifePostActivity.this.contentView.setBackground(new BitmapDrawable(LifePostActivity.this.getResources(), blurMap));
                    }
                });
            }
            this.comments = jobj.getJSONArray("comments");
            if (this.comments.length() > 0) {
                this.findViewById(R.id.lifepost_nodata_area).setVisibility(View.GONE);
                this.listComments.setVisibility(View.VISIBLE);
                this.layout_bottom.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.e(TAG, "onDataLoad: fail");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED && requestCode == REQUEST_CODE_LOGIN){
            getDailyTopic(false);
        }else if(resultCode == RESULT_OK && requestCode == ADDPOST){
            getDailyTopic(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BaseAdapter adapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return comments == null ? 0 : comments.length();
        }

        @Override
        public Object getItem(int i) {
            try {
                return LifePostActivity.this.comments.get(i);
            } catch (JSONException x) {
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(LifePostActivity.this, R.layout.lifepost_comment, null);
            }
            JSONObject jobj = (JSONObject) this.getItem(i);
            TextView tv = (TextView) view.findViewById(R.id.lifepost_comment_liked);
            tv.setText(String.format("%d", jobj.optInt("liked_count", 0)));
            String author = jobj.optString("user", "");
            tv = (TextView) view.findViewById(R.id.lifepost_comment_author);
            tv.setText(author);
            tv = (TextView) view.findViewById(R.id.lifepost_comment_content);
            tv.setText(jobj.optString("content"));
            View v = view.findViewById(R.id.lifepost_comment_likearea);
            v.setTag(jobj.optInt("id"));
            ImageView iv = (ImageView) view.findViewById(R.id.lifepost_comment_like);
            iv.setTag(v);
            iv.setOnClickListener(LifePostActivity.this.likeClick);
            iv.setImageDrawable(LifePostActivity.this.getResources().getDrawable(jobj.optInt("liked", 0) == 0 ? R.drawable.like : R.drawable.liked));
            if (!author.equals(LifePostActivity.this.user.getNickname())) {
                view.findViewById(R.id.lifepost_mycomment).setVisibility(View.GONE);
//                v.setBackgroundColor(Color.WHITE);
            } else {
                view.findViewById(R.id.lifepost_mycomment).setVisibility(View.VISIBLE);
//                v.setBackgroundColor(Color.argb(255, 252, 211, 145));
            }
            View normalPanel = view.findViewById(R.id.normal_area);
            View menuPanel = view.findViewById(R.id.comment_menu);
            normalPanel.setVisibility(View.VISIBLE);
            menuPanel.setVisibility(View.GONE);
            Pair<View, View> menuHolder = new Pair<>(normalPanel, menuPanel);

            ImageView moreaction = (ImageView) view.findViewById(R.id.comment_moreaction);
            moreaction.setTag(menuHolder);
            moreaction.setOnClickListener(LifePostActivity.this.openMenuListener);

            ImageView close = (ImageView) view.findViewById(R.id.post_close);
            close.setTag(menuHolder);
            close.setOnClickListener(LifePostActivity.this.closeMenuListener);

            ImageView share = (ImageView) view.findViewById(R.id.post_share);
            share.setTag(jobj);
            share.setOnClickListener(LifePostActivity.this.commentShareListener);

            ImageView copy = (ImageView) view.findViewById(R.id.post_copy);
            copy.setTag(jobj);
            copy.setOnClickListener(LifePostActivity.this.commentCopyListener);

            ImageView impeach = (ImageView) view.findViewById(R.id.post_delete);
            impeach.setImageDrawable(LifePostActivity.this.getResources().getDrawable(author.equals(LifePostActivity.this.user.getNickname()) ? R.drawable.deletepost : R.drawable.impeach));
            impeach.setTag(jobj);
            impeach.setOnClickListener(LifePostActivity.this.commentImpeachListener);

            return view;
        }
    };

    private View.OnClickListener likeClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!UserInfo.isUserLogged(LifePostActivity.this)) {
                LoginUtils.startLogin(LifePostActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                return;
            }
            final View likearea = (View) view.getTag();
            int id = (Integer) likearea.getTag();
            final ImageView iv = (ImageView) view;
            final Drawable drliked = LifePostActivity.this.getResources().getDrawable(R.drawable.liked);
            final boolean isliking = iv.getDrawable().getConstantState() == drliked.getConstantState();
            List<Pair<String, String>> fields = new ArrayList<>();
            fields.add(new Pair<String, String>("user_id", LifePostActivity.this.user.getId().toString()));
            fields.add(new Pair<String, String>("token", LifePostActivity.this.user.getToken()));
            fields.add(new Pair<String, String>("item_type", "comment"));
            fields.add(new Pair<String, String>("item_id", String.valueOf(id)));
            fields.add(new Pair<String, String>("like", isliking ? "0" : "1"));
            List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
            headers.add(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
            Object[] params = new Object[]{
                    String.format("%s/like", CommonUtilities.WS_HOST),
                    new HttpAsyncTask.TaskDelegate() {
                        @Override
                        public void taskCompletionResult(byte[] result) {
                            JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                            iv.setImageDrawable(isliking ? LifePostActivity.this.getResources().getDrawable(R.drawable.like) : drliked);
                            TextView tv = (TextView) likearea.findViewById(R.id.lifepost_comment_liked);
                            int likecount = Integer.parseInt(tv.getText().toString());
                            tv.setText(String.valueOf(isliking ? likecount - 1 : likecount + 1));
                        }
                    },
                    WebAPIHelper.HttpMethod.POST,
                    WebAPIHelper.buildHttpQuery(fields).getBytes(),
                    headers
            };
            new HttpAsyncTask().execute(params);
        }
    };

    private View.OnClickListener openMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Pair<View, View> menuHolder = (Pair<View, View>) view.getTag();
            menuHolder.second.setVisibility(View.VISIBLE);
            menuHolder.first.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener closeMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Pair<View, View> menuHolder = (Pair<View, View>) view.getTag();
            menuHolder.second.setVisibility(View.GONE);
            menuHolder.first.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener commentCopyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            JSONObject jobj = (JSONObject) view.getTag();
            String s = jobj.optString("content");
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", s);
            clipboard.setPrimaryClip(clip);
            String msg = LifePostActivity.this.getString(R.string.share_copylink_result, LifePostActivity.this.getString(R.string.share_result_succeed));
            Toast.makeText(LifePostActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener commentShareListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            JSONObject jobj = (JSONObject) view.getTag();
            ShareUtils su = new ShareUtils(LifePostActivity.this);
            su.setContent(jobj.optString("content"));
            BitmapDrawable bdr = (BitmapDrawable) LifePostActivity.this.contentView.getBackground();
            if (bdr != null) {
                su.setPicture(bdr.getBitmap());
            }
            su.setUrl(LifePostActivity.this.daily_topic.optString("share_link"));
            su.share();
        }
    };

    private View.OnClickListener commentImpeachListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!UserInfo.isUserLogged(LifePostActivity.this)) {
                LoginUtils.startLogin(LifePostActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                return;
            }
            JSONObject jobj = (JSONObject) view.getTag();
            String author = jobj.optString("user");
            Object[] params;
            if (author.equals(LifePostActivity.this.user.getNickname())) {
                ArrayList<Pair<String, String>> fields = new ArrayList<>();
                fields.add(new Pair<String, String>("user_id", LifePostActivity.this.user.getId().toString()));
                final int id = jobj.optInt("id");
                fields.add(new Pair<String, String>("comment_id", String.valueOf(id)));
                fields.add(new Pair<String, String>("token", LifePostActivity.this.user.getToken()));
                ArrayList<Pair<String, String>> headers = new ArrayList<>();
                headers.add(WebAPIHelper.getHttpFormUrlHeader());
                params = new Object[]{
                        String.format("%s/comment_del", CommonUtilities.WS_HOST),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                progressBar.setVisibility(View.GONE);
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                try {
                                    int code = jobj.getJSONObject("result").getInt("code");
                                    if (code != 0) {
                                        throw new Exception(String.format("delete comment returns %d", code));
                                    }
                                    for (int i = 0; i < LifePostActivity.this.comments.length(); i++) {
                                        if (id == LifePostActivity.this.comments.getJSONObject(i).optInt("id")) {
                                            LifePostActivity.this.comments.remove(i);
                                            LifePostActivity.this.adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    Toast.makeText(LifePostActivity.this, R.string.lifepost_del_comment_success, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Log.e(TAG, String.format("delete comment fail: %s", e.getMessage()));
                                    Toast.makeText(LifePostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.POST,
                        WebAPIHelper.buildHttpQuery(fields).getBytes(),
                        headers
                };
            } else {
                ArrayList<Pair<String, String>> fields = new ArrayList<>();
                fields.add(new Pair<String, String>("user_id", LifePostActivity.this.user.getId().toString()));
                fields.add(new Pair<String, String>("comment_id", String.valueOf(jobj.optInt("id"))));
                fields.add(new Pair<String, String>("token", LifePostActivity.this.user.getToken()));
                ArrayList<Pair<String, String>> headers = new ArrayList<>();
                headers.add(WebAPIHelper.getHttpFormUrlHeader());
                params = new Object[]{
                        String.format("%s/report", CommonUtilities.WS_HOST),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                progressBar.setVisibility(View.GONE);
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                try {
                                    JSONObject resultData = jobj.getJSONObject("result");
                                    if (resultData.optInt("code") != 0) {
                                        Toast.makeText(LifePostActivity.this, resultData.optString("message"), Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(LifePostActivity.this, R.string.lifepost_impeach_comment_success, Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(LifePostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.POST,
                        WebAPIHelper.buildHttpQuery(fields).getBytes(),
                        headers
                };
            }
            progressBar.setVisibility(View.VISIBLE);
            new HttpAsyncTask().execute(params);
        }
    };

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(view.getChildCount() < 1)
            return;
        View child = view.getChildAt(0);
        if (firstVisibleItem == 0) {
            int top = child.getTop();
            int height = child.getHeight() - Utils.dipToPixel(this, 59 + 25 - 10);
            title.setAlpha(1f - (float) -top * 1.2f / (float) height);
            desc.setAlpha(1f - (float) -top * 1.2f / (float) height);
            if(-top >= height){
                showTitle();
            }else{
                hideTitle();
            }
        }else{
            showTitle();
        }
    }

    private void hideTitle() {
        layout_title.setBackgroundColor(getResources().getColor(R.color.transparent));
        lifepost_back.setImageResource(R.drawable.back);
        lifepost_share.setImageResource(R.drawable.share_w);
        lifepost_title.setTextColor(getResources().getColor(R.color.white));
        lifepost_title_shadow.setVisibility(View.INVISIBLE);
        statusbar.setBackgroundColor(Color.parseColor("#33000000"));
    }

    private void showTitle() {
        layout_title.setBackgroundColor(getResources().getColor(R.color.white));
        lifepost_back.setImageResource(R.drawable.back_b);
        lifepost_share.setImageResource(R.drawable.share_b);
        lifepost_title.setTextColor(getResources().getColor(R.color.text_gray_drak));
        lifepost_title_shadow.setVisibility(View.VISIBLE);
        statusbar.setBackgroundColor(Color.parseColor("#d0d0d0"));
    }
}
