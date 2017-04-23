package com.wewow.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wewow.R;
import com.wewow.dto.Institute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iris on 17/4/5.
 */

public  class RecycleViewInstitutesOfSubjectAdapter
        extends RecyclerView.Adapter<RecycleViewInstitutesOfSubjectAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<HashMap<String, Object>> list;


    public static class ViewHolder extends RecyclerView.ViewHolder {


        public RecyclerView viewInstituteList;

        public TextView textViewContent;

        public ViewHolder(View view) {
            super(view);
            viewInstituteList=(RecyclerView) view.findViewById(R.id.listViewInstitutes);
            textViewContent=(TextView)view.findViewById(R.id.textViewContent);
        }

        @Override
        public String toString() {
            return super.toString() + " '" ;
        }
    }

    public Object getValueAt(int position) {
        return list.get(position);
    }

    public RecycleViewInstitutesOfSubjectAdapter(Context context, ArrayList<HashMap<String, Object>> list) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_life_institue_of_subject, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        HashMap<String, Object> stringObjectHashMap = list.get(position);

        holder.textViewContent.setText(stringObjectHashMap.get("content").toString());
        List<Institute> institutes=(List<Institute>)stringObjectHashMap.get("list");

        ArrayList<HashMap<String, Object>> listItemInstitute = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < institutes.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            //

            Institute institute = institutes.get(i);
            map.put("imageView", institute.getImage());
            map.put("textViewNum",holder.textViewContent.getContext().getResources().getString(R.string.number_refix) + institutes.get(i).getOrder());
            map.put("textViewTitle", institutes.get(i).getTitle());
            map.put("textViewRead", institutes.get(i).getRead_count());
            map.put("textViewCollection", institutes.get(i).getLiked_count());

            listItemInstitute.add(map);
        }


        holder.viewInstituteList.setLayoutManager(new LinearLayoutManager(holder.viewInstituteList.getContext()));
        holder.viewInstituteList.setAdapter(new RecycleViewInstitutesOfSearchResultAdapter(holder.viewInstituteList.getContext(),
                listItemInstitute));


    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}