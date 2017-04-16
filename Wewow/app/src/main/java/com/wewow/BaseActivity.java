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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.dto.Feedback;
import com.wewow.utils.DataCleanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private int[] iconResIcon = {R.drawable.selector_btn_home, R.drawable.selector_btn_all_artists, R.drawable.selector_btn_all_institutes, R.drawable.selector_btn_chat,
            R.drawable.selector_btn_favourite, R.drawable.selector_btn_lover_of_life_subscribed, R.drawable.selector_btn_life_about,
            R.drawable.selector_btn_share, R.drawable.selector_btn_clear_cache, R.drawable.selector_btn_logout};
    private NavigationView mainNavView;

    private TextView tvusername, tvuserdesc;

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
//        setUpNavigationView();
        setUpToolBar();

    }

    private void setUpNavigationView() {

//        mainNavView=(NavigationView)findViewById(R.id.navigation_view);
////        mainNavView.setItemTextColor(getResources().getColorStateList(R.color.nav_menu_item_color));
////        mainNavView.setItemIconTintList(getResources().getColorStateList(R.color.nav_menu_item_color));

        mainNavView.setItemTextColor(null);
        mainNavView.setItemIconTintList(null);
    }


    private void setUpNavigation() {
        planetTitles = getResources().getStringArray(R.array.planets_array);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        View VheandrView = LayoutInflater.from(this).inflate(R.layout.list_header_drawer, null);
        drawerList.addHeaderView(VheandrView, null, true);

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

        /**
         * 这里是登录页入口代码sample，登录结果见void onActivityResult 的resultcode，RESULT_CANCELED or  RESULT_OK
         * 登录名，token从UserUtils对象获取
         */
        this.tvusername = (TextView) VheandrView.findViewById(R.id.textViewUsername);

        //fix crash when clicking logout
        this.tvuserdesc = (TextView) VheandrView.findViewById(R.id.textViewSignature);
//        usertv.setText("Anonymous");

        if (UserInfo.isUserLogged(this)) {

            this.tvusername.setText(UserInfo.getCurrentUser(this).getNickname());

            this.tvuserdesc.setText(UserInfo.getCurrentUser(this).getDesc());
        } else {
            this.tvusername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.setClass(BaseActivity.this, LoginActivity.class);
                    BaseActivity.this.startActivityForResult(i, LoginActivity.REQUEST_CODE_LOGIN);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        Log.d("BaseActivity", "login return");
        if (!UserInfo.isUserLogged(this)) {
            Log.d("BaseActivity", "Not logged");
            return;
        }
        UserInfo ui = UserInfo.getCurrentUser(this);
        Log.d("BaseActivity", String.format("%s %s", ui.getOpen_id(), ui.getToken()));

        TextView usertv = (TextView) this.findViewById(R.id.textViewUsername);
        usertv.setText(UserInfo.getCurrentUser(this).getNickname());
        TextView userSignature = (TextView) findViewById(R.id.textViewSignature);
        userSignature.setText(UserInfo.getCurrentUser(this).getDesc());
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
            //selectItem(position);
            Log.d("BaseActivity", String.format("onItemClick: %d", position));
            if (position == 0) {
                Intent i = new Intent();
                i.setClass(BaseActivity.this, LoginActivity.class);
                BaseActivity.this.startActivityForResult(i, LoginActivity.REQUEST_CODE_LOGIN);
                return;
            }

            HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
            String text = (String) map.get("menuText");
            int resid = (Integer) map.get("icon");
//            Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
            switch (position - 1) {
                case 0:
                    Intent intentMain = new Intent(BaseActivity.this, MainActivity.class);
                    BaseActivity.this.startActivity(intentMain);
                    break;
                case 1:
                    Intent intent = new Intent(BaseActivity.this, ListArtistActivity.class);
                    BaseActivity.this.startActivity(intent);
                    break;
                case 2:
                    Intent intentLab = new Intent(BaseActivity.this, LifeLabActivity.class);
                    BaseActivity.this.startActivity(intentLab);
                    break;
                case 3:
                    Intent intentFeedback = new Intent(BaseActivity.this, FeedbackActivity.class);
                    BaseActivity.this.startActivity(intentFeedback);
                    break;

                case 6:
                    Intent intentSubscribedArtists = new Intent(BaseActivity.this, ListSubscribedArtistActivity.class);
                    BaseActivity.this.startActivity(intentSubscribedArtists);
                    break;
                case 7:
                    Intent intentAbout = new Intent(BaseActivity.this, AboutActivity.class);
                    BaseActivity.this.startActivity(intentAbout);
                    break;
                case 10:
                    //clear cache
                    DataCleanUtils.cleanAllApplicationData(BaseActivity.this);
                    break;
                case 11:
                    Log.d("BaseActivity", "Logout");
                    UserInfo.logout(BaseActivity.this);
                    BaseActivity.this.tvusername.setText(R.string.login_gologin);
                    BaseActivity.this.tvuserdesc.setText(R.string.login_to_see_more);
                    break;
                case 4:
                default:
                    break;
            }
        }
    }

    private void selectItem(int position) {
        drawerLayout.closeDrawer(GravityCompat.START);
//        Toast.makeText(BaseActivity.this, planetTitles[position - 1], Toast.LENGTH_SHORT).show();
        switch (position - 1) {
            case 0:
                break;
            case 1:
                Intent intent = new Intent(BaseActivity.this, ListArtistActivity.class);
                BaseActivity.this.startActivity(intent);
                break;
            default:
                break;
        }
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
