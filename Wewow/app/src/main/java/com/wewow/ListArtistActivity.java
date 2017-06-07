package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.jaeger.library.StatusBarUtil;
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
import java.util.Stack;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/24.
 */
public class ListArtistActivity extends BaseActivity {


    private int currentPage = 1;
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private ListViewArtistsAdapter adapter;
    private MaterialRefreshLayout refreshLayout;
    private String artistId;
    private List<Artist> artistsTemp;
    private List<Artist> allArtists;
    private boolean isHeaderAdded = false;
    private ArrayList<String> followStatus;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuselectedPosition(1);
        setContentView(R.layout.activity_list_artist);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        initData();

        setUpToolBar();
    }

    private void initData() {

        listView = (ListView) findViewById(R.id.listViewArtists);
        followStatus = new ArrayList<String>();
        listItem = new ArrayList<HashMap<String, Object>>();
        allArtists = new ArrayList<Artist>();
        refreshLayout = (MaterialRefreshLayout) findViewById(R.id.refresh);


        refreshLayout.setShowArrow(false);
        int[] colors = {getResources().getColor(R.color.font_color)};
        refreshLayout.setProgressColors(colors);
        refreshLayout.setLoadMore(true);
        refreshLayout.finishRefreshLoadMore();

        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                currentPage = 1;
                LinearLayout layoutBottom=(LinearLayout)findViewById(R.id.layout_bottom);
                layoutBottom.setVisibility(View.GONE);
                if (Utils.isNetworkAvailable(ListArtistActivity.this)) {

//                    checkcacheUpdatedOrNot();
                    getArtistListFromServer();

                } else {
                    Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();

                    SettingUtils.set(ListArtistActivity.this, CommonUtilities.NETWORK_STATE, false);
                    setUpArtistsFromCache();

                }

            }

            @Override
            public void onfinish() {

                refreshLayout.finishRefreshLoadMore();
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {

                boolean isLastPageLoaded = false;
                try {
                    isLastPageLoaded = isLastPageLoaded();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!isLastPageLoaded) {

                    getArtistListFromServer();
                } else {
                    adapter.notifyDataSetChanged();

                    onfinish();
                    LinearLayout layoutBottom=(LinearLayout)findViewById(R.id.layout_bottom);
                    layoutBottom.setVisibility(View.VISIBLE);
                }


            }
        });


        refreshLayout.autoRefresh();

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


        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button)).setImageResource(R.drawable.search_b);


        final String[] testStrings = getResources().getStringArray(R.array.test_array);
//        int completeTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        AutoCompleteTextView completeText = (AutoCompleteTextView) searchView
//                .findViewById(completeTextId) ;


        AutoCompleteTextView completeText = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_search, R.id.text, testStrings);

