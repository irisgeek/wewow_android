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

public  class RecycleViewArtistsOfHomePageAdapter
        extends RecyclerView.Adapter<RecycleViewArtistsOfHomePageAdapter.ViewHolder> {

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
        public TextView textViewName;
        public TextView textViewDesc;
        public TextView textViewArticleCount;
        public TextView textViewFollowerCount;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.imageViewIcon);
           textViewName = (TextView)view.findViewById(R.id.textViewNickName);
           textViewDesc= (TextView)view.findViewById(R.id.textViewDesc);
           textViewArticleCount=(TextView)view.findViewById(R.id.textViewRead);
            textViewFollowerCount=(TextView)view.findViewById(R.id.textViewCollection);
        }

        @Override
        public String toString() {
            return super.toString() + " '" ;
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewArtistsOfHomePageAdapter(Context context, ArrayList<HashMap<String, Object>> list) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_lover_of_life_recommended, parent, false);
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
        holder.textViewName.setText(stringObjectHashMap.get("textViewName").toString());
        holder.textViewDesc.setText(stringObjectHashMap.get("textViewDesc").toString());
        holder.textViewArticleCount.setText(stringObjectHashMap.get("textViewArticleCount").toString());
        holder.textViewFollowerCount.setText(stringObjectHashMap.get("textViewFollowerCount").toString());

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