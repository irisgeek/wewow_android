package com.wewow.adapter;

import android.content.Context;
import android.media.Image;
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
import java.util.List;

/**
 * Created by iris on 17/3/23.
 */
public class ListViewMenuAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    private List<String> newIcons;
    public ListViewMenuAdapter(Context context, ArrayList<HashMap<String, Object>> list,List<String> newIcons)
    {
        this.context = context;
        this.list=list;
        this.newIcons=newIcons;

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
            convertView = inflater.inflate(R.layout.list_item_drawer, null);
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageViewIcon);
            holder.textViewTitle = (TextView)convertView.findViewById(R.id.textViewMenuItem);
            holder.imageNew=(ImageView)convertView.findViewById(R.id.imageNew);

            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        HashMap<String, Object> stringObjectHashMap = list.get(position);
        holder.imageView.setImageResource((int)stringObjectHashMap.get("icon"));
        holder.textViewTitle.setText(stringObjectHashMap.get("menuText").toString());
        if(newIcons.get(position).equals("1")) {
            holder.imageNew.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.imageNew.setVisibility(View.GONE);

        }


        return convertView;



}

    static class ViewHolder
    {
        public ImageView imageView;
        public TextView textViewTitle;
        public ImageView imageNew;
    }

}