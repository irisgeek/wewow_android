package com.wewow;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.wewow.adapter.FragmentAdapter;
import com.wewow.adapter.FragmentSearchResultAdapter;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.Banner;
import com.wewow.dto.Institute;
import com.wewow.dto.Post;
import com.wewow.dto.collectionCategory;
import com.wewow.fragment.searchResultListFragment;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.InvalidMarkException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/4/16.
 */
public class SearchResultActivity extends BaseActivity {
    private String keyword = "";
    private FragmentSearchResultAdapter adapter;
    private ArrayList<ArrayList<HashMap<String, Object>>> list;
    private boolean refresh=false;
    public LinearLayout  progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.menu_checked_color));

        Intent intent = getIntent();
        keyword = intent.getExtras().getString("key_word");
        progressBar=(LinearLayout)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (Utils.isNetworkAvailable(this)) {
            //if banner data never cached or outdated

            checkcacheUpdatedOrNot();
        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();

            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            //if banner data cached
            if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword, this)) {
                String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword);
                List<Article> articles = new ArrayList<Article>();
                List<Institute> institutes = new ArrayList<Institute>();
                List<Artist> artists = new ArrayList<Artist>();
                List<Post> posts = new ArrayList<Post>();

                try {
                    articles = parseArticleFromString(fileContent);
                    institutes = parseInstitutesFromString(fileContent);
                    artists = parseArtistsFromString(fileContent);
                    posts = parsePostFromString(fileContent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setUpViewPager(articles, institutes, artists, posts);
            }
        }
        setUpToolBar();
