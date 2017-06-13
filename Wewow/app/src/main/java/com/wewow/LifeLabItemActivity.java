package com.wewow;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.dto.LabCollection;
import com.wewow.utils.BlurBuilder;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.ShareUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wewow.LoginActivity.REQUEST_CODE_LOGIN;

/**
 * Created by suncjs on 2017/4/8.
 */

public class LifeLabItemActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LifeLabItemActivity";
    public static final String LIFELAB_COLLECTION = "LIFELAB_COLLECTION";
    private LabCollection lc;
    private LabCollectionDetail lcd = new LabCollectionDetail();
    private ExpandableListView lvArticles;
    private LinearLayout container;
    private BitmapDrawable picture;
    private ImageView like, lifelab_foot_collect;
    private TextView lifelab_fav_count, lifelab_foot_collect_count;
    private CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        Parcelable p = intent.getParcelableExtra(LIFELAB_COLLECTION);
        this.lc = (LabCollection) p;
//        StatusBarUtil.setTranslucent(this, 127);
        if (android.os.Build.VERSION.SDK_INT > 18) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_lifelab_item);
        this.setupUI();
    }

    private void setupUI() {
        lifelab_fav_count = (TextView) findViewById(R.id.lifelab_fav_count);
        this.container = (LinearLayout) this.findViewById(R.id.lifelab_item_container);
//        new RemoteImageLoader(this, this.lc.image, new RemoteImageLoader.RemoteImageListener() {
//            @Override
//            public void onRemoteImageAcquired(Drawable dr) {
//                LifeLabItemActivity.this.findViewById(R.id.lifelab_item_bg).setBackground(dr);
//            }
//        });
        TextView title = (TextView) this.findViewById(R.id.lifelab_item_title);
        title.setText(this.lc.title);
        ImageView ivback = (ImageView) this.findViewById(R.id.lifelab_item_back);
        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabItemActivity.this.finish();
            }
        });
        findViewById(R.id.lifelab_share).setOnClickListener(this);
        this.like = (ImageView) this.findViewById(R.id.lifelab_fav);
        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.layout_lifelab_fav).setOnClickListener(this);

        getCollectionInfo(true);
    }

    private void getCollectionInfo(final boolean isFirst) {
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("collection_id", String.valueOf(this.lc.id)));
        if (UserInfo.isUserLogged(this)) {
            fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(this).getId().toString()));
        }
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/collection_info", CommonUtilities.WS_HOST), fields),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        LabCollectionDetail x = LabCollectionDetail.parse(jobj);
                        if (x != null) {
                            LifeLabItemActivity.this.lcd = x;
                            if (isFirst) {
//                            LifeLabItemActivity.this.adapter.notifyDataSetChanged();
//                            LifeLabItemActivity.this.expandAll();
                                LifeLabItemActivity.this.display();
//                            Glide.with(LifeLabItemActivity.this).load(x.collection_image).bitmapTransform(new BlurT(this, 15)).into()
                                new RemoteImageLoader(LifeLabItemActivity.this, x.collection_image, new RemoteImageLoader.RemoteImageListener() {
                                    @Override
                                    public void onRemoteImageAcquired(Drawable dr) {
                                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                                        Bitmap blurBitMap = BlurBuilder.blur(LifeLabItemActivity.this, bitmap);
                                        bitmap.recycle();
                                        LifeLabItemActivity.this.findViewById(R.id.lifelab_item_bg).setBackground(new BitmapDrawable(getResources(), blurBitMap));
                                        LifeLabItemActivity.this.findViewById(R.id.lifelab_item_bg).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ValueAnimator va = ObjectAnimator.ofFloat(LifeLabItemActivity.this.container, "alpha", 0f, 1f);
                                                va.setDuration(300);
                                                va.start();
                                            }
                                        });
                                    }
                                });
                            } else { //update like
                                like.setImageDrawable(getResources().getDrawable(lcd.liked ? R.drawable.marked : R.drawable.mark));
                                lifelab_fav_count.setText(lcd.liked_count + "");
                                lifelab_foot_collect.setImageDrawable(getResources().getDrawable(lcd.liked ? R.drawable.marked : R.drawable.mark_white));
                                lifelab_foot_collect_count.setText(lcd.liked_count + "");
                            }
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void display() {
        this.setArticles();
        this.setupPosts();
        this.setArtists();
        this.setFoot();
    }

    private void setArticles() {
//        this.lvArticles = (ExpandableListView) this.findViewById(R.id.list_lifelab_article);
//        this.lvArticles.setAdapter(this.adapter);
//        this.lvArticles.setGroupIndicator(null);
//        this.lvArticles.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
//                return true;
//            }
//        });
        lifelab_fav_count.setVisibility(lcd.liked_count == 0 ? View.GONE : View.VISIBLE);
        lifelab_fav_count.setText(lcd.liked_count + "");
        //int gc = this.lcd.getArticleGroupCount() > 2 ? 2 : this.lcd.getArticleGroupCount();
        int gc = this.lcd.getArticleGroupCount();
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(Utils.dipToPixel(this, 8), Utils.dipToPixel(this, 6), Utils.dipToPixel(this, 8), 0);
        for (int i = 0; i < gc; i++) {
            String g = this.lcd.getArticleGroup(i);
            this.addArticleView(g, groupParams);
        }
        this.like.setImageDrawable(this.getResources().getDrawable(this.lcd.liked ? R.drawable.marked : R.drawable.mark));
    }

    private void addArticleView(String group, LinearLayout.LayoutParams groupParams) {
        View groupView = View.inflate(this, R.layout.lifelab_item_article_group, null);
        TextView tv = (TextView) groupView.findViewById(R.id.lifelab_item_group_title);
        tv.setText(group);
        View cardview = View.inflate(this, R.layout.cardview_lifelab_item, null);
        LinearLayout item_container = (LinearLayout) cardview.findViewById(R.id.item_container);
        item_container.addView(groupView);
        //int cc = this.lcd.getArticleCount(group) > 2 ? 2 : this.lcd.getArticleCount(group);
        int cc = this.lcd.getArticleCount(group);
        for (int i = 0; i < cc; i++) {
            LabCollectionDetail.Article a = this.lcd.getArticle(group, i);
            View itemView = View.inflate(this, R.layout.lifelab_item_article, null);
//            itemView.setBackgroundColor(i % 2 == 0 ? Color.rgb(252, 230, 194) : Color.WHITE);
            itemView.setTag(a.id);
            tv = (TextView) itemView.findViewById(R.id.lifelab_item_article_category);
            tv.setText(a.wewow_category);
            tv = (TextView) itemView.findViewById(R.id.lifelab_item_article_title);
            tv.setText(a.title);
            final ImageView iv = (ImageView) itemView.findViewById(R.id.lifelab_item_article_img);
            Glide.with(this)
                    .load(a.image_320_160)
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade()
                    .into(iv);
            itemView.setOnClickListener(this.articleClickListener);
            item_container.addView(itemView);
        }
        container.addView(cardview, groupParams);
    }

    private View.OnClickListener articleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = (Integer) view.getTag();
            Intent intent = new Intent(LifeLabItemActivity.this, ArticleActivity.class);
            intent.putExtra(ArticleActivity.ARTICLE_ID, id);
            LifeLabItemActivity.this.startActivity(intent);
        }
    };

    private void setupPosts() {
        if (this.lcd.getPostCount() == 0) {
            return;
        }
        LabCollectionDetail.Post p = this.lcd.getPost(0);
        View view = View.inflate(this, R.layout.lifelab_item_discuz, null);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(Utils.dipToPixel(this, 8), Utils.dipToPixel(this, 6), Utils.dipToPixel(this, 8), 0);
        TextView tv = (TextView) view.findViewById(R.id.lifelab_item_discuz_title);
        tv.setText(this.lcd.daily_topic_section);
        tv = (TextView) view.findViewById(R.id.tv_lifelab_item_discuz_topic);
        tv.setText(p.title);
        tv = (TextView) view.findViewById(R.id.tv_lifelab_item_discuz_count);
        tv.setText(p.comment_count + getString(R.string.discuss_people_number));
        final ImageView iv = (ImageView) view.findViewById(R.id.iv_lifelab_item_discuz);
        Glide.with(this)
                .load(p.image_160_160)
                .placeholder(R.drawable.banner_loading_spinner)
                .crossFade()
                .into(iv);
        view.setTag(p.id);
        view.setOnClickListener(this.postClickListener);
        container.addView(view, groupParams);
    }

    private View.OnClickListener postClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = (Integer) view.getTag();
            Intent intent = new Intent(LifeLabItemActivity.this, LifePostActivity.class);
            intent.putExtra(LifePostActivity.POST_ID, id);
            LifeLabItemActivity.this.startActivity(intent);
        }
    };

    private void setArtists() {
        if (this.lcd.getArtistCount() == 0) {
            return;
        }
        View view = View.inflate(this, R.layout.lifelab_item_artists, null);
        LinearLayout r = (LinearLayout) view.findViewById(R.id.lifelab_item_artist_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, Utils.dipToPixel(this, 8), 0, Utils.dipToPixel(this, 5));
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.setMargins(Utils.dipToPixel(this, 4), Utils.dipToPixel(this, 2), Utils.dipToPixel(this, 4), Utils.dipToPixel(this, 5));
        for (int i = 0; i < this.lcd.getArtistCount(); i++) {
            View itemView = View.inflate(this, R.layout.lifelab_item_artist, null);
            final LabCollectionDetail.Artist a = this.lcd.getArtist(i);
            TextView tv = (TextView) itemView.findViewById(R.id.lifelab_item_artist_name);
            tv.setText(a.nickname);
            tv = (TextView) itemView.findViewById(R.id.lifelab_item_artist_desc);
            tv.setText(a.desc);
            tv = (TextView) itemView.findViewById(R.id.lifelab_item_artist_articlecount);
            tv.setText(a.article_count + "");
            tv = (TextView) itemView.findViewById(R.id.lifelab_item_artist_follower);
            tv.setText(a.follow_count + "");
            final ImageView iv = (ImageView) itemView.findViewById(R.id.lifelab_item_artist_logo);
            Glide.with(this)
                    .load(a.image)
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade()
                    .into(iv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LifeLabItemActivity.this, DetailArtistActivity.class);
                    intent.putExtra("id", a.id + "");
                    startActivity(intent);
                }
            });
            r.addView(itemView, params1);
        }
        this.container.addView(view, params);
    }

    private void setFoot() {
        View view = View.inflate(this, R.layout.lifelab_item_foot, null);
        TextView tv = (TextView) view.findViewById(R.id.lifelab_item_desc);
        tv.setText(String.format(this.getString(R.string.lifelab_item_desc), this.lcd.editor));
        lifelab_foot_collect_count = (TextView) view.findViewById(R.id.lifelab_foot_collect_count);
        lifelab_foot_collect_count.setVisibility(lcd.liked_count == 0 ? View.GONE : View.VISIBLE);
        lifelab_foot_collect_count.setText(lcd.liked_count + "");
        lifelab_foot_collect = (ImageView) view.findViewById(R.id.lifelab_foot_collect);
        lifelab_foot_collect.setImageResource(lcd.liked ? R.drawable.marked : R.drawable.mark_white);
        this.container.addView(view);

        findViewById(R.id.layout_footer_feedback).setOnClickListener(this);
        findViewById(R.id.layout_footer_share).setOnClickListener(this);
        findViewById(R.id.layout_lifelab_foot_collect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_footer_feedback:
                if (!UserInfo.isUserLogged(LifeLabItemActivity.this)) {
                    LoginUtils.startLogin(LifeLabItemActivity.this, REQUEST_CODE_LOGIN);
                } else {
                    startActivity(new Intent(LifeLabItemActivity.this, FeedbackActivity.class));
                }
                break;
            case R.id.lifelab_share:
            case R.id.layout_footer_share:
                if (LifeLabItemActivity.this.lcd == null) {
                    return;
                }
                ShareUtils su = new ShareUtils(LifeLabItemActivity.this);
                su.setContent(LifeLabItemActivity.this.lcd.share_title);
                su.setUrl(LifeLabItemActivity.this.lcd.share_link);
                if (LifeLabItemActivity.this.picture != null) {
                    su.setPicture(LifeLabItemActivity.this.picture.getBitmap());
                }
                su.share();
                break;
            case R.id.layout_lifelab_fav:
            case R.id.layout_lifelab_foot_collect:
                if (!UserInfo.isUserLogged(LifeLabItemActivity.this)) {
                    LoginUtils.startLogin(LifeLabItemActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                    return;
                }
                Drawable.ConstantState notliked = LifeLabItemActivity.this.getResources().getDrawable(R.drawable.mark).getConstantState();
                Drawable.ConstantState currentlike = LifeLabItemActivity.this.like.getDrawable().getConstantState();
                final Integer like = notliked.equals(currentlike) ? 1 : 0;
                ArrayList<Pair<String, String>> fields = new ArrayList<>();
                UserInfo ui = UserInfo.getCurrentUser(LifeLabItemActivity.this);
                fields.add(new Pair<>("user_id", ui.getId().toString()));
                fields.add(new Pair<>("token", ui.getToken()));
                fields.add(new Pair<>("item_type", "collection"));
                fields.add(new Pair<>("item_id", String.valueOf(LifeLabItemActivity.this.lc.id)));
                fields.add(new Pair<>("like", like.toString()));
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
                                        if(i==403){
                                            LoginUtils.startLogin(LifeLabItemActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                                        }
                                        else {
                                            throw new Exception(String.valueOf(i));
                                        }
                                    }
                                    String s;
                                    LifeLabItemActivity.this.like.setImageDrawable(LifeLabItemActivity.this.getResources().getDrawable(like == 1 ? R.drawable.marked : R.drawable.mark));
                                    lifelab_foot_collect.setImageDrawable(LifeLabItemActivity.this.getResources().getDrawable(like == 1 ? R.drawable.marked : R.drawable.mark_white));
                                    if (like == 1) {
                                        lcd.liked_count += 1;
                                        s = LifeLabItemActivity.this.getString(R.string.fav_succeed);
                                        MessageBoxUtils.messageBoxWithNoButton(LifeLabItemActivity.this, true, s, 2500);
                                    } else {
                                        lcd.liked_count -= 1;
                                    }
                                    lifelab_fav_count.setText(lcd.liked_count + "");
                                    lifelab_foot_collect_count.setText(lcd.liked_count + "");
                                } catch (Exception e) {
                                    Log.e(TAG, String.format("favourite fail: %s", e.getMessage()));
                                    Toast.makeText(LifeLabItemActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    String s = LifeLabItemActivity.this.getString(like == 1 ? R.string.fav_fail : R.string.unfav_fail);
                                    MessageBoxUtils.messageBoxWithNoButton(LifeLabItemActivity.this, false, s, 2500);
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.POST,
                        WebAPIHelper.buildHttpQuery(fields).getBytes(),
                        headers
                };
                new HttpAsyncTask().execute(params);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED && requestCode == REQUEST_CODE_LOGIN) {
            getCollectionInfo(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
        private void expandAll() {
            for (int i = 0; i < this.adapter.getGroupCount(); i++) {
                this.lvArticles.expandGroup(i);
            }
        }

        private BaseExpandableListAdapter adapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                int l = LifeLabItemActivity.this.lcd.getArticleGroupCount();
                return l > 2 ? 2 : l;
            }

            @Override
            public int getChildrenCount(int i) {
                String group = LifeLabItemActivity.this.lcd.getArticleGroup(i);
                //Log.d(TAG, String.format("getChildrenCount: %s %d", group, LifeLabItemActivity.this.lcd.getArticleCount(group)));
                int l = LifeLabItemActivity.this.lcd.getArticleCount(group);
                return l > 2 ? 2 : l;
            }

            @Override
            public Object getGroup(int i) {
                return LifeLabItemActivity.this.lcd.getArticleGroup(i);
            }

            @Override
            public Object getChild(int i, int i1) {
                String group = LifeLabItemActivity.this.lcd.getArticleGroup(i);
                return LifeLabItemActivity.this.lcd.getArticle(group, i1);
            }

            @Override
            public long getGroupId(int i) {
                return i;
            }

            @Override
            public long getChildId(int i, int i1) {
                return i * 1000 + i1;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = View.inflate(LifeLabItemActivity.this, R.layout.lifelab_item_article_group, null);
                }
                TextView tv = (TextView) view.findViewById(R.id.lifelab_item_group_title);
                tv.setText(LifeLabItemActivity.this.lcd.getArticleGroup(i));
                return view;
            }

            @Override
            public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = View.inflate(LifeLabItemActivity.this, R.layout.lifelab_item_article, null);
                }
                String group = LifeLabItemActivity.this.lcd.getArticleGroup(i);
                LabCollectionDetail.Article a = LifeLabItemActivity.this.lcd.getArticle(group, i1);
                view.setBackgroundColor(i1 % 2 == 0 ? Color.rgb(252, 230, 194) : Color.WHITE);
                final ImageView iv = (ImageView) view.findViewById(R.id.lifelab_item_article_img);
                new RemoteImageLoader(LifeLabItemActivity.this, a.image_320_160, new RemoteImageLoader.RemoteImageListener() {
                    @Override
                    public void onRemoteImageAcquired(Drawable dr) {
                        BitmapDrawable old = (BitmapDrawable) iv.getDrawable();
                        iv.setImageDrawable(dr);
                        if (old != null) {
                            old.getBitmap().recycle();
                        }
                    }
                });
                TextView tv = (TextView) view.findViewById(R.id.lifelab_item_article_title);
                tv.setText(a.title);
                tv = (TextView) view.findViewById(R.id.lifelab_item_article_category);
                tv.setText(a.wewow_category);
                return view;
            }

            @Override
            public boolean isChildSelectable(int i, int i1) {
                return false;
            }

        };
    */
    private static class LabCollectionDetail {

        private LabCollectionDetail() {

        }

        public static class Article {
            public int id;
            public String section;
            public String section_id;
            public String title;
            public String wewow_category;
            public String image_320_160;
        }

        public static class Artist {
            public String desc;
            public int id;
            public String image;
            public String nickname;
            public int follow_count;
            public int article_count;
        }

        public static class Post {
            public int id;
            public String title;
            public String image_160_160;
            public int comment_count;
        }

        private List<Artist> artists = new ArrayList<Artist>();
        private Map<String, List<Article>> articles = new HashMap<String, List<Article>>();
        private List<String> articleKeys = new ArrayList<String>();
        private List<Post> posts = new ArrayList<Post>();

        public String collection_desc;
        public String collection_image;
        public String collection_title;
        public String daily_topic_section;
        public String editor;
        public String share_link;
        public String share_title;
        public int liked_count;
        public boolean liked;

        public static LabCollectionDetail parse(JSONObject jobj) {
            try {
                JSONObject data = jobj.getJSONObject("result").getJSONObject("data").getJSONObject("collection_data");
                LabCollectionDetail lcd = new LabCollectionDetail();
                lcd.collection_desc = data.getString("collection_desc");
                lcd.collection_image = data.getString("collection_image");
                lcd.collection_title = data.getString("collection_title");
                lcd.daily_topic_section = data.getString("daily_topic_section");
                lcd.editor = data.getString("editor");
                lcd.share_link = data.getString("share_link");
                lcd.liked = data.optString("liked").equals("0") ? false : true;
                JSONArray jarticles = data.getJSONArray("article_list");
                for (int i = 0; i < jarticles.length(); i++) {
                    Article article = parseArticle(jarticles.getJSONObject(i));
                    String k = article.section;
                    if (lcd.articles.containsKey(k)) {
                        lcd.articles.get(k).add(article);
                    } else {
                        ArrayList<Article> l = new ArrayList<Article>();
                        l.add(article);
                        lcd.articles.put(k, l);
                        lcd.articleKeys.add(k);
                    }
                }
                JSONArray jartists = data.getJSONArray("artist_list");
                for (int i = 0; i < jartists.length(); i++) {
                    Artist artist = parseArtist(jartists.getJSONObject(i));
                    lcd.artists.add(artist);
                }
                JSONArray jposts = data.getJSONArray("daily_topic_list");
                for (int i = 0; i < jposts.length(); i++) {
                    Post post = parsePost(jposts.getJSONObject(i));
                    lcd.posts.add(post);
                }
                lcd.share_title = data.getString("share_title");
                lcd.liked_count = data.optInt("liked_count");
                return lcd;
            } catch (JSONException e) {
                Log.e(TAG, "parse fail");
                return null;
            }
        }

        public static Article parseArticle(JSONObject jobj) throws JSONException {
            Article a = new Article();
            a.id = jobj.getInt("id");
            a.image_320_160 = jobj.getString("image_320_160");
            a.section = jobj.getString("section");
            a.section_id = jobj.getString("section_id");
            a.title = jobj.getString("title");
            a.wewow_category = jobj.getString("wewow_category");
            return a;
        }

        public static Artist parseArtist(JSONObject jobj) throws JSONException {
            Artist a = new Artist();
            a.id = jobj.getInt("id");
            a.desc = jobj.getString("desc");
            a.image = jobj.getString("image");
            a.nickname = jobj.getString("nickname");
            a.follow_count = jobj.optInt("follow_count");
            a.article_count = jobj.optInt("article_count");
            return a;
        }

        public static Post parsePost(JSONObject jobj) throws JSONException {
            Post p = new Post();
            p.id = jobj.getInt("id");
            p.image_160_160 = jobj.getString("image_160_160");
            p.title = jobj.getString("title");
            p.comment_count = jobj.optInt("comment_count");
            return p;
        }

        public int getArticleGroupCount() {
            return this.articles.size();
        }

        public int getArticleCount(String group) {
            return this.articles.containsKey(group) ?
                    this.articles.get(group).size() : 0;
        }

        public String getArticleGroup(int i) {
            return i < 0 || i >= this.articles.size() ? null :
                    this.articleKeys.get(i);
        }

        public Article getArticle(String group, int i) {
            List<Article> l = this.articles.get(group);
            return i < 0 || i >= l.size() ? null : l.get(i);
        }

        public int getArtistCount() {
            return this.artists.size();
        }

        public Artist getArtist(int i) {
            return i < 0 || i >= this.artists.size() ? null :
                    this.artists.get(i);
        }

        public int getPostCount() {
            return this.posts.size();
        }

        public Post getPost(int i) {
            return i < 0 || i >= this.posts.size() ? null :
                    this.posts.get(i);
        }
    }
}
