package com.wewow.adapter;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
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

public class RecycleViewArtistOfSubscribedArtistList
        extends RecyclerView.Adapter<RecycleViewArtistOfSubscribedArtistList.ViewHolder> {


    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    private List<String> read;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {


        public final View mView;

        public ImageView imageViewTop;
        public ImageView imageViewBottom;
        public ImageView imageView;
        public TextView textViewName;
        public TextView textViewDesc;
        public TextView textViewArticleCount;
        public TextView textViewFollowerCount;
        public ImageView imageNew;
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
            layoutHeader=(LinearLayout)mView.findViewById(R.id.layoutHeader);
            imageViewBottom=(ImageView)mView.findViewById(R.id.imageViewBottomLine);
            imageViewTop=(ImageView)mView.findViewById(R.id.imageViewTopLine);
            imageViewDivider=(ImageView)mView.findViewById(R.id.imageViewDivider);
            imageNew=(ImageView)mView.findViewById(R.id.imageNew);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textViewName.getText();
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewArtistOfSubscribedArtistList(Context context, ArrayList<HashMap<String, Object>> list,  List<String> read) {
        this.context = context;
        this.list=list;
        this.read=read;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_lover_of_life_subscribed, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

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



        if(read.get(position).toString().equals("1")) {
            holder.imageNew.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.imageNew.setVisibility(View.GONE);
        }
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


    @Override
    public int getItemCount() {
        return list.size();
    }
}