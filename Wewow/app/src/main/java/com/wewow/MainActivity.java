//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.wewow;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.growingio.android.sdk.collection.GrowingIO;
import com.wewow.adapter.FragmentAdapter;
import com.wewow.dto.Banner;
import com.wewow.dto.Institute;
import com.wewow.dto.LabCollection;
import com.wewow.dto.collectionCategory;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.CustomViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/3.
 */
public class MainActivity extends BaseActivity {


    private ViewPager viewPager;
    private ArrayList<View> pageview;
    private ImageView imageView;
    private ImageView[] imageViews;

    private ViewGroup group;
    private List<View> mListViews = new ArrayList<View>();
    ;
    private ViewPager viewPagerLoverOfLife;

    private ListView listViewInstituteRecommended;
    private int[] backgroundRes = {R.drawable.dummy_latest_life_institue, R.drawable.dummy_latest_life_institue};
    private String[] dummyVols = {"vol.79", "vol.64"};

    private String[] dummyTitles = {"猫奴养成计划", "手帐记录生活"};
    private String[] dummyReadCount = {"8121", "7231"};
    private String[] dummyCollectionCount = {"1203", "1232"};
    private TabLayout mTabLayout;
    private FragmentAdapter adapter;
    private Context context;

    private boolean onPauseCalled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Utils.setActivityToBeFullscreen(this);

        setContentView(R.layout.activity_main);
        context = this;
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        Utils.regitsterNetSateBroadcastReceiver(this);
//        setUpNavigationTabDummy(null);

//        setUpNavigationTab();
        if (Utils.isNetworkAvailable(this)) {
            //if banner data never cached or outdated

            checkcacheUpdatedOrNot();
        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();

            SettingUtils.set(this, CommonUtilities.NETWORK_STATE, false);
            //if banner data cached
            if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_BANNER, this)) {
                String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_BANNER);
                List<Banner> banners = new ArrayList<Banner>();
                try {
                    banners = parseBannersFromString(fileContent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setUpViewPagerBanner(banners);
            }

            //if tab title cached
            if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_TAB_TITLE, this)) {
                String fileContent = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_TAB_TITLE);
                List<collectionCategory> categories = new ArrayList<collectionCategory>();
                try {
                    categories = parseCategoriesFromString(fileContent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setUpNavigationTab(categories);
            }

        }


//        setUpScrollView();


    }

    private void checkcacheUpdatedOrNot() {
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {
            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(context, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);
                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_BANNER, context, cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getBannerInfoFromServer();
                        } else {
                            String fileContent = FileCacheUtil.getCache(MainActivity.this, CommonUtilities.CACHE_FILE_BANNER);
                            List<Banner> banners = new ArrayList<Banner>();
                            try {
                                banners = parseBannersFromString(fileContent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            setUpViewPagerBanner(banners);

                        }

                        isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_TAB_TITLE, context, cacheUpdatedTime);
                        if (isCacheDataOutdated) {
                            getTabTitlesFromServer();
                        } else {
                            String fileContent = FileCacheUtil.getCache(context, CommonUtilities.CACHE_FILE_TAB_TITLE);
                            List<collectionCategory> categories = new ArrayList<collectionCategory>();
                            try {
                                categories = parseCategoriesFromString(fileContent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            setUpNavigationTab(categories);
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


    private List<collectionCategory> parseCategoriesFromString(String fileContent) throws JSONException {


        List<collectionCategory> categories = new ArrayList<collectionCategory>();
        JSONObject jsonObject = new JSONObject(fileContent);
        JSONArray results = jsonObject.getJSONObject("result").getJSONObject("data").getJSONArray("collection_category_list");
        for (int i = 0; i < results.length(); i++) {
            collectionCategory category = new collectionCategory();
            JSONObject result = results.getJSONObject(i);
            category.setId(result.getString("id"));
            category.setTitle(result.getString("title"));
            categories.add(category);
        }
        return categories;
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (!Utils.isNetworkAvailable(this) && onPauseCalled) {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
        }

        //resendQuest
        if (!FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_TAB_TITLE, this) && Utils.isNetworkAvailable(this) && onPauseCalled) {

            getTabTitlesFromServer();

        }

        if (!FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_BANNER, this) && Utils.isNetworkAvailable(this) && onPauseCalled) {

            getBannerInfoFromServer();

        }

        onPauseCalled = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseCalled = true;
    }


    private void setUpNavigationTab(List<collectionCategory> titles) {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.home_page) + " "));
        for (int i = 0; i < titles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i).getTitle()));
        }


