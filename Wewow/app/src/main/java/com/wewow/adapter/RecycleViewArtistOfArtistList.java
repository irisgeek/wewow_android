package com.wewow.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wewow.LoginActivity;
import com.wewow.R;
import com.wewow.UserInfo;
import com.wewow.netTask.ITask;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.FileCacheUtil;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.Utils;

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
 * Created by iris on 17/4/5.
 */

public class RecycleViewArtistOfArtistList
        extends RecyclerView.Adapter<RecycleViewArtistOfArtistList.ViewHolder> {

    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    private String id;
    private List<String> followStatus;
    private List<Integer> followCount;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {


        public final View mView;
        public ImageView imageView;
        public ImageView imageViewTop;
        public ImageView imageViewBottom;
        public TextView textViewName;
        public TextView textViewDesc;
        public TextView textViewArticleCount;
        public TextView textViewFollowerCount;
        public ImageView imageViewFollowed;
        public ImageView imageViewDivider;
        public LinearLayout layoutHeader;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = (ImageView) mView.findViewById(R.id.imageViewIcon);
            textViewName = (TextView) mView.findViewById(R.id.textViewNickName);
            textViewDesc = (TextView) mView.findViewById(R.id.textViewDesc);
            textViewArticleCount = (TextView) mView.findViewById(R.id.textViewArticle);
            textViewFollowerCount = (TextView) mView.findViewById(R.id.textViewFollow);
            imageViewFollowed = (ImageView) mView.findViewById(R.id.imageViewFollowed);
            layoutHeader=(LinearLayout)mView.findViewById(R.id.layoutHeader);
            imageViewBottom=(ImageView)mView.findViewById(R.id.imageViewBottomLine);
            imageViewTop=(ImageView)mView.findViewById(R.id.imageViewTopLine);
            imageViewDivider=(ImageView)mView.findViewById(R.id.imageViewDivider);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textViewName.getText();
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewArtistOfArtistList(Context context, ArrayList<HashMap<String, Object>> list, List<String> followStatus,List<Integer> followCount) {
        this.context = context;
        this.list = list;
        this.followStatus = followStatus;
        this.followCount = followCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_lover_of_life, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final HashMap<String, Object> stringObjectHashMap = list.get(position);
        Glide.with(context)
                .load(stringObjectHashMap.get("imageView").toString())
                .placeholder(R.drawable.artist_loading_spinner)
                .crossFade()
                .into(holder.imageView);
        holder.textViewName.setText(stringObjectHashMap.get("textViewName").toString());
        holder.textViewDesc.setText(stringObjectHashMap.get("textViewDesc").toString());
        holder.textViewArticleCount.setText(stringObjectHashMap.get("textViewArticleCount").toString());

        holder.textViewFollowerCount.setText(followCount.get(position)+"");
        if (followStatus.get(position).toString().equals("1")) {
            holder.imageViewFollowed.setImageResource(R.drawable.followed);
        } else {
            holder.imageViewFollowed.setImageResource(R.drawable.follow);
        }
        holder.imageViewFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (UserInfo.isUserLogged(context)) {

                    id = stringObjectHashMap.get("id").toString();
                    postReadToServer(holder,position, id, followStatus.get(position).equals("1") ? 0 : 1);

                } else {
                    LoginUtils.startLogin((Activity) context, LoginActivity.REQUEST_CODE_LOGIN);
                }

            }
        });
        if(position==0)
        {
            holder.layoutHeader.setVisibility(View.VISIBLE);
            holder.imageViewBottom.setVisibility(View.VISIBLE);
            holder.imageViewTop.setVisibility(View.GONE);
            holder.imageViewDivider.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.layoutHeader.setVisibility(View.GONE);
            if(position==(list.size()-1))
            {
                holder.imageViewBottom.setVisibility(View.GONE);
                holder.imageViewTop.setVisibility(View.VISIBLE);
                holder.imageViewDivider.setVisibility(View.GONE);
            }
            else
            {
                holder.imageViewBottom.setVisibility(View.VISIBLE);
                holder.imageViewTop.setVisibility(View.VISIBLE);
                holder.imageViewDivider.setVisibility(View.VISIBLE);
            }
        }


    }
    private void postReadToServer(final ViewHolder holder,final int position, final String artistId, final int read) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        String userId = UserInfo.getCurrentUser(context).getId().toString();
        String token = UserInfo.getCurrentUser(context).getToken().toString();


        iTask.followArtist(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(context), userId, artistId, token, read, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    JSONObject responseObject = new JSONObject(realData);

                    if (!responseObject.getJSONObject("result").getString("code").equals("0")) {
                        if (responseObject.getJSONObject("result").getString("code").equals("403")) {
                            LoginUtils.startLogin((Activity) context, LoginActivity.REQUEST_CODE_LOGIN);
                        } else {
                            Toast.makeText(context, responseObject.getJSONObject("result").getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        followStatus.set(position, read == 0 ? "0" : "1");
                        holder.imageViewFollowed.setImageResource(read == 0 ? R.drawable.follow : R.drawable.followed);
                        if (read == 1) {
                            MessageBoxUtils.messageBoxWithNoButton(context, true, read == 0 ? context.getResources()
                                    .getString(R.string.cancel_follow_artist_success) : context.getResources()
                                    .getString(R.string.follow_artist_success), 1000);
                            int followC=followCount.get(position);

                            followCount.set(position,followC+1);

                            holder.textViewFollowerCount.setText(followCount.get(position)+"");

                        }
                        else {
                            int followC=followCount.get(position);

                            followCount.set(position,followC-1);
                            holder.textViewFollowerCount.setText(followCount.get(position)+"");
                        }
                        notifyDataSetChanged();
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_LIST, context);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_DETAIL + artistId, context);
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_SUBSCRIBED_ARTISTS_LIST, context);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, context.getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}