package com.wewow;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.dto.Comment;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AllCommentActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private Context context;
    private ListView list_comment;
    private CircleProgressBar progressBar;
    private View footer, view_empty;
    private EditText editTextContent;
    private int page = 1;
    private int comment_count;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private String articleId;
    private boolean dataChange;

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
        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        view_empty = findViewById(R.id.view_empty);
        editTextContent = (EditText) findViewById(R.id.editTextContent);
        footer = View.inflate(this, R.layout.lifelab_foot, null);
        ImageView iv_loading = (ImageView) footer.findViewById(R.id.iv_loading);
        Glide.with(this).load(R.drawable.bottom_loading).into(iv_loading);
        list_comment.addFooterView(footer, null, false);
        adapter = new CommentAdapter();
        list_comment.setAdapter(adapter);

        list_comment.setOnScrollListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.imageViewSend).setOnClickListener(this);

        getCommentData();
    }

    private void getCommentData() {
        List<Pair<String, String>> ps = new ArrayList<>();
        ps.add(new Pair<>("page", page + ""));
        ps.add(new Pair<>("item_type", "article"));
        ps.add(new Pair<>("item_id", articleId));
        if (UserInfo.isUserLogged(this)) {
            ps.add(new Pair<>("user_id", UserInfo.getCurrentUser(this).getId() + ""));
        }
        progressBar.setVisibility(View.VISIBLE);
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
                                comment_count = data.optInt("comment_count");
                                Type type = new TypeToken<ArrayList<Comment>>(){}.getType();
                                List<Comment> list = new Gson().fromJson(data.optString("comment_list"), type);
                                if(list != null && list.size() > 0){
                                    view_empty.setVisibility(View.GONE);
                                    commentList.addAll(list);
                                    adapter.notifyDataSetChanged();
                                }
                                if(isAllLoaded()){
                                    list_comment.removeFooterView(footer);
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void addComment(String content) {
        if(content.isEmpty())
            return;
        UserInfo ui = UserInfo.getCurrentUser(context);
        List<Pair<String, String>> ps = new ArrayList<>();
        ps.add(new Pair<>("token", ui.getToken()));
        ps.add(new Pair<>("user_id", ui.getId() + ""));
        ps.add(new Pair<>("item_type", "article"));
        ps.add(new Pair<>("item_id", articleId));
        ps.add(new Pair<>("content", content));
        ArrayList<Pair<String, String>> header = new ArrayList<>();
        header.add(WebAPIHelper.getHttpFormUrlHeader());
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/add_comment", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(context, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                if(r.getInt("code")==403){
                                    LoginUtils.startLogin(AllCommentActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                                }
                                else {
                                    Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                dataChange = true;
                                JSONObject data = r.getJSONObject("data");
                                Comment comment = new Gson().fromJson(data.optString("new_comment"), Comment.class);
                                commentList.add(comment);
                                adapter.notifyDataSetChanged();
                                editTextContent.getText().clear();
                                hideSoftInput(editTextContent);
                                view_empty.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                WebAPIHelper.buildHttpQuery(ps).getBytes(),
                header
        };
        new HttpAsyncTask().execute(params);
    }

    private void postLike(final CommentAdapter.ViewHolder holder, final Comment comment){
        UserInfo ui = UserInfo.getCurrentUser(context);
        List<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<>("user_id", ui.getId() + ""));
        fields.add(new Pair<>("token", ui.getToken()));
        fields.add(new Pair<>("item_type", "comment"));
        fields.add(new Pair<>("item_id", comment.getId()));
        fields.add(new Pair<>("like", comment.getLiked() == 1 ? "0" : "1"));
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Content-Type", "application/x-www-form-urlencoded"));
        Object[] params = new Object[]{
                String.format("%s/like", CommonUtilities.WS_HOST),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(context, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                if(r.getInt("code")==403){
                                    LoginUtils.startLogin(AllCommentActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                                }
                                else {
                                    Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                dataChange = true;
                                comment.setLiked(comment.getLiked() == 1 ? 0 : 1);
                                if(comment.getLiked() == 1){
                                    comment.setLiked_count(comment.getLiked_count() + 1);
                                } else{
                                    comment.setLiked_count(comment.getLiked_count() - 1);
                                }
                                holder.iv_comment_liked.setImageResource(comment.getLiked() == 1 ? R.drawable.liked : R.drawable.like);
                                holder.liked_count.setText(comment.getLiked_count() + "");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                WebAPIHelper.buildHttpQuery(fields).getBytes(),
                headers
        };
        new HttpAsyncTask().execute(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                onExit();
                break;
            case R.id.imageViewSend:
                addComment(editTextContent.getText().toString().trim());
                break;
        }
    }

    private boolean isAllLoaded() {
        return comment_count == 0 || comment_count <= commentList.size();
    }

    private void hideSoftInput(EditText editText){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0) ;
    }

    private void onExit() {
        String s = editTextContent.getText().toString().trim();
        if (s.isEmpty()) {
            if(dataChange){
                setResult(RESULT_OK);
            }
            this.finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.addpost_quit_prompt)
                .setNegativeButton(R.string.prompt_denied, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(R.string.prompt_comfirmd, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if(dataChange){
                            setResult(RESULT_OK);
                        }
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        onExit();
    }

    int scrollState;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        switch (scrollState){
            case SCROLL_STATE_TOUCH_SCROLL:
                hideSoftInput(editTextContent);
                break;
            case SCROLL_STATE_IDLE:
                if (view.getLastVisiblePosition() == (view.getCount() - 1) && !isAllLoaded()) {
                    page ++;
                    getCommentData();
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private class CommentAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return commentList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(context, R.layout.article_comment, null);
                holder = new ViewHolder();
                holder.author = (TextView) convertView.findViewById(R.id.article_comment_author);
                holder.date = (TextView) convertView.findViewById(R.id.article_comment_date);
                holder.content = (TextView) convertView.findViewById(R.id.article_comment_content);
                holder.liked_count = (TextView) convertView.findViewById(R.id.article_comment_liked_count);
                holder.iv_comment_liked = (ImageView) convertView.findViewById(R.id.iv_comment_liked);
                holder.comment_like_area = convertView.findViewById(R.id.comment_like_area);
                convertView.setTag(holder);
            } else{
                holder = (ViewHolder) convertView.getTag();
            }
            final Comment comment = commentList.get(position);
            holder.author.setText(comment.getUser());
            holder.date.setText(comment.getTime());
            holder.content.setText(comment.getContent());
            holder.liked_count.setText(comment.getLiked_count() + "");
            holder.iv_comment_liked.setImageResource(comment.getLiked() == 1 ? R.drawable.liked : R.drawable.like);
            holder.comment_like_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postLike(holder, comment);
                }
            });
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
            ImageView iv_comment_liked;
            View comment_like_area;
        }
    }
}
