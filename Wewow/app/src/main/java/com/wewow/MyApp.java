package com.wewow;

import android.app.Application;

import com.growingio.android.sdk.collection.Configuration;
import com.growingio.android.sdk.collection.GrowingIO;

/**
 * Created by iris on 17/4/24.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GrowingIO.startWithConfiguration(this, new Configuration()
                .useID()
                .trackAllFragments()
                .setChannel(getResources().getString(R.string.growingio_channel)));

    }
}