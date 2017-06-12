package com.wewow.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
 * Created by iris on 17/3/23.
 */
public class ListViewArtistsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    private String id;
    private List<String> followStatus;

    public ListViewArtistsAdapter(Context context, ArrayList<HashMap<String, Object>> list,List<String> followStatus) {
        this.context = context;
        this.list = list;
       this.followStatus=followStatus;


    }

    @Override
    public int getCount() {
        // How many items are in the data set represented by this Adapter.(在此适配器中所代表的数据集中的条目数)
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // Get the data item associated with the specified position in the data set.(获取数据集中与指定索引对应的数据项)
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // Get the row id associated with the specified position in the list.(取在列表中与指定索引对应的行id)
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_lover_of_life, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageViewIcon);
            holder.textViewName = (TextView) convertView.findViewById(R.id.textViewNickName);
            holder.textViewDesc = (TextView) convertView.findViewById(R.id.textViewDesc);
            holder.textViewArticleCount = (TextView) convertView.findViewById(R.id.textViewArticle);
            holder.textViewFollowerCount = (TextView) convertView.findViewById(R.id.textViewFollow);
            holder.imageViewFollowed = (ImageView) convertView.findViewById(R.id.imageViewFollowed);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final HashMap<String, Object> stringObjectHashMap = list.get(position);
        Glide.with(context)
                .load(stringObjectHashMap.get("imageView").toString())
                .placeholder(R.drawable.artist_loading_spinner)
                .crossFade()
                .into(holder.imageView);
        holder.textViewName.setText(stringObjectHashMap.get("textViewName").toString());
        holder.textViewDesc.setText(stringObjectHashMap.get("textViewDesc").toString());
        holder.textViewArticleCount.setText(stringObjectHashMap.get("textViewArticleCount").toString());
        holder.textViewFollowerCount.setText(stringObjectHashMap.get("textViewFollowerCount").toString());
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


        return convertView;


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
                        if(responseObject.getJSONObject("result").getString("code").equals("403"))
                        {
                            LoginUtils.startLogin((Activity) context, LoginActivity.REQUEST_CODE_LOGIN);
                        }
                        else {
                            Toast.makeText(context, responseObject.getJSONObject("result").getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        followStatus.set(position, read == 0 ? "0" : "1");
                        holder.imageViewFollowed.setImageResource(read == 0 ? R.drawable.follow : R.drawable.followed);
                        if(read==1) {
                            MessageBoxUtils.messageBoxWithNoButton(context, true, read == 0 ? context.getResources()
                                    .getString(R.string.cancel_follow_artist_success) : context.getResources()
                                    .getString(R.string.follow_artist_success), 2500);
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

    static class ViewHolder {
        public ImageView imageView;
        public TextView textViewName;
        public TextView textViewDesc;
        public TextView textViewArticleCount;
        public TextView textViewFollowerCount;
        public ImageView imageViewFollowed;
    }

}