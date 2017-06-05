package com.wewow.fragment;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.DetailArtistActivity;
import com.wewow.LifeLabItemActivity;
import com.wewow.R;
import com.wewow.adapter.ListViewAdapter;
import com.wewow.adapter.RecycleViewArticlesOfArtistDetail;
import com.wewow.adapter.RecycleViewArtistsOfHomePageAdapter;
import com.wewow.adapter.RecycleViewInstitutesOfSearchResultAdapter;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.Institute;
import com.wewow.dto.LabCollection;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoadMoreListener;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.CircleImageView;
import com.wewow.view.RecyclerViewUpRefresh;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/13.
 */
public class categaryFragment  extends Fragment implements LoadMoreListener {



    private String[] dummyVols = {"vol.79", "vol.64"};

    private String[] dummyTitles = {"猫奴养成计划", "手帐记录生活"};
    private String[] dummyReadCount = {"8121", "7231"};
    private String[] dummyCollectionCount = {"1203", "1232"};
    private ViewPager viewPagerLoverOfLife;
    private ListView listViewInstituteRecommended;

    private View view;
    private String categoryId="";
    private RecyclerView rv;
    private String totalPages;
    private int currentPage=1;
    private RecyclerViewUpRefresh rvInstitue;
    private ArrayList<HashMap<String, Object>> listItem;
    private RecycleViewInstitutesOfSearchResultAdapter adapter;
    private boolean refresh=false;

    private TextView textViewArtist;
    private TextView textViewInstitute;

    public categaryFragment() {

    }

    public static categaryFragment newInstance(String text){
        Bundle bundle = new Bundle();
        bundle.putString("id",text);
        categaryFragment blankFragment = new categaryFragment();
        blankFragment.setArguments(bundle);
        return  blankFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            categoryId = bundle.getString("id");
        }
        view= inflater.inflate(R.layout.fragment_other_categary, container, false);
        initData();

//        swipeRefreshLayout.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        swipeRefreshLayout.setRefreshing(true);
//
//                                    }
//                                }
//        );


