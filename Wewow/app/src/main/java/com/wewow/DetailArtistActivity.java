package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.wewow.adapter.RecycleViewArtistsOfHomePageAdapter;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.ArtistDetail;
import com.wewow.netTask.ITask;
import com.wewow.utils.AppBarStateChangeListener;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoadMoreListener;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.RecyclerViewUpRefresh;

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
public class DetailArtistActivity extends BaseActivity implements LoadMoreListener {


    private String id;
    private ImageView imageViewSubscribe;
    private boolean updateArtistList = false;
    private int currentPage = 1;
    private RecyclerViewUpRefresh rv;
    private ArrayList<HashMap<String, Object>> listItem;
    private RecycleViewArticlesOfArtistDetail adapter;
    private String totalPages;
    private String followed;
    private ArtistDetail artistCurrent;
    private ImageView imageView;
    private TextView textViewNickName;
    private TextView textViewDesc;
    private String nickName;
    private CollapsingToolbarLayout collapsingToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_detail_artist);
        Intent getIntent = getIntent();
        id = getIntent.getStringExtra("id");
        listItem = new ArrayList<HashMap<String, Object>>();
        initAppBar();


//        initData();
        rv = (RecyclerViewUpRefresh) findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setCanloadMore(true);
        rv.setLoadMoreListener(this);

        if (Utils.isNetworkAvailable(this)) {

            checkcacheUpdatedOrNot();

        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();


            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            setUpArtistFromCache();

        }


        setUpToolBar();


    }

    private void initAppBar() {
        imageView = (ImageView) findViewById(R.id.imageViewIcon);

        textViewNickName = (TextView) findViewById(R.id.textViewNickName);
        textViewDesc = (TextView) findViewById(R.id.textViewDesc);
       collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.font_color));

        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                Log.d("STATE", state.name());
                if (state == State.EXPANDED) {
                    imageView.setVisibility(View.VISIBLE);
                    textViewNickName.setVisibility(View.VISIBLE);
                    textViewDesc.setVisibility(View.VISIBLE);
//                    isAppBarFolded = false;
                    //展开状态

                } else if (state == State.COLLAPSED) {

                    imageView.setVisibility(View.GONE);
                    textViewNickName.setVisibility(View.GONE);
                    textViewDesc.setVisibility(View.GONE);
//                    isAppBarFolded = true;

                    //折叠状态

                } else {
                    imageView.setVisibility(View.GONE);
                    textViewNickName.setVisibility(View.GONE);
                    textViewDesc.setVisibility(View.GONE);
                    //中间状态

                }
            }
        });
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
                        rv.loadMoreComplete();

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
                            getArtistFromServer(false);
                        } else {
                            setUpArtistFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    rv.loadMoreComplete();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    rv.loadMoreComplete();
//                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                rv.loadMoreComplete();
                Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);
            }

        });


    }

    private void getArtistFromServer(final boolean refresh) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId = "0";
        String userToken = "0";
        //check user login or not
        if (UserInfo.isUserLogged(DetailArtistActivity.this)) {
            userId = UserInfo.getCurrentUser(DetailArtistActivity.this).getId().toString();
            userToken = UserInfo.getCurrentUser(DetailArtistActivity.this).getToken();

        }
        iTask.artistDetail(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, id, currentPage, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                ArtistDetail artist = new ArtistDetail();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                        swipeRefreshLayout.setRefreshing(false);
                        rv.loadMoreComplete();

                    } else {
                        if (refresh) {
                            artist = parseArticlesFromString(realData);
                        } else {
                            artist = parseArtistFromString(realData);
                            FileCacheUtil.setCache(realData, DetailArtistActivity.this, CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, 0);
                        }


                        setUpArtist(artist, refresh);


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                    rv.loadMoreComplete();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                    rv.loadMoreComplete();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(DetailArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);
                rv.loadMoreComplete();

            }
        });
    }

    private void setUpArtist(final ArtistDetail artist, boolean refresh) {

        artistCurrent=artist;

        if (!refresh) {

            Glide.with(this)

                    .load(artist.getArtist().getImage()).crossFade().fitCenter().placeholder(R.drawable.banner_loading_spinner).placeholder(R.drawable.banner_loading_spinner).into(imageView);

            nickName=artist.getArtist().getNickname();
            textViewNickName.setText(nickName);

            collapsingToolbar.setTitle(nickName);
            textViewDesc.setText(artist.getArtist().getDesc());

            imageViewSubscribe = (ImageView) findViewById(R.id.imageViewSubscribe);
            followed = artist.getArtist().getFollowed();
            if (followed.equals("1")) {
                imageViewSubscribe.setImageResource(R.drawable.subscribed);
            } else {
                imageViewSubscribe.setImageResource(R.drawable.subscribe);
            }

            imageViewSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (UserInfo.isUserLogged(DetailArtistActivity.this)) {

                        postReadToServer(artist.getArtist().getId(), Integer.parseInt(followed.equals("1") ? "0" : "1"));
                        if (followed.equals("1")) {
                            imageViewSubscribe.setImageResource(R.drawable.subscribe);
                        } else {
                            imageViewSubscribe.setImageResource(R.drawable.subscribed);
                        }
                    } else {
                        LoginUtils.startLogin(DetailArtistActivity.this, LoginActivity.REQUEST_CODE_ARTIST_DETAIL);
                    }


                }
            });

            TextView textViewCount = (TextView) findViewById(R.id.textViewCount);
            textViewCount.setText(artist.getArtist().getFollower_count() + getResources().getString(R.string.subscriber));
        }

        ArrayList<HashMap<String, Object>> listItemCopy = new ArrayList<HashMap<String, Object>>();
        listItemCopy.addAll(listItem);
