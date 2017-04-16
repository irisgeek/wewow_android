package com.wewow;


import android.app.SearchManager;
import android.content.Context;
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
import com.wewow.adapter.ListViewFeedbackAdapter;
import com.wewow.dto.Feedback;
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
 * Created by iris on 17/4/13.
 */
public class FeedbackActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {


    private int currentPage = 1;
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private ListViewFeedbackAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.setActivityToBeFullscreen(this);


        setContentView(R.layout.activity_feed_back);
        initData();

        if (Utils.isNetworkAvailable(this)) {

            checkcacheUpdatedOrNot();

        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);

            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            setUpFeedbacksFromCache();

        }
        setUpToolBar();

    }

    private void initData() {

        listView = (ListView) findViewById(R.id.listViewFeedbacks);

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
        getSupportActionBar().setTitle(getResources().getString(R.string.talk_to_wewow));

    }

    private List<Feedback> parseFeedbackFromString(String realData) throws JSONException {

        List<Feedback> feedbacks = new ArrayList<Feedback>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("feedbacks");
        for (int i = 0; i < results.length(); i++) {
            Feedback feedback = new Feedback();
            JSONObject result = results.getJSONObject(i);
            feedback.setId(result.getString("id"));
            feedback.setContent(result.getString("content"));
            feedback.setImage_height(result.getString("image_height"));
            feedback.setFrom(result.getString("from"));
            feedback.setContent_type(result.getString("content_type"));
            feedback.setTime(result.getString("time"));
            feedback.setReply_to(result.getString("reply_to"));
            feedback.setImage_width(result.getString("image_width"));

            feedbacks.add(feedback);
        }

        return feedbacks;
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
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_FEEDBACKS, FeedbackActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getFeedbackFromServer();
                        } else {
                            setUpFeedbacksFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    private void setUpFeedbacksFromCache() {
        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_FEEDBACKS, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_FEEDBACKS);
            List<Feedback> feedbacks = new ArrayList<Feedback>();
            try {
                feedbacks = parseFeedbackFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpFeedbacks(feedbacks, false);
        }
    }

    private void setUpFeedbacks(List<Feedback> feedbacks, boolean refresh) {


        ArrayList<HashMap<String, Object>> listItemCopy = new ArrayList<HashMap<String, Object>>();
        listItemCopy.addAll(listItem);
        if (refresh) {

            if (listItem != null && listItem.size() > 0) {
                listItem.clear();

            }
        }

        for (int i = 0; i < feedbacks.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("from", feedbacks.get(i).getFrom());
            map.put("content_type", feedbacks.get(i).getContent_type());
            map.put("imageView", feedbacks.get(i).getContent());
            map.put("textView", feedbacks.get(i).getContent());


            listItem.add(map);
        }

        listItem.addAll(listItemCopy);
        if (!refresh)
        {
            adapter = new ListViewFeedbackAdapter(this, listItem);

            listView.setAdapter(adapter);

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                                                HashMap<String, Object> stringObjectHashMap = (  HashMap<String, Object>)adapter.getItem(position);
//                                                String artistId=stringObjectHashMap.get("id").toString();
//                                                Intent intent = new Intent(FeedbackActivity.this,DetailArtistActivity.class);
//                                                intent.putExtra("id",artistId);
//                                                startActivity(intent);
                                            }
                                        }
        );
        adapter.notifyDataSetChanged();
        currentPage++;
        swipeRefreshLayout.setRefreshing(false);


    }

    private void getFeedbackFromServer() {
        swipeRefreshLayout.setRefreshing(true);
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        String userId = "0";
        //check user login or not
        if (UserInfo.isUserLogged(FeedbackActivity.this)) {
            userId = UserInfo.getCurrentUser(FeedbackActivity.this).getId().toString();

        }
        iTask.feedbacks(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, currentPage, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<Feedback> feedbacks = new ArrayList<Feedback>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        feedbacks = parseFeedbackFromString(realData);

                        if (currentPage > 1) {
                            setUpFeedbacks(feedbacks, true);
                        } else {
                            FileCacheUtil.setCache(realData, FeedbackActivity.this, CommonUtilities.CACHE_FILE_FEEDBACKS, 0);
                            setUpFeedbacks(feedbacks, false);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
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

            getFeedbackFromServer();
        }
        else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTISTS_LIST, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTISTS_LIST);
            JSONObject object = new JSONObject(fileContent);
            String totalPages = object.getJSONObject("result").getJSONObject("data").getString("total_pages");
            if (currentPage>Integer.parseInt(totalPages)) {

                result = true;
            }
        }

        return result;

    }
}