        if (Utils.isNetworkAvailable(getActivity())) {

            checkCacheUpdatedOrNot();

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();


            SettingUtils.set(getActivity(), CommonUtilities.NETWORK_STATE, false);

            setUpArtistsAndInstituesFromCache(view);
        }
//        setUpViewPagerLoverOfLife(view);
//        setUpListViewInstituteRecommend(view);
        return view;

    }

    private void initData() {
        listItem = new ArrayList<HashMap<String, Object>>();

        rv = (RecyclerView) view.findViewById(R.id.recyclerview_artists);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(linearLayoutManager);
        rv.setNestedScrollingEnabled(false);
        rvInstitue = (RecyclerViewUpRefresh)view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(rv.getContext());

        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        rvInstitue.setLayoutManager(layoutManager);
        rvInstitue.setNestedScrollingEnabled(false);
        rvInstitue.setCanloadMore(true);
        rvInstitue.setLoadMoreListener(this);

        textViewArtist=(TextView)view.findViewById(R.id.textViewArtist);
        textViewInstitute=(TextView)view.findViewById(R.id.textViewLifeLab);

        NestedScrollView scroller = (NestedScrollView) view.findViewById(R.id.scrollview);

        if (scroller != null) {

            scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (scrollY > oldScrollY) {
                    }
                    if (scrollY < oldScrollY) {
                    }

                    if (scrollY == 0) {
                    }

                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                       rvInstitue.setNestedScrollingEnabled(true);
                    }
                }
            });
        }

    }

    public static AnimationSet moveToViewLocation(long startOff) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.2f, Animation.RELATIVE_TO_SELF, 0.0f);

        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);

        AnimationSet set = new AnimationSet(true);
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
                0.2f, Animation.RELATIVE_TO_SELF, 0.0f);
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        AnimationSet set = new AnimationSet(true);

        set.addAnimation(mHiddenAction);
        set.addAnimation(alpha);
        set.setStartOffset(startOff);
        set.setDuration(200);
        set.setFillAfter(true);
        set.setInterpolator(new AccelerateInterpolator());
        return set;
    }

    private void setUpArtistsAndInstituesFromCache(View view) {

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES+categoryId, getActivity())) {
            String fileContent = FileCacheUtil.getCache(getActivity(), CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES+categoryId);
            List<Institute> institutes = new ArrayList<Institute>();
            List<Artist> artists = new ArrayList<Artist>();

            try {
                institutes = parseInstituteListFromString(fileContent);
                artists = parseArtistsListFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpArtistsAndInstitute(institutes, artists, true, view);
        }
    }

    private void checkCacheUpdatedOrNot() {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(getActivity()), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();


                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long) (Double.parseDouble(cacheUpdatedTimeStamp) * 1000);

                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES + categoryId, getActivity(), cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getDataFromServer(false);
                        } else {
                            setUpArtistsAndInstituesFromCache(view);
                        }


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("MainActivity", "request banner failed: " + error.toString());
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();


            }
        });


    }

    private void getDataFromServer(final boolean refresh) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.categoryArtistsAndInstitutes(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(getActivity()), categoryId, currentPage, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<Institute> institutes = new ArrayList<Institute>();

                List<Artist> artists = new ArrayList<Artist>();


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        rvInstitue.loadMoreComplete();


                    } else {

                        if (!refresh) {

                            FileCacheUtil.setCache(realData, getActivity(), CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES + categoryId, 0);
                        }
                        institutes = parseInstituteListFromString(realData);
                        artists = parseArtistsListFromString(realData);
                        setUpArtistsAndInstitute(institutes, artists, false, view);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    rvInstitue.loadMoreComplete();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    rvInstitue.loadMoreComplete();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                rvInstitue.loadMoreComplete();

                Log.i("MainActivity", "request banner failed: " + error.toString());

            }
        });
    }

    private void setUpArtistsAndInstitute(List<Institute> institutes, List<Artist> artists,boolean isFromCache,View view) {
        setUpViewPagerLoverOfLife(artists,view);
        setUpListViewInstituteRecommend(institutes, view);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutMain);
        linearLayout.setVisibility(View.VISIBLE);
        rvInstitue.setNestedScrollingEnabled(false);
        if(currentPage-1==1) {
            startAnimation();
        }


    }

    private void startAnimation() {

        textViewArtist.startAnimation(moveToViewLocation(0));
        textViewInstitute.startAnimation(moveToViewLocation(0));

        rvInstitue.startAnimation(contentsMoveToViewLocation(100));
        rv.startAnimation(contentsMoveToViewLocation(100));
    }

    public void setUpListViewInstituteRecommend(List<Institute> institutes,View rootView) {

        ArrayList<HashMap<String, Object>> listItemCopy = new ArrayList<HashMap<String, Object>>();
        listItemCopy.addAll(listItem);


        if (listItem != null && listItem.size() > 0) {
            listItem.clear();

        }

        if (currentPage != 1) {
            listItem.addAll(listItemCopy);
        }


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

            listItem.add(map);
        }



        if (!refresh) {

            adapter = new RecycleViewInstitutesOfSearchResultAdapter(getActivity(),
                    listItem);

            adapter.setOnItemClickListener(new RecycleViewInstitutesOfSearchResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    LabCollection lc = new LabCollection();
                    lc.image = listItem.get(position).get("imageView").toString();
                    lc.title =  listItem.get(position).get("textViewTitle").toString();
                    lc.id = Long.parseLong(listItem.get(position).get("id").toString());
                    Intent intent = new Intent(getActivity(), LifeLabItemActivity.class);
                    intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);

                    startActivity(intent);

                }

            });
            rvInstitue.setAdapter(adapter);

        } else {
            adapter.notifyDataSetChanged();
        }

        currentPage++;
        rvInstitue.loadMoreComplete();


