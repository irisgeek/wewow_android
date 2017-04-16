package com.wewow.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wewow.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iris on 17/4/13.
 */
public class ListViewFeedbackAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    public ListViewFeedbackAdapter(Context context, ArrayList<HashMap<String, Object>> list)
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
            convertView = inflater.inflate(R.layout.list_item_feedback, null);
            holder.layoutWhite = (LinearLayout)convertView.findViewById(R.id.layoutWhite);
            holder.textViewWhite = (TextView)convertView.findViewById(R.id.textFeedbackWhite);
            holder.imageViewWhite= (ImageView)convertView.findViewById(R.id.imageViewWhite);

            holder.layoutBlack = (LinearLayout)convertView.findViewById(R.id.layoutBlack);
            holder.textViewBlack = (TextView)convertView.findViewById(R.id.textFeedbackBlack);
            holder.imageViewBlack= (ImageView)convertView.findViewById(R.id.imageViewBlack);

            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        HashMap<String, Object> stringObjectHashMap = list.get(position);
        if(stringObjectHashMap.get("from").toString().equals("user")) {
            holder.layoutBlack.setVisibility(View.VISIBLE);
            holder.layoutWhite.setVisibility(View.GONE);

            if(stringObjectHashMap.get("content_type").toString().equals("1")) {
                Glide.with(context)
                        .load(stringObjectHashMap.get("imageView").toString())
                        .placeholder(R.drawable.banner_loading_spinner)
                        .crossFade()
                        .into(holder.imageViewBlack);
                holder.imageViewBlack.setVisibility(View.VISIBLE);
                holder.textViewBlack.setVisibility(View.GONE);

            }else {
                holder.textViewBlack.setText(stringObjectHashMap.get("textView").toString());
                holder.imageViewBlack.setVisibility(View.GONE);
                holder.textViewBlack.setVisibility(View.VISIBLE);
            }

        }
        else
        {   holder.layoutBlack.setVisibility(View.GONE);
            holder.layoutWhite.setVisibility(View.VISIBLE);

            if(stringObjectHashMap.get("content_type").toString().equals("1")) {
                Glide.with(context)
                        .load(stringObjectHashMap.get("imageView").toString())
                        .placeholder(R.drawable.banner_loading_spinner)
                        .crossFade()
                        .into(holder.imageViewWhite);
                holder.imageViewWhite.setVisibility(View.VISIBLE);
                holder.textViewWhite.setVisibility(View.GONE);
            }else {
                holder.textViewWhite.setText(stringObjectHashMap.get("textView").toString());
                holder.imageViewWhite.setVisibility(View.GONE);
                holder.textViewWhite.setVisibility(View.VISIBLE);
            }
        }


        return convertView;



}

    static class ViewHolder
    {
        public LinearLayout layoutWhite;
        public TextView textViewWhite;
        public ImageView imageViewWhite;

        public LinearLayout layoutBlack;
        public TextView textViewBlack;
        public ImageView imageViewBlack;
    }

}