//        if (refresh) {

        if (listItem != null && listItem.size() > 0) {
            listItem.clear();

        }
//
        if (currentPage != 1) {
            listItem.addAll(listItemCopy);
        }


        final List<Article> articles = artist.getArticles();

        for (int i = 0; i < articles.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("image", articles.get(i).getImage_642_320());

            map.put("title", articles.get(i).getTitle());
            //todo
            listItem.add(map);
        }

        if (!refresh) {

            adapter = new RecycleViewArticlesOfArtistDetail(this,
                    listItem);

            rv.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }


        adapter.setOnItemClickListener(new RecycleViewArticlesOfArtistDetail.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(DetailArtistActivity.this, ArticleActivity.class);
                String articleId = articles.get(position).getId();
                intent.putExtra(ArticleActivity.ARTICLE_ID, Integer.parseInt(articleId));
                DetailArtistActivity.this.startActivity(intent);

            }

        });
        currentPage++;
        rv.loadMoreComplete();


    }

    private void postReadToServer(String artistId, final int read) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        String userId = UserInfo.getCurrentUser(DetailArtistActivity.this).getId().toString();
        String token = UserInfo.getCurrentUser(DetailArtistActivity.this).getToken().toString();


        iTask.followArtist(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(DetailArtistActivity.this), userId, artistId, token, read, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    JSONObject responseObject = new JSONObject(realData);

                    if (!responseObject.getJSONObject("result").getString("code").equals("0")) {
                        Toast.makeText(DetailArtistActivity.this, responseObject.getJSONObject("result").getString("message").toString(), Toast.LENGTH_SHORT).show();


                    } else {
                        updateArtistList = true;
                        if (read == 0) {
                            followed = "0";

                        } else {
                            followed = "1";
                        }
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, DetailArtistActivity.this);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, DetailArtistActivity.this);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_LIST, DetailArtistActivity.this);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailArtistActivity.this, DetailArtistActivity.this.getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(DetailArtistActivity.this, DetailArtistActivity.this.getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void setUpArtistFromCache() {

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id);
            ArtistDetail artist = new ArtistDetail();
            try {
                artist = parseArtistFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpArtist(artist, false);
        }
    }


    private void initData() {

        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this)
                .load("https://wewow.wewow.com.cn/article/20170327/14513-amanda-kerr-39507.jpg?x-oss-process=image/resize,m_fill,h_384,w_720,,limit_0/quality,Q_40/format,jpg")
                .fitCenter().placeholder(R.drawable.banner_loading_spinner)
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
        if (id == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("followed", followed);
            setResult(0, intent);
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

//        listView.setAdapter(new ListViewArtistsAdapter(this, listItem));
    }

    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_b);
