package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wewow.dto.LabCollection;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        new RemoteImageLoader(this, this.lc.image, new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                LifeLabItemActivity.this.findViewById(R.id.lifelab_item_root).setBackground(dr);
            }
        });
        TextView title = (TextView) this.findViewById(R.id.lifelab_item_title);
        title.setText(this.lc.title);
        ImageView ivback = (ImageView) this.findViewById(R.id.lifelab_item_back);
        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabItemActivity.this.finish();
            }
        });
        Object[] params = new Object[]{
                String.format("%s/collection_info?collection_id=%s", CommonUtilities.WS_HOST, this.lc.id),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        LabCollectionDetail x = LabCollectionDetail.parse(jobj);
                        if (x != null) {
                            LifeLabItemActivity.this.lcd = x;
                            LifeLabItemActivity.this.adapter.notifyDataSetChanged();
                            LifeLabItemActivity.this.expandAll();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
        this.setArticles();
    }

    private void setArticles() {
        this.lvArticles = (ExpandableListView) this.findViewById(R.id.list_lifelab_article);
        this.lvArticles.setAdapter(this.adapter);
        this.lvArticles.setGroupIndicator(null);
        this.lvArticles.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });
    }

    private void expandAll() {
        for (int i = 0; i < this.adapter.getGroupCount(); i++) {
            this.lvArticles.expandGroup(i);
        }
    }

    private BaseExpandableListAdapter adapter = new BaseExpandableListAdapter() {
        @Override
        public int getGroupCount() {
            return LifeLabItemActivity.this.lcd.getArticleGroupCount();
        }

        @Override
        public int getChildrenCount(int i) {
            String group = LifeLabItemActivity.this.lcd.getArticleGroup(i);
            Log.d(TAG, String.format("getChildrenCount: %s %d", group, LifeLabItemActivity.this.lcd.getArticleCount(group)));
            return LifeLabItemActivity.this.lcd.getArticleCount(group);
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
