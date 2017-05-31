package com.wewow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wewow.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iris on 17/4/5.
 */

public  class RecycleViewInstitutesOfSearchResultAdapter
        extends RecyclerView.Adapter<RecycleViewInstitutesOfSearchResultAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<HashMap<String, Object>> list;

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }
    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {


        public ImageView imageView;
        public TextView textViewTitle;
        public TextView textViewNum;
        public TextView textViewRead;
        public TextView textViewCollection;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.imageViewIcon);
            imageView = (ImageView)view.findViewById(R.id.imageViewInstitue);
            textViewTitle = (TextView)view.findViewById(R.id.textViewTitle);
            textViewNum = (TextView)view.findViewById(R.id.textViewNum);
            textViewCollection=(TextView)view.findViewById(R.id.textViewCollection);
            textViewRead=(TextView)view.findViewById(R.id.textViewRead);
        }

        @Override
        public String toString() {
            return super.toString() + " '" ;
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewInstitutesOfSearchResultAdapter(Context context, ArrayList<HashMap<String, Object>> list) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_life_institue_of_search_result, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        HashMap<String, Object> stringObjectHashMap = list.get(position);
        Glide.with(holder.imageView.getContext())
                .load(stringObjectHashMap.get("imageView").toString())
                .placeholder(R.drawable.banner_loading_spinner)
                .crossFade()
                .into(holder.imageView);
        holder.textViewTitle.setText(stringObjectHashMap.get("textViewTitle").toString());
        holder.textViewNum.setText(stringObjectHashMap.get("textViewNum").toString());
        holder.textViewRead.setText(stringObjectHashMap.get("textViewRead").toString());
        holder.textViewCollection.setText(stringObjectHashMap.get("textViewCollection").toString());

        if(mOnItemClickListener != null){
            //为ItemView设置监听器
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition(); // 1
                    mOnItemClickListener.onItemClick(holder.itemView,position); // 2
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}