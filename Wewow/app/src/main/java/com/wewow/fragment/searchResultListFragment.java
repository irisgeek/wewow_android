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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.ArticleActivity;
import com.wewow.DetailArtistActivity;
import com.wewow.FeedbackActivity;
import com.wewow.LifeLabItemActivity;
import com.wewow.LifePostActivity;
import com.wewow.ListSubscribedArtistActivity;
import com.wewow.LoginActivity;
import com.wewow.R;
import com.wewow.SearchResultActivity;
import com.wewow.UserInfo;
import com.wewow.adapter.ListViewAdapter;
import com.wewow.adapter.RecycleViewArticlesOfArtistDetail;
import com.wewow.adapter.RecycleViewArtistsOfSearchResultAdapter;
import com.wewow.adapter.RecycleViewInstitutesOfSearchResultAdapter;
import com.wewow.adapter.RecycleViewPostOfSearchResultAdapter;
import com.wewow.dto.Article;
import com.wewow.dto.Artist;
import com.wewow.dto.Institute;
import com.wewow.dto.LabCollection;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.SettingUtils;
import com.wewow.utils.Utils;
import com.wewow.view.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
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

    private String category = CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTICLE;
    private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle bundle = getArguments();
        if (bundle != null) {
            category = bundle.getString("catetory");
            list=(ArrayList<HashMap<String, Object>>)bundle.getSerializable("list");
        }
        view =inflater.inflate(
                R.layout.fragment_search_result_list, container, false);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerview);

        if(list!=null&&list.size()>0) {
            setupRecyclerView(rv);
        }
        else
        {
            showSearchWithNoResultHint();
        }
        return view;
    }

    private void showSearchWithNoResultHint() {
        TextView textViewTellWewow=(TextView)view.findViewById(R.id.textViewTellWewow);
        textViewTellWewow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfo.isUserLogged(getActivity())) {
                    Intent intentFeedback = new Intent(getActivity(), FeedbackActivity.class);
                    getActivity().startActivity(intentFeedback);
                } else {
                    LoginUtils.startLogin(getActivity(), LoginActivity.REQUEST_CODE_FEEDBACK);
                }
            }
        });
        LinearLayout layout =(LinearLayout)view.findViewById(R.id.layoutNoResult);
        layout.setVisibility(View.VISIBLE);

    }

    public searchResultListFragment() {

    }

    public static searchResultListFragment newInstance(String text, ArrayList<HashMap<String, Object>> list) {
        Bundle bundle = new Bundle();
        bundle.putString("catetory", text);
        bundle.putSerializable("list", (Serializable) list);
        searchResultListFragment blankFragment = new searchResultListFragment();
        blankFragment.setArguments(bundle);



        return blankFragment;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayout layout =(LinearLayout)view.findViewById(R.id.layoutNoResult);
        layout.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        if (category.equals(CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTICLE)) {


            RecycleViewArticlesOfArtistDetail adapter = new RecycleViewArticlesOfArtistDetail(getActivity(), list);
            adapter.setOnItemClickListener(new RecycleViewArticlesOfArtistDetail.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getActivity(), ArticleActivity.class);
                    String articleId=list.get(position).get("id").toString();
                    intent.putExtra(ArticleActivity.ARTICLE_ID,Integer.parseInt(articleId));
                    getActivity().startActivity(intent);

                }

            });
            recyclerView.setAdapter(adapter);

        }
        else

        if (category.equals(CommonUtilities.RESEARCH_RESULT_CATEGORY_INSTITUTE)) {

            RecycleViewInstitutesOfSearchResultAdapter adapter = new RecycleViewInstitutesOfSearchResultAdapter(getActivity(), list);
            adapter.setOnItemClickListener(new RecycleViewInstitutesOfSearchResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    LabCollection lc = new LabCollection();
                    lc.image = list.get(position).get("imageView").toString();
                    lc.title =  list.get(position).get("textViewTitle").toString();
                    lc.id = Long.parseLong(list.get(position).get("id").toString());
                    Intent intent = new Intent(getActivity(), LifeLabItemActivity.class);
                    intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);

                    startActivity(intent);

                }

            });
            recyclerView.setAdapter(adapter);

        }
        else

        if (category.equals(CommonUtilities.RESEARCH_RESULT_CATEGORY_ARTIST)) {


            RecycleViewArtistsOfSearchResultAdapter adapter = new RecycleViewArtistsOfSearchResultAdapter(getActivity(), list);

            adapter.setOnItemClickListener(new RecycleViewArtistsOfSearchResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    HashMap<String, Object> stringObjectHashMap = (HashMap<String, Object>) list.get(position);
                    String artistId = stringObjectHashMap.get("id").toString();
                    Intent intent = new Intent(getActivity(), DetailArtistActivity.class);
                    intent.putExtra("id", artistId);
                    startActivity(intent);

                }

            });


            recyclerView.setAdapter(adapter);

        }
        else
        if (category.equals(CommonUtilities.RESEARCH_RESULT_CATEGORY_POST)) {

            RecycleViewPostOfSearchResultAdapter adapter = new RecycleViewPostOfSearchResultAdapter(getActivity(), list);

            adapter.setOnItemClickListener(new RecycleViewPostOfSearchResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getActivity(), LifePostActivity.class);
                    intent.putExtra(LifePostActivity.POST_ID, Integer.parseInt(list.get(position).get("id").toString()));
                    startActivity(intent);

                }

            });
            recyclerView.setAdapter(adapter);

        }


    }


}