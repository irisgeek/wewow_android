package com.wewow.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.wewow.fragment.categaryFragment;
import com.wewow.fragment.homeFragment;
import com.wewow.fragment.searchResultListFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iris on 17/3/19.
 */

public class FragmentSearchResultAdapter extends FragmentPagerAdapter {

    private ArrayList<ArrayList<HashMap<String, Object>>> list;
    private List<String> ids;
    private List<String> listTitle;
    private static final String TAG = FragmentSearchResultAdapter.class.getSimpleName();
    private FragmentManager fm;
    private List<searchResultListFragment> fgs = null;

    public FragmentSearchResultAdapter(FragmentManager fm,   ArrayList< ArrayList<HashMap<String, Object>>> list, List<String> ids, List<String> listTitle,  List<searchResultListFragment> fgs ) {
        super(fm);
        this.list = list;
        this.fm=fm;
        this.ids=ids;
        this.listTitle=listTitle;
        this.fgs=fgs;

    }

    @Override
    public Fragment getItem(int position) {

        return fgs.get(position);

    }

    public String getId(int position)
    {
        return  ids.get(position);
    }




    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return listTitle.get(position);
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

   @Override
     public Object instantiateItem(ViewGroup container, int position) {
       searchResultListFragment f = (searchResultListFragment) super.instantiateItem(container, position);
             String title = listTitle.get(position);

            return f;
         }

             @Override
     public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
         }

    public void setFragments(List<searchResultListFragment> fragments) {
        if(this.fgs != null){
            FragmentTransaction ft = fm.beginTransaction();
            for(Fragment f:this.fgs){
                ft.remove(f);
            }
            ft.commit();
            ft=null;
            fm.executePendingTransactions();
        }
        this.fgs = fragments;
        notifyDataSetChanged();
    }
}

