package com.wewow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wewow.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iris on 17/3/23.
 */
public class ListViewArtistsAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
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
        ViewHolder holder;
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
        HashMap<String, Object> stringObjectHashMap = list.get(position);
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
        }


        return convertView;



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