//
//
//        listViewInstituteRecommended = (ListView) rootView.findViewById(R.id.listViewSelectedInstituteCategory);
//
//        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
//
//        for (int i = 0; i < institutes.size(); i++) {
//            HashMap<String, Object> map = new HashMap<String, Object>();
//
//            //
//
//            Institute institute = institutes.get(i);
//            map.put("imageView", institute.getImage());
//            map.put("textViewNum", getActivity().getResources().getString(R.string.number_refix) + institutes.get(i).getOrder());
//            map.put("textViewTitle", institutes.get(i).getTitle());
//            map.put("textViewRead", institutes.get(i).getRead_count());
//            map.put("textViewCollection", institutes.get(i).getLiked_count());
//            map.put("id",institutes.get(i).getId());
//
//            listItem.add(map);
//        }
//        ListViewAdapter listItemAdapter = new ListViewAdapter(rootView.getContext(), listItem);
//
////        SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(), listItem,//data source
////                R.layout.list_item_life_institue_recommended,
////
////                new String[]{"image","textVol", "textTitle", "textReadCount", "textCollectionCount"},
////                //ids
////                new int[]{R.id.imageViewInstitue,R.id.textViewNum, R.id.textViewTitle, R.id.textViewRead, R.id.textViewCollection}
////        );
//        listViewInstituteRecommended.setAdapter(listItemAdapter);
//        listViewInstituteRecommended.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                LabCollection lc = new LabCollection();
//                HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
//
//                lc.image = map.get("imageView").toString();
//                lc.title = map.get("textViewTitle").toString();
//                lc.id = Long.parseLong(map.get("id").toString());
//                Intent intent = new Intent(getActivity(), LifeLabItemActivity.class);
//                intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);
//
//                startActivity(intent);
//            }
//        });
//        listItemAdapter.notifyDataSetChanged();
//
//        //fix bug created by scrollview
//        fixListViewHeight(listViewInstituteRecommended);
    }


    public void setUpViewPagerLoverOfLife(final List<Artist> artists, View rootView) {


        ArrayList<HashMap<String, Object>> listItemArtist = new ArrayList<HashMap<String, Object>>();


        for (int i = 0; i < artists.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            map.put("imageView", artists.get(i).getImage());

            map.put("textViewName", artists.get(i).getNickname());
            map.put("textViewDesc", artists.get(i).getDesc());
            map.put("textViewArticleCount", artists.get(i).getArticle_count());
            map.put("textViewFollowerCount", artists.get(i).getFollower_count());
            map.put("id", artists.get(i).getId());

            listItemArtist.add(map);
        }


        RecycleViewArtistsOfHomePageAdapter adapterArtists= new RecycleViewArtistsOfHomePageAdapter(getActivity(), listItemArtist);
        OverScrollDecoratorHelper.setUpOverScroll(rv, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

        adapterArtists.setOnItemClickListener(new RecycleViewArtistsOfHomePageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String artistId=artists.get(position).getId();

                Intent intent = new Intent(getActivity(),DetailArtistActivity.class);
                intent.putExtra("id",artistId);
                startActivity(intent);

            }

        });
        rv.setAdapter(adapterArtists);



    }

    private List<Artist> parseArtistsListFromString(String realData) throws JSONException {

        List<Artist> artists = new ArrayList<Artist>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("artist_list");
        for (int i = 0; i < results.length(); i++) {
            Artist artist = new Artist();
            JSONObject result = results.getJSONObject(i);
            artist.setId(result.getString("id"));
            artist.setNickname(result.getString("nickname"));
            artist.setDesc(result.getString("desc"));
            artist.setImage(result.getString("image"));
            artist.setArticle_count(result.getString("article_count"));
            artist.setFollower_count(result.getString("follow_count"));

            artists.add(artist);
        }

        return artists;
    }

    private List<Institute> parseInstituteListFromString(String realData) throws JSONException {
        List<Institute> institutes = new ArrayList<Institute>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("collection_list");
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    @Override
    public void onLoadMore() {
        rvInstitue.setNestedScrollingEnabled(false);
        boolean isLastPageLoaded = false;
        try {
            isLastPageLoaded = isLastPageLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!isLastPageLoaded) {

            getDataFromServer(true);
        } else {
            rvInstitue.loadMoreComplete();
        }


    }

    private boolean isLastPageLoaded() throws JSONException {

        boolean result = false;

        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES + categoryId, getActivity())) {
            String fileContent = FileCacheUtil.getCache(getActivity(), CommonUtilities.CACHE_FILE_CATEGORY_ARTISTS_AND_INSTITUTES + categoryId);
            JSONObject object = new JSONObject(fileContent);
            totalPages = object.getJSONObject("result").getJSONObject("data").getString("total_pages");
            if (currentPage > Integer.parseInt(totalPages)) {

                result = true;
            }
        }

        return result;
    }

}
