package com.wewow;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyCollectionActivity extends BaseActivity {

    private static final String TAG = "MyCollectionActivity";
    private LinearLayout labs_container;
    private JSONObject likedinfo;
    private ListView mycollist;
    private JSONArray articles = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_my_collection);
        this.getSupportActionBar().setTitle(R.string.my_collection);
        this.setupUI();
        Log.d(TAG, "onCreate: " + this.getSupportActionBar().getTitle());
        this.loadData();
    }

    private void setupUI() {
        this.labs_container = (LinearLayout) this.findViewById(R.id.collected_lablist);
        this.mycollist = (ListView) this.findViewById(R.id.mycollection_list);
        this.mycollist.setAdapter(this.listAdpater);
        this.mycollist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int id = (Integer) view.getTag();
                Intent ai = new Intent(MyCollectionActivity.this, ArticleActivity.class);
                ai.putExtra(ArticleActivity.ARTICLE_ID, id);
                MyCollectionActivity.this.startActivity(ai);
            }
        });
    }

    private BaseAdapter listAdpater = new BaseAdapter() {
        @Override
        public int getCount() {
            return MyCollectionActivity.this.articles.length();
        }

        @Override
        public Object getItem(int i) {
            return MyCollectionActivity.this.articles.opt(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(MyCollectionActivity.this, R.layout.mycollection_article_item, null);
            }
            JSONObject jobj = (JSONObject) this.getItem(i);
            final ImageView iv = (ImageView) view.findViewById(R.id.mycollection_pic);
            new RemoteImageLoader(MyCollectionActivity.this, jobj.optString("image_642_320"), new RemoteImageLoader.RemoteImageListener() {
                @Override
                public void onRemoteImageAcquired(Drawable dr) {
                    BitmapDrawable bdr = (BitmapDrawable) iv.getBackground();
                    if (bdr != null) {
                        bdr.getBitmap().recycle();
                    }
                    iv.setBackground(dr);
                }
            });
            TextView tv = (TextView) view.findViewById(R.id.mycollection_title);
            tv.setText(jobj.optString("title"));
            view.setTag(jobj.optInt("id"));
            return view;
        }
    };

    private void loadData() {
        if (!UserInfo.isUserLogged(this)) {
            Intent i = new Intent(this, LoginActivity.class);
            this.startActivity(i);
            this.finish();
            return;
        }
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(this).getId().toString()));
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/user_liked_ac", CommonUtilities.WS_HOST), fields),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        try {
                            JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                            JSONObject data = jobj.getJSONObject("result").getJSONObject("data");
                            MyCollectionActivity.this.onDataLoad(data);
                        } catch (JSONException e) {
                            Log.e(TAG, String.format("get collection list fail: %s", e.getMessage()));
                            Toast.makeText(MyCollectionActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void onDataLoad(JSONObject jobj) {
        this.labs_container.removeAllViews();
        this.likedinfo = jobj;
        try {
            this.articles = jobj.getJSONArray("articles");
            JSONArray collections = jobj.getJSONArray("collections");
            for (int i = 0; i < collections.length(); i++) {
                JSONObject collection = collections.getJSONObject(i);
                TextView col = (TextView) View.inflate(this, R.layout.liked_collection, null);
                col.setText(collection.optString("collection_title"));
                col.setTag(collection.optString("collection_id"));
                this.labs_container.addView(col);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) col.getLayoutParams();
                lp.gravity = Gravity.CENTER;
                lp.width = Utils.dipToPixel(this, 75);
                lp.height = LinearLayout.LayoutParams.MATCH_PARENT;
                lp.setMargins(Utils.dipToPixel(this, 4), 0, 0, 0);
                col.setLayoutParams(lp);
            }
            this.listAdpater.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e(TAG, String.format("get collection list fail: %s", e.getMessage()));
            Toast.makeText(MyCollectionActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
        }
    }
}
