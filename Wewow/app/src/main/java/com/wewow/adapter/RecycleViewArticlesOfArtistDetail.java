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
import java.util.List;

/**
 * Created by iris on 17/4/5.
 */

public  class RecycleViewArticlesOfArtistDetail
        extends RecyclerView.Adapter<RecycleViewArticlesOfArtistDetail.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<HashMap<String, Object>> list;


    public static class ViewHolder extends RecyclerView.ViewHolder {


        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        public final TextView textViewCount;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.imageViewInstitue);
            mTextView = (TextView) view.findViewById(R.id.textViewTitle);
            textViewCount=(TextView) view.findViewById(R.id.textViewCount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewArticlesOfArtistDetail(Context context, ArrayList<HashMap<String, Object>> list) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_detail_lover_of_life, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

//            holder.mTextView.setText(mValues.get(position));

//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, CheeseDetailActivity.class);
//                    intent.putExtra(CheeseDetailActivity.EXTRA_NAME, holder.mBoundString);
//
//                    context.startActivity(intent);
//                }
//            });

        Glide.with(holder.mImageView.getContext())
                .load(list.get(position).get("image").toString())
                .fitCenter()
                .into(holder.mImageView);
        holder.mTextView.setText(list.get(position).get("title").toString());
//        holder.textViewCount.setText(list.get(position).get("count").toString());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}