package com.wewow.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.R;
import com.wewow.adapter.ListViewAdapter;
import com.wewow.adapter.RecycleViewArticlesOfArtistDetail;
import com.wewow.dto.Artist;
import com.wewow.dto.Institute;
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
import java.util.Random;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/13.
 */
public class searchResultListFragment extends Fragment {

    private String category=CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTICLE;
    private ArrayList<HashMap<String, Object>> list=new ArrayList<HashMap<String, Object>>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle bundle = getArguments();
        if (bundle != null) {
            category = bundle.getString("catetory");
        }
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_search_result_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    public searchResultListFragment ()
    {

    }

    public static searchResultListFragment newInstance(String text,ArrayList<HashMap<String, Object>> list) {
        Bundle bundle = new Bundle();
        bundle.putString("catetory", text);
        searchResultListFragment blankFragment = new searchResultListFragment();
        blankFragment.setArguments(bundle);

        return blankFragment;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    if(category.equals (CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTICLE))
        {
            recyclerView.setAdapter(new RecycleViewArticlesOfArtistDetail(getActivity(),null));

    }
    }


}