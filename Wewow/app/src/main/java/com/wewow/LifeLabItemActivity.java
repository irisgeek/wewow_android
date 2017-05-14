package com.wewow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.dto.LabCollection;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.PhotoUtils;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.ShareUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suncjs on 2017/4/8.
 */

public class LifeLabItemActivity extends Activity {

    private static final String TAG = "LifeLabItemActivity";
    public static final String LIFELAB_COLLECTION = "LIFELAB_COLLECTION";
    private LabCollection lc;
    private LabCollectionDetail lcd = new LabCollectionDetail();
    private ExpandableListView lvArticles;
    private LinearLayout container;
    private BitmapDrawable picture;
    private ImageView like;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        Parcelable p = intent.getParcelableExtra(LIFELAB_COLLECTION);
        this.lc = (LabCollection) p;
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_lifelab_item);
        this.setupUI();
    }

    private void setupUI() {
        this.container = (LinearLayout) this.findViewById(R.id.lifelab_item_container);
//        new RemoteImageLoader(this, this.lc.image, new RemoteImageLoader.RemoteImageListener() {
//            @Override
//            public void onRemoteImageAcquired(Drawable dr) {
//                LifeLabItemActivity.this.findViewById(R.id.lifelab_item_root).setBackground(dr);
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
        this.findViewById(R.id.lifelab_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
        this.like = (ImageView) this.findViewById(R.id.lifelab_fav);
        this.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserInfo.isUserLogged(LifeLabItemActivity.this)) {
                    Intent logini = new Intent(LifeLabItemActivity.this, LoginActivity.class);
                    LifeLabItemActivity.this.startActivity(logini);
                    return;
                }
                Drawable.ConstantState notliked = LifeLabItemActivity.this.getResources().getDrawable(R.drawable.favourite_b).getConstantState();
                Drawable.ConstantState currentlike = LifeLabItemActivity.this.like.getDrawable().getConstantState();
                final Integer like = notliked.equals(currentlike) ? 1 : 0;
                ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
                UserInfo ui = UserInfo.getCurrentUser(LifeLabItemActivity.this);
                fields.add(new Pair<String, String>("user_id", ui.getId().toString()));
                fields.add(new Pair<String, String>("token", ui.getToken()));
                fields.add(new Pair<String, String>("item_type", "collection"));
                fields.add(new Pair<String, String>("item_id", String.valueOf(LifeLabItemActivity.this.lc.id)));
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
                                    LifeLabItemActivity.this.like.setImageDrawable(LifeLabItemActivity.this.getResources().getDrawable(like == 1 ? R.drawable.favourite : R.drawable.favourite_b));
                                } catch (Exception e) {
                                    Log.e(TAG, String.format("favourite fail: %s", e.getMessage()));
                                    Toast.makeText(LifeLabItemActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
        ProgressDialogUtil.getInstance(this).showProgressDialog();
        ArrayList<Pair<String,String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("collection_id", String.valueOf(this.lc.id)));
        if (UserInfo.isUserLogged(this)) {
            fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(this).getId().toString()));
        }
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/collection_info", CommonUtilities.WS_HOST), fields),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        ProgressDialogUtil.getInstance(LifeLabItemActivity.this).finishProgressDialog();
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        LabCollectionDetail x = LabCollectionDetail.parse(jobj);
                        if (x != null) {
                            LifeLabItemActivity.this.lcd = x;
//                            LifeLabItemActivity.this.adapter.notifyDataSetChanged();
//                            LifeLabItemActivity.this.expandAll();
                            LifeLabItemActivity.this.display();
                            new RemoteImageLoader(LifeLabItemActivity.this, x.collection_image, new RemoteImageLoader.RemoteImageListener() {
                                @Override
                                public void onRemoteImageAcquired(Drawable dr) {
                                    LifeLabItemActivity.this.findViewById(R.id.lifelab_item_root).setBackground(dr);
                                }
                            });
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
        //this.setArticles();
        //this.setupPosts();
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
        int gc = this.lcd.getArticleGroupCount() > 2 ? 2 : this.lcd.getArticleGroupCount();
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(Utils.dipToPixel(this, 8), Utils.dipToPixel(this, 6), Utils.dipToPixel(this, 8), 0);
        for (int i = 0; i < gc; i++) {
            String g = this.lcd.getArticleGroup(i);
            this.addArticleView(g, groupParams);
        }
        this.like.setImageDrawable(this.getResources().getDrawable(this.lcd.liked ? R.drawable.favourite : R.drawable.favourite_b));
    }

    private void addArticleView(String group, LinearLayout.LayoutParams groupParams) {
        View groupView = View.inflate(this, R.layout.lifelab_item_article_group, null);
        TextView tv = (TextView) groupView.findViewById(R.id.lifelab_item_group_title);
        tv.setText(group);
        View cardview = View.inflate(this, R.layout.cardview_lifelab_item, null);
        LinearLayout item_container = (LinearLayout) cardview.findViewById(R.id.item_container);
        item_container.addView(groupView);
        int cc = this.lcd.getArticleCount(group) > 2 ? 2 : this.lcd.getArticleCount(group);
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
            new RemoteImageLoader(this, a.image_320_160, new RemoteImageLoader.RemoteImageListener() {
                @Override
                public void onRemoteImageAcquired(Drawable dr) {
                    BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
                    iv.setImageDrawable(dr);
                    if (bd != null) {
                        bd.getBitmap().recycle();
                    }
                }
            });
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
        tv.setText(this.lcd.liked_count + getString(R.string.discuss_people_number));
        final ImageView iv = (ImageView) view.findViewById(R.id.iv_lifelab_item_discuz);
        new RemoteImageLoader(this, p.image_664_250, new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
                iv.setImageDrawable(dr);
                if (bd != null) {
                    bd.getBitmap().recycle();
                }
            }
        });
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
            new RemoteImageLoader(this, a.image, new RemoteImageLoader.RemoteImageListener() {
                @Override
                public void onRemoteImageAcquired(Drawable dr) {
                    BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
                    Bitmap bm = PhotoUtils.drawableToBitmap(dr);
                    RoundedBitmapDrawable rdr = PhotoUtils.createRoundedDrawable(LifeLabItemActivity.this, bm, bm.getWidth());
                    iv.setImageDrawable(rdr);
                    if (bd != null) {
                        bd.getBitmap().recycle();
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LifeLabItemActivity.this,DetailArtistActivity.class);
                    intent.putExtra("id",a.id);
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
        this.container.addView(view);
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
            public String image_664_250;
        }

        private List<Artist> artists = new ArrayList<Artist>();
        private Map<String, List<Article>> articles = new HashMap<String, List<Article>>();
        private List<Post> posts = new ArrayList<Post>();

        public String collection_desc;
        public String collection_image;
        public String collection_title;
        public String daily_topic_section;
        public String editor;
        public String share_link;
        public String share_title;
        public String liked_count;
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
                lcd.liked_count = data.getString("liked_count");
                lcd.addTestArtist();
                return lcd;
            } catch (JSONException e) {
                Log.e(TAG, "parse fail");
                return null;
            }
        }

        private void addTestArtist() {
            if (this.artists.size() == 0) {
                for (int i = 0; i < 3; i++) {
                    Artist artist = new Artist();
                    artist.nickname = String.format("生活家%d", i);
                    artist.desc = String.format("生活家描述%d", i);
                    artist.image = this.collection_image;
                    this.artists.add(artist);
                }
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
            p.image_664_250 = jobj.getString("image_664_250");
            p.title = jobj.getString("title");
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
                    this.articles.keySet().toArray()[i].toString();
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