//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.home)));
//
//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test1)));
//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test2)));
//
//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test3)));
//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test4)));
//
//
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> ids = new ArrayList<String>();
        list.add(getResources().getString(R.string.home));
        ids.add("0");
//        list.add(getResources().getString(R.string.test1));
//        list.add(getResources().getString(R.string.test2));
//        list.add(getResources().getString(R.string.test3));
//        list.add(getResources().getString(R.string.test4));

        for (collectionCategory category : titles) {
            list.add(category.getTitle());
            ids.add(category.getId());
        }

        setUpTabs(list,ids);

    }

    private void setUpNavigationTabDummy(List<collectionCategory> titles) {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

//        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.home_page) + " "));
//        for (int i = 0; i < titles.size(); i++) {
//            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i).getTitle()));
//        }


        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.home)));

        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test2)));

        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test3)));
        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.test4)));


        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> ids = new ArrayList<String>();
        list.add(getResources().getString(R.string.home));
        ids.add("0");
        list.add(getResources().getString(R.string.test1));
        list.add(getResources().getString(R.string.test2));
        list.add(getResources().getString(R.string.test3));
        list.add(getResources().getString(R.string.test4));
//
//        for (collectionCategory category : titles) {
//            list.add(category.getTitle());
//            ids.add(category.getId());
//        }

