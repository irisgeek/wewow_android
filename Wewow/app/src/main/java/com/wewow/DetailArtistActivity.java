package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.adapter.ListViewArtistsAdapter;
import com.wewow.dto.Artist;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.support.v7.widget.RecyclerView;

/**
 * Created by iris on 17/3/24.
 */
public class DetailArtistActivity extends BaseActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_detail_artist);
        Intent getIntent = getIntent();
        String id = getIntent.getStringExtra("id");

        initData();


//        setUpToolBar();
        RecyclerView rv = (RecyclerView)findViewById(R.id.recyclerview);
        setupRecyclerView(rv);

    }

    private void initData() {


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.toolbar, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        menuItem.setVisible(true);
//
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        final SearchView searchView =
//                (SearchView) menu.findItem(R.id.search).getActionView();
//
//        searchView.setQueryHint(getResources().getString(R.string.search_hint));
//
//
//        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button)).setImageResource(R.drawable.selector_btn_search);
//
//
//        final String[] testStrings = getResources().getStringArray(R.array.test_array);
////        int completeTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
////        AutoCompleteTextView completeText = (AutoCompleteTextView) searchView
////                .findViewById(completeTextId) ;
//
//
//        AutoCompleteTextView completeText = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_search, R.id.text, testStrings);
//
//        completeText.setAdapter(adapter);
//        completeText.setTextColor(getResources().getColor(R.color.search_text_view_color));
//        completeText.setHintTextColor(getResources().getColor(R.color.search_text_view_hint_color));
//        completeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                searchView.setQuery(testStrings[position], true);
//            }
//        });
//
//        completeText.setThreshold(0);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(DetailArtistActivity.this, query, Toast.LENGTH_SHORT).show();
////                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
////                layout.setVisibility(View.GONE);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
//            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//            layout.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpListViewDummy() {

        ListView listView = (ListView) findViewById(R.id.listViewArtists);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 8; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("imageView", "https://wewow.wewow.com.cn/article/20170327/14513-amanda-kerr-39507.jpg?x-oss-process=image/resize,m_fill,h_384,w_720,,limit_0/quality,Q_40/format,jpg");

            map.put("textViewName", "下厨房");
            map.put("textViewDesc", "唯美食与爱不可辜负");
            map.put("textViewArticleCount", "22");
            map.put("textViewFollowerCount", "534");

            listItem.add(map);
        }

        listView.setAdapter(new ListViewArtistsAdapter(this, listItem));
    }

    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.selector_btn_menu);
        getSupportActionBar().setTitle(getResources().getString(R.string.all_artists_title));

    }

    private List<Artist> parseArtistsListFromString(String realData) throws JSONException {

        List<Artist> artists = new ArrayList<Artist>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("artist_list");
        for (int i = 0; i < results.length(); i++) {
            Artist artist = new Artist();
            JSONObject result = results.getJSONObject(i);
            artist.setId(result.getString("id"));
            artist.setNickname(result.getString("nickname"));
            artist.setDesc(result.getString("desc"));
            artist.setImage(result.getString("image"));
            artist.setArticle_count(result.getString("article_count"));
            artist.setFollower_count(result.getString("follow_count"));
            artist.setFollowed(result.getString("followed"));

            artists.add(artist);
        }

        return artists;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        String[] list={"1","1","1","1","1","1","1","1"};
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(this,
                getRandomSublist(list, 8)));
    }

    private List<String> getRandomSublist(String[] array, int amount) {
        ArrayList<String> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array[random.nextInt(array.length)]);
        }
        return list;
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<String> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.imageViewInstitue);
                mTextView = (TextView) view.findViewById(R.id.textViewNum);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public String getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, List<String> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_detail_lover_of_life, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mBoundString = mValues.get(position);
//            holder.mTextView.setText(mValues.get(position));

//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, CheeseDetailActivity.class);
//                    intent.putExtra(CheeseDetailActivity.EXTRA_NAME, holder.mBoundString);
//
//                    context.startActivity(intent);
//                }
//            });

            Glide.with(holder.mImageView.getContext())
                    .load("https://wewow.wewow.com.cn/article/20170327/14513-amanda-kerr-39507.jpg?x-oss-process=image/resize,m_fill,h_384,w_720,,limit_0/quality,Q_40/format,jpg")
                    .fitCenter()
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }

}
