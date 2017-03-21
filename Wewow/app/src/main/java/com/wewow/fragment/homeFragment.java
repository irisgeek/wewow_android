package com.wewow.fragment;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wewow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iris on 17/3/13.
 */
public class homeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private String[] dummyVols = {"vol.79", "vol.64"};

    private String[] dummyTitles = {"猫奴养成计划", "手帐记录生活"};
    private String[] dummyReadCount = {"8121", "7231"};
    private String[] dummyCollectionCount = {"1203", "1232"};
    private ListView listViewInstituteRecommended;
    private ViewPager viewPagerLoverOfLife;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView textViewHotArtist;
    private TextView textViewLatest;
    private TextView textViewRecommendedInstitute;
    private CardView viewLatest;


    public homeFragment() {

    }

    public static homeFragment newInstance(String text) {
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        homeFragment blankFragment = new homeFragment();
        blankFragment.setArguments(bundle);
        return blankFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                swipeRefreshLayout.setRefreshing(false);
                                                setUpViewPagerLoverOfLife();
                                                setUpListViewInstituteRecommend();

                                                //dummy effect
                                                LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.layoutHome);
                                                linearLayout.setVisibility(View.VISIBLE);

                                                startAnimation();
                                            }
                                        }, 2000);

                                    }
                                }
        );


    }

    private void startAnimation() {
        textViewHotArtist.startAnimation(moveToViewLocation(0));
        textViewLatest.startAnimation(moveToViewLocation(0));
        textViewRecommendedInstitute.startAnimation(moveToViewLocation(0));

        viewLatest.startAnimation(contentsMoveToViewLocation(100));
        viewPagerLoverOfLife.startAnimation(contentsMoveToViewLocation(100));
        listViewInstituteRecommended.startAnimation(contentsMoveToViewLocation(100));
    }

    private void initData() {

        textViewHotArtist = (TextView) getActivity().findViewById(R.id.textViewPopularArtist);
        textViewLatest = (TextView) getActivity().findViewById(R.id.textViewLatest);
        textViewRecommendedInstitute = (TextView) getActivity().findViewById(R.id.textViewSelectedInstitute);
        viewLatest = (CardView) getActivity().findViewById(R.id.cardViewLatest);


    }


    public static AnimationSet moveToViewLocation(long startOff) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);

        AnimationSet set= new AnimationSet(true);
        set.addAnimation(mHiddenAction);
        set.addAnimation(alpha);
        set.setDuration(300);
        set.setStartOffset(startOff);
        set.setFillAfter(true);
        set.setInterpolator(new AccelerateInterpolator());


        return set;
    }

    public static AnimationSet contentsMoveToViewLocation(long startOff) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        AnimationSet set= new AnimationSet(true);

        set.addAnimation(mHiddenAction);
        set.addAnimation(alpha);
        set.setStartOffset(startOff);
        set.setDuration(200);
        set.setFillAfter(true);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    public void setUpViewPagerLoverOfLife() {

        //blank view for bounce effect
        View left = new View(getActivity());
        List<View> mListViews = new ArrayList<View>();
        mListViews.add(left);

        for (int i = 0; i < 3; i++) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_lover_of_life_recommended, null);

            //to set data


            mListViews.add(view);
        }

        View right = new View(getActivity());
        mListViews.add(right);
        MyPagerAdapter myAdapter = new MyPagerAdapter();

        myAdapter.setList(mListViews);
        viewPagerLoverOfLife = (ViewPager) getActivity().findViewById(R.id.viewpagerLayout);

        viewPagerLoverOfLife.setAdapter(myAdapter);
        viewPagerLoverOfLife.setCurrentItem(1);
        viewPagerLoverOfLife.setOnPageChangeListener(new BouncePageChangeListener(
                viewPagerLoverOfLife, mListViews));
        viewPagerLoverOfLife.setPageMargin(getResources().getDimensionPixelSize(R.dimen.life_lover_recommended_page_margin));
        myAdapter.notifyDataSetChanged();


    }

    public void setUpListViewInstituteRecommend() {

        listViewInstituteRecommended = (ListView) getActivity().findViewById(R.id.listViewSelectedInstitute);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 2; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //


            map.put("textVol", dummyVols[i]);
            map.put("textTitle", dummyTitles[i]);
            map.put("textReadCount", dummyReadCount[i]);
            map.put("textCollectionCount", dummyCollectionCount[i]);

            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(), listItem,//data source
                R.layout.list_item_life_institue_recommended,

                new String[]{"textVol", "textTitle", "textReadCount", "textCollectionCount"},
                //ids
                new int[]{R.id.textViewNum, R.id.textViewTitle, R.id.textViewRead, R.id.textViewCollection}
        );
        listViewInstituteRecommended.setAdapter(listItemAdapter);
        listItemAdapter.notifyDataSetChanged();

        //fix bug created by scrollview
        fixListViewHeight(listViewInstituteRecommended);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {

        setUpViewPagerLoverOfLife();
        setUpListViewInstituteRecommend();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                swipeRefreshLayout.setRefreshing(false);
            }
        }, 6000);

    }


    private class BouncePageChangeListener implements ViewPager.OnPageChangeListener {

        private ViewPager myViewPager;
        private List<View> mListView;

        public BouncePageChangeListener(ViewPager viewPager, List<View> listView) {
            myViewPager = viewPager;
            mListView = listView;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {

            if (position == 0) {
                myViewPager.setCurrentItem(1);
            } else if (position >= 3) {

                myViewPager.setCurrentItem(3);


            }

        }

        @Override
        public void onPageSelected(int arg0) {


        }

    }


    private class MyPagerAdapter extends PagerAdapter {

        private List<View> mListViews;

        public void setList(List<View> listViews) {
            mListViews = listViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {

            if (getCount() > 1) {
                ((ViewPager) arg0).removeView(mListViews.get(arg1));
            }

        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public float getPageWidth(int position) {
            float width = (float) 0.7;
//            if (position == 0) {
//                width = (float) 1;
//            } else {
//                int last = mListViews.size() - 1;
//                if (position == last) {
//                    width = (float) 1;
//
//                } else {
//                    width = (float) 0.33;
//                }
//            }
            return width;
        }

        @Override
        public int getCount() {

            return mListViews.size();

        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {

            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);

            return mListViews.get(arg1);

        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == (arg1);

        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {

            return null;

        }


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

    }

}
