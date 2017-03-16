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

import android.app.ActionBar;
import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.wewow.R;
import com.wewow.dto.Banner;
import com.wewow.fragment.categaryFragment;
import com.wewow.fragment.homeFragment;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.Utils;
import com.wewow.view.BounceScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

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
    private MyAdapter adapter;
    private RelativeLayout loadingLayout;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        context=this;
        hideProgressBar();


        setUpNavigationTab();
        getBannerInfoFromServer();

//        setUpScrollView();


    }

    private void hideProgressBar() {
        loadingLayout = (RelativeLayout) findViewById(R.id.loading);
        loadingLayout.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        loadingLayout = (RelativeLayout) findViewById(R.id.loading);
        loadingLayout.setVisibility(View.VISIBLE);
    }


    private void setUpNavigationTab() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        List<String> titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.home));
        titles.add(getResources().getString(R.string.test1));
        titles.add(getResources().getString(R.string.test2));
        titles.add(getResources().getString(R.string.test3));
        titles.add(getResources().getString(R.string.test4));


        for (int i = 0; i < titles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        }

        setUpTabs(titles);

    }

    private void setUpTabs(List<String> titles) {
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        ViewPager viewPagerTabs = (ViewPager) findViewById(R.id.pagerTabs);
        adapter = new MyAdapter(getSupportFragmentManager(), titles);
        viewPagerTabs.setAdapter(adapter);
        viewPagerTabs.setOffscreenPageLimit(5);
        mTabLayout.setupWithViewPager(viewPagerTabs);

        viewPager.setFocusable(true);
        viewPager.setFocusableInTouchMode(true);
        viewPager.requestFocus();

    }

    private void setUpScrollView() {

        ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollView1);
        final GestureDetectorCompat mDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetectorCompat.onTouchEvent(event);
                return false;
            }
        });
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


    private void setUpViewPagerBanner(List<Banner>  banners) {


        group = (ViewGroup) findViewById(R.id.viewGroup);



        LayoutInflater inflater = getLayoutInflater();

        pageview = new ArrayList<View>();
        for (int i = 0; i < banners.size(); i++) {

            View view = inflater.inflate(R.layout.banner_item, null);
            ImageView imageBanner=(ImageView)view.findViewById(R.id.imageViewIcon);
            TextView textViewBannerTitle=(TextView)view.findViewById(R.id.textViewBannerTitle);
            textViewBannerTitle.setText(banners.get(i).getTitle());
            Glide.with(context)
                    .load(banners.get(i).getImage())
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade()
                    .into(imageBanner);
            pageview.add(view);
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

    private void getBannerInfoFromServer() {
        showProgressBar();
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
     iTask.banner(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
               List<Banner> banners=new ArrayList<Banner>();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if(!realData.contains(CommonUtilities.SUCCESS))
                    {
                        Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        JSONObject jsonObject=new JSONObject(realData);
                        JSONArray results = jsonObject.getJSONObject("result").getJSONArray("data");
                        for (int i = 0; i < results.length(); i++) {
                            Banner banner=new Banner();
                            JSONObject result = results.getJSONObject(i);
                            System.out.println(result.getString("image")+" "+result.getString("type")+" "
                                    +result.getString("id")+" "+result.getString("title"));
                            banner.setId(result.getString("id"));
                            banner.setImage(result.getString("image"));
                            banner.setType(result.getString("type"));
                            banner.setTitle(result.getString("title"));
                            banners.add(banner);
                        }
                        hideProgressBar();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (Math.abs(distanceY) > Math.abs(distanceX)) {//判断是否竖直滑动


                //是否向下滑动
                boolean isScrollDown = e1.getRawY() < e2.getRawY() ? true : false;

                if (!isScrollDown) {
                    RelativeLayout layoutBanner = (RelativeLayout) findViewById(R.id.layoutBanner);
                    layoutBanner.setVisibility(View.GONE);
                    LinearLayout layoutForScrollUp = (LinearLayout) findViewById(R.id.layoutForScrollUp);
                    layoutForScrollUp.setVisibility(View.VISIBLE);
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }


    }

    public class MyAdapter extends FragmentPagerAdapter {

        private List<String> list;

        public MyAdapter(FragmentManager fm, List<String> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            if (position != 0) {
                return categaryFragment.newInstance(list.get(position));

            }
            return homeFragment.newInstance(list.get(position));
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }
}
