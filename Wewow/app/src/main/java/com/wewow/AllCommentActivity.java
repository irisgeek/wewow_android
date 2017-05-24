package com.wewow;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.dto.Comment;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AllCommentActivity extends Activity implements View.OnClickListener {

    private Context context;
    private ListView list_comment;
    private CircleProgressBar progressBar;
    private View foot;
    private int page = 1;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private String articleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_all_comment);
        articleId = getIntent().getStringExtra("articleId");
        initView();
    }

    private void initView() {
        list_comment = (ListView) findViewById(R.id.list_comment);
        progressBar = (CircleProgressBar) this.findViewById(R.id.progressBar);
        foot = View.inflate(this, R.layout.lifelab_foot, null);
//        list_comment.addFooterView(foot, null, false);
        adapter = new CommentAdapter();
        list_comment.setAdapter(adapter);

        findViewById(R.id.back).setOnClickListener(this);

        getCommentData();
    }

    private void initData(List<Comment> list) {

    }

    private void getCommentData() {
        List<Pair<String, String>> ps = new ArrayList<>();
        ps.add(new Pair<>("page", page + ""));
        ps.add(new Pair<>("item_type", "article"));
        ps.add(new Pair<>("item_id", articleId));
        if (UserInfo.isUserLogged(this)) {
            ps.add(new Pair<>("user_id", UserInfo.getCurrentUser(this).getId() + ""));
        }
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/get_comment", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(context, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject data = r.getJSONObject("data");
                                Type type = new TypeToken<ArrayList<Comment>>(){}.getType();
                                List<Comment> list = new Gson().fromJson(data.optString("comment_list"), type);
                                commentList.addAll(list);
                                adapter.notifyDataSetChanged();
//                                initData(list);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
        }
    }

    private class CommentAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return commentList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(context, R.layout.article_comment, null);
                holder = new ViewHolder();
                holder.author = (TextView) convertView.findViewById(R.id.article_comment_author);
                holder.date = (TextView) convertView.findViewById(R.id.article_comment_date);
                holder.content = (TextView) convertView.findViewById(R.id.article_comment_content);
                holder.liked_count = (TextView) convertView.findViewById(R.id.article_comment_liked_count);
                convertView.setTag(holder);
            } else{
                holder = (ViewHolder) convertView.getTag();
            }
            Comment comment = commentList.get(position);
            holder.author.setText(comment.getUser());
            holder.date.setText(comment.getTime());
            holder.content.setText(comment.getContent());
            holder.liked_count.setText(comment.getLiked_count() + "");
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        class ViewHolder{
            TextView author, date, content, liked_count;
        }
    }
}
