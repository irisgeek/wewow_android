package com.wewow.adapter;

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
import com.wewow.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iris on 17/3/23.
 */
public class ListViewArtistsAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    private String id;

    public ListViewArtistsAdapter(Context context, ArrayList<HashMap<String, Object>> list)
    {
        this.context = context;
        this.list=list;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_lover_of_life, null);
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageViewIcon);
            holder.textViewName = (TextView)convertView.findViewById(R.id.textViewNickName);
            holder.textViewDesc= (TextView)convertView.findViewById(R.id.textViewDesc);
            holder.textViewArticleCount=(TextView)convertView.findViewById(R.id.textViewArticle);
            holder.textViewFollowerCount=(TextView)convertView.findViewById(R.id.textViewFollow);
            holder.imageViewFollowed=(ImageView)convertView.findViewById(R.id.imageViewFollowed);

            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final HashMap<String, Object> stringObjectHashMap = list.get(position);
        Glide.with(context)
                .load(stringObjectHashMap.get("imageView").toString())
                .placeholder(R.drawable.banner_loading_spinner)
                .crossFade()
                .into(holder.imageView);
        holder.textViewName.setText(stringObjectHashMap.get("textViewName").toString());
        holder.textViewDesc.setText(stringObjectHashMap.get("textViewDesc").toString());
        holder.textViewArticleCount.setText(stringObjectHashMap.get("textViewArticleCount").toString());
        holder.textViewFollowerCount.setText(stringObjectHashMap.get("textViewFollowerCount").toString());
        if(stringObjectHashMap.get("imageViewFollowed").toString().equals("1")) {
            holder.imageViewFollowed.setImageResource(R.drawable.followed);



        }
        else
        {
            holder.imageViewFollowed.setImageResource(R.drawable.follow);
            holder.imageViewFollowed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.imageViewFollowed.setImageResource(R.drawable.followed);

                    if(UserInfo.isUserLogged(context)) {

                        id = stringObjectHashMap.get("id").toString();
                        postReadToServer(id);
                    }
                    else
                    {
                        Intent i = new Intent();
                        i.setClass(context, LoginActivity.class);
                        context.startActivity(i);
                    }

                }
            });


        }


        return convertView;



}

    private void postReadToServer(String artistId) {

        ITask iTask = Utils.getItask(CommonUtilities.WS_HOST);

        String   userId = UserInfo.getCurrentUser(context).getId().toString();
        String token=UserInfo.getCurrentUser(context).getToken().toString();
        int read=1;


        iTask.followArtist(CommonUtilities.REQUEST_HEADER_PREFIX + Utils.getAppVersionName(context), userId, artistId, token, read, new Callback<JSONObject>() {

            @Override
            public void success(JSONObject object, Response response) {


                try {
                    String realData = Utils.convertStreamToString(response.getBody().in());
                    JSONObject responseObject=new JSONObject(realData);

                    if (!responseObject.getJSONObject("result").getString("code").equals("0")) {
                        Toast.makeText(context, context.getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();


                    }
                    else
                    {
                        FileCacheUtil.clearCacheData(CommonUtilities.CACHE_FILE_ARTISTS_LIST, context);
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

    static class ViewHolder
    {
        public ImageView imageView;
        public TextView textViewName;
        public TextView textViewDesc;
        public TextView textViewArticleCount;
        public TextView textViewFollowerCount;
        public ImageView imageViewFollowed;
    }

}