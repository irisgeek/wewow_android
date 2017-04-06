package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
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
import com.wewow.adapter.RecycleViewArticlesOfArtistDetail;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.ArtistDetail;
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
public class DetailArtistActivity extends BaseActivity {


    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_detail_artist);
        Intent getIntent = getIntent();
        id = getIntent.getStringExtra("id");

//        initData();
        RecyclerView rv = (RecyclerView)findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));

        if (Utils.isNetworkAvailable(this)) {

            checkcacheUpdatedOrNot();

        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();


            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            setUpArtistFromCache();

        }


        setUpToolBar();


    }




    private void checkcacheUpdatedOrNot() {

        ITask iTask = Utils.getItask(com.wewow.utils.CommonUtilities.WS_HOST);
        iTask.updateAt(com.wewow.utils.CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(com.wewow.utils.CommonUtilities.SUCCESS)) {
                        Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = com.wewow.utils.FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, DetailArtistActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getArtistFromServer();
                        } else {
                            setUpArtistFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
//                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);
            }

        });


    }

    private void getArtistFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId = "0";
        //check user login or not
        if (UserInfo.isUserLogged(DetailArtistActivity.this)) {
            userId = UserInfo.getCurrentUser(DetailArtistActivity.this).getOpen_id();

        }
        iTask.artistDetail(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, id, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                ArtistDetail artist = new ArtistDetail();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        artist = parseArtistFromString(realData);

                        FileCacheUtil.setCache(realData, DetailArtistActivity.this, CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, 0);
                        setUpArtist(artist);


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void setUpArtist(ArtistDetail artist) {

        ImageView imageView=(ImageView)findViewById(R.id.imageViewIcon);
        Glide.with(this)
        .load(artist.getArtist().getImage()).crossFade().into(imageView);

        TextView textViewNickName=(TextView)findViewById(R.id.textViewNickName);
        textViewNickName.setText(artist.getArtist().getNickname());

        TextView textViewDesc=(TextView)findViewById(R.id.textViewDesc);
        textViewDesc.setText(artist.getArtist().getDesc());

        ImageView imageViewSubscribe=(ImageView) findViewById(R.id.imageViewSubscribe);
        if(artist.getArtist().getFollowed().equals("1"))
        {
            imageViewSubscribe.setImageResource(R.drawable.subscribed);
        }
        else {
            imageViewSubscribe.setImageResource(R.drawable.subscribe);
        }

        TextView textViewCount=(TextView)findViewById(R.id.textViewCount);
        textViewCount.setText(artist.getArtist().getFollower_count()+getResources().getString(R.string.subscriber));


        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        List<Article> articles=artist.getArticles();


        for (int i = 0; i < articles.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("image", articles.get(i).getImage_320_160());

            map.put("title", articles.get(i).getTitle());
       //todo
            listItem.add(map);
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(new RecycleViewArticlesOfArtistDetail(this,
                listItem));


    }

    private void setUpArtistFromCache() {

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL+id, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTISTS_DETAIL+id);
           ArtistDetail artist= new ArtistDetail();
            try {
                artist = parseArtistFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpArtist(artist);
        }
    }


    private void initData() {

        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this)
                .load("https://wewow.wewow.com.cn/article/20170327/14513-amanda-kerr-39507.jpg?x-oss-process=image/resize,m_fill,h_384,w_720,,limit_0/quality,Q_40/format,jpg")
                .fitCenter()
                .into(imageView);

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
        if(id==android.R.id.home) {
          finish();
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
        toolbar.setNavigationIcon(R.drawable.selector_btn_back);
//        getSupportActionBar().setTitle(getResources().getString(R.string.all_artists_title));

    }

    private ArtistDetail parseArtistFromString(String realData) throws JSONException {

        ArtistDetail artistDetail = new ArtistDetail();
        Artist artist = new Artist();
        JSONObject object = new JSONObject(realData);
        JSONObject results = object.getJSONObject("result").getJSONObject("data").getJSONObject("artist");
        artist.setFollower_count(results.getString("follow_count"));
        artist.setNickname(results.getString("nickname"));
        artist.setImage(results.getString("image_750_512"));
        artist.setDesc(results.getString("desc"));
        artist.setFollowed(results.getString("followed"));
        artistDetail.setArtist(artist);
        List<Article> articles = new ArrayList<Article>();


        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("article_list");

        for (int i = 0; i < array.length(); i++) {
            Article article = new Article();

            JSONObject result = array.getJSONObject(i);
            article.setId(result.getString("id"));
            article.setImage_284_160(result.getString("image_284_160"));
            article.setImage_320_160(result.getString("image_320_160"));
            article.setTitle(result.getString("title"));
            article.setWewow_category(result.getString("wewow_category"));

            articles.add(article);
        }
        artistDetail.setArticles(articles);

        return artistDetail;
    }






}
