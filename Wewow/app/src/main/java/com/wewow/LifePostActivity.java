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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.utils.BlurBuilder;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.ShareUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LifePostActivity extends AppCompatActivity {

    private static final String TAG = "LifePostActivity";
    public static final String POST_ID = "POST_ID";

    private TextView title;
    private TextView desc;
    private View contentView;
    private ImageView addpost;
    private JSONArray comments = new JSONArray();
    private UserInfo user;
    private int postId;
    private JSONObject daily_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_post);
        Utils.setActivityToBeFullscreen(this);
        this.setupUI();
        Intent i = this.getIntent();
        postId = i.getIntExtra(POST_ID, -1);
        this.user = UserInfo.isUserLogged(this) ? UserInfo.getCurrentUser(this) : UserInfo.getAnonymouUser();
        Object[] params = new Object[]{
                String.format("%s/daily_topic?user_id=%d&daily_topic_id=%d", CommonUtilities.WS_HOST, LifePostActivity.this.user.getId(), postId),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        ProgressDialogUtil.getInstance(LifePostActivity.this).finishProgressDialog();
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj != null) {
                            LifePostActivity.this.onDataLoad(jobj.optJSONObject("result").optJSONObject("data"));
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        ProgressDialogUtil.getInstance(this).showProgressDialog();
        new HttpAsyncTask().execute(params);
    }

    private void setupUI() {
        this.findViewById(R.id.lifepost_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifePostActivity.this.finish();
            }
        });
        this.title = (TextView) this.findViewById(R.id.lifepost_title);
        this.desc = (TextView) this.findViewById(R.id.lifepost_desc);
        this.contentView = this.findViewById(R.id.lifepost_root);
        this.addpost = (ImageView) this.findViewById(R.id.lifepost_newpost);
        this.addpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LifePostActivity.this, AddPostActivity.class);
                BitmapDrawable bdr = (BitmapDrawable) LifePostActivity.this.contentView.getBackground();
                if (bdr != null) {
                    i.putExtra(AddPostActivity.BACK_GROUND, Utils.getBitmapBytes(bdr.getBitmap()));
                }
                i.putExtra(AddPostActivity.TOPIC_ID, LifePostActivity.this.postId);
                LifePostActivity.this.startActivity(i);
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

    private void onDataLoad(JSONObject jobj) {
        if (jobj == null) {
            return;
        }
        try {
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
            this.comments = jobj.getJSONArray("comments");
            if (this.comments.length() > 0) {
                this.findViewById(R.id.lifepost_nodata_area).setVisibility(View.GONE);
                ListView lv = (ListView) this.findViewById(R.id.lifepost_comments);
                lv.setVisibility(View.VISIBLE);
                lv.setAdapter(this.adapter);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onDataLoad: fail");
        }
    }

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return LifePostActivity.this.comments.length();
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
                v.setBackgroundColor(Color.WHITE);
            } else {
                view.findViewById(R.id.lifepost_mycomment).setVisibility(View.VISIBLE);
                v.setBackgroundColor(Color.argb(255, 252, 211, 145));
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
            return view;
        }
    };

    private View.OnClickListener likeClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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
}
