package com.wewow.utils;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.wewow.R;

/**
 * Created by iris on 17/3/6.
 */
public class NetStateUtils extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(!gprs.isConnected() && !wifi.isConnected())
        {
            if(SettingUtils.get(context, CommonUtilities.NETWORK_STATE, false))
            {
//                Toast.makeText(context,context.getResources().getString(R.string.networkError),Toast.LENGTH_LONG).show();

            }
            SettingUtils.set(context, CommonUtilities.NETWORK_STATE, false);


        }
        else{
//
            if(!SettingUtils.get(context,CommonUtilities.NETWORK_STATE,true))
            {
              //TO DO send blocked requests
            }
            SettingUtils.set(context,CommonUtilities.NETWORK_STATE,true);
        }
    }
}