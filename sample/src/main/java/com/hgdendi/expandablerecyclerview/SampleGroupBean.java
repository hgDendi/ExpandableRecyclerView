package com.hgdendi.expandablerecyclerview;

import android.support.annotation.NonNull;

import com.hgdendi.expandablerecycleradapter.BaseExpandableRecyclerViewAdapter;

import java.util.List;

class SampleGroupBean implements BaseExpandableRecyclerViewAdapter.GroupNode {

    private List<SampleChildBean> mList;
    private String mName;

    SampleGroupBean(@NonNull List<SampleChildBean> list, @NonNull String name) {
        mList = list;
        mName = name;
    }

    @Override
    public int getChildCount() {
        return mList.size();
    }

    public String getName() {
        return mName;
    }

    public SampleChildBean getChildAt(int index) {
        return mList.size() <= index ? null : mList.get(index);
    }
}
