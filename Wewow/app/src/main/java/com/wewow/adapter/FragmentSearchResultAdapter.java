package com.wewow.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.wewow.fragment.categaryFragment;
import com.wewow.fragment.homeFragment;
import com.wewow.fragment.searchResultListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iris on 17/3/19.
 */

public class FragmentSearchResultAdapter extends FragmentPagerAdapter {

    private ArrayList<HashMap<String, Object>> list;
    private List<String> ids;
    private static final String TAG = FragmentSearchResultAdapter.class.getSimpleName();
    private FragmentManager fm;

    public FragmentSearchResultAdapter(FragmentManager fm,   ArrayList<HashMap<String, Object>> list, List<String> ids) {
        super(fm);
        this.list = list;
        this.fm=fm;
        this.ids=ids;

    }

    @Override
    public Fragment getItem(int position) {
        if (position != 0) {
            return searchResultListFragment.newInstance(ids.get(position),list);

        }
        return homeFragment.newInstance(ids.get(position));
    }

    public String getId(int position)
    {
        return  ids.get(position);
    }


    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, "getItemPosition(" + object.getClass().getSimpleName() + ")");
        if (object instanceof homeFragment) {
        } else if (object instanceof categaryFragment) {
        }

        return super.getItemPosition(object);
    };

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
