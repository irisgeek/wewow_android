package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boycy815.pinchimageview.PinchImageView;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class ShowImageActivity extends ActionBarActivity {

    private static final String TAG = "ShowImageActivity";
    public static final String IMAGE_INDEX = "IMAGE_INDEX";
    public static final String IMAGE_LIST = "IMAGE_LIST";
    private ArrayList<String> pictures;
    private int index;

    private TextView imageOrder;
    private TextView imageCount;
    private PinchImageView imgView;
    private ViewPager pager;
    private int screenWidth;
    private int pictureHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_show_image);
        this.screenWidth = Utils.dipToPixel(this, Utils.getScreenWidthDp(this));
        Intent intent = this.getIntent();
        this.index = intent.getIntExtra(IMAGE_INDEX, 0);
        this.pictures = intent.getStringArrayListExtra(IMAGE_LIST);
        this.imageOrder = (TextView) this.findViewById(R.id.image_order);
        this.imageOrder.setText(String.valueOf(this.index + 1));
        this.imageCount = (TextView) this.findViewById(R.id.image_count);
        this.imageCount.setText(String.valueOf(this.pictures.size()));
        this.pager = (ViewPager) this.findViewById(R.id.image_pager);
        this.pictureHeight = this.pager.getLayoutParams().height;
        this.pager.setAdapter(new FragmentStatePagerAdapter(this.getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                String url = ShowImageActivity.this.pictures.get(position);
                ShowImageFragment sf = new ShowImageFragment();
                Bundle b = new Bundle();
                b.putString(ShowImageFragment.IMAGE_URL, url);
                b.putInt(ShowImageFragment.IMAGE_INDEX, position);
                b.putFloat(ShowImageFragment.IMAGE_AREA_HEIGHT, pictureHeight);
                b.putFloat(ShowImageFragment.IMAGE_AREA_WIDTH, screenWidth);
                sf.setArguments(b);
                return sf;
            }

            @Override
            public int getCount() {
                return ShowImageActivity.this.pictures.size();
            }
        });
        this.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //
            }

            @Override
            public void onPageSelected(int position) {
                ShowImageActivity.this.imageOrder.setText(String.valueOf(position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//
            }
        });
        this.pager.setCurrentItem(this.index);
    }

}
