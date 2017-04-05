package com.wewow;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_lifelab_list);
        this.getSupportActionBar().setTitle(R.string.lifelab_title);
        this.setupUI();
    }

    private void setupUI() {
        this.lvlifelab = (ListView) this.findViewById(R.id.list_lifelab);
        this.lvlifelab.setAdapter(new LifeLabAdapter());
        View foot = View.inflate(this, R.layout.layout_lifelab_foot, null);
        foot.findViewById(R.id.tv_lab_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabActivity.this.startDataLoading();
            }
        });
        this.lvlifelab.addFooterView(foot);
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
                //view = View.inflate(LifeLabActivity.this, R.layout.layout_lifelab_item, null);
                view = View.inflate(LifeLabActivity.this, R.layout.layout_lifelab_item1, null);
            }
            LabData.LabCollection labcol = (LabData.LabCollection) this.getItem(i);
            TextView tv = (TextView) view.findViewById(R.id.tv_lab_title);
            tv.setText(labcol.title);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_lab_image);
            LifeLabActivity.this.loadItemImage(iv, labcol);
            //TextView tvvol = (TextView) view.findViewById(R.id.tv_lab_num);
            //tvvol.setText(String.format("vol. %d", labcol.order));
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
        this.toggleProgressDialog(true);
        List<Pair<String, String>> ps = new ArrayList<Pair<String, String>>();
        ps.add(new Pair<String, String>("page", String.valueOf(this.labData.getPageToLoad())));
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/index_collections", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        LifeLabActivity.this.toggleProgressDialog(false);
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

        public void addData(JSONObject jobj) {
            try {
                this.pageCount = jobj.getInt("total_pages");
                this.pagesize = jobj.getInt("pagesize");
                this.currentPage = jobj.getInt("current_page");
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
                    this.collections.add(lc);
                }
            } catch (JSONException e) {
                Log.e(TAG, "addData fail");
            }
        }

        public int getCurrentPage() {
            return this.currentPage;
        }

        public int getPageToLoad() {
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

        public class LabCollection {
            public long id;
            public String image;
            public String title;
            public String date;
            public String liked_count;
            public int order;
            public String read_count;
        }

        public boolean isItemLoaded(int i) {
            return this.collections.size() > i;
        }

        public LabCollection get(int i) {
            return this.isItemLoaded(i) ? this.collections.get(i) : null;
        }
    }

    private void loadItemImage(final ImageView target, final LabData.LabCollection data) {
        if (this.isImageSaved(data.image)) {
            byte[] buf = this.getImageFile(data.image);
            Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            BitmapDrawable bd = new BitmapDrawable(this.getResources(), bm);
            target.setImageDrawable(bd);
            //bm.recycle();
        } else {
            Object[] params = new Object[]{
                    data.image,
                    new HttpAsyncTask.TaskDelegate() {
                        @Override
                        public void taskCompletionResult(byte[] result) {
                            if (result != null) {
                                Bitmap bm = BitmapFactory.decodeByteArray(result, 0, result.length);
                                BitmapDrawable bd = new BitmapDrawable(LifeLabActivity.this.getResources(), bm);
                                target.setImageDrawable(bd);
                                //bm.recycle();
                                LifeLabActivity.this.saveImage(data.image, result);
                            }
                        }
                    },
                    WebAPIHelper.HttpMethod.GET,
                    null,
                    null,
                    true
            };
            new HttpAsyncTask().execute(params);
        }
    }

    private boolean isImageSaved(String url) {
        String fn = this.getImageFilePath(url);
        File f = new File(fn);
        return f.exists() && f.isFile() && (System.currentTimeMillis() - f.lastModified() < 1000 * 3600);
//        return false;
    }

    private void saveImage(String url, byte[] buf) {
        Log.d(TAG, String.format("saveImage %s %d bytes", url, buf.length));
        String fn = this.getImageFilePath(url);
        try {
            FileOutputStream fos = new FileOutputStream(fn);
            fos.write(buf, 0, buf.length);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, String.format("write file error for %s", url));
        }
    }

    private byte[] getImageFile(String url) {
        String path = this.getImageFilePath(url);
        try {
            File f = new File(path);
            byte[] buf = new byte[(int) f.length()];
            FileInputStream fis = new FileInputStream(path);
            fis.read(buf, 0, buf.length);
            fis.close();
            return buf;
        } catch (FileNotFoundException e) {
            Log.e(TAG, String.format("getImageFile: fail %s", url));
            return null;
        } catch (IOException e) {
            Log.e(TAG, String.format("getImageFile: IO error %s", e.getMessage()));
            return null;
        }
    }

    private String getImageFilePath(String url) {
        File root = this.getCacheDir();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] in = url.getBytes("utf-8");
            md.update(in);
            byte[] out = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : out) {
                sb.append(String.format("%02X", b));
            }
            String fn = String.format("%s/%s", root.getAbsolutePath(), sb.toString());
            Log.d(TAG, String.format("file %s for url %s", fn, url));
            return fn;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
