package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.wewow.adapter.ListViewArtistsAdapter;
import com.wewow.adapter.ListViewFeedbackAdapter;
import com.wewow.dto.Feedback;
import com.wewow.dto.Token;
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
public class FeedbackActivity extends BaseActivity{


    private int currentPage = 1;
    private ListView listView;
    private MaterialRefreshLayout refreshLayout;
    private ArrayList<HashMap<String, Object>> listItem;
    private ListViewFeedbackAdapter adapter;
    private Token token;
    private PickImageDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.setActivityToBeFullscreen(this);


        setContentView(R.layout.activity_feed_back);
        initData();

//        if (Utils.isNetworkAvailable(this)) {
//
//            checkcacheUpdatedOrNot();
//
//        } else {
//            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
//            refreshLayout.finishRefresh();
//
//            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
//            setUpFeedbacksFromCache();
//
//        }
        setUpToolBar();
        getToken();

    }

    private void initData() {

        listView = (ListView) findViewById(R.id.listViewFeedbacks);

        listItem = new ArrayList<HashMap<String, Object>>();
        refreshLayout = (MaterialRefreshLayout) findViewById(R.id.refresh);


        refreshLayout.setShowArrow(false);
        int[] colors = {getResources().getColor(R.color.font_color)};
        refreshLayout.setProgressColors(colors);

        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
//                currentPage = 1;
                if (currentPage == 1) {
                    if (Utils.isNetworkAvailable(FeedbackActivity.this)) {

//                        checkcacheUpdatedOrNot();

                        getFeedbackFromServer();

                    } else {
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();

                        SettingUtils.set(FeedbackActivity.this, CommonUtilities.NETWORK_STATE, false);
//                        setUpFeedbacksFromCache();

                    }
                } else {

                    boolean isLastPageLoaded = false;
                    try {
                        isLastPageLoaded = isLastPageLoaded();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!isLastPageLoaded) {

                        getFeedbackFromServer();
                    } else {
                        refreshLayout.finishRefresh();
                    }
                }

            }

            @Override
            public void onfinish() {

                refreshLayout.finishRefreshLoadMore();
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {

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
        for (int i = results.length() - 1; i >= 0; i--) {
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

    private Feedback parseFeedbackSent(String realData) throws JSONException {

        Feedback feedback = new Feedback();

        JSONObject object = new JSONObject(realData);
        JSONObject result = object.getJSONObject("result").getJSONObject("data");


        feedback.setId(result.getString("id"));
        feedback.setContent(result.getString("content"));
        feedback.setImage_height(result.getString("image_height"));
        feedback.setFrom(result.getString("from"));
        feedback.setContent_type(result.getString("content_type"));
        feedback.setTime(result.getString("time"));
        feedback.setReply_to(result.getString("reply_to"));
        feedback.setImage_width(result.getString("image_width"));


        return feedback;
    }

    private void checkcacheUpdatedOrNot() {
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                        refreshLayout.finishRefresh();

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
                    refreshLayout.finishRefresh();
                } catch (JSONException e) {
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    refreshLayout.finishRefresh();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();
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

//            setUpFeedbacks(feedbacks, false);
        }
    }

    private void getToken()
    {

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_TOKEN, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_TOKEN);
            try {
                token = parseTokenFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {
            getTokenFromServer();
        }

    }

    private void setUpFeedbacks(List<Feedback> feedbacks, boolean refresh, boolean feedbackSent) {


        if (feedbackSent) {

            for (int i = 0; i < feedbacks.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();

                //

                map.put("from", feedbacks.get(i).getFrom());
                map.put("content_type", feedbacks.get(i).getContent_type());
                map.put("imageView", feedbacks.get(i).getContent());
                map.put("textView", feedbacks.get(i).getContent());


                listItem.add(map);
            }

        } else {
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
        }
        if (!refresh) {
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
        setUpButtons();
        refreshLayout.finishRefresh();
        if ((!refresh)||feedbackSent) {
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            listView.setStackFromBottom(true);
        }
        else
        {
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
            listView.setStackFromBottom(true);
        }

    }

    private void setUpButtons() {
        ImageView buttonSendPic = (ImageView) findViewById(R.id.imageViewSendPic);
        buttonSendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                choosePic();
                dialog=PickImageDialog.build(new PickSetup()
                        .setTitle(getResources().getString(R.string.choose))
                        .setCameraButtonText(getResources().getString(R.string.camera))
                        .setGalleryButtonText(getResources().getString(R.string.gallery))
                        .setCancelText(getResources().getString(R.string.cancel))
                ).setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult pickResult) {
                        dialog.dismiss();

                    }
                }).show(FeedbackActivity.this);
            }
        });

        ImageView buttonSendText = (ImageView) findViewById(R.id.imageViewSend);
        buttonSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendText();

            }
        });
    }

    private void SendText() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        UserInfo currentUser = UserInfo.getCurrentUser(FeedbackActivity.this);
        String userId = currentUser.getId().toString();
        String token = currentUser.getToken().toString();
        final EditText textContent = (EditText) findViewById(R.id.editTextContent);
        textContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContent.setText("");
            }
        });
        String content = textContent.getText().toString();
        if(!content.trim().equals("")) {
            textContent.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textContent.getWindowToken(), 0);

            iTask.feedbackText(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, token, content, "0", "", new Callback<JSONObject>() {

                @Override
                public void success(JSONObject object, Response response) {
                    List<Feedback> feedbacks = new ArrayList<Feedback>();

                    try {
                        String realData = Utils.convertStreamToString(response.getBody().in());

                        JSONObject result = new JSONObject(realData).getJSONObject("result");
                        String code = result.get("code").toString();
                        String message = result.get("message").toString();
                        if (!code.equals("0")) {
                            Toast.makeText(FeedbackActivity.this, message, Toast.LENGTH_SHORT).show();


                        } else {

                            Feedback feedback = parseFeedbackSent(realData);
                            feedbacks.add(feedback);
                            setUpFeedbacks(feedbacks, true, true);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                }
            });
        }
        else {
            Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.no_content), Toast.LENGTH_SHORT).show();
        }

    }

    private void choosePic() {


    }

    private void updloadImage()
    {
        String endpoint = "http://oss-cn-beijing.aliyuncs.com";



    }

    public void asyncPutObjectFromLocalFile(String filePath) {


        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(token.getAccessKeyId(),token.getAccessKeySecret());

        OSS oss = new OSSClient(getApplicationContext(), CommonUtilities.OOS_ENDPOINT, credentialProvider);
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(CommonUtilities.BUCKETNAME, "textImage", filePath);

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }


    private void getTokenFromServer()
    {
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        iTask.getTokenToUploadFiles(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                token = new Token();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                    } else {
                        token = parseTokenFromString(realData);

                            FileCacheUtil.setCache(realData, FeedbackActivity.this, CommonUtilities.CACHE_FILE_TOKEN, 0);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();

            }
        });

    }

    private Token parseTokenFromString(String realData) throws JSONException {
        return null;
    }

    private void getFeedbackFromServer() {
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
                        refreshLayout.finishRefresh();

                    } else {
                        feedbacks = parseFeedbackFromString(realData);

                        if (currentPage > 1) {
                            setUpFeedbacks(feedbacks, true,false);
                        } else {
                            FileCacheUtil.setCache(realData, FeedbackActivity.this, CommonUtilities.CACHE_FILE_FEEDBACKS, 0);
                            setUpFeedbacks(feedbacks, false,false);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();

            }
        });
    }

    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_FEEDBACKS, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_FEEDBACKS);
            JSONObject object = new JSONObject(fileContent);
            String totalPages = object.getJSONObject("result").getJSONObject("data").getString("total_pages");
            if (currentPage > Integer.parseInt(totalPages)) {

                result = true;
            }
        }

        return result;

    }


}
