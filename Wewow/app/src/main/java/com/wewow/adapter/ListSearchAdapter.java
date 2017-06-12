package com.wewow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
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

public class ListSearchAdapter extends BaseAdapter implements Filterable {
    private ArrayFilter mFilter;
    private List<String> mList;
    private Context context;
    private ArrayList<String> mUnfilteredData;

    public ListSearchAdapter(List<String> mList, Context context) {
        this.mList = mList;
        this.context = context;
    }

    @Override
    public int getCount() {

        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {

            view = View.inflate(context, R.layout.list_item_search, null);

            holder = new ViewHolder();
            holder.tv_name = (TextView) view.findViewById(R.id.text);
            holder.imageViewTop=(ImageView)view.findViewById(R.id.imageViewTop);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        if (position == 0) {
            holder.tv_name.setTextColor(context.getResources().getColor(R.color.search_hot_search));
            holder.imageViewTop.setVisibility(View.VISIBLE);
        } else {
            holder.tv_name.setTextColor(context.getResources().getColor(R.color.search_hot_search_words));
            holder.imageViewTop.setVisibility(View.INVISIBLE);
        }

        holder.tv_name.setText(mList.get(position));
        return view;
    }

    static class ViewHolder {
        public TextView tv_name;
        public ImageView imageViewTop;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<String>(mList);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<String> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<String> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String pc = unfilteredValues.get(i);
                    if (pc != null) {

                        if (pc.startsWith(prefixString)) {

                            newValues.add(pc);
                        }

                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            //noinspection unchecked
            mList = (List<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}