//        getSupportActionBar().setTitle(getResources().getString(R.string.all_artists_title));

    }

    private ArtistDetail parseArtistFromString(String realData) throws JSONException {

        ArtistDetail artistDetail = new ArtistDetail();
        Artist artist = new Artist();
        JSONObject object = new JSONObject(realData);
        JSONObject results = object.getJSONObject("result").getJSONObject("data").getJSONObject("artist");
        artist.setFollower_count(results.getString("follow_count"));
        artist.setNickname(results.getString("nickname"));
        artist.setImage(results.getString("image_120_120"));
        artist.setDesc(results.getString("desc"));
        artist.setFollowed(results.getString("followed"));

        artist.setId(results.getString("id"));
        artistDetail.setArtist(artist);
        List<Article> articles = new ArrayList<Article>();


        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("article_list");

        for (int i = 0; i < array.length(); i++) {
            Article article = new Article();

            JSONObject result = array.getJSONObject(i);
            article.setId(result.getString("id"));
            article.setImage_284_160(result.getString("image_284_160"));
            article.setImage_320_160(result.getString("image_320_160"));
            article.setImage_642_320(result.getString("image_688_316"));
            article.setTitle(result.getString("title"));
            article.setWewow_category(result.getString("wewow_category"));

            articles.add(article);
        }
        artistDetail.setArticles(articles);

        return artistDetail;
    }

    private ArtistDetail parseArticlesFromString(String realData) throws JSONException {

        ArtistDetail artistDetail = new ArtistDetail();

        JSONObject object = new JSONObject(realData);

        List<Article> articles = new ArrayList<Article>();


        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("article_list");

        for (int i = 0; i < array.length(); i++) {
            Article article = new Article();

            JSONObject result = array.getJSONObject(i);
            article.setId(result.getString("id"));
            article.setImage_284_160(result.getString("image_284_160"));
            article.setImage_320_160(result.getString("image_320_160"));
            article.setImage_642_320(result.getString("image_688_316"));
            article.setTitle(result.getString("title"));
            article.setWewow_category(result.getString("wewow_category"));

            articles.add(article);
        }
        artistDetail.setArticles(articles);

        return artistDetail;
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("followed", followed);
        setResult(0, intent);
        finish();
        super.onBackPressed();
    }


    @Override
    public void onLoadMore() {

        boolean isLastPageLoaded = false;
        try {
            isLastPageLoaded = isLastPageLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!isLastPageLoaded) {

            getArtistFromServer(true);
        } else {
            rv.loadMoreComplete();
        }


    }

    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id);
            JSONObject object = new JSONObject(fileContent);
            totalPages = object.getJSONObject("result").getJSONObject("data").getString("total_pages");
            if (currentPage > Integer.parseInt(totalPages)) {

                result = true;
            }
        }

        return result;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.REQUEST_CODE_ARTIST_DETAIL&&resultCode!=LoginActivity.RESULT_CANCELED) {
            postReadToServer(artistCurrent.getArtist().getId(), Integer.parseInt(followed.equals("1") ? "0" : "1"));
            if (followed.equals("1")) {
                imageViewSubscribe.setImageResource(R.drawable.subscribe);
            } else {
                imageViewSubscribe.setImageResource(R.drawable.subscribed);
            }

        }
    }

}