//        completeText.setAdapter(adapter);
        completeText.setTextColor(getResources().getColor(R.color.search_hot_search_words));
        completeText.setHintTextColor(getResources().getColor(R.color.search_text_view_hint_color_of_artist_list));
        completeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(testStrings[position], true);
            }
        });
        final Menu menuFinal = menu;
        completeText.setThreshold(0);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                MenuItem menuItem = menuFinal.findItem(R.id.search);
                menuItem.collapseActionView();

                List<Artist> artistsBySearch = new ArrayList<Artist>();
                for (Artist artist : artistsTemp) {
                    if (artist.getNickname().contains(query)) {
                        artistsBySearch.add(artist);
                    }
                }
                listItem.clear();

                for (int i = 0; i < artistsBySearch.size(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();

                    //

                    map.put("imageView", artistsBySearch.get(i).getImage());

                    map.put("textViewName", artistsBySearch.get(i).getNickname());
                    map.put("textViewDesc", artistsBySearch.get(i).getDesc());
                    map.put("textViewArticleCount", artistsBySearch.get(i).getArticle_count());
                    map.put("textViewFollowerCount", artistsBySearch.get(i).getFollower_count());
                    map.put("imageViewFollowed", artistsBySearch.get(i).getFollowed());
                    map.put("id", artistsBySearch.get(i).getId());

                    listItem.add(map);
                }
                adapter.notifyDataSetChanged();

//                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//                layout.setVisibility(View.GONE);
                return true;
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

//        listView.setAdapter(new ListViewArtistsAdapter(this, listItem));
    }

    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.menu_b);
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

    private void checkcacheUpdatedOrNot() {
        ITask iTask = Utils.getItask(com.wewow.utils.CommonUtilities.WS_HOST);
        iTask.updateAt(com.wewow.utils.CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(com.wewow.utils.CommonUtilities.SUCCESS)) {
                        Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                        refreshLayout.finishRefresh();

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = com.wewow.utils.FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_ARTISTS_LIST, ListArtistActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getArtistListFromServer();
                        } else {
                            setUpArtistsFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    refreshLayout.finishRefresh();
                    Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    refreshLayout.finishRefresh();
                    Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

            }

        });
    }

    private void setUpArtistsFromCache() {
        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTISTS_LIST, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTISTS_LIST);
            artistsTemp = new ArrayList<Artist>();
            try {
                artistsTemp = parseArtistsListFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpArtists(artistsTemp, false);
        }
    }

    private void setUpArtists(List<Artist> artists, boolean refresh) {


        ArrayList<HashMap<String, Object>> listItemCopy = new ArrayList<HashMap<String, Object>>();
        ArrayList<Artist> artistsCopy = new ArrayList<Artist>();
        artistsCopy.addAll(allArtists);

        listItemCopy.addAll(listItem);
//        if (refresh) {

        if (listItem != null && listItem.size() > 0) {
            listItem.clear();
            allArtists.clear();

        }
//
        if (currentPage != 1) {
            listItem.addAll(listItemCopy);
            allArtists.addAll(artistsCopy);
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
            map.put("id", artists.get(i).getId());

            listItem.add(map);
        }
        allArtists.addAll(artists);


        if (followStatus != null && followStatus.size() > 0) {
            followStatus.clear();
        }
        for (HashMap<String, Object> objectHashMap : listItem) {
            String status = objectHashMap.get("imageViewFollowed").toString();
            followStatus.add(status);
        }

        if (!refresh) {
            adapter = new ListViewArtistsAdapter(this, listItem, followStatus);

            listView.setAdapter(adapter);

//            if(!isHeaderAdded) {
//
//                isHeaderAdded=true;
////                View view = View.inflate(this, R.layout.list_header_artist, null);
////                listView.addHeaderView(view);
//            }

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                HashMap<String, Object> stringObjectHashMap = (HashMap<String, Object>) adapter.getItem(position);
                                                artistId = stringObjectHashMap.get("id").toString();
                                                Intent intent = new Intent(ListArtistActivity.this, DetailArtistActivity.class);
                                                intent.putExtra("id", artistId);
                                                startActivityForResult(intent, 8);
                                            }
                                        }
        );
        adapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
        currentPage++;
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
//        CardView view=(CardView)findViewById(R.id.refreshCardView);
//        view.setVisibility(View.VISIBLE);


    }

    private void updateArtists(List<Artist> artists) {


        if (listItem != null && listItem.size() > 0) {
            listItem.clear();

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
            map.put("id", artists.get(i).getId());

            listItem.add(map);
        }

        if (followStatus != null && followStatus.size() > 0) {
            followStatus.clear();
        }
        for (HashMap<String, Object> objectHashMap : listItem) {
            String status = objectHashMap.get("imageViewFollowed").toString();
            followStatus.add(status);
        }

        adapter.notifyDataSetChanged();


    }

    private void getArtistListFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId = "0";
        //check user login or not
        if (UserInfo.isUserLogged(ListArtistActivity.this)) {
            userId = UserInfo.getCurrentUser(ListArtistActivity.this).getId().toString();

        }
        iTask.allArtists(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, currentPage, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                artistsTemp = new ArrayList<Artist>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                        refreshLayout.finishRefreshLoadMore();

                    } else {
                        artistsTemp = parseArtistsListFromString(realData);
                        JSONObject objectData = new JSONObject(realData);
                        totalPages = Integer.parseInt(objectData.getJSONObject("result").getJSONObject("data").getString("total_pages"));

                        if (currentPage > 1) {
                            setUpArtists(artistsTemp, true);
                        } else {
                            FileCacheUtil.setCache(realData, ListArtistActivity.this, CommonUtilities.CACHE_FILE_ARTISTS_LIST, 0);
                            setUpArtists(artistsTemp, false);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefreshLoadMore();
                    refreshLayout.finishRefresh();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefreshLoadMore();
                    refreshLayout.finishRefresh();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefreshLoadMore();
                refreshLayout.finishRefresh();

            }
        });
    }


    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;


        if (currentPage > totalPages) {

            result = true;
        }


        return result;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 8 && resultCode == 0) {
//            List<Artist> updatedArtists = new ArrayList<Artist>();
//            for (Artist artist : allArtists) {
//                if (artist.getId().equals(artistId)) {
//                   artist.setFollowed(followed);
//                }
//                updatedArtists.add(artist);
//            }
//            updateArtists(updatedArtists);
            String followed = data.getStringExtra("followed").toString();
            for (int i = 0; i < allArtists.size(); i++) {
                if (allArtists.get(i).getId().equals(artistId)) {
                    followStatus.set(i, followed.equals("1") ? "1" : "0");

                }
            }
            adapter.notifyDataSetChanged();

        }
    }
}
