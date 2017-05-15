package com.wewow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.dto.LabCollection;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suncjs on 2017/3/26.
 */

public class LifeLabActivity extends BaseActivity {

    private ListView lvlifelab;
    private ProgressDialog pdlg = null;
    private int page = 1;
    private static final String TAG = "LifeLabActivity";
    private LabData labData = new LabData();
    private SwipeRefreshLayout swipe;
    private View foot;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_lifelab_list);
        this.getSupportActionBar().setTitle(R.string.lifelab_title);
        this.setupUI();
    }

    private void setupUI() {
        this.swipe = (SwipeRefreshLayout) this.findViewById(R.id.lifelab_swipe);
        this.foot = View.inflate(this, R.layout.lifelab_foot, null);
        this.foot.findViewById(R.id.tv_lab_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabActivity.this.startDataLoading();
            }
        });
        this.lvlifelab = (ListView) this.findViewById(R.id.list_lifelab);
        this.lvlifelab.addFooterView(this.foot);
        this.lvlifelab.setAdapter(new LifeLabAdapter());
        this.lvlifelab.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LabCollection lc = (LabCollection) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(LifeLabActivity.this, LifeLabItemActivity.class);
                intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);
                Bundle b = new Bundle();
                LifeLabActivity.this.startActivity(intent);
            }
        });
        this.lvlifelab.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                //Log.d(TAG, String.format("onScroll firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", i, i1, i2));
                if ((LifeLabActivity.this.labData.getAllCount() == 0) || LifeLabActivity.this.labData.isAllLoaded()) {
                    LifeLabActivity.this.foot.setVisibility(View.GONE);
                    Log.w(TAG, String.format("onScroll: allCount:%d  isAllLoaded:%b", LifeLabActivity.this.labData.getAllCount(), LifeLabActivity.this.labData.isAllLoaded()));
                    return;
                }
                LifeLabActivity.this.foot.setVisibility(View.VISIBLE);
                if (i + i1 == i2) {
                    Log.d(TAG, "onScroll: Show refresh");
                    if (LifeLabActivity.this.labData.isAllLoaded()) {
                        LifeLabActivity.this.foot.setVisibility(View.GONE);
                    } else {
                        LifeLabActivity.this.foot.setVisibility(View.VISIBLE);
                    }
                    LifeLabActivity.this.swipe.setRefreshing(true);
                    LifeLabActivity.this.startDataLoading();
                }
            }
        });
        this.startDataLoading();
    }

    private class LifeLabAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return LifeLabActivity.this.labData.getCount();
        }

        @Override
        public Object getItem(int i) {
            return LifeLabActivity.this.labData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(LifeLabActivity.this, R.layout.lifelab_item, null);
                //view = View.inflate(LifeLabActivity.this, R.layout.layout_lifelab_item1, null);
            }
            LabCollection labcol = (LabCollection) this.getItem(i);
            TextView tv = (TextView) view.findViewById(R.id.tv_lab_title);
            tv.setText(labcol.title);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_lab_image);
            LifeLabActivity.this.loadItemImage(iv, labcol);
            TextView tvvol = (TextView) view.findViewById(R.id.tv_lab_num);
            tvvol.setText(String.format("vol. %d", labcol.order));
            TextView tvread = (TextView) view.findViewById(R.id.tv_lab_read);
            tvread.setText(labcol.read_count);
            TextView tvlike = (TextView) view.findViewById(R.id.tv_lab_collection);
            tvlike.setText(labcol.liked_count);
            return view;
        }
    }

    private void startDataLoading() {
        if (this.hasLocalData()) {
            this.loadLocalData();
        } else {
            this.loadRemoteData();
        }
    }

    private boolean hasLocalData() {
        return false;
    }

    private void loadLocalData() {
        // TODO: 2017/3/26
    }

    private void loadRemoteData() {
        //this.toggleProgressDialog(true);
        if (this.isLoading) {
            return;
        }
        this.isLoading = true;
        List<Pair<String, String>> ps = new ArrayList<Pair<String, String>>();
        ps.add(new Pair<String, String>("page", String.valueOf(this.labData.getPageToLoad())));
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/index_collections", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        //LifeLabActivity.this.toggleProgressDialog(false);
                        LifeLabActivity.this.swipe.setRefreshing(false);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(LifeLabActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                                Log.d(TAG, String.format("returned code: %d", r.getInt("code")));
                            } else {
                                LifeLabActivity.this.newDataLoad(r.getJSONObject("data"));
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                        LifeLabActivity.this.isLoading = false;
                    }
                },
                WebAPIHelper.HttpMethod.GET,
                null,
                null,
                true
        };
        new HttpAsyncTask().execute(params);
    }

    private void toggleProgressDialog(boolean isshow) {
        if (isshow && ((this.pdlg == null) || !this.pdlg.isShowing())) {
            this.pdlg = ProgressDialog.show(this, null, null, true, false);
        } else if (!isshow) {
            this.pdlg.dismiss();
        }
    }

    private void newDataLoad(JSONObject data) {
        this.labData.addData(data);
        ListAdapter la = this.lvlifelab.getAdapter();
        LifeLabAdapter adp = (LifeLabAdapter) (la instanceof LifeLabAdapter ? la : ((HeaderViewListAdapter) la).getWrappedAdapter());
        adp.notifyDataSetChanged();
    }

    private class LabData {

        private List<LabCollection> collections = new ArrayList<LabCollection>();
        private int pagesize = 10;
        private int collectionCount = 0;
        private int pageCount = 0;
        private int currentPage = -1;

        public LabData() {

        }

        public synchronized void addData(JSONObject jobj) {
            Log.d(TAG, String.format("before add allcount: %d  count: %d ", this.getAllCount(), this.getCount()));
            try {
                this.pageCount = jobj.getInt("total_pages");
                this.pagesize = jobj.getInt("pagesize");
                this.currentPage = jobj.getInt("current_page");
                this.collectionCount = jobj.getInt("collection_count");
                JSONArray lst = jobj.getJSONArray("collection_list");
                for (int i = 0; i < lst.length(); i++) {
                    JSONObject jj = lst.getJSONObject(i);
                    LabCollection lc = new LabCollection();
                    lc.id = jj.getLong("collection_id");
                    lc.image = jj.getString("collection_image");
                    lc.title = jj.getString("collection_title");
                    lc.date = jj.getString("date");
                    lc.liked_count = jj.getString("liked_count");
                    lc.order = jj.getInt("order");
                    lc.read_count = jj.getString("read_count");
                    lc.image_642_320 = jj.getString("image_642_320");
                    lc.image_688_316 = jj.getString("image_688_316");
                    this.collections.add(lc);
                }
            } catch (JSONException e) {
                Log.e(TAG, "addData fail");
            }
            Log.d(TAG, String.format("after add allcount: %d  count: %d ", this.getAllCount(), this.getCount()));
        }

        public int getCurrentPage() {
            return this.currentPage;
        }

        public synchronized int getPageToLoad() {
            return this.currentPage <= 0 ? 1 : this.currentPage + 1;
        }

        public int getCount() {
            return this.collections.size();
        }

        public int getAllCount() {
            return this.collectionCount;
        }

        public int getPageCount() {
            return this.pageCount;
        }

        public boolean isItemLoaded(int i) {
            return this.collections.size() > i;
        }

        public LabCollection get(int i) {
            return this.isItemLoaded(i) ? this.collections.get(i) : null;
        }

        public boolean isAllLoaded() {
            return this.getCount() == this.getAllCount();
        }
    }

    private void loadItemImage(final ImageView target, final LabCollection data) {
        new RemoteImageLoader(this, data.image_688_316, new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                BitmapDrawable oldbd = (BitmapDrawable) target.getDrawable();
                target.setImageDrawable(dr);
                if (oldbd != null) {
                    oldbd.getBitmap().recycle();
                }
            }
        });
    }
}
