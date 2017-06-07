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

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.growingio.android.sdk.collection.GrowingIO;
import com.jaeger.library.StatusBarUtil;
import com.wewow.adapter.FragmentAdapter;
import com.wewow.adapter.ListSearchAdapter;
import com.wewow.dto.Banner;
import com.wewow.dto.Institute;
import com.wewow.dto.LabCollection;
import com.wewow.dto.collectionCategory;
import com.wewow.netTask.ITask;
import com.wewow.utils.AppBarStateChangeListener;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.NetStateUtils;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.CustomViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/3.
 */
public class MainActivity extends BaseActivity implements TextWatcher {


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
    private ImageView imageViewHome, layoutMenu;
    private ImageView imageViewSearch, layoutSearch;
    private TextView textTitle;
    private AutoCompleteTextView searchView;
    private boolean isSearchViewShown = false;
    private boolean isAppBarFolded = false;
    private Toolbar toolbar;
    private List<String> hotWords;
    private boolean resetDropdownOffset = false;
    public LinearLayout progressBar;
    private ImageView imageViewUnderLine;
    private Field field;
    private final BroadcastReceiver mybroadcast = new NetStateUtils();
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout mAppBarLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Utils.setActivityToBeFullscreen(this);
        setMenuselectedPosition(0);
        setContentView(R.layout.activity_main);

        context = this;
//        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 100);

        if (android.os.Build.VERSION.SDK_INT > 18) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                         window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //设置根布局的内边距
//                         CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.main_content);
//             if(checkDeviceHasNavigationBar(this))
//             {
//                 layout.setPadding(0,0, 0,getVirtualBarHeigh() );
//             }
//             else
//             {
//                 layout.setPadding(0, 0, 0, 0);
//
//             }
        }

//        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                hideBottomUIMenu();
//            }
//        });
//        this.hideBottomUIMenu();
        progressBar = (LinearLayout) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);


        viewPager = (ViewPager) findViewById(R.id.viewPager);
        float density = Utils.getSceenDensity(this);
//        Utils.regitsterNetSateBroadcastReceiver(this);
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getResources().getString(R.string.home));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.font_color));


        initAppBar();
//        setUpNavigationTabDummy(null);

//        setUpNavigationTab();

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
            if (Utils.isNetworkAvailable(this)) {
                setUpNavigationTabTitle(categories);
            } else {
                setUpNavigationTab(categories);
            }

        }
        if (Utils.isNetworkAvailable(this)) {
            //if banner data never cached or outdated

            checkcacheUpdatedOrNot();
        } else {
            Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
        }


//        }

        setUpToolBar();
//        setUpScrollView();


    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);

        }
    }


    private void initAppBar() {
        imageViewHome = (ImageView) findViewById(R.id.btnBack);
        imageViewSearch = (ImageView) findViewById(R.id.btnSearch);
        textTitle = (TextView) findViewById(R.id.textTitle);
        imageViewUnderLine = (ImageView) findViewById(R.id.imageViewUnderlineOfSearchView);

        searchView = (AutoCompleteTextView) findViewById(R.id.editTextSearch);
        layoutMenu = (ImageView) findViewById(R.id.layoutMenu);
        layoutSearch = (ImageView) findViewById(R.id.layoutSearch);
        layoutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isSearchViewShown) {
                    drawerLayout.openDrawer(GravityCompat.START);

                } else {
                    removeCover();
                    searchView.setVisibility(View.INVISIBLE);
                    imageViewUnderLine.setVisibility(View.INVISIBLE);
                    searchView.setText("");
                    resetDropdownOffset = true;
                    imageViewHome.setImageResource(R.drawable.menu);
                    isSearchViewShown = false;
                    if (isAppBarFolded) {
                        textTitle.setVisibility(View.VISIBLE);
                        imageViewHome.setImageResource(R.drawable.menu_b);
                    }
                }

            }
        });


        layoutSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String queryText = searchView.getText().toString().trim();
                if (!isSearchViewShown) {
                    if (isAppBarFolded) {
                        imageViewHome.setImageResource(R.drawable.back_b);
                    } else {
                        imageViewHome.setImageResource(R.drawable.back);
                    }

                    ListSearchAdapter adapter = new ListSearchAdapter(hotWords, MainActivity.this);
//
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item_search, R.id.text, hotWords);


                    searchView.setAdapter(adapter);
                    searchView.setHint(getResources().getString(R.string.search_hint));

                    searchView.setThreshold(0);
                    if (resetDropdownOffset) {
//                        searchView.setDropDownVerticalOffset(40);
//                        resetDropdownOffset = false;
                    }
                    showUnderLine();

                    showCover();
                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            //execute the task
                            searchView.setVisibility(View.VISIBLE);
                            textTitle.setVisibility(View.GONE);
                            searchView.requestFocus();
                            InputMethodManager inputManager =
                                    (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.showSoftInput(searchView, 0);
                        }
                    }, 100);

                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            //execute the task
                            searchView.showDropDown();
                        }
                    }, 200);

                    searchView.addTextChangedListener(MainActivity.this);
                    searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position != 0) {
                                searchView.setText(hotWords.get(position), true);
                                layoutSearch.performClick();
                            } else {
                                searchView.setText("");
                            }
