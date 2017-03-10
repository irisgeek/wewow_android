//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.wewow;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by iris on 17/3/6.
 */
public class BaseActivity extends ActionBarActivity {
    protected String[] planetTitles;
    protected DrawerLayout drawerLayout;
    protected ListView drawerList;
    protected FrameLayout frameLayout;
    private Toolbar toolbar;
    private int[] iconResIcon = {R.drawable.home, R.drawable.lover_of_life, R.drawable.life_institue, R.drawable.chat,
            R.drawable.my_favorites, R.drawable.lover_of_life_subscribed, R.drawable.about,
            R.drawable.share_menu, R.drawable.clear_cache, R.drawable.logout};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        frameLayout = (FrameLayout) drawerLayout.findViewById(R.id.content_frame);

        getLayoutInflater().inflate(layoutResID, frameLayout, true);

        super.setContentView(drawerLayout);

        setUpNavigation();
        setUpToolBar();

    }


    private void setUpNavigation() {
        planetTitles = getResources().getStringArray(R.array.planets_array);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //
            if (i == 4 || i == 7) {
                map.put("icon", 0);
                map.put("menuText", "");
                listItem.add(map);
                map = new HashMap<String, Object>();
            }

            map.put("icon", iconResIcon[i]);
            map.put("menuText", planetTitles[i]);

            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,//data source
                R.layout.list_item_drawer,

                new String[]{"icon", "menuText"},
                //ids
                new int[]{R.id.imageViewIcon, R.id.textViewMenuItem}
        );
        drawerList.setAdapter(listItemAdapter);


        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        View VheandrView = LayoutInflater.from(this).inflate(R.layout.list_header_drawer, null);
        drawerList.addHeaderView(VheandrView);
    }

    private void setUpToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.selector_btn_menu);
        getSupportActionBar().setTitle(" ");

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Toast.makeText(BaseActivity.this, planetTitles[position], Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setVisible(false);
        return true;
    }

}
