/**
 * ExpandableRecyclerViewAdapter
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

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public abstract class ExpandableRecyclerViewAdapter
        <GroupItem extends ExpandableRecyclerViewAdapter.GroupNode,
                GroupViewHolder extends ExpandableRecyclerViewAdapter.GroupViewHolderTemplate,
                ChildViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ExpandableRecyclerViewAdapter.class.getSimpleName();

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_CHILD = 1;
    private static final int TYPE_EMPTY = ViewProducer.VIEW_TYPE_EMPTY;
    private static final int TYPE_HEADER = ViewProducer.VIEW_TYPE_HEADER;

    protected Set<Integer> mExpandGroupList;
    private boolean mIsEmpty;
    private ExpandableRecyclerViewOnClickListener<GroupItem> mListener;
    private ViewProducer mEmptyViewProducer;
    private ViewProducer mHeaderViewProducer;

    public ExpandableRecyclerViewAdapter() {
        mExpandGroupList = new TreeSet<>();
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // after notifyDataSetChange(),clear outdated list
                mExpandGroupList.clear();
            }
        });
    }

    /******************
     * abstract methods
     ******************/

    abstract public int getGroupCount();

    abstract public GroupItem getGroupItem(int position);

    abstract public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent);

    abstract public ChildViewHolder onCreateChildViewHolder(ViewGroup parent);

    abstract public void onBindGroupViewHolder(GroupViewHolder holder, GroupItem groupItem, boolean isExpand);

    protected void onBindGroupViewHolder(GroupViewHolder holder, GroupItem groupItem, boolean isExpand, List<Object> payload) {
        onBindGroupViewHolder(holder, groupItem, isExpand);
    }

    abstract public void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex);

    protected void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex, List<Object> payload) {
        onBindChildViewHolder(holder, groupItem, childIndex);
    }

    /******************
     * external interface
     ******************/

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
        return mExpandGroupList.contains(groupIndex);
    }

    public final boolean expandGroup(int groupIndex) {
        if (getGroupItem(groupIndex).isExpandable() && !isGroupExpand(groupIndex)) {
            mExpandGroupList.add(groupIndex);
            final int position = getPosition(groupIndex);
            notifyItemRangeInserted(position + 1, getGroupItem(groupIndex).getChildCount());
            notifyItemChanged(position);
            return true;
        }
        return false;
    }

    public final void foldAll() {
        Iterator<Integer> iter = mExpandGroupList.iterator();
        while (iter.hasNext()) {
            Integer groupIndex = iter.next();
            final int position = getPosition(groupIndex);
            notifyItemRangeRemoved(position + 1, getGroupItem(groupIndex).getChildCount());
            notifyItemChanged(position);
            iter.remove();
        }
    }

    public final boolean foldGroup(int groupIndex) {
        if (isGroupExpand(groupIndex)) {
            mExpandGroupList.remove(groupIndex);
            final int position = getPosition(groupIndex);
            notifyItemRangeRemoved(position + 1, getGroupItem(groupIndex).getChildCount());
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
        for (Integer integer : mExpandGroupList) {
            if (integer >= getGroupCount()) {
                Log.e(TAG, "invalid index in expandgroupList : " + integer);
                continue;
            }
            result += getGroupItem(integer).getChildCount();
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getPosition(int groupIndex) {
        int result = groupIndex;
        for (Integer integer : mExpandGroupList) {
            if (integer < groupIndex) {
                result += getGroupItem(integer).getChildCount();
            }
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getGroupPosition(int position) {
        if (mIsEmpty || position < 0 || (mHeaderViewProducer != null && position == 0)) {
            return -1;
        }
        return translateToDoubleIndex(position)[0];
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
            if (mExpandGroupList.contains(i)) {
                position -= getGroupItem(i).getChildCount();
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
                return mEmptyViewProducer.onCreateEmptyViewHolder(parent);
            case TYPE_HEADER:
                return mHeaderViewProducer.onCreateEmptyViewHolder(parent);
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
                mEmptyViewProducer.onBindEmptyViewHolder(holder);
                break;
            case TYPE_HEADER:
                mHeaderViewProducer.onBindEmptyViewHolder(holder);
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
                    final boolean isExpand = mExpandGroupList.contains(groupIndex);
                    if (mListener == null || !mListener.onGroupClicked(groupItem, isExpand)) {
                        final int adapterPosition = holder.getAdapterPosition();
                        if (isExpand) {
                            mExpandGroupList.remove(groupIndex);
                            notifyItemRangeRemoved(adapterPosition + 1, groupItem.getChildCount());
                        } else {
                            mExpandGroupList.add(groupIndex);
                            notifyItemRangeInserted(adapterPosition + 1, groupItem.getChildCount());
                        }
                        holder.onFoldUnfold(!isExpand, ExpandableRecyclerViewAdapter.this);
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
            if (mExpandGroupList.contains(groupCursor)) {
                final int childCount = getGroupItem(groupCursor).getChildCount();
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
        int getChildCount();

        /**
         * whether this GroupNode is expandable
         *
         * @return
         */
        boolean isExpandable();
    }

    public static abstract class GroupViewHolderTemplate extends RecyclerView.ViewHolder {
        public GroupViewHolderTemplate(View itemView) {
            super(itemView);
        }

        /**
         * optimize for partial invalidate,
         * when switching fold status
         *
         * @param isExpanding
         * @param relatedAdapter
         */
        protected void onFoldUnfold(boolean isExpanding, RecyclerView.Adapter relatedAdapter) {
            relatedAdapter.notifyItemChanged(getAdapterPosition());
        }
    }


    public interface ExpandableRecyclerViewOnClickListener<GroupBean extends GroupNode> {

        void onGroupClicked(GroupBean groupItem);

        boolean onGroupLongClicked(GroupBean groupItem);

        /**
         * @param groupItem
         * @param isExpand
         * @return whether intercept the click event
         */
        boolean onGroupClicked(GroupBean groupItem, boolean isExpand);

        void onChildClicked(GroupBean groupItem, int childIndex);
    }
}