//

                        }
                    });
                    searchView.setThreshold(0);

                    isSearchViewShown = true;
                } else {


                    if (!queryText.equals("")) {
                        searchView.setText("");
                        searchView.setVisibility(View.INVISIBLE);
                        imageViewUnderLine.setVisibility(View.INVISIBLE);
                        Intent intentSearch = new Intent(MainActivity.this, SearchResultActivity.class);
                        intentSearch.putExtra("key_word", queryText);
                        startActivity(intentSearch);
                        if (isAppBarFolded) {
                            imageViewHome.setImageResource(R.drawable.menu_b);
                        } else {
                            imageViewHome.setImageResource(R.drawable.menu);
                        }

                        isSearchViewShown = false;
                        if (isAppBarFolded) {
                            textTitle.setVisibility(View.VISIBLE);
                        }
                    } else {

                        if (isAppBarFolded) {
                            imageViewHome.setImageResource(R.drawable.back_b);
                        } else {
                            imageViewHome.setImageResource(R.drawable.back);
                        }
                        searchView.setHint(getResources().getString(R.string.search_hint));

                        final String[] testStrings = getResources().getStringArray(R.array.test_array);
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item_search, R.id.text, hotWords);
                        ListSearchAdapter adapter = new ListSearchAdapter(hotWords, MainActivity.this);
                        searchView.setAdapter(adapter);
                        searchView.requestFocus();

                        if (resetDropdownOffset) {
//                            resetDropdownOffset = false;
//                            searchView.setDropDownVerticalOffset(-40);
                        }
//                        searchView.setDropDownVerticalOffset(-40);
                        showUnderLine();

                        showCover();
                        new Handler().postDelayed(new Runnable() {

                            public void run() {
                                //execute the task
                                searchView.setVisibility(View.VISIBLE);
                                textTitle.setVisibility(View.GONE);
                                searchView.requestFocus();
                                InputMethodManager inputManager =
                                        (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.showSoftInput(searchView, 0);
                            }
                        }, 100);

                        new Handler().postDelayed(new Runnable() {

                            public void run() {
                                //execute the task
                                searchView.showDropDown();
                            }
                        }, 200);
                        searchView.addTextChangedListener(MainActivity.this);
                        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position != 0) {
                                    searchView.setText(hotWords.get(position), true);
                                    imageViewSearch.performClick();
                                }
//

                            }
                        });
                        searchView.setThreshold(0);

                        isSearchViewShown = true;
                    }

                }

            }
        });


        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        if(Build.VERSION.SDK_INT>=21) {
            mAppBarLayout.setNestedScrollingEnabled(false);
        }
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                Log.d("STATE", state.name());
                if (state == State.EXPANDED) {
                    searchView.setTextColor(getResources().getColor(R.color.search_text_view_color));
                    searchView.setHintTextColor(getResources().getColor(R.color.search_text_view_hint_color));

                    imageViewHome.setImageResource(R.drawable.menu);
                    imageViewSearch.setImageResource(R.drawable.search);
                    textTitle.setVisibility(View.GONE);
                    searchView.setVisibility(View.INVISIBLE);
                    imageViewUnderLine.setVisibility(View.INVISIBLE);
                    imageViewUnderLine.setImageResource(R.color.search_text_view_hint_color);
                    isAppBarFolded = false;
                    toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    //展开状态

                } else if (state == State.COLLAPSED) {
                    searchView.setTextColor(getResources().getColor(R.color.font_color));
                    searchView.setHintTextColor(getResources().getColor(R.color.search_hot_search));
                    imageViewHome.setImageResource(R.drawable.menu_b);
                    imageViewSearch.setImageResource(R.drawable.search_b);

                    textTitle.setVisibility(View.VISIBLE);
                    resetDropdownOffset = true;
                    searchView.setVisibility(View.INVISIBLE);
                    imageViewUnderLine.setVisibility(View.INVISIBLE);
                    imageViewUnderLine.setImageResource(R.color.search_hot_search);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.white));


                    isAppBarFolded = true;

                    //折叠状态

                } else {
                    toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    //中间状态

                }
            }
        });

    }

    private void showUnderLine() {

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.search_view_underline_anim);

        imageViewUnderLine.setVisibility(View.VISIBLE);
        imageViewUnderLine.startAnimation(animation);

    }

    private void showCover() {

        RelativeLayout layoutCover = (RelativeLayout) findViewById(R.id.layoutCover);
        layoutCover.setVisibility(View.VISIBLE);
        layoutCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                searchView.setVisibility(View.INVISIBLE);
//                if(isAppBarFolded)
//                {
//                    textTitle.setVisibility(View.VISIBLE);
//                }
                removeCover();
            }
        });
    }

    private void removeCover() {

        RelativeLayout layoutCover = (RelativeLayout) findViewById(R.id.layoutCover);
        layoutCover.setVisibility(View.GONE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);


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
//                            String fileContent = FileCacheUtil.getCache(MainActivity.this, CommonUtilities.CACHE_FILE_BANNER);
//                            List<Banner> banners = new ArrayList<Banner>();
//                            try {
//                                banners = parseBannersFromString(fileContent);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            setUpViewPagerBanner(banners);

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

                        isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_HOT_WORDS, context, cacheUpdatedTime);
                        if (isCacheDataOutdated) {
                            getSearhHotWordsFromServer();
                        } else {
                            String fileContent = FileCacheUtil.getCache(context, CommonUtilities.CACHE_FILE_HOT_WORDS);

                            try {
                                hotWords = parseHotwordsFromString(fileContent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
        regitsterNetSateBroadcastReceiver(this);

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
        unregisterReceiver(mybroadcast);
    }


    private void setUpNavigationTab(List<collectionCategory> titles) {
        setUpNavigationTabTitle(titles);


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

        setUpTabs(list, ids);

    }

    private void setUpNavigationTabTitle(List<collectionCategory> titles) {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addTab(mTabLayout.newTab().setText(" " + getResources().getString(R.string.home_page) + " "));
        for (int i = 0; i < titles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i).getTitle()));
        }
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


    private void setUpTabs(List<String> titles, List<String> ids) {


        CustomViewPager viewPagerTabs = (CustomViewPager) findViewById(R.id.pagerTabs);
        adapter = new FragmentAdapter(getSupportFragmentManager(), titles, ids);
        viewPagerTabs.setAdapter(adapter);
        viewPagerTabs.setOffscreenPageLimit(5);
        mTabLayout.setupWithViewPager(viewPagerTabs);
        progressBar.setVisibility(View.GONE);
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
            view.setClickable(true);
            view.setFocusable(true);
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        AppBarLayout.LayoutParams mParams = (AppBarLayout.LayoutParams) mAppBarLayout.getChildAt(0).getLayoutParams();
                        mParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED |
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                    }
                    else
                    {

                        AppBarLayout.LayoutParams mParams = (AppBarLayout.LayoutParams) mAppBarLayout.getChildAt(0).getLayoutParams();
                        mParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED |
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                    }
                }
            });

            pageview.add(view);
            final int j = i;
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

        group.removeAllViews();
        //
        imageViews = new ImageView[pageview.size()];
        for (int i = 0; i < pageview.size(); i++) {
            imageView = new ImageView(MainActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setPadding(12, 0, 12, 0);
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
        if(Build.VERSION.SDK_INT>=21) {
            viewPager.setNestedScrollingEnabled(false);
        }

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
            if (banner.getType().equals("html")) {
                banner.setUrl(result.getString("url"));
            }
            banners.add(banner);
        }
        return banners;
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
//                if(position!=0) {
//                    searchView.setQuery(testStrings[position], true);
//                }
////                Intent intentSearch= new Intent(MainActivity.this,SearchResultActivity.class);
////                intentSearch.putExtra("key_word",testStrings[position]);
////                startActivity(intentSearch);
//
//            }
//        });
//        final Menu menuFinal=menu;
//        completeText.setThreshold(0);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
////                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
////                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
////                layout.setVisibility(View.GONE);
//
//                MenuItem menuItem = menuFinal.findItem(R.id.search);
//                menuItem.collapseActionView();
//                Intent intentSearch= new Intent(MainActivity.this,SearchResultActivity.class);
//                intentSearch.putExtra("key_word",query);
//                startActivity(intentSearch);
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

    private void setUpToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(null);

    }


    private void getSearhHotWordsFromServer() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.getHotSearchWords(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();

                    } else {
                        hotWords = parseHotwordsFromString(realData);
                        FileCacheUtil.setCache(realData, MainActivity.this, CommonUtilities.CACHE_FILE_HOT_WORDS, 0);


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

    private ArrayList<String> parseHotwordsFromString(String realData) throws JSONException {

        ArrayList<String> words = new ArrayList<String>();
        String s = new JSONObject(realData).getJSONObject("result")
                .getJSONObject("data")
                .getString("hot_words").toString();
        s = s.substring(2, s.length() - 2);
        String[] list = s.split("\",\"");
        for (int i = 0; i < list.length + 1; i++) {
            if (i == 0) {
                words.add(getResources().getString(R.string.hot_search_text));
            } else {
                words.add(list[i - 1]);
            }
        }

        return words;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub
        RelativeLayout layoutCover = (RelativeLayout) findViewById(R.id.layoutCover);
        layoutCover.setVisibility(View.GONE);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        RelativeLayout layoutCover = (RelativeLayout) findViewById(R.id.layoutCover);
        layoutCover.setVisibility(View.GONE);

    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        RelativeLayout layoutCover = (RelativeLayout) findViewById(R.id.layoutCover);
        layoutCover.setVisibility(View.GONE);
    }


    public int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    // 获取ActionBar的高度
    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * 获取虚拟功能键高度
     */
    public int getVirtualBarHeigh() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }

    public void regitsterNetSateBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mybroadcast, filter);
    }


}