//        setUpTabs();

    }

    private void setUpViewPager(List<Article> articles, List<Institute> institutes, List<Artist> artists, List<Post> posts) {

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        List<String> categories= new ArrayList<String>();
        categories.add(CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTICLE);
        categories.add(CommonUtilities.RESEARCH_RESULT_CATEGORY_INSTITUTE);
        categories.add(CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTIST);
        categories.add(CommonUtilities.RESEARCH_RESULT_CATEGORY_POST);


       list= new ArrayList<ArrayList<HashMap<String, Object>>>();

        ArrayList<HashMap<String, Object>> listItemArticle= new ArrayList<HashMap<String, Object>>();


        for (int i = 0; i < articles.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //
            map.put("id",articles.get(i).getId());
            map.put("image", articles.get(i).getImage_642_320());

            map.put("title", articles.get(i).getTitle());
            //todo
            listItemArticle.add(map);
        }

        list.add(listItemArticle);


        ArrayList<HashMap<String, Object>> listItemInstitute = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < institutes.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            Institute institute = institutes.get(i);
            map.put("id",institute.getId());
            map.put("imageView", institute.getImage());
            map.put("textViewNum",getResources().getString(R.string.number_refix) + institutes.get(i).getOrder());
            map.put("textViewTitle", institutes.get(i).getTitle());
            map.put("textViewRead", institutes.get(i).getRead_count());
            map.put("textViewCollection", institutes.get(i).getLiked_count());

            listItemInstitute.add(map);
        }

        list.add(listItemInstitute);


        ArrayList<HashMap<String, Object>> listItemArtist = new ArrayList<HashMap<String, Object>>();


        for (int i = 0; i < artists.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("imageView", artists.get(i).getImage());

            map.put("textViewName", artists.get(i).getNickname());
            map.put("textViewDesc", artists.get(i).getDesc());
            map.put("textViewArticleCount", artists.get(i).getArticle_count());
            map.put("textViewFollowerCount", artists.get(i).getFollower_count());
            map.put("id",artists.get(i).getId());

            listItemArtist.add(map);
        }
        list.add(listItemArtist);

        ArrayList<HashMap<String, Object>> listItemPost = new ArrayList<HashMap<String, Object>>();


        for (int i = 0; i < posts.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("imageView", posts.get(i).getImage_642_320());

            map.put("textViewTitle", posts.get(i).getTitle());
            map.put("id",posts.get(i).getId());

            listItemPost.add(map);
        }
        list.add(listItemPost);

        ArrayList<String> listTitle = new ArrayList<String>();
        listTitle.add(getResources().getString(R.string.search_result_category_1));
        listTitle.add(getResources().getString(R.string.search_result_category_2));
        listTitle.add(getResources().getString(R.string.search_result_category_3));
        listTitle.add(getResources().getString(R.string.search_result_category_4));

        List<searchResultListFragment> fgs = new ArrayList<searchResultListFragment>();
        for(int i=0;i<categories.size();i++)
        {
            searchResultListFragment fragment=searchResultListFragment.newInstance(categories.get(i), list.get(i));
            fgs.add(fragment);
        }

        if(!refresh) {

            adapter = new FragmentSearchResultAdapter(getSupportFragmentManager(), list, categories, listTitle,fgs);
            viewPager.setAdapter(adapter);
        }
        else {
            adapter.setFragments(fgs);
            adapter.notifyDataSetChanged();
        }




        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_1)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_2)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_3)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_4)));
        tabLayout.setupWithViewPager(viewPager);

        progressBar.setVisibility(View.GONE);

    }

    private List<Post> parsePostFromString(String fileContent) throws JSONException{
        List<Post> posts = new ArrayList<Post>();

        JSONObject object = new JSONObject(fileContent);
        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("daily_topics");

        for (int i = 0; i < array.length(); i++) {
            Post post = new Post();

            JSONObject result = array.getJSONObject(i);
            post.setId(result.getString("id"));
            post.setImage_642_320(result.getString("image_642_320"));
            post.setTitle(result.getString("title"));

            posts.add(post);
        }
        return  posts;

    }

    private List<Artist> parseArtistsFromString(String fileContent) throws JSONException{

        List<Artist> artists = new ArrayList<Artist>();

        JSONObject object = new JSONObject(fileContent);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("artists");
        for (int i = 0; i < results.length(); i++) {
            Artist artist = new Artist();
            JSONObject result = results.getJSONObject(i);
            artist.setId(result.getString("id"));
            artist.setNickname(result.getString("nickname"));
            artist.setDesc(result.getString("desc"));
            artist.setImage(result.getString("image_120_120"));
            artist.setArticle_count(result.getString("article_count"));
            artist.setFollower_count(result.getString("follow_count"));

            artists.add(artist);
        }

        return artists;
    }

    private List<Institute> parseInstitutesFromString(String fileContent) throws JSONException{
        List<Institute> institutes = new ArrayList<Institute>();

        JSONObject object = new JSONObject(fileContent);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("collections");
        for (int i = 0; i < results.length(); i++) {
            Institute institute = new Institute();
            JSONObject result = results.getJSONObject(i);
            institute.setId(result.getString("collection_id"));
            institute.setTitle(result.getString("collection_title"));
            institute.setOrder(result.getString("order"));
            institute.setImage(result.getString("image_642_320"));
            institute.setRead_count(result.getString("read_count"));
            institute.setLiked_count(result.getString("liked_count"));


            institutes.add(institute);
        }

        return institutes;
    }

    private List<Article> parseArticleFromString(String fileContent) throws JSONException {
        List<Article> articles = new ArrayList<Article>();

        JSONObject object = new JSONObject(fileContent);
        JSONArray array = object.getJSONObject("result").getJSONObject("data").getJSONArray("articles");

        for (int i = 0; i < array.length(); i++) {
            Article article = new Article();

            JSONObject result = array.getJSONObject(i);
            article.setId(result.getString("id"));
            article.setImage_642_320(result.getString("image_642_320"));
            article.setTitle(result.getString("title"));

            articles.add(article);
        }
        return  articles;
    }

    private void checkcacheUpdatedOrNot() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(SearchResultActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword, SearchResultActivity.this, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getSearchInfoFromServer();
                        } else {
                            String fileContent = FileCacheUtil.getCache(SearchResultActivity.this, CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword);
                            List<Banner> banners = new ArrayList<Banner>();
                            List<Article> articles = new ArrayList<Article>();
                            List<Institute> institutes = new ArrayList<Institute>();
                            List<Artist> artists = new ArrayList<Artist>();
                            List<Post> posts = new ArrayList<Post>();

                            try {
                                articles = parseArticleFromString(fileContent);
                                institutes = parseInstitutesFromString(fileContent);
                                artists = parseArtistsFromString(fileContent);
                                posts = parsePostFromString(fileContent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            setUpViewPager(articles, institutes, artists, posts);

                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {

            }

        });

    }

    private void getSearchInfoFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.search(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), keyword, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(SearchResultActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    } else {

                        FileCacheUtil.setCache(realData, SearchResultActivity.this, CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword, 0);


                        List<Article> articles = new ArrayList<Article>();
                        List<Institute> institutes = new ArrayList<Institute>();
                        List<Artist> artists = new ArrayList<Artist>();
                        List<Post> posts = new ArrayList<Post>();


                        articles = parseArticleFromString(realData);
                        institutes = parseInstitutesFromString(realData);
                        artists = parseArtistsFromString(realData);
                        posts = parsePostFromString(realData);


                        setUpViewPager(articles, institutes, artists, posts);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("MainActivity", "request banner failed: " + error.toString());

            }
        });
    }

    private void setUpTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_1)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_2)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_3)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.search_result_category_4)));
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setUpToolBar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.selector_btn_back);
//        getSupportActionBar().setTitle(keyword);
        final EditText editText=(EditText)findViewById(R.id.editTextSearch);
        editText.setText(keyword);
        editText.setSelection(editText.length());
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_DONE) {
                   search(editText);
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                return false;
            }
        });
        ImageView imageViewSearch=(ImageView)findViewById(R.id.btnSearch);
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                search(editText);



            }
        });
        ImageView imageViewBack=(ImageView)findViewById(R.id.btnBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

    }

    private void search(EditText editText) {
        keyword = editText.getText().toString().trim();
        refresh=true;
        progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(SearchResultActivity.this)) {
            //if banner data never cached or outdated

            getSearchInfoFromServer();
        } else {
            Toast.makeText(SearchResultActivity.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();

            SettingUtils.set(SearchResultActivity.this, CommonUtilities.NETWORK_STATE, false);
            //if banner data cached
            if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword, SearchResultActivity.this)) {
                String fileContent = FileCacheUtil.getCache(SearchResultActivity.this, CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword);
                List<Article> articles = new ArrayList<Article>();
                List<Institute> institutes = new ArrayList<Institute>();
                List<Artist> artists = new ArrayList<Artist>();
                List<Post> posts = new ArrayList<Post>();

                try {
                    articles = parseArticleFromString(fileContent);
                    institutes = parseInstitutesFromString(fileContent);
                    artists = parseArtistsFromString(fileContent);
                    posts = parsePostFromString(fileContent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setUpViewPager(articles, institutes, artists, posts);
            }
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new searchResultListFragment(), getResources().getString(R.string.search_result_category_1));
        adapter.addFragment(new searchResultListFragment(), getResources().getString(R.string.search_result_category_2));
        adapter.addFragment(new searchResultListFragment(), getResources().getString(R.string.search_result_category_3));
        adapter.addFragment(new searchResultListFragment(), getResources().getString(R.string.search_result_category_4));
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.search) {
////            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
////            layout.setVisibility(View.VISIBLE);
//            return true;
//        }
//        if(id==android.R.id.home) {
//            finish();
//            return true;
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
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
//
//        completeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                searchView.setQuery(testStrings[position], true);
//
//                keyword = testStrings[position];
//
//
//
//            }
//        });
//
//        final Menu menuFinal=menu;
//        completeText.setThreshold(0);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                keyword = query;
//                refresh=true;
//                MenuItem menuItem = menuFinal.findItem(R.id.search);
//                menuItem.collapseActionView();
//                getSupportActionBar().setTitle(keyword);
//                progressBar.setVisibility(View.VISIBLE);
//                if (Utils.isNetworkAvailable(SearchResultActivity.this)) {
//                    //if banner data never cached or outdated
//
//                    getSearchInfoFromServer();
//                } else {
//                    Toast.makeText(SearchResultActivity.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
//
//                    SettingUtils.set(SearchResultActivity.this, CommonUtilities.NETWORK_STATE, false);
//                    //if banner data cached
//                    if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword, SearchResultActivity.this)) {
//                        String fileContent = FileCacheUtil.getCache(SearchResultActivity.this, CommonUtilities.CACHE_FILE_SEARCH_RESULT + keyword);
//                        List<Article> articles = new ArrayList<Article>();
//                        List<Institute> institutes = new ArrayList<Institute>();
//                        List<Artist> artists = new ArrayList<Artist>();
//                        List<Post> posts = new ArrayList<Post>();
//
//                        try {
//                            articles = parseArticleFromString(fileContent);
//                            institutes = parseInstitutesFromString(fileContent);
//                            artists = parseArtistsFromString(fileContent);
//                            posts = parsePostFromString(fileContent);
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        setUpViewPager(articles, institutes, artists, posts);
//                    }
//                }
//
//
//                return true;
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
//        return true;
//    }

}
