package com.wewow.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.wewow.fragment.categaryFragment;
import com.wewow.fragment.homeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iris on 17/3/19.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private List<String> list;
    private static final String TAG = FragmentAdapter.class.getSimpleName();
    private FragmentManager fm;

    public FragmentAdapter(FragmentManager fm, List<String> list) {
        super(fm);
        this.list = list;
        this.fm=fm;

    }

    @Override
    public Fragment getItem(int position) {
        if (position != 0) {
            return categaryFragment.newInstance(list.get(position));

        }
        return homeFragment.newInstance(list.get(position));
    }


    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, "getItemPosition(" + object.getClass().getSimpleName() + ")");
        if (object instanceof homeFragment) {
            ((homeFragment) object).setUpListViewInstituteRecommend();
            ((homeFragment) object).setUpViewPagerLoverOfLife();
        } else if (object instanceof categaryFragment) {
            ((categaryFragment) object).setUpListViewInstituteRecommend();
            ((categaryFragment) object).setUpViewPagerLoverOfLife();
        }

        return super.getItemPosition(object);
    };

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
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
