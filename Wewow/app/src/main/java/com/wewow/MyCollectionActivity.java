package com.wewow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyCollectionActivity extends BaseActivity {

    private static final String TAG = "MyCollectionActivity";
    private LinearLayout labs_container;
    private JSONObject likedinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_my_collection);
        this.getSupportActionBar().setTitle(R.string.my_collection);
        this.setupUI();
        Log.d(TAG, "onCreate: " + this.getSupportActionBar().getTitle());
        this.loadData();
    }

    private void setupUI() {
        this.labs_container = (LinearLayout) this.findViewById(R.id.collected_lablist);
    }

    private void loadData() {
        if (!UserInfo.isUserLogged(this)) {
            Intent i = new Intent(this, LoginActivity.class);
            this.startActivity(i);
            this.finish();
            return;
        }
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("user_id", UserInfo.getCurrentUser(this).getId().toString()));
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/user_liked_ac", CommonUtilities.WS_HOST), fields),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        try {
                            JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                            JSONObject data = jobj.getJSONObject("result").getJSONObject("data");
                            MyCollectionActivity.this.onDataLoad(data);
                        } catch (JSONException e) {
                            Log.e(TAG, String.format("get collection list fail: %s", e.getMessage()));
                            Toast.makeText(MyCollectionActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void onDataLoad(JSONObject jobj) {
        this.labs_container.removeAllViews();
        this.likedinfo = jobj;
        try {
            JSONArray articles = jobj.getJSONArray("articles");
            JSONArray collections = jobj.getJSONArray("collections");
            for (int i = 0; i < collections.length(); i++) {
                JSONObject collection = collections.getJSONObject(i);
                TextView col = (TextView) View.inflate(this, R.layout.liked_collection, null);
                col.setText(collection.optString("collection_title"));
                col.setTag(collection.optString("collection_id"));
                this.labs_container.addView(col);
            }
        } catch (JSONException e) {
            Log.e(TAG, String.format("get collection list fail: %s", e.getMessage()));
            Toast.makeText(MyCollectionActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
        }
    }
}
