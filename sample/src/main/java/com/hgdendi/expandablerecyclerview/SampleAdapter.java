package com.hgdendi.expandablerecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hgdendi.expandablerecycleradapter.BaseExpandableRecyclerViewAdapter;

import java.util.List;

public class SampleAdapter extends BaseExpandableRecyclerViewAdapter<SampleGroupBean, SampleAdapter.GroupVH, SampleAdapter.ChildVH> {

    private List<SampleGroupBean> mList;

    public SampleAdapter(List<SampleGroupBean> list) {
        mList = list;
    }

    @Override
    public int getGroupCount() {
        return mList.size();
    }

    @Override
    public SampleGroupBean getGroupItem(int position) {
        return mList.get(position);
    }

    @Override
    public GroupVH onCreateGroupViewHolder(ViewGroup parent) {
        return new GroupVH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_group, parent, false));
    }

    @Override
    public ChildVH onCreateChildViewHolder(ViewGroup parent) {
        return new ChildVH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_child, parent, false));
    }

    @Override
    public void onBindGroupViewHolder(GroupVH holder, SampleGroupBean sampleGroupBean, boolean isExpand) {
        holder.nameTv.setText(sampleGroupBean.getName());
        if (sampleGroupBean.getChildCount() <= 0) {
            holder.foldIv.setVisibility(View.INVISIBLE);
        } else {
            holder.foldIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBindChildViewHolder(ChildVH holder, SampleGroupBean sampleGroupBean, int childIndex) {
        holder.nameTv.setText(sampleGroupBean.getChildAt(childIndex).getName());
    }

    static class GroupVH extends RecyclerView.ViewHolder {
        ImageView foldIv;
        TextView nameTv;

        GroupVH(View itemView) {
            super(itemView);
            foldIv = (ImageView) itemView.findViewById(R.id.group_item_indicator);
            nameTv = (TextView) itemView.findViewById(R.id.group_item_name);
        }
    }

    static class ChildVH extends RecyclerView.ViewHolder {
        TextView nameTv;

        ChildVH(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.child_item_name);
        }
    }
}
