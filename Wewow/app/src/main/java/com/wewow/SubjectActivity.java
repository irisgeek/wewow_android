package com.wewow;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.adapter.ListViewArtistsAdapter;
import com.wewow.adapter.RecycleViewArticlesOfArtistDetail;
import com.wewow.adapter.RecycleViewInstitutesOfSubjectAdapter;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.ArtistDetail;
import com.wewow.dto.Institute;
import com.wewow.dto.Subject;
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
public class SubjectActivity extends BaseActivity {


    private String id;
    private ImageView imageViewSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_subject);
        Intent getIntent = getIntent();
        id = getIntent.getStringExtra("id");


        if (Utils.isNetworkAvailable(this)) {

            checkcacheUpdatedOrNot();

        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();


            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            setUpSubjectFromCache();

        }


        setUpToolBar();


    }


    private void checkcacheUpdatedOrNot() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + id, SubjectActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getSubjectFromServer();
                        } else {
                            setUpSubjectFromCache();
//                            setUpListViewDummy();

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
//                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);
            }

        });


    }


    private void getSubjectFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        iTask.subject(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), id, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                Subject subject = new Subject();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        subject = parseSubjectFromString(realData);

                        FileCacheUtil.setCache(realData, SubjectActivity.this, CommonUtilities.CACHE_FILE_SUBJECT + id, 0);
                        setUpSuject(subject);


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(SubjectActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
//                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void setUpSuject(final Subject subject) {

        ImageView imageView = (ImageView) findViewById(R.id.imageViewTop);
        Glide.with(this)

                .load(subject.getImage()).crossFade().fitCenter().placeholder(R.drawable.banner_loading_spinner).placeholder(R.drawable.banner_loading_spinner).into(imageView);

        TextView textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewTitle.setText(subject.getTitle());

        TextView textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewDate.setText(subject.getDate());

        TextView textViewContent = (TextView) findViewById(R.id.textViewContent);
        textViewContent.setText(subject.getContent());



        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        List<List<Institute>> institutes = subject.getInstitutes();
        List<String> institutesDescs=subject.getInstituteDescs();



        for (int i = 0; i < institutes.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("list", institutes.get(i));

            map.put("content", institutesDescs.get(i));
            //todo
            listItem.add(map);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.listViewInstitutes);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(new RecycleViewInstitutesOfSubjectAdapter(this,
                listItem));


    }



    private void setUpSubjectFromCache() {

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SUBJECT + id, this)) {
            String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_SUBJECT + id);
            Subject subject = new Subject();
            try {
                subject = parseSubjectFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setUpSuject(subject);
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
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.selector_btn_back);
//        getSupportActionBar().setTitle(getResources().getString(R.string.all_artists_title));

    }

    private Subject parseSubjectFromString(String realData) throws JSONException {

        Subject subject = new Subject();
        JSONObject object = new JSONObject(realData);
        JSONObject result = object.getJSONObject("result").getJSONObject("data");
        subject.setTitle(result.getString("title"));
        subject.setContent(result.getString("content"));
        subject.setDate(result.getString("date"));
        subject.setImage(result.getString("image"));

        List<List<Institute>> institues = new ArrayList<List<Institute>>();
        List<String> instituteDescs = new ArrayList<String>();


        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("collection_sections");

        for (int i = 0; i < array.length(); i++) {
            List<Institute> instituteList = new ArrayList<Institute>();
            String desc = "";


            JSONObject json = array.getJSONObject(i);
            JSONArray collections = json.getJSONArray("collections");
            for (int j = 0; j < collections.length(); j++) {
                Institute institute = new Institute();
                JSONObject collectionsJSONObject = collections.getJSONObject(j);
                institute.setId(collectionsJSONObject.getString("collection_id"));
                institute.setTitle(collectionsJSONObject.getString("collection_title"));
                institute.setOrder(collectionsJSONObject.getString("order"));
                institute.setImage(collectionsJSONObject.getString("image_642_320"));
                institute.setRead_count(collectionsJSONObject.getString("read_count"));
                institute.setLiked_count(collectionsJSONObject.getString("liked_count"));

                instituteList.add(institute);
            }

            desc = json.getString("content");
            institues.add(instituteList);
            instituteDescs.add(desc);
        }
        subject.setInstituteDescs(instituteDescs);
        subject.setInstitutes(institues);

        return subject;
    }


}
