package com.wewow;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

/**
 * Created by iris on 17/4/16.
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_wewow);
        setUpToolBar();
    }


    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));

    }

}
