/**
 * BaseExpandableRecyclerViewAdapter
 * https://github.com/hgDendi/ExpandableRecyclerView
 * <p>
 * Copyright (c) 2017 hg.dendi
 * <p>
 * MIT License
 * https://rem.mit-license.org/
 * <p>
 * email: hg.dendi@gmail.com
 * Date: 2017-09-01
 */

package com.hgdendi.expandablerecycleradapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public abstract class BaseExpandableRecyclerViewAdapter
        <GroupItem extends BaseExpandableRecyclerViewAdapter.GroupNode,
                GroupViewHolder extends BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder,
                ChildViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = BaseExpandableRecyclerViewAdapter.class.getSimpleName();

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_CHILD = 1;
    private static final int TYPE_EMPTY = ViewProducer.VIEW_TYPE_EMPTY;
    private static final int TYPE_HEADER = ViewProducer.VIEW_TYPE_HEADER;

    protected Set<GroupItem> mExpandGroupSet;
    private boolean mIsEmpty;
    private ExpandableRecyclerViewOnClickListener<GroupItem> mListener;
    private ViewProducer mEmptyViewProducer;
    private ViewProducer mHeaderViewProducer;

    public BaseExpandableRecyclerViewAdapter() {
        mExpandGroupSet = new TreeSet<>();
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // after notifyDataSetChange(),clear outdated list
                List<GroupItem> retainItem = new ArrayList<>();
                for (int i = 0; i < getGroupCount(); i++) {
                    GroupItem groupItem = getGroupItem(i);
                    if (mExpandGroupSet.contains(groupItem)) {
                        retainItem.add(groupItem);
                    }
                }
                mExpandGroupSet.clear();
                mExpandGroupSet.addAll(retainItem);
            }
        });
    }

    /**
     * get group count
     *
     * @return group count
     */
    abstract public int getGroupCount();

    /**
     * get groupItem related to GroupCount
     *
     * @param groupIndex the index of group item in group list
     * @return related GroupItem
     */
    abstract public GroupItem getGroupItem(int groupIndex);

    /**
     * create {@link GroupViewHolder} for group item
     *
     * @param parent
     * @return
     */
    abstract public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent);

    /**
     * create {@link ChildViewHolder} for child item
     *
     * @param parent
     * @return
     */
    abstract public ChildViewHolder onCreateChildViewHolder(ViewGroup parent);

    /**
     * bind {@link GroupViewHolder}
     *
     * @param holder
     * @param groupItem
     * @param isExpand
     */
    abstract public void onBindGroupViewHolder(GroupViewHolder holder, GroupItem groupItem, boolean isExpand);

    /**
     * bind {@link GroupViewHolder} with payload , used to invalidate partially
     *
     * @param holder
     * @param groupItem
     * @param isExpand
     * @param payload
     */
    protected void onBindGroupViewHolder(GroupViewHolder holder, GroupItem groupItem, boolean isExpand, List<Object> payload) {
        onBindGroupViewHolder(holder, groupItem, isExpand);
    }

    /**
     * bind {@link ChildViewHolder}
     *
     * @param holder
     * @param groupItem
     * @param childIndex
     */
    abstract public void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex);

    /**
     * bind {@link ChildViewHolder} with payload , used to invalidate partially
     *
     * @param holder
     * @param groupItem
     * @param childIndex
     * @param payload
     */
    protected void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex, List<Object> payload) {
        onBindChildViewHolder(holder, groupItem, childIndex);
    }


    public void setEmptyViewProducer(ViewProducer emptyViewProducer) {
        if (mEmptyViewProducer != emptyViewProducer) {
            mEmptyViewProducer = emptyViewProducer;
            if (mIsEmpty) {
                notifyDataSetChanged();
            }
        }
    }

    public void setHeaderViewProducer(ViewProducer headerViewProducer) {
        if (mHeaderViewProducer != headerViewProducer) {
            mHeaderViewProducer = headerViewProducer;
            notifyDataSetChanged();
        }
    }

    public final void setListener(ExpandableRecyclerViewOnClickListener<GroupItem> listener) {
        mListener = listener;
    }

    public final boolean isGroupExpand(int groupIndex) {
        return mExpandGroupSet.contains(getGroupItem(groupIndex));
    }

    public final boolean expandGroup(int groupIndex) {
        GroupItem groupItem = getGroupItem(groupIndex);
        if (groupItem.isExpandable() && !isGroupExpand(groupIndex)) {
            mExpandGroupSet.add(groupItem);
            final int position = getPosition(groupIndex);
            notifyItemRangeInserted(position + 1, groupItem.getChildCount());
            notifyItemChanged(position);
            return true;
        }
        return false;
    }

    public final void foldAll() {
        Iterator<GroupItem> iter = mExpandGroupSet.iterator();
        while (iter.hasNext()) {
            GroupItem groupItem = iter.next();
            final int position = getPosition(getGroupIndex(groupItem));
            notifyItemRangeRemoved(position + 1, groupItem.getChildCount());
            notifyItemChanged(position);
            iter.remove();
        }
    }

    public final boolean foldGroup(int groupIndex) {
        GroupItem groupItem = getGroupItem(groupIndex);
        if (mExpandGroupSet.remove(groupItem)) {
            final int position = getPosition(groupIndex);
            notifyItemRangeRemoved(position + 1, groupItem.getChildCount());
            notifyItemChanged(position);
            return true;
        }
        return false;
    }

    @Override
    public final int getItemCount() {
        int result = getGroupCount();
        if (result == 0 && mEmptyViewProducer != null) {
            mIsEmpty = true;
            return mHeaderViewProducer == null ? 1 : 2;
        }
        mIsEmpty = false;
        for (GroupItem groupItem : mExpandGroupSet) {
            if (getGroupIndex(groupItem) >= 0) {
                Log.e(TAG, "invalid index in expandgroupList : " + groupItem);
                continue;
            }
            result += groupItem.getChildCount();
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getPosition(int groupIndex) {
        int result = groupIndex;
        for (GroupItem groupItem : mExpandGroupSet) {
            if (getGroupIndex(groupItem) >= 0) {
                result += groupItem.getChildCount();
            }
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getGroupPosition(int groupIndex) {
        if (mIsEmpty || groupIndex < 0 || (mHeaderViewProducer != null && groupIndex == 0)) {
            return -1;
        }
        return translateToDoubleIndex(groupIndex)[0];
    }

    public final int getGroupIndex(@NonNull GroupItem groupItem) {
        for (int i = 0; i < getGroupCount(); i++) {
            if (groupItem.equals(getGroupItem(i))) {
                return i;
            }
        }
        return -1;
    }

    /******************
     * common
     ******************/

    @Override
    public final int getItemViewType(int position) {
        if (mHeaderViewProducer != null && position == 0) {
            return TYPE_HEADER;
        }
        if (mIsEmpty) {
            return TYPE_EMPTY;
        }
        int firstValidIndex = mHeaderViewProducer == null ? 0 : 1;
        final int groupCount = getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            position--;
            if (position < firstValidIndex) {
                return TYPE_GROUP;
            }
            GroupItem groupItem = getGroupItem(i);
            if (mExpandGroupSet.contains(groupItem)) {
                position -= groupItem.getChildCount();
                if (position < firstValidIndex) {
                    return TYPE_CHILD;
                }
            }
        }
        return TYPE_GROUP;
    }


    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EMPTY:
                return mEmptyViewProducer.onCreateViewHolder(parent);
            case TYPE_HEADER:
                return mHeaderViewProducer.onCreateViewHolder(parent);
            case TYPE_CHILD:
                return onCreateChildViewHolder(parent);
            case TYPE_GROUP:
            default:
                return onCreateGroupViewHolder(parent);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        switch (holder.getItemViewType()) {
            case TYPE_EMPTY:
                mEmptyViewProducer.onBindViewHolder(holder);
                break;
            case TYPE_HEADER:
                mHeaderViewProducer.onBindViewHolder(holder);
                break;
            case TYPE_CHILD:
                final int[] childCoord = translateToDoubleIndex(position);
                bindChildViewHolder((ChildViewHolder) holder, getGroupItem(childCoord[0]), childCoord[0], childCoord[1], payloads);
                break;
            case TYPE_GROUP:
            default:
                bindGroupViewHolder((GroupViewHolder) holder, translateToDoubleIndex(position)[0], payloads);
        }
    }

    protected void bindGroupViewHolder(final GroupViewHolder holder, final int groupIndex, List<Object> payload) {
        final GroupItem groupItem = getGroupItem(groupIndex);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    return mListener.onGroupLongClicked(groupItem);
                }
                return false;
            }
        });
        if (!groupItem.isExpandable()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onGroupClicked(groupItem);
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isExpand = mExpandGroupSet.contains(groupItem);
                    if (mListener == null || !mListener.onInterceptGroupExpandEvent(groupItem, isExpand)) {
                        final int adapterPosition = holder.getAdapterPosition();
                        if (isExpand) {
                            mExpandGroupSet.remove(groupItem);
                            notifyItemRangeRemoved(adapterPosition + 1, groupItem.getChildCount());
                        } else {
                            mExpandGroupSet.add(groupItem);
                            notifyItemRangeInserted(adapterPosition + 1, groupItem.getChildCount());
                        }
                        holder.onExpandStatusChanged(BaseExpandableRecyclerViewAdapter.this, !isExpand);
                    }
                }
            });
        }
        onBindGroupViewHolder(holder, groupItem, isGroupExpand(groupIndex));
    }

    protected void bindChildViewHolder(ChildViewHolder holder, final GroupItem groupItem, final int groupIndex, final int childIndex, List<Object> payload) {
        onBindChildViewHolder(holder, groupItem, childIndex, payload);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onChildClicked(groupItem, childIndex);
                }
            }
        });
    }

    /**
     * position translation
     * from adapterPosition to group-child coord
     *
     * @param position adapterPosition
     * @return int[]{groupIndex,childIndex}
     */
    protected final int[] translateToDoubleIndex(int position) {
        if (mHeaderViewProducer != null) {
            position--;
        }
        final int[] result = new int[2];
        final int groupCount = getGroupCount();
        int positionCursor = 0;
        for (int groupCursor = 0; groupCursor < groupCount; groupCursor++) {
            if (positionCursor == position) {
                result[0] = groupCursor;
                result[1] = -1;
                break;
            }
            GroupItem groupItem = getGroupItem(groupCursor);
            if (mExpandGroupSet.contains(groupItem)) {
                final int childCount = groupItem.getChildCount();
                final int offset = position - positionCursor;
                if (childCount >= offset) {
                    result[0] = groupCursor;
                    result[1] = offset - 1;
                    break;
                }
                positionCursor += childCount;
            }
            positionCursor++;
        }
        return result;
    }

    /******************
     * interface
     ******************/

    public interface GroupNode {
        /**
         * get num of children
         *
         * @return
         */
        int getChildCount();

        /**
         * whether this GroupNode is expandable
         *
         * @return
         */
        boolean isExpandable();
    }

    public static abstract class BaseGroupViewHolder extends RecyclerView.ViewHolder {
        public BaseGroupViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * optimize for partial invalidate,
         * when switching fold status.
         * Default implementation is update the whole {@link android.support.v7.widget.RecyclerView.ViewHolder#itemView}.
         *
         * @param relatedAdapter
         * @param isExpanding
         */
        protected void onExpandStatusChanged(RecyclerView.Adapter relatedAdapter, boolean isExpanding) {
            relatedAdapter.notifyItemChanged(getAdapterPosition());
        }
    }


    public interface ExpandableRecyclerViewOnClickListener<GroupBean extends GroupNode> {

        /**
         * called when an unexpandable group item is clicked
         * @param groupItem
         */
        void onGroupClicked(GroupBean groupItem);

        /**
         * called when group item is long clicked
         * @param groupItem
         * @return
         */
        boolean onGroupLongClicked(GroupBean groupItem);

        /**
         * called when an expandable group item is clicked
         * @param groupItem
         * @param isExpand
         * @return whether intercept the click event
         */
        boolean onInterceptGroupExpandEvent(GroupBean groupItem, boolean isExpand);

        /**
         * called when child is clicked
         * @param groupItem
         * @param childIndex
         */
        void onChildClicked(GroupBean groupItem, int childIndex);
    }
}

