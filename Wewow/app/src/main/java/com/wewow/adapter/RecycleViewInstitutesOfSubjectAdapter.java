package com.wewow.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wewow.LifeLabItemActivity;
import com.wewow.R;
import com.wewow.dto.Institute;
import com.wewow.dto.LabCollection;

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
    private Context context;
    private ArrayList<ArrayList<HashMap<String, Object>>> listInstitutes;


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
        this.context=context;
        listInstitutes= new ArrayList<ArrayList<HashMap<String, Object>>>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_life_institue_of_subject, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

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
            map.put("id",institutes.get(i).getId());

            listItemInstitute.add(map);
        }
        listInstitutes.add(listItemInstitute);

        holder.viewInstituteList.setLayoutManager(new LinearLayoutManager(holder.viewInstituteList.getContext()));
        RecycleViewInstitutesOfSearchResultAdapter adapter = new RecycleViewInstitutesOfSearchResultAdapter(holder.viewInstituteList.getContext(),
                listItemInstitute);

        adapter.setOnItemClickListener(new RecycleViewInstitutesOfSearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                LabCollection lc = new LabCollection();
                lc.image = listInstitutes.get(position).get(pos).get("imageView").toString();
                lc.title = listInstitutes.get(position).get(pos).get("textViewTitle").toString();
                lc.id = Long.parseLong(listInstitutes.get(position).get(pos).get("id").toString());
                Intent intent = new Intent(context, LifeLabItemActivity.class);
                intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);

                context.startActivity(intent);

            }

        });

        holder.viewInstituteList.setAdapter(adapter);



    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}