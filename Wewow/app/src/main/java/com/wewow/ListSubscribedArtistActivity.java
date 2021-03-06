package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.jaeger.library.StatusBarUtil;
import com.wewow.adapter.ListViewArtistsAdapter;
import com.wewow.adapter.ListViewSubscribedArtistsAdapter;
import com.wewow.adapter.RecycleViewArtistOfSubscribedArtistList;
import com.wewow.dto.Artist;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoadMoreListener;
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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/24.
 */
public class ListSubscribedArtistActivity extends BaseActivity implements LoadMoreListener {


    private int currentPage = 1;
    private ArrayList<HashMap<String, Object>> listItem;
    private RecycleViewArtistOfSubscribedArtistList adapter;
    private int selectedPosition=0;
    private ArrayList<String> read;
    private int totalPages = 1;
    private LinearLayout progressBar;
    private RecyclerViewUpRefresh rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.setActivityToBeFullscreen(this);

        setMenuselectedPosition(5);
        setContentView(R.layout.activity_list_artist_subscribed);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        initData();


        setUpToolBar();

    }

    private void initData() {
        progressBar=(LinearLayout)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        rv = (RecyclerViewUpRefresh) findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setCanloadMore(true);
        rv.setLoadMoreListener(this);


        listItem = new ArrayList<HashMap<String, Object>>();
        read=new ArrayList<String>();


        currentPage = 1;
        if (Utils.isNetworkAvailable(ListSubscribedArtistActivity.this)) {

//                    checkcacheUpdatedOrNot();
            getArtistListFromServer();

        } else {
            Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            rv.loadMoreComplete();
            progressBar.setVisibility(View.GONE);

            SettingUtils.set(ListSubscribedArtistActivity.this, CommonUtilities.NETWORK_STATE, false);
            setUpArtistsFromCache();

        }


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



    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
//        swipeRefreshLayout.setRefreshing(true);
//        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
//        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
//            @Override
//            public void success(JSONObject object, Response response) {
//
//
//                try {
//                    String realData = Utils.convertStreamToString(response.getBody().in());
//                    if (!realData.contains(CommonUtilities.SUCCESS)) {
//                        Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//
//                        swipeRefreshLayout.setRefreshing(false);
//
//                    } else {
//                        JSONObject jsonObject = new JSONObject(realData);
//                        String cacheUpdatedTimeStamp = jsonObject
//                                .getJSONObject("result")
//                                .getJSONObject("data")
//                                .getString("update_at");
//
//                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
//                        boolean isCacheDataOutdated = FileCacheUtil
//                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, ListSubscribedArtistActivity.this, cacheUpdatedTime);
//
//                        if (isCacheDataOutdated) {
//                            getArtistListFromServer();
//                        } else {
//                            setUpArtistsFromCache();
////                            setUpListViewDummy();
//
//                        }
//
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
//                } catch (JSONException e) {
//                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);
//            }
//
//        });
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
        ArrayList<String> readCopy=new ArrayList<String>();
        listItemCopy.addAll(listItem);
        readCopy.addAll(read);
//        if (refresh) {

            if (listItem != null && listItem.size() > 0) {
                listItem.clear();

            }
            if (read != null && read.size() > 0) {
                read.clear();

//            }

        }

        if (currentPage != 1) {

            listItem.addAll(listItemCopy);
            read.addAll(readCopy);
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
            map.put("read", artists.get(i).getRead());

            listItem.add(map);
            read.add(artists.get(i).getRead());
        }



        if (!refresh)
        {
            adapter = new RecycleViewArtistOfSubscribedArtistList(this, listItem,read);

            rv.setAdapter(adapter);

        }
        rv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                HashMap<String, Object> stringObjectHashMap = (  HashMap<String, Object>)listItem.get(position);
                                                String artistId=stringObjectHashMap.get("id").toString();
                                                String read=stringObjectHashMap.get("read").toString();
                                                if(read.equals("1"))
                                                {
                                                    postReadToServer(artistId);

                                                }
                                                Intent intent = new Intent(ListSubscribedArtistActivity.this,DetailArtistActivity.class);
                                                intent.putExtra("id",artistId);
                                                startActivityForResult(intent, 10);
                                                selectedPosition=position;
                                            }
                                        }
        );
        adapter.notifyDataSetChanged();
        rv.setVisibility(View.VISIBLE);
        currentPage++;
        rv.loadMoreComplete();
        progressBar.setVisibility(View.GONE);
        boolean isLastPageLoaded = false;
        try {
            isLastPageLoaded = isLastPageLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isLastPageLoaded) {
            rv.loadMoreEnd();
        }

    }

    private void postReadToServer(final String artistId) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

          String   userId = UserInfo.getCurrentUser(ListSubscribedArtistActivity.this).getId().toString();
        String token=UserInfo.getCurrentUser(this).getToken().toString();
        final String read="1";


        iTask.artist_read(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, token, artistId, read, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    String code = new JSONObject(realData).getJSONObject("result").get("code").toString();
                    if (!code.equals("0")) {
                        Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    } else {
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_LIST, ListSubscribedArtistActivity.this);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + artistId, ListSubscribedArtistActivity.this);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, ListSubscribedArtistActivity.this);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getArtistListFromServer() {
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
                        rv.loadMoreComplete();
                        progressBar.setVisibility(View.GONE);

                    } else {
                        artists = parseArtistsListFromString(realData);
                        JSONObject objectData = new JSONObject(realData);
                        totalPages = Integer.parseInt(objectData.getJSONObject("result").getJSONObject("data").getString("follow_total_page"));

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
                    rv.loadMoreComplete();
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    rv.loadMoreComplete();
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListSubscribedArtistActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                rv.loadMoreComplete();
                progressBar.setVisibility(View.GONE);

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

        if(resultCode==0&&requestCode==10)
        {
            read.set(selectedPosition, "0");
            adapter.notifyDataSetChanged();


            if(!read.contains("1"))
            {
                FileCacheUtil.setCache("0", ListSubscribedArtistActivity.this, CommonUtilities.CACHE_FILE_ARTIST_UPDATE, 0);

                updateMenuForSubscribedAritstNotification();

            }
        }

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

            getArtistListFromServer();
        } else {
            adapter.notifyDataSetChanged();
            rv.loadMoreComplete();
            rv.loadMoreEnd();
        }

    }
}
