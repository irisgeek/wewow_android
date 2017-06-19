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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wewow.adapter.ListViewMenuAdapter;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.DataCleanUtils;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.ShareUtils;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    private int[] iconResIconSelected = {R.drawable.home_b, R.drawable.lover_of_life_b, R.drawable.life_institute_b, R.drawable.chat_b,
            R.drawable.my_favorites_b, R.drawable.lover_of_life_subscribed_b, R.drawable.about_b,
            R.drawable.share_menu_b, R.drawable.clear_cache_b, R.drawable.logout_b};
    private NavigationView mainNavView;

    private TextView tvusername, tvuserdesc;
    private ImageView imageViewSetting, imageViewUserCover;
    private MaterialDialog dialog;
    private int[] bgRes = {R.drawable.cover1, R.drawable.cover2, R.drawable.cover3,
            R.drawable.cover4, R.drawable.cover5, R.drawable.cover6};
    private ArrayList<HashMap<String, Object>> listItem;
    private ListViewMenuAdapter adapter;
    private List<String> newIcons;
    public static final int REQUEST_CODE_MENU = 11;
    private int menuselectedPosition = 16;
    private boolean isOnPauseCalled = false;

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
        newIcons = new ArrayList<String>();

        if (UserInfo.isUserLogged(this) && Utils.isNetworkAvailable(this)) {
            String userId = UserInfo.getCurrentUser(this).getId().toString();
            getNewFeedsAndArtistInfo(userId);

        } else {
            setUpNavigation("0", "0");
//        setUpNavigationView();
        }
        setUpToolBar();


    }

    protected void setMenuselectedPosition(int position) {
        this.menuselectedPosition = position;
    }

    private void getNewFeedsAndArtistInfo(String userId) {
        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);


        iTask.user_notification(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(this), userId, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    if (!realData.contains(CommonUtilities.SUCCESS)) {
                        Toast.makeText(BaseActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                    } else {

                        List<String> updates = parseFeedbackAndArtistUpdate(realData);
                        setUpNavigation(updates.get(0), updates.get(1));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(BaseActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(BaseActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(BaseActivity.this, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


            }
        });
    }

    private List<String> parseFeedbackAndArtistUpdate(String realData) throws JSONException {

        List<String> results = new ArrayList<String>();
        JSONObject jsonObject = new JSONObject(realData);
        JSONObject result = jsonObject.getJSONObject("result").getJSONObject("data");
        results.add(result.getString("feedback_reply"));
        results.add(result.getString("artist_update"));
        return results;

    }


    private void setUpNavigationView() {

//        mainNavView=(NavigationView)findViewById(R.id.navigation_view);
////        mainNavView.setItemTextColor(getResources().getColorStateList(R.color.nav_menu_item_color));
////        mainNavView.setItemIconTintList(getResources().getColorStateList(R.color.nav_menu_item_color));

        mainNavView.setItemTextColor(null);
        mainNavView.setItemIconTintList(null);
    }


    private void setUpNavigation(String feedbackUpdate, String artistUpdate) {
        FileCacheUtil.setCache(artistUpdate, BaseActivity.this, CommonUtilities.CACHE_FILE_ARTIST_UPDATE, 0);
        planetTitles = getResources().getStringArray(R.array.planets_array);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        View VheandrView = LayoutInflater.from(this).inflate(R.layout.list_header_drawer, null);
        drawerList.addHeaderView(VheandrView, null, true);

        listItem = new ArrayList<HashMap<String, Object>>();
        if (newIcons.size() > 0) {
            newIcons.clear();

        }

        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //
            if (i == 4 || i == 6) {
                map.put("icon", 0);
                map.put("menuText", "");
                map.put("new", "0");
                newIcons.add("0");
                listItem.add(map);
                map = new HashMap<String, Object>();
            }

            if (i == menuselectedPosition) {
                map.put("icon", iconResIconSelected[i]);
            } else {
                map.put("icon", iconResIcon[i]);
            }

            map.put("menuText", planetTitles[i]);
            if (i == 3) {
                map.put("new", feedbackUpdate);
                newIcons.add(feedbackUpdate);
            } else if (i == 5) {
                map.put("new", artistUpdate);
                newIcons.add(artistUpdate);
            } else {
                map.put("new", "0");
                newIcons.add("0");
            }


            listItem.add(map);
        }
//
//        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,//data source
//                R.layout.list_item_drawer,
//
//                new String[]{"icon", "menuText"},
//                //ids
//                new int[]{R.id.imageViewIcon, R.id.textViewMenuItem}
//        );
        adapter = new ListViewMenuAdapter(this, listItem, newIcons, menuselectedPosition);
        drawerList.setAdapter(adapter);


        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        /**
         * 这里是登录页入口代码sample，登录结果见void onActivityResult 的resultcode，RESULT_CANCELED or  RESULT_OK
         * 登录名，token从UserUtils对象获取
         */
        this.tvusername = (TextView) VheandrView.findViewById(R.id.textViewUsername);

        //fix crash when clicking logout
        this.tvuserdesc = (TextView) VheandrView.findViewById(R.id.textViewSignature);
        this.imageViewSetting = (ImageView) VheandrView.findViewById(R.id.imageViewSetting);
        this.imageViewUserCover = (ImageView) VheandrView.findViewById(R.id.userCover);
//        usertv.setText("Anonymous");

        if (UserInfo.isUserLogged(this)) {

            this.tvusername.setText(UserInfo.getCurrentUser(this).getNickname());

            this.tvuserdesc.setText(UserInfo.getCurrentUser(this).getDesc());
            imageViewSetting.setVisibility(View.VISIBLE);
//            imageViewUserCover.setImageResource(bgRes[Integer.parseInt(UserInfo.getCurrentUser(this).getBackground_id()) - 1]);
            int resId;
            try {
                resId = bgRes[Integer.parseInt(UserInfo.getCurrentUser(this).getBackground_id()) - 1];
            } catch (NumberFormatException e) {
                resId = bgRes[0];
            } catch (IndexOutOfBoundsException e) {
                resId = bgRes[0];
            }
            imageViewUserCover.setImageResource(resId);
        }


        this.findViewById(R.id.userloginarea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserInfo.isUserLogged(BaseActivity.this)) {
                    Intent edIntent = new Intent(BaseActivity.this, UserInfoActivity.class);
                    BaseActivity.this.startActivityForResult(edIntent, UserInfoActivity.REQUEST_CODE_MENU);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    LoginUtils.startLogin(BaseActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                }
            }
        });
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
        imageViewSetting.setVisibility(View.VISIBLE);
        int resId;
        try {
            resId = bgRes[Integer.parseInt(ui.getBackground_id()) - 1];
        } catch (NumberFormatException e) {
            resId = bgRes[0];
        } catch (IndexOutOfBoundsException e) {
            resId = bgRes[0];
        }
        imageViewUserCover.setImageResource(resId);

        if (requestCode == LoginActivity.REQUEST_CODE_FEEDBACK) {
            Intent intentFeedback = new Intent(BaseActivity.this, FeedbackActivity.class);
            BaseActivity.this.startActivity(intentFeedback);

        } else if (requestCode == LoginActivity.REQUEST_CODE_SUBSCRIBED_ARTISTS) {
            FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_LIST, this);
            Intent intentSubscribedArtists = new Intent(BaseActivity.this, ListSubscribedArtistActivity.class);
            BaseActivity.this.startActivity(intentSubscribedArtists);

        } else if (requestCode == BaseActivity.REQUEST_CODE_MENU) {
            updateMenuForFeedbackNotification();
        }


    }

    private void setUpToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.menu_b);
        getSupportActionBar().setTitle(" ");

    }

    private void getShareData() {
        List<Pair<String, String>> ps = new ArrayList<>();
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/share_app", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(BaseActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                Toast.makeText(BaseActivity.this, r.optString("message"), Toast.LENGTH_LONG).show();
                            } else {
                                String url = r.optJSONObject("data").optString("url");
                                ShareUtils su = new ShareUtils(BaseActivity.this);
                                su.setUrl(url);
                                su.setContent(getResources().getString(R.string.share_text));
                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                                su.setPicture(bmp);
                                su.share();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(BaseActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //selectItem(position);
            Log.d("BaseActivity", String.format("onItemClick: %d", position));
            if (position == 0) {
                LoginUtils.startLogin(BaseActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                return;
            }

            HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
            String text = (String) map.get("menuText");
            int resid = (Integer) map.get("icon");
//            Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();

            switch (position - 1) {
                case 0:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Intent intentMain = new Intent(BaseActivity.this, MainActivity.class);
                    startActivity(intentMain);

                    break;
                case 1:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Intent intent = new Intent(BaseActivity.this, ListArtistActivity.class);
                    BaseActivity.this.startActivity(intent);
                    break;
                case 2:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Intent intentLab = new Intent(BaseActivity.this, LifeLabActivity.class);
                    BaseActivity.this.startActivity(intentLab);
                    break;
                case 3:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    if (UserInfo.isUserLogged(BaseActivity.this)) {
                        Intent intentFeedback = new Intent(BaseActivity.this, FeedbackActivity.class);
                        BaseActivity.this.startActivityForResult(intentFeedback, BaseActivity.REQUEST_CODE_MENU);
                    } else {
                        LoginUtils.startLogin(BaseActivity.this, LoginActivity.REQUEST_CODE_FEEDBACK);
                    }
                    break;

                case 5:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    if (UserInfo.isUserLogged(BaseActivity.this)) {
                        Intent intentCollection = new Intent(BaseActivity.this, MyCollectionActivity.class);
                        BaseActivity.this.startActivity(intentCollection);
                    } else {
                        LoginUtils.startLogin(BaseActivity.this, LoginActivity.REQUEST_CODE_MY_COLLECTION);
                    }
                    break;
                case 6:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    if (UserInfo.isUserLogged(BaseActivity.this)) {
                        Intent intentSubscribedArtists = new Intent(BaseActivity.this, ListSubscribedArtistActivity.class);
                        BaseActivity.this.startActivity(intentSubscribedArtists);
                    } else {
                        LoginUtils.startLogin(BaseActivity.this, LoginActivity.REQUEST_CODE_SUBSCRIBED_ARTISTS);
                    }
                    break;
                case 8:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Intent intentAbout = new Intent(BaseActivity.this, AboutActivity.class);
                    BaseActivity.this.startActivity(intentAbout);
                    break;

                case 9:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    getShareData();
                    break;

                case 10:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    //clear cache
                    dialog = new MaterialDialog.Builder(BaseActivity.this)

                            .content(R.string.clear_cache_alert_content)
                            .positiveColor(getResources().getColor(R.color.menu_checked_color))
                            .negativeColor(getResources().getColor(R.color.font_color))
                            .positiveText(R.string.confirm)
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // TODO
                                    dialog.dismiss();
                                    DataCleanUtils.cleanAllApplicationData(BaseActivity.this);
                                    showCacheClearedToast();


                                }
                            })

                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // TODO
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    break;
                case 11:
                    Log.d("BaseActivity", "Logout");
                    if (UserInfo.isUserLogged(BaseActivity.this)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                        MessageBoxUtils.messageBoxWithButtons(BaseActivity.this, getString(R.string.logout_content),
                                new String[]{getString(R.string.confirm), getString(R.string.cancel)},
                                new Object[]{0, 1},
                                new MessageBoxUtils.MsgboxButtonListener[]{
                                        new MessageBoxUtils.MsgboxButtonListener() {
                                            @Override
                                            public boolean shouldCloseMessageBox(Object tag) {
                                                return true;
                                            }

                                            @Override
                                            public void onClick(Object tag) {
                                                UserInfo.logout(BaseActivity.this);

                                                updateUIforLogout();

                                            }
                                        },
                                        new MessageBoxUtils.MsgboxButtonListener() {
                                            @Override
                                            public boolean shouldCloseMessageBox(Object tag) {
                                                return true;
                                            }

                                            @Override
                                            public void onClick(Object tag) {
                                            }
                                        }
                                });
                    } else {
//                        UserInfo.logout(BaseActivity.this);
//                        BaseActivity.this.tvusername.setText(R.string.login_gologin);
//                        BaseActivity.this.tvuserdesc.setText(R.string.login_to_see_more);
//
//                        imageViewSetting.setVisibility(View.GONE);
//                        imageViewUserCover.setImageResource(bgRes[1]);
                    }
                case 4:
                default:
                    break;
            }
        }
    }

    protected void updateUIforLogout() {
        adapter.notifyDataSetChanged();
        BaseActivity.this.tvusername.setText(R.string.login_gologin);
        BaseActivity.this.tvuserdesc.setText(R.string.login_to_see_more);
        imageViewSetting.setVisibility(View.GONE);
        imageViewUserCover.setImageResource(bgRes[1]);
    }

    private void showCacheClearedToast() {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.clear_cache_toast_view,
                null);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
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

    protected void updateMenuForSubscribedAritstNotification()

    {
        this.newIcons.set(6, "0");
        this.adapter.notifyDataSetChanged();

    }

    protected void updateMenuForFeedbackNotification()

    {
        this.newIcons.set(3, "0");
        this.adapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOnPauseCalled) {
            if (FileCacheUtil.isCacheDataExist(CommonUtilities.CACHE_FILE_ARTIST_UPDATE, this)) {
                String value = FileCacheUtil.getCache(this, CommonUtilities.CACHE_FILE_ARTIST_UPDATE);
                if (value.equals("0")) {

                    this.newIcons.set(6, "0");
                }
            }


            this.adapter.notifyDataSetChanged();
            if (UserInfo.isUserLogged(this)) {

                this.tvusername.setText(UserInfo.getCurrentUser(this).getNickname());

                this.tvuserdesc.setText(UserInfo.getCurrentUser(this).getDesc());
                imageViewSetting.setVisibility(View.VISIBLE);
//            imageViewUserCover.setImageResource(bgRes[Integer.parseInt(UserInfo.getCurrentUser(this).getBackground_id()) - 1]);
                int resId;
                try {
                    resId = bgRes[Integer.parseInt(UserInfo.getCurrentUser(this).getBackground_id()) - 1];
                } catch (NumberFormatException e) {
                    resId = bgRes[0];
                } catch (IndexOutOfBoundsException e) {
                    resId = bgRes[0];
                }
                imageViewUserCover.setImageResource(resId);
            } else {
                BaseActivity.this.tvusername.setText(R.string.login_gologin);
                BaseActivity.this.tvuserdesc.setText(R.string.login_to_see_more);
                imageViewSetting.setVisibility(View.GONE);
                imageViewUserCover.setImageResource(bgRes[1]);

            }

            isOnPauseCalled = false;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPauseCalled = true;
    }
}
