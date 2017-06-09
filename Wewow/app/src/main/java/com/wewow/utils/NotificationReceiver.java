package com.wewow.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;

import com.avos.avoscloud.AVOSCloud;
import com.wewow.ArticleActivity;
import com.wewow.DetailArtistActivity;
import com.wewow.LifeLabItemActivity;
import com.wewow.LifePostActivity;
import com.wewow.MainActivity;
import com.wewow.R;
import com.wewow.SubjectActivity;
import com.wewow.WebPageActivity;
import com.wewow.dto.LabCollection;

import org.json.JSONObject;

/**
 * Created by iris on 17/3/6.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("com.wewow.push")) {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
                final String message = json.getString("alert");
                final String title = json.getString("title");
                final int target_type = Integer.parseInt(json.getString("target_type"));
                final String target_id = json.getString("target_id");
                Intent resultIntent = new Intent(AVOSCloud.applicationContext, MainActivity.class);
                switch (target_type) {
                    case 0:

                        break;
                    case 1:
                        //life lab
                        resultIntent = new Intent(AVOSCloud.applicationContext, LifeLabItemActivity.class);
                        LabCollection lc = new LabCollection();
                        lc.title = title;
                        lc.id =Long.parseLong(target_id);
                        resultIntent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);
                        //TO-DO
                        break;
                    case 2:
                        //life post
                        resultIntent = new Intent(AVOSCloud.applicationContext, LifePostActivity.class);
                        resultIntent.putExtra(LifePostActivity.POST_ID, Integer.parseInt(target_id));
                        break;
                    case 3:
                        //article
                        resultIntent = new Intent(AVOSCloud.applicationContext, ArticleActivity.class);
                        resultIntent.putExtra(ArticleActivity.ARTICLE_ID, Integer.parseInt(target_id));
                        break;
                    case 4:
                        //artist
                        resultIntent = new Intent(AVOSCloud.applicationContext, DetailArtistActivity.class);
                        intent.putExtra("id", Integer.parseInt(target_id));
                        break;
                    case 5:
                        //subject
                        resultIntent = new Intent(AVOSCloud.applicationContext, SubjectActivity.class);
                        resultIntent.putExtra("id", Integer.parseInt(target_id));
                        break;
                    case 6:
                        //h5 page
                        resultIntent = new Intent(AVOSCloud.applicationContext, WebPageActivity.class);
                        resultIntent.putExtra("url", target_id);

                        break;
                    default:
                        break;
                }
                long[] pattern = { 500, 500, 500, 500, 500, 500, 500, 500, 500 };
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(AVOSCloud.applicationContext, 0, resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(AVOSCloud.applicationContext)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(
                                        title)
                                .setContentText(message)
                                .setTicker(message).setLights(Color.BLUE, 500, 500).setVibrate(pattern)
                        .setAutoCancel(true);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);

                int mNotificationId = 10086;
                NotificationManager mNotifyMgr =
                        (NotificationManager) AVOSCloud.applicationContext
                                .getSystemService(
                                        Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        } catch (Exception e) {

        }
    }
}