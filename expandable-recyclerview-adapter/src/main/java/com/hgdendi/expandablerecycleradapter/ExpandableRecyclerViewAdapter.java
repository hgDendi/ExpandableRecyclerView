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
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public abstract class ExpandableRecyclerViewAdapter
        <GroupItem extends ExpandableRecyclerViewAdapter.GroupNode,
                GroupViewHolder extends RecyclerView.ViewHolder,
                ChildViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ExpandableRecyclerViewAdapter.class.getSimpleName();

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_CHILD = 1;

    private Set<Integer> mExpandGroupList = new TreeSet<>();
    private ExpandableRecyclerViewOnClickListener mListener;

    /******************
     * abstract methods
     ******************/

    abstract public int getGroupCount();

    abstract public GroupItem getGroupItem(int position);

    abstract public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent);

    abstract public ChildViewHolder onCreateChildViewHolder(ViewGroup parent);

    abstract public void onBindGroupViewHolder(GroupViewHolder holder, GroupItem groupItem, boolean isExpand);

    abstract public void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex);

    /******************
     * external interface
     ******************/

    public final void setListener(ExpandableRecyclerViewOnClickListener listener) {
        mListener = listener;
    }

    public final boolean isGroupExpand(int groupIndex) {
        return mExpandGroupList.contains(groupIndex);
    }

    public final boolean expandGroup(int groupIndex) {
        if (getGroupItem(groupIndex).getChildCount() <= 0) {
            return false;
        }
        if (!isGroupExpand(groupIndex)) {
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
        for (Integer integer : mExpandGroupList) {
            result += getGroupItem(integer).getChildCount();
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
        return result;
    }

    /******************
     * common
     ******************/

    @Override
    public final int getItemViewType(int position) {
        final int groupCount = getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            position--;
            if (position < 0) {
                return TYPE_GROUP;
            }
            if (mExpandGroupList.contains(i)) {
                position -= getGroupItem(i).getChildCount();
                if (position < 0) {
                    return TYPE_CHILD;
                }
            }
        }
        return TYPE_GROUP;
    }


    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CHILD) {
            return onCreateChildViewHolder(parent);
        } else {
            return onCreateGroupViewHolder(parent);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CHILD) {
            final int[] childCoord = translateToDoubleIndex(position);
            bindChildViewHolder((ChildViewHolder) holder, getGroupItem(childCoord[0]), childCoord[0], childCoord[1]);
        } else {
            bindGroupViewHolder((GroupViewHolder) holder, translateToDoubleIndex(position)[0]);
        }
    }

    private void bindGroupViewHolder(final GroupViewHolder holder, final int groupIndex) {
        final GroupItem groupItem = getGroupItem(groupIndex);
        onBindGroupViewHolder(holder, groupItem, isGroupExpand(groupIndex));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupItem.getChildCount() <= 0) {
                    return;
                }
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
                    notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    private void bindChildViewHolder(ChildViewHolder holder, final GroupItem groupItem, final int groupIndex, final int childIndex) {
        onBindChildViewHolder(holder, groupItem, childIndex);
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
     * translate adapterPosition to 2-dimensional coord
     *
     * @param position adapterPosition
     * @return int[]{groupIndex,childIndex}
     */
    private final int[] translateToDoubleIndex(int position) {
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
    }

    public interface ExpandableRecyclerViewOnClickListener {

        /**
         * @param groupItem
         * @param isExpand
         * @return whether intercept expand/fold operation
         */
        boolean onGroupClicked(GroupNode groupItem, boolean isExpand);

        void onChildClicked(GroupNode groupItem, int childIndex);
    }
}

