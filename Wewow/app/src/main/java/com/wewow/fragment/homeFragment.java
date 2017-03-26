package com.wewow.fragment;

import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.R;
import com.wewow.adapter.ListViewAdapter;
import com.wewow.dto.Artist;
import com.wewow.dto.Banner;
import com.wewow.dto.Institute;
import com.wewow.dto.collectionCategory;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
    private int requestSentCount = 0;


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
        initData();
        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        if (Utils.isNetworkAvailable(getActivity())) {

            checkCacheUpdatedOrNot();

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);

            SettingUtils.set(getActivity(), CommonUtilities.NETWORK_STATE, false);
            setUpLatestInstitueFromCache();
            setUpRecommendedArtistsAndInstituesFromCache();
        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    private void setUpRecommendedArtistsAndInstituesFromCache() {
        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_RECOMMANDED_ARTISTS_AND_INSTITUTES, getActivity())) {
            String fileContent = FileCacheUtil.getCache(getActivity(), CommonUtilities.CACHE_FILE_RECOMMANDED_ARTISTS_AND_INSTITUTES);
            List<Institute> institutes = new ArrayList<Institute>();
            List<Artist> artists = new ArrayList<Artist>();

            try {
                institutes = parseInstituteListFromString(fileContent);
                artists = parseArtistsListFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpRecommendArtistsAndInstitute(institutes,artists,true);
        }
    }

    private void setUpLatestInstitueFromCache() {
        if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_LATEST_INSTITUTE, getActivity())) {
            String fileContent = FileCacheUtil.getCache(getActivity(), CommonUtilities.CACHE_FILE_LATEST_INSTITUTE);
            Institute institute = new Institute();
            try {
                institute = parseInstituteFromString(fileContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpLatestInstitue(institute,true);
        }
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

    private void checkCacheUpdatedOrNot() {
        swipeRefreshLayout.setRefreshing(true);

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.updateAt(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(getActivity()), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        JSONObject jsonObject = new JSONObject(realData);
                        String cacheUpdatedTimeStamp = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("data")
                                .getString("update_at");

                        long cacheUpdatedTime = (long)(Double.parseDouble(cacheUpdatedTimeStamp) * 1000);

                        boolean isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_LATEST_INSTITUTE, getActivity(), cacheUpdatedTime);

                        if (isCacheDataOutdated) {
                            getLatestInstituteFromServer();
                        } else {
                            setUpLatestInstitueFromCache();
                        }

                        isCacheDataOutdated = FileCacheUtil
                                .isCacheDataFailure(CommonUtilities.CACHE_FILE_RECOMMANDED_ARTISTS_AND_INSTITUTES, getActivity(), cacheUpdatedTime);

                        if (isCacheDataOutdated) {

                            getRecommendArtistsAndInstitueFromServer();

                        } else {
                            setUpRecommendedArtistsAndInstituesFromCache();
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("MainActivity", "request banner failed: " + error.toString());
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });


    }


    private void getLatestInstituteFromServer() {
        swipeRefreshLayout.setRefreshing(true);
        requestSentCount++;
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.latestInstite(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(getActivity()), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                Institute institute = new Institute();

                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {
                        institute = parseInstituteFromString(realData);
                        FileCacheUtil.setCache(realData, getActivity(), CommonUtilities.CACHE_FILE_LATEST_INSTITUTE, 0);
                        setUpLatestInstitue(institute,false);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("MainActivity", "request banner failed: " + error.toString());
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });

    }

    private void getRecommendArtistsAndInstitueFromServer() {

        requestSentCount++;
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);
        iTask.hotArtistisAndInstitutes(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(getActivity()), new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {
                List<Institute> institutes = new ArrayList<Institute>();

                List<Artist> artists = new ArrayList<Artist>();


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {

                        FileCacheUtil.setCache(realData, getActivity(), CommonUtilities.CACHE_FILE_RECOMMANDED_ARTISTS_AND_INSTITUTES, 0);
                        institutes = parseInstituteListFromString(realData);
                        artists = parseArtistsListFromString(realData);

                        setUpRecommendArtistsAndInstitute(institutes, artists,false);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);

                Log.i("MainActivity", "request banner failed: " + error.toString());

            }
        });
    }

    private void setUpRecommendArtistsAndInstitute(List<Institute> institutes, List<Artist> artists,boolean isFromCache) {
        setUpViewPagerLoverOfLife(artists);
        setUpListViewInstituteRecommend(institutes);

        requestSentCount--;

        if (requestSentCount == 0||isFromCache) {
            swipeRefreshLayout.setRefreshing(false);
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.layoutHome);
            linearLayout.setVisibility(View.VISIBLE);

            startAnimation();

        }


    }

    private List<Artist> parseArtistsListFromString(String realData) throws JSONException {

        List<Artist> artists = new ArrayList<Artist>();

        JSONObject object = new JSONObject(realData);
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("recommend_artist_list");
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
        JSONArray results = object.getJSONObject("result").getJSONObject("data").getJSONArray("recommend_collections");
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


    private void setUpLatestInstitue(Institute institue ,boolean isFromCache) {

        ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageViewLatestInstitue);
        Glide.with(getActivity())
                .load(institue.getImage())
                .placeholder(R.drawable.banner_loading_spinner)
                .crossFade(300)
                .into(imageView);

        TextView textViewNum = (TextView) getActivity().findViewById(R.id.textViewNum);
        textViewNum.setText(getActivity().getResources().getString(R.string.number_refix) + institue.getOrder());

        TextView textViewTitle = (TextView) getActivity().findViewById(R.id.textViewTitle);
        textViewTitle.setText(institue.getTitle());

        TextView textViewReadCount = (TextView) getActivity().findViewById(R.id.textViewRead);
        textViewReadCount.setText(institue.getRead_count());

        TextView textViewCollectionCount = (TextView) getActivity().findViewById(R.id.textViewCollection);
        textViewCollectionCount.setText(institue.getLiked_count());
        requestSentCount--;

        if (requestSentCount == 0||isFromCache) {
            swipeRefreshLayout.setRefreshing(false);

        }


    }


    private Institute parseInstituteFromString(String fileContent) throws JSONException {


        Institute institutes = new Institute();
        JSONObject jsonObject = new JSONObject(fileContent);
        JSONObject result = jsonObject.getJSONObject("result").getJSONObject("data");
        institutes.setId(result.getString("id"));
        institutes.setRead_count(result.getString("read_count"));
        institutes.setTitle(result.getString("title"));
        institutes.setImage(result.getString("image"));
        institutes.setLiked_count(result.getString("liked_count"));
        institutes.setOrder(result.getString("order"));
        return institutes;
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

    public void setUpViewPagerLoverOfLifeDummy() {

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

    public void setUpViewPagerLoverOfLife(List<Artist> artists) {

        //blank view for bounce effect
        View left = new View(getActivity());
        List<View> mListViews = new ArrayList<View>();
        mListViews.add(left);

        for (int i = 0; i < 3; i++) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_lover_of_life_recommended, null);
            CircleImageView image = (CircleImageView) view.findViewById(R.id.imageViewIcon);
            Glide.with(getActivity())
                    .load(artists.get(i).getImage())
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade(300)
                    .into(image);

            TextView textNickName = (TextView) view.findViewById(R.id.textViewNickName);
            TextView textDesc = (TextView) view.findViewById(R.id.textViewDesc);
            TextView textArticleCount = (TextView) view.findViewById(R.id.textViewRead);
            TextView textFollowerCount = (TextView) view.findViewById(R.id.textViewCollection);

            textNickName.setText(artists.get(i).getNickname());
            textDesc.setText(artists.get(i).getDesc());

            textArticleCount.setText(artists.get(i).getArticle_count());
            textFollowerCount.setText(artists.get(i).getFollower_count());


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

    public void setUpListViewInstituteRecommendDummy() {

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

    public void setUpListViewInstituteRecommend(List<Institute> institutes) {

        listViewInstituteRecommended = (ListView) getActivity().findViewById(R.id.listViewSelectedInstitute);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 2; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            Institute institute = institutes.get(i);
            map.put("imageView", institute.getImage());
            map.put("textViewNum", getActivity().getResources().getString(R.string.number_refix) + institutes.get(i).getOrder());
            map.put("textViewTitle", institutes.get(i).getTitle());
            map.put("textViewRead", institutes.get(i).getRead_count());
            map.put("textViewCollection", institutes.get(i).getLiked_count());

            listItem.add(map);
        }
        ListViewAdapter listItemAdapter = new ListViewAdapter(getActivity(), listItem);

//        SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(), listItem,//data source
//                R.layout.list_item_life_institue_recommended,
//
//                new String[]{"image","textVol", "textTitle", "textReadCount", "textCollectionCount"},
//                //ids
//                new int[]{R.id.imageViewInstitue,R.id.textViewNum, R.id.textViewTitle, R.id.textViewRead, R.id.textViewCollection}
//        );
        listViewInstituteRecommended.setAdapter(listItemAdapter);
        listItemAdapter.notifyDataSetChanged();

        //fix bug created by scrollview
        fixListViewHeight(listViewInstituteRecommended);
    }

    @Override
    public void onRefresh() {

//        setUpViewPagerLoverOfLife();
//        setUpListViewInstituteRecommend();
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