//        setUpTabs(list,ids);

    }


    private void setUpTabs(List<String> titles,List<String> ids) {


        CustomViewPager viewPagerTabs = (CustomViewPager) findViewById(R.id.pagerTabs);
        adapter = new FragmentAdapter(getSupportFragmentManager(), titles,ids);
        viewPagerTabs.setAdapter(adapter);
        viewPagerTabs.setOffscreenPageLimit(5);
        mTabLayout.setupWithViewPager(viewPagerTabs);

        viewPager.setFocusable(true);
        viewPager.setFocusableInTouchMode(true);
        viewPager.requestFocus();

    }




    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listViewItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        viewPager.setFocusable(true);
        viewPager.setFocusableInTouchMode(true);
        viewPager.requestFocus();
    }


    private void setUpViewPagerBanner(final List<Banner> banners) {


        group = (ViewGroup) findViewById(R.id.viewGroup);


        LayoutInflater inflater = getLayoutInflater();

        pageview = new ArrayList<View>();
        for (int i = 0; i < banners.size(); i++) {

            View view = inflater.inflate(R.layout.banner_item, null);
            ImageView imageBanner = (ImageView) view.findViewById(R.id.imageViewIcon);
            TextView textViewBannerTitle = (TextView) view.findViewById(R.id.textViewBannerTitle);
            textViewBannerTitle.setText(banners.get(i).getTitle());
            Glide.with(context)
                    .load(banners.get(i).getImage())
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade(300)
                    .into(imageBanner);
            pageview.add(view);
            final int j=i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Banner banner = banners.get(j);
                    String type = banner.getType();
                    if (type.equals(CommonUtilities.BANNER_TYPE_SUBJECT)) {
                        Intent intent = new Intent(MainActivity.this, SubjectActivity.class);
                        intent.putExtra("id", banner.getId());
                        startActivity(intent);
                    } else if (type.equals(CommonUtilities.BANNER_TYPE_COLLECTION)) {
                        LabCollection lc = new LabCollection();
                        lc.image = banner.getImage();
                        lc.title = banner.getTitle();
                        lc.id = Long.parseLong(banner.getId());
                        Intent intent = new Intent(MainActivity.this, LifeLabItemActivity.class);
                        intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);

                        startActivity(intent);
                    } else if (type.equals(CommonUtilities.BANNER_TYPE_POST)) {
                        Intent intent = new Intent(MainActivity.this, LifePostActivity.class);
                        intent.putExtra(LifePostActivity.POST_ID, Integer.parseInt(banner.getId()));
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, WebPageActivity.class);
                        intent.putExtra("url", banner.getUrl());
                        startActivity(intent);

                    }
                }
            });
        }

        //
        imageViews = new ImageView[pageview.size()];
        for (int i = 0; i < pageview.size(); i++) {
            imageView = new ImageView(MainActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
            imageView.setPadding(20, 0, 20, 0);
            imageViews[i] = imageView;

            //
            if (i == 0) {
                imageViews[i].setBackgroundResource(R.drawable.dot_b);
            } else {
                imageViews[i].setBackgroundResource(R.drawable.dot);
            }

            group.addView(imageViews[i]);
        }


        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override

            public int getCount() {
                // TODO Auto-generated method stub
                return pageview.size();
            }

            @Override

            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView(pageview.get(arg1));
            }

            public Object instantiateItem(View arg0, int arg1) {
                ((ViewPager) arg0).addView(pageview.get(arg1));
                return pageview.get(arg1);
            }


        };


        //set adapter
        viewPager.setAdapter(mPagerAdapter);

        //set page change listener
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());

    }

    private void getTabTitlesFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.indexCollectionCategorys(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<collectionCategory> categories = new ArrayList<collectionCategory>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();

                    } else {
                        categories = parseCategoriesFromString(realData);
                        FileCacheUtil.setCache(realData, MainActivity.this, CommonUtilities.CACHE_FILE_TAB_TITLE, 0);

                        setUpNavigationTab(categories);

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

    private void getBannerInfoFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.banner(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<Banner> banners = new ArrayList<Banner>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();

                    } else {
                        banners = parseBannersFromString(realData);
                        FileCacheUtil.setCache(realData, MainActivity.this, CommonUtilities.CACHE_FILE_BANNER, 0);

                        setUpViewPagerBanner(banners);

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

    private List<Banner> parseBannersFromString(String realData) throws JSONException {
        List<Banner> banners = new ArrayList<Banner>();
        JSONObject jsonObject = new JSONObject(realData);
        JSONArray results = jsonObject.getJSONObject("result").getJSONArray("data");
        for (int i = 0; i < results.length(); i++) {
            Banner banner = new Banner();
            JSONObject result = results.getJSONObject(i);
            System.out.println(result.getString("image") + " " + result.getString("type") + " "
                    + result.getString("id") + " " + result.getString("title"));
            banner.setId(result.getString("id"));
            banner.setImage(result.getString("image"));
            banner.setType(result.getString("type"));
            banner.setTitle(result.getString("title"));
            if(banner.getType().equals("html"))
            {
                banner.setUrl(result.getString("url"));
            }
            banners.add(banner);
        }
        return banners;
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
//                Intent intentSearch= new Intent(MainActivity.this,SearchResultActivity.class);
//                intentSearch.putExtra("key_word",testStrings[position]);
//                startActivity(intentSearch);

            }
        });
        final Menu menuFinal=menu;
        completeText.setThreshold(0);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
//                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//                layout.setVisibility(View.GONE);

                MenuItem menuItem = menuFinal.findItem(R.id.search);
                menuItem.collapseActionView();
                Intent intentSearch= new Intent(MainActivity.this,SearchResultActivity.class);
                intentSearch.putExtra("key_word",query);
                startActivity(intentSearch);

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

    class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        //
        public void onPageSelected(int arg0) {
            // TODO Auto-generated method stub
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[arg0].setBackgroundResource(R.drawable.dot_b);
                if (arg0 != i) {
                    imageViews[i].setBackgroundResource(R.drawable.dot);
                }
            }

        }

    }



    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {


            adapter.notifyDataSetChanged();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

}
