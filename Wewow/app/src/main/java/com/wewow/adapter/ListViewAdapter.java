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
public class ListViewAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    public ListViewAdapter(Context context,ArrayList<HashMap<String, Object>> list)
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
            convertView = inflater.inflate(R.layout.list_item_life_institue_recommended, null);
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageViewInstitue);
            holder.textViewTitle = (TextView)convertView.findViewById(R.id.textViewTitle);
            holder.textViewNum = (TextView)convertView.findViewById(R.id.textViewNum);
            holder.textViewCollection=(TextView)convertView.findViewById(R.id.textViewCollection);
            holder.textViewRead=(TextView)convertView.findViewById(R.id.textViewRead);

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
        holder.textViewTitle.setText(stringObjectHashMap.get("textViewTitle").toString());
        holder.textViewNum.setText(stringObjectHashMap.get("textViewNum").toString());
        holder.textViewRead.setText(stringObjectHashMap.get("textViewRead").toString());
        holder.textViewCollection.setText(stringObjectHashMap.get("textViewCollection").toString());

        return convertView;



}

    static class ViewHolder
    {
        public ImageView imageView;
        public TextView textViewTitle;
        public TextView textViewNum;
        public TextView textViewRead;
        public TextView textViewCollection;
    }

}