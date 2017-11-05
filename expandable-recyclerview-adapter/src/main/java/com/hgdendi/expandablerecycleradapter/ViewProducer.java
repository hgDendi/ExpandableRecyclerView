/**
 * ViewProducer
 * https://github.com/hgDendi/ExpandableRecyclerView
 * <p>
 * Copyright (c) 2017 hg.dendi
 * <p>
 * MIT License
 * https://rem.mit-license.org/
 * <p>
 * email: hg.dendi@gmail.com
 * Date: 2017-10-18
 */

package com.hgdendi.expandablerecycleradapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public interface ViewProducer {
    int VIEW_TYPE_EMPTY = Integer.MAX_VALUE;
    int VIEW_TYPE_HEADER = Integer.MAX_VALUE - 1;

    /**
     * equivalent to {@link android.support.v7.widget.RecyclerView.Adapter#onCreateViewHolder(RecyclerView.ViewHolder, int)}
     *
     * @param parent
     * @return
     */
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);

    /**
     * equivalent to {@link android.support.v7.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
     *
     * @param holder
     */
    void onBindViewHolder(RecyclerView.ViewHolder holder);

    public static class DefaultEmptyViewHolder extends RecyclerView.ViewHolder {
        public DefaultEmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
