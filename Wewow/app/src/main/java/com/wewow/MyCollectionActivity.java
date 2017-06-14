package com.wewow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jaeger.library.StatusBarUtil;
import com.wewow.utils.BlurBuilder;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyCollectionActivity extends BaseActivity {

    private static final String TAG = "MyCollectionActivity";
    private LinearLayout labs_container;
    private TableLayout labman_container;
    private JSONObject likedinfo;
    private ListView mycollist;
    private TextView articleCategory;
    private View article_category_line, collection_man_area;
    private ImageView iv_man_area;
    private JSONArray articles = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuselectedPosition(4);
        setContentView(R.layout.activity_my_collection);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        this.getSupportActionBar().setTitle(R.string.my_collection);
        this.setupUI();
        this.loadData();
    }

    private void setupUI() {
        this.labs_container = (LinearLayout) this.findViewById(R.id.collected_lablist);
        collection_man_area = findViewById(R.id.collection_man_area);
        iv_man_area = (ImageView) findViewById(R.id.iv_collection_man_area);
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
        this.articleCategory = (TextView) this.findViewById(R.id.article_category);
        this.articleCategory.setOnClickListener(this.categoryClickListener);
        article_category_line = findViewById(R.id.article_category_line);
        ImageView iv = (ImageView) this.findViewById(R.id.collection_expand);
        iv.setImageDrawable(this.getResources().getDrawable(R.drawable.expanded));
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyCollectionActivity.this.likedinfo == null) {
                    return;
                }
                ImageView iv = (ImageView) view;
                Drawable expdr = MyCollectionActivity.this.getResources().getDrawable(R.drawable.expanded);
                Drawable coldr = MyCollectionActivity.this.getResources().getDrawable(R.drawable.expanded_up);
                if (iv.getDrawable().getConstantState().equals(expdr.getConstantState())) {
                    iv.setImageDrawable(coldr);
                    Bitmap bm = BlurBuilder.blur(MyCollectionActivity.this, takeScreenShot(), 0.4f, 10f);
                    iv_man_area.setImageBitmap(bm);
                    iv_man_area.setVisibility(View.VISIBLE);
                    collection_man_area.setVisibility(View.VISIBLE);
                    Log.d(TAG, "expand: ");
                } else {
                    iv.setImageDrawable(expdr);
                    iv_man_area.setVisibility(View.GONE);
                    collection_man_area.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "collapse: ");
                }

            }
        });
        this.labman_container = (TableLayout) this.findViewById(R.id.coldellist);
        collection_man_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });
        ImageView unfav = (ImageView) this.findViewById(R.id.collection_unfav);
        final Drawable dumpbin = MyCollectionActivity.this.getResources().getDrawable(R.drawable.delcollection);
        unfav.setBackground(dumpbin);
        this.findViewById(R.id.collection_unfav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView iv = (ImageView) view;
                if (iv.getBackground().getConstantState().equals(dumpbin.getConstantState())) {
                    iv.setBackground(MyCollectionActivity.this.getResources().getDrawable(R.drawable.delcollectiondone));
                    MyCollectionActivity.this.onCollectedLabDataLoad(MyCollectionActivity.this.likedinfo.optJSONArray("collections"), true);
                } else {
                    iv.setBackground(dumpbin);
                    MyCollectionActivity.this.onCollectedLabDataLoad(MyCollectionActivity.this.likedinfo.optJSONArray("collections"), false);
                }
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
            Glide.with(MyCollectionActivity.this)
                    .load(jobj.optString("image_642_320"))
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade()
                    .into(iv);
            TextView tv = (TextView) view.findViewById(R.id.mycollection_title);
            tv.setText(jobj.optString("title"));
            view.setTag(jobj.optInt("id"));
            return view;
        }
    };

    public View.OnClickListener categoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateClickStatus(view);
            String id = view.getTag().toString();
            if (id.equals("0")) {
                MyCollectionActivity.this.articleCategory.setTextColor(Color.parseColor("#333631"));
                article_category_line.setVisibility(View.VISIBLE);
                try {
                    MyCollectionActivity.this.onListDataLoaded(MyCollectionActivity.this.likedinfo.getJSONArray("articles"));
                } catch (JSONException e) {
                    //
                }
            } else {
                MyCollectionActivity.this.articleCategory.setTextColor(Color.parseColor("#7f333631"));
                article_category_line.setVisibility(View.INVISIBLE);
                ArrayList<Pair<String, String>> fields = new ArrayList<>();
                fields.add(new Pair<String, String>("collection_id", id));
                if (UserInfo.isUserLogged(MyCollectionActivity.this)) {
                    fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(MyCollectionActivity.this).getId().toString()));
                }
                Object[] params = new Object[]{
                        WebAPIHelper.addUrlParams(String.format("%s/user_liked_collection_articles", CommonUtilities.WS_HOST), fields),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                try {
                                    MyCollectionActivity.this.onListDataLoaded(jobj.getJSONObject("result").getJSONArray("data"));
                                } catch (JSONException e) {
                                    Log.e(TAG, String.format("user_liked_collection_articles: %s", e.getMessage()));
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.GET
                };
                new HttpAsyncTask().execute(params);
            }
            this.toggleCategories(view);
        }

        private void toggleCategories(View view) {
            int selected = Color.parseColor("#333631");
            int unselected = Color.parseColor("#7f333631");
            for (int i = 0; i < MyCollectionActivity.this.labs_container.getChildCount(); i++) {
                View subView = MyCollectionActivity.this.labs_container.getChildAt(i);
                if (!(subView instanceof TextView)) {
                    continue;
                }
                ((TextView) subView).setTextColor(subView == view ? selected : unselected);
            }
        }
    };

    private void updateClickStatus(View view) {
        for (int i = 0; i < labs_container.getChildCount(); i++) {
            LinearLayout child = (LinearLayout) labs_container.getChildAt(i);
            if (child == view) {
                ((TextView) child.getChildAt(0)).setTextColor(Color.parseColor("#333631"));
                child.getChildAt(1).setVisibility(View.VISIBLE);
            } else {
                ((TextView) child.getChildAt(0)).setTextColor(Color.parseColor("#7f333631"));
                child.getChildAt(1).setVisibility(View.INVISIBLE);
            }
        }
    }

    private void loadData() {
        if (!UserInfo.isUserLogged(this)) {
            LoginUtils.startLogin(this, LoginActivity.REQUEST_CODE_LOGIN);
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
        this.likedinfo = jobj;
        try {
            this.onCollectedLabDataLoad(jobj.getJSONArray("collections"));
            this.onListDataLoaded(jobj.getJSONArray("articles"));
        } catch (JSONException e) {
            Log.e(TAG, String.format("get collection list fail: %s", e.getMessage()));
            Toast.makeText(MyCollectionActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
        }
    }

    private void onCollectedLabDataLoad(JSONArray collections) {
        this.onCollectedLabDataLoad(collections, false);
    }

    private void onCollectedLabDataLoad(JSONArray collections, boolean showDelete) {
        this.labs_container.removeAllViews();
        this.labman_container.removeAllViews();
        int delrowcount = 3;
        TableRow tr = null;
        for (int i = 0; i < collections.length(); i++) {
            JSONObject collection;
            try {
                collection = collections.getJSONObject(i);
            } catch (JSONException e) {
                continue;
            }
            View child = View.inflate(this, R.layout.liked_collection, null);
            TextView col = (TextView) child.findViewById(R.id.tv_collection_tab);
            col.setText(collection.optString("collection_title"));
            child.setTag(collection.optString("collection_id"));
            child.setOnClickListener(this.categoryClickListener);
            this.labs_container.addView(child);

            if (i % delrowcount == 0) {
                tr = this.addTableRow();
            }
            this.addDelItem(tr, collection, showDelete);
            if (i == collections.length() - 1) {
                //Log.d(TAG, String.format("last index %d", i));
                for (int j = i; (j + 1) % delrowcount != 0; j++) {
                    //Log.d(TAG, String.format("supplemnet %d", j));
                    this.addDelItem(tr, null, showDelete);
                }
            }
        }
    }

    private TableRow addTableRow() {
        TableRow tr = new TableRow(this);
        this.labman_container.addView(tr);
        TableLayout.LayoutParams lp = (TableLayout.LayoutParams) tr.getLayoutParams();
        lp.bottomMargin = Utils.dipToPixel(this, 20);
        tr.setLayoutParams(lp);
        return tr;
    }

    private void addDelItem(TableRow tr, JSONObject collection, boolean showDelete) {
        LinearLayout delitem = (LinearLayout) View.inflate(this, R.layout.mycollection_lab_del_item, null);
        ImageView iv = (ImageView) delitem.findViewById(R.id.img_lab_del);
        if (collection != null) {
            TextView tv = (TextView) delitem.findViewById(R.id.lab_del);
            tv.setText(collection.optString("collection_title"));
            iv.setTag(collection.optString("collection_id"));
            iv.setVisibility(showDelete ? View.VISIBLE : View.INVISIBLE);
            iv.setOnClickListener(this.colDeleteListener);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }
        tr.addView(delitem);
        TableRow.LayoutParams tp = (TableRow.LayoutParams) delitem.getLayoutParams();
        tp.weight = 1;
        delitem.setLayoutParams(tp);
    }

    private void onListDataLoaded(JSONArray arr) {
        this.articles = arr;
        this.mycollist.getHandler().post(new Runnable() {
            @Override
            public void run() {
                MyCollectionActivity.this.mycollist.smoothScrollToPosition(0);
                MyCollectionActivity.this.listAdpater.notifyDataSetChanged();
            }
        });
    }

    private View.OnClickListener colDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String id = view.getTag().toString();
            ArrayList<Pair<String, String>> fields = new ArrayList<>();
            UserInfo ui = UserInfo.getCurrentUser(MyCollectionActivity.this);
            fields.add(new Pair<String, String>("user_id", ui.getId().toString()));
            fields.add(new Pair<String, String>("token", ui.getToken()));
            fields.add(new Pair<String, String>("item_type", "collection"));
            fields.add(new Pair<String, String>("item_id", id));
            fields.add(new Pair<String, String>("like", "0"));
            ArrayList<Pair<String, String>> headers = new ArrayList<>();
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
                                        LoginUtils.startLogin(MyCollectionActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                                    }
                                    else {
                                        throw new JSONException(String.format("result: %d", i));
                                    }
                                }
                                MyCollectionActivity.this.onCollectionDeleted(id);
                            } catch (JSONException e) {
                                Log.e(TAG, "taskCompletionResult: " + e.getMessage());
                            }
                        }
                    },
                    WebAPIHelper.HttpMethod.POST,
                    WebAPIHelper.buildHttpQuery(fields).getBytes(),
                    headers
            };
            new HttpAsyncTask().execute(params);
        }
    };

    private void onCollectionDeleted(String id) {
        try {
            JSONArray arr = this.likedinfo.getJSONArray("collections");
            int i = 0;
            while (i < arr.length()) {
                JSONObject obj = arr.getJSONObject(i);
                if (id.equals(obj.optString("collection_id"))) {
                    arr.remove(i);
                    this.likedinfo.put("collections", arr);
                    break;
                }
                i++;
            }
            this.onCollectedLabDataLoad(arr, true);
        } catch (JSONException e) {
            Log.e(TAG, "onCollectionDeleted: " + e.getMessage());
        }
    }

    private Bitmap takeScreenShot(){
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();

        Rect frame = new Rect();
        view.getWindowVisibleDisplayFrame(frame);
        int topHeight = frame.top + Utils.dipToPixel(this, 110);

        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(bm, 0, topHeight, width, height - topHeight);
        view.destroyDrawingCache();
        return b;
    }
}
