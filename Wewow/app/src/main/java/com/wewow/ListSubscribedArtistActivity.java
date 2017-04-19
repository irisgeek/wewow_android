package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.wewow.adapter.ListViewArtistsAdapter;
import com.wewow.adapter.ListViewSubscribedArtistsAdapter;
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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/24.
 */
public class ListSubscribedArtistActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {


    private int currentPage = 1;
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private ListViewSubscribedArtistsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.setActivityToBeFullscreen(this);


        setContentView(R.layout.activity_list_artist_subscribed);
        initData();

        if (Utils.isNetworkAvailable(this)) {

            checkcacheUpdatedOrNot();

        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);

            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            setUpArtistsFromCache();

        }
        setUpToolBar();

    }

    private void initData() {

        listView = (ListView) findViewById(R.id.listViewArtists);

        listItem = new ArrayList<HashMap<String, Object>>();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
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
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setVisible(true);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint(getResources().getString(R.string.search_hint));


        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button)).setImageResource(R.drawable.selector_btn_search);


        final String[] testStrings = getResources().getStringArray(R.array.test_array);
//        int completeTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        AutoCompleteTextView completeText = (AutoCompleteTextView) searchView
//                .findViewById(completeTextId) ;


        AutoCompleteTextView completeText = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_search, R.id.text, testStrings);

        completeText.setAdapter(adapter);
        completeText.setTextColor(getResources().getColor(R.color.search_text_view_color));
        completeText.setHintTextColor(getResources().getColor(R.color.search_text_view_hint_color));
        completeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(testStrings[position], true);
            }
        });

        completeText.setThreshold(0);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ListSubscribedArtistActivity.this, query, Toast.LENGTH_SHORT).show();
//                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//                layout.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
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
        getSupportActionBar().setTitle(getResources().getString(R.string.all_artists_subscribed_title));

    }

    private List<Artist> parseArtistsListFromString(String realData) throws JSONException {

        List<Artist> artists = new ArrayList<Artist>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("follow");
        for (int i = 0; i < results.length(); i++) {
            Artist artist = new Artist();
            JSONObject result = results.getJSONObject(i);
            artist.setId(result.getString("id"));
            artist.setNickname(result.getString("nickname"));
            artist.setDesc(result.getString("desc"));
            artist.setImage(result.getString("image"));
            artist.setArticle_count(result.getString("article_count"));
            artist.setFollower_count(result.getString("follow_count"));
            artist.setRead(result.getString("read"));

            artists.add(artist);
        }

        return artists;
    }

    private void checkcacheUpdatedOrNot() {
        swipeRefreshLayout.setRefreshing(true);
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, ListSubscribedArtistActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getArtistListFromServer();
                        } else {
                            setUpArtistsFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    private void setUpArtistsFromCache() {
        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST);
            List<Artist> artists = new ArrayList<Artist>();
            try {
                artists = parseArtistsListFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpArtists(artists, false);
        }
    }

    private void setUpArtists(List<Artist> artists, boolean refresh) {


        ArrayList<HashMap<String, Object>> listItemCopy = new ArrayList<HashMap<String, Object>>();
        listItemCopy.addAll(listItem);
        if (refresh) {

            if (listItem != null && listItem.size() > 0) {
                listItem.clear();

            }
        }

        for (int i = 0; i < artists.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("imageView", artists.get(i).getImage());

            map.put("textViewName", artists.get(i).getNickname());
            map.put("textViewDesc", artists.get(i).getDesc());
            map.put("textViewArticleCount", artists.get(i).getArticle_count());
            map.put("textViewFollowerCount", artists.get(i).getFollower_count());
            map.put("imageViewFollowed", artists.get(i).getFollowed());
            map.put("id",artists.get(i).getId());
            map.put("read",artists.get(i).getRead());

            listItem.add(map);
        }

        listItem.addAll(listItemCopy);
        if (!refresh)
        {
            adapter = new ListViewSubscribedArtistsAdapter(this, listItem);

            listView.setAdapter(adapter);

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                HashMap<String, Object> stringObjectHashMap = (  HashMap<String, Object>)adapter.getItem(position);
                                                String artistId=stringObjectHashMap.get("id").toString();
                                                Intent intent = new Intent(ListSubscribedArtistActivity.this,DetailArtistActivity.class);
                                                intent.putExtra("id",artistId);
                                                startActivity(intent);
                                            }
                                        }
        );
        adapter.notifyDataSetChanged();
        currentPage++;
        swipeRefreshLayout.setRefreshing(false);


    }

    private void getArtistListFromServer() {
        swipeRefreshLayout.setRefreshing(true);
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId = "0";
        //check user login or not
        if (UserInfo.isUserLogged(ListSubscribedArtistActivity.this)) {
            userId = UserInfo.getCurrentUser(ListSubscribedArtistActivity.this).getId().toString();

        }
        iTask.artistsSubscribed(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, currentPage, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<Artist> artists = new ArrayList<Artist>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        artists = parseArtistsListFromString(realData);

                        if (currentPage > 1) {
                            setUpArtists(artists, true);
                        } else {
                            FileCacheUtil.setCache(realData, ListSubscribedArtistActivity.this, CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, 0);
                            setUpArtists(artists, false);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    @Override
    public void onRefresh() {

        boolean isLastPageLoaded = false;
        try {
            isLastPageLoaded = isLastPageLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!isLastPageLoaded) {

            getArtistListFromServer();
        }
        else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST);
            JSONObject object = new JSONObject(fileContent);
            String totalPages = object.getJSONObject("result").getJSONObject("data").getString("follow_total_page");
            if (currentPage>Integer.parseInt(totalPages)) {

                result = true;
            }
        }

        return result;

    }
}
