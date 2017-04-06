package com.wewow.fragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wewow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iris on 17/3/13.
 */
public class categaryFragment  extends Fragment {



    private String[] dummyVols = {"vol.79", "vol.64"};

    private String[] dummyTitles = {"猫奴养成计划", "手帐记录生活"};
    private String[] dummyReadCount = {"8121", "7231"};
    private String[] dummyCollectionCount = {"1203", "1232"};
    private ViewPager viewPagerLoverOfLife;
    private ListView listViewInstituteRecommended;
    private SwipeRefreshLayout swipeRefreshLayout
            ;
    private View view;


    public categaryFragment() {

    }

    public static categaryFragment newInstance(String text){
        Bundle bundle = new Bundle();
        bundle.putString("text",text);
        categaryFragment blankFragment = new categaryFragment();
        blankFragment.setArguments(bundle);
        return  blankFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_other_categary, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);

//        swipeRefreshLayout.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        swipeRefreshLayout.setRefreshing(true);
//
//                                    }
//                                }
//        );

        setUpViewPagerLoverOfLife(view);
        setUpListViewInstituteRecommend(view);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setUpViewPagerLoverOfLife(View viewRoot) {

        //blank view for bounce effect
        View left = new View(viewRoot.getContext());
        List<View> mListViews = new ArrayList<View>();
        mListViews.add(left);

        for (int i = 0; i < 3; i++) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_lover_of_life_recommended, null);

            //to set data


            mListViews.add(view);
        }

        View right = new View(viewRoot.getContext());
        mListViews.add(right);
        MyPagerAdapter myAdapter = new MyPagerAdapter();

        myAdapter.setList(mListViews);
      viewPagerLoverOfLife = (ViewPager)viewRoot.findViewById(R.id.viewpagerLayoutCategory);

        viewPagerLoverOfLife.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
        viewPagerLoverOfLife.setCurrentItem(1);
        viewPagerLoverOfLife.setOnPageChangeListener(new BouncePageChangeListener(
                viewPagerLoverOfLife, mListViews));
        viewPagerLoverOfLife.setPageMargin(getResources().getDimensionPixelSize(R.dimen.life_lover_recommended_page_margin));


    }
    public void setUpListViewInstituteRecommend(View view) {

       listViewInstituteRecommended = (ListView) view.findViewById(R.id.listViewSelectedInstituteCategory);

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

        SimpleAdapter listItemAdapter = new SimpleAdapter(view.getContext(), listItem,//data source
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
