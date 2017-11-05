/**
 * BaseCheckableExpandableRecyclerViewAdapter
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class BaseCheckableExpandableRecyclerViewAdapter
        <GroupItem extends BaseCheckableExpandableRecyclerViewAdapter.CheckableGroupItem<ChildItem>,
                ChildItem,
                GroupViewHolder extends BaseCheckableExpandableRecyclerViewAdapter.BaseCheckableGroupViewHolder,
                ChildViewHolder extends BaseCheckableExpandableRecyclerViewAdapter.BaseCheckableChildViewHolder>
        extends BaseExpandableRecyclerViewAdapter<GroupItem, GroupViewHolder, ChildViewHolder> {

    private static final String TAG = BaseCheckableExpandableRecyclerViewAdapter.class.getSimpleName();

    private final Object PAYLOAD_CHECKMODE = this;
    public static final int CHECK_MODE_NONE = 0;
    public static final int CHECK_MODE_PARTIAL = CHECK_MODE_NONE + 1;
    public static final int CHECK_MODE_ALL = CHECK_MODE_NONE + 2;

    private final Set<CheckedItem<GroupItem, ChildItem>> mCheckedSet = new HashSet<>();
    private CheckStatusChangeListener<GroupItem, ChildItem> mOnCheckStatusChangeListener;

    /**
     * max num of items to be selected at the same time
     * if equals to 1 , new choice will override old choice
     * otherwise , the new checking-clickevent will be ignored
     */
    private int mMaxCheckedNum;

    public BaseCheckableExpandableRecyclerViewAdapter(int maxCheckedNum) {
        if (maxCheckedNum <= 0) {
            throw new IllegalArgumentException("invalid maxCheckedNum " + maxCheckedNum);
        }
        mMaxCheckedNum = maxCheckedNum;
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // after notifyDataSetChange(),clear outdated list
                mCheckedSet.clear();
            }
        });
    }

    public final Set<CheckedItem<GroupItem, ChildItem>> getCheckedSet() {
        return mCheckedSet;
    }

    public final int getSelectedCount() {
        return mCheckedSet.size();
    }

    public final void setOnCheckStatusChangeListener(CheckStatusChangeListener<GroupItem, ChildItem> onCheckStatusChangeListener) {
        mOnCheckStatusChangeListener = onCheckStatusChangeListener;
    }

    public final void setCheckedSet(List<CheckedItem> checkedSet) {
        clearCheckedListAndUpdateUI();
        if (checkedSet == null || checkedSet.size() <= 0) {
            return;
        }
        for (CheckedItem checkedItem : checkedSet) {
            addToCheckedList(checkedItem);
        }
    }

    /******************
     * common
     ******************/

    @Override
    public void onBindGroupViewHolder(final GroupViewHolder groupViewHolder, final GroupItem groupItem, boolean isExpand) {
        if (groupViewHolder.getCheckableRegion() != null) {
            groupViewHolder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onGroupChecked(
                            groupItem,
                            groupViewHolder,
                            translateToDoubleIndex(groupViewHolder.getAdapterPosition())[0]);
                }
            });
        }
    }

    @Override
    protected void onBindGroupViewHolder(GroupViewHolder groupViewHolder, GroupItem groupItem, boolean isExpand, List<Object> payload) {
        if (payload == null || payload.size() == 0) {
            onBindGroupViewHolder(groupViewHolder, groupItem, isExpand);
            return;
        }
        for (Object o : payload) {
            if (o.equals(PAYLOAD_CHECKMODE)) {
                groupViewHolder.setCheckMode(getGroupCheckedMode(groupItem));
            }
        }
    }

    @Override
    public void onBindChildViewHolder(final ChildViewHolder holder, final GroupItem groupItem, final int childIndex) {
        final ChildItem childItem = groupItem.getChildren().get(childIndex);
        holder.setCheckMode(getChildCheckedMode(childItem));
        if (holder.getCheckableRegion() != null) {
            holder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onChildChecked(
                            groupItem,
                            childItem,
                            translateToDoubleIndex(holder.getAdapterPosition())[0],
                            childIndex, holder);
                }
            });
        }
    }

    @Override
    protected void onBindChildViewHolder(ChildViewHolder holder, GroupItem groupItem, int childIndex, List<Object> payload) {
        if (payload == null || payload.size() == 0) {
            onBindChildViewHolder(holder, groupItem, childIndex);
            return;
        }
        for (Object o : payload) {
            if (o.equals(PAYLOAD_CHECKMODE)) {
                final ChildItem childItem = groupItem.getChildren().get(childIndex);
                holder.setCheckMode(getChildCheckedMode(childItem));
            }
        }
    }

    @Override
    protected void bindChildViewHolder(final ChildViewHolder holder, final GroupItem groupItem, final int groupIndex, final int childIndex, List<Object> payload) {
        super.bindChildViewHolder(holder, groupItem, groupIndex, childIndex, payload);
        final ChildItem childItem = groupItem.getChildren().get(childIndex);
        holder.setCheckMode(getChildCheckedMode(childItem));
        if (holder.getCheckableRegion() != null) {
            holder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onChildChecked(groupItem, childItem, groupIndex, childIndex, holder);
                }
            });
        }
    }

    /******************
     * concerning check
     ******************/

    private int getGroupCheckedMode(GroupItem groupItem) {
        if (!groupItem.isExpandable()) {
            return isItemSelected(groupItem) ? CHECK_MODE_ALL : CHECK_MODE_NONE;
        } else {
            int checkedCount = 0;
            for (ChildItem childItem : groupItem.getChildren()) {
                if (isItemSelected(childItem)) {
                    checkedCount++;
                }
            }
            if (checkedCount == 0) {
                return CHECK_MODE_NONE;
            } else if (checkedCount == groupItem.getChildCount()) {
                return CHECK_MODE_ALL;
            } else {
                return CHECK_MODE_PARTIAL;
            }
        }
    }

    private int getChildCheckedMode(ChildItem childItem) {
        return isItemSelected(childItem) ? CHECK_MODE_ALL : CHECK_MODE_NONE;
    }

    private final void onGroupChecked(GroupItem groupItem, GroupViewHolder holder, int groupIndex) {
        int checkedMode = getGroupCheckedMode(groupItem);
        if (groupItem.isExpandable()) {
            //有子菜单
            switch (checkedMode) {
                case CHECK_MODE_NONE:
                case CHECK_MODE_PARTIAL:
                    selectAllInGroup(holder, groupItem, groupIndex, true);
                    break;
                case CHECK_MODE_ALL:
                default:
                    selectAllInGroup(holder, groupItem, groupIndex, false);
                    break;
            }
        } else {
            //无子菜单
            if (isItemSelected(groupItem)) {
                if (!onInterceptGroupCheckStatusChanged(groupItem, groupIndex, false)
                        && removeFromCheckedList(groupItem)) {
                    holder.setCheckMode(getGroupCheckedMode(groupItem));
                }
            } else {
                if (!onInterceptGroupCheckStatusChanged(groupItem, groupIndex, true)
                        && addToCheckedList(groupItem)) {
                    holder.setCheckMode(getGroupCheckedMode(groupItem));
                }
            }
        }
    }

    private void selectAllInGroup(GroupViewHolder holder, GroupItem groupItem, int groupIndex, boolean selectAll) {
        if (selectAll && !isGroupExpand(groupIndex)) {
            expandGroup(groupIndex);
        }
        final List<ChildItem> children = groupItem.getChildren();
        final int groupAdapterPosition = holder.getAdapterPosition();
        final int originalGroupCheckedMode = getGroupCheckedMode(groupItem);
        for (int i = 0; i < children.size(); i++) {
            ChildItem childItem = children.get(i);
            if (selectAll) {
                if (isItemSelected(childItem)) {
                    continue;
                }
                if (!onInterceptChildCheckStatusChanged(groupItem, groupIndex, i, true)) {
                    addToCheckedList(groupItem, childItem);
                    notifyItemChanged(groupAdapterPosition + i + 1, PAYLOAD_CHECKMODE);
                }
            } else {
                if (!isItemSelected(childItem)) {
                    continue;
                }
                if (!onInterceptChildCheckStatusChanged(groupItem, groupIndex, i, false)
                        && removeFromCheckedList(groupItem, childItem)) {
                    notifyItemChanged(groupAdapterPosition + i + 1, PAYLOAD_CHECKMODE);
                }
            }

        }
        final int currentGroupCheckedMode = getGroupCheckedMode(groupItem);
        if (currentGroupCheckedMode != originalGroupCheckedMode) {
            holder.setCheckMode(currentGroupCheckedMode);
        }
    }

    private void onChildChecked(GroupItem groupItem, ChildItem childItem, int groupIndex, int childIndex, ChildViewHolder holder) {
        final int originalGroupMode = getGroupCheckedMode(groupItem);
        boolean changeFlag = false;
        if (getChildCheckedMode(childItem) == CHECK_MODE_ALL) {
            if (!onInterceptChildCheckStatusChanged(groupItem, groupIndex, childIndex, false)
                    && removeFromCheckedList(groupItem, childItem)) {
                holder.setCheckMode(getChildCheckedMode(childItem));
                changeFlag = true;
            }
        } else {
            if (!onInterceptChildCheckStatusChanged(groupItem, groupIndex, childIndex, true)
                    && addToCheckedList(groupItem, childItem)) {
                holder.setCheckMode(getChildCheckedMode(childItem));
                changeFlag = true;
            }
        }

        if (changeFlag && getGroupCheckedMode(groupItem) != originalGroupMode) {
            notifyItemChanged(getPosition(groupIndex), PAYLOAD_CHECKMODE);
        }
    }

    private boolean onInterceptGroupCheckStatusChanged(GroupItem groupItem, int groupIndex, boolean targetStatus) {
        return mOnCheckStatusChangeListener != null && mOnCheckStatusChangeListener.onInterceptGroupCheckStatusChange(groupItem, groupIndex, targetStatus);
    }

    private boolean onInterceptChildCheckStatusChanged(GroupItem groupItem, int groupIndex, int childIndex, boolean targetStatus) {
        return mOnCheckStatusChangeListener != null && mOnCheckStatusChangeListener.onInterceptChildCheckStatusChange(groupItem, groupIndex, childIndex, targetStatus);
    }

    private boolean isItemSelected(GroupItem groupItem) {
        for (CheckedItem checkedItem : mCheckedSet) {
            if (checkedItem.getCheckedItem().equals(groupItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean isItemSelected(ChildItem childItem) {
        for (CheckedItem checkedItem : mCheckedSet) {
            if (checkedItem.getCheckedItem().equals(childItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean addToCheckedList(GroupItem groupItem) {
        return addToCheckedList(groupItem, null);
    }

    private boolean addToCheckedList(GroupItem groupItem, ChildItem childItem) {
        return addToCheckedList(new CheckedItem<>(groupItem, childItem));
    }

    private boolean addToCheckedList(CheckedItem<GroupItem,ChildItem> checkedItem) {
        if (mMaxCheckedNum == 1) {
            clearCheckedListAndUpdateUI();
        } else if (mMaxCheckedNum <= mCheckedSet.size()) {
            return false;
        }
        return mCheckedSet.add(checkedItem);
    }

    private void clearCheckedListAndUpdateUI() {
        Iterator<CheckedItem<GroupItem, ChildItem>> iter = mCheckedSet.iterator();
        while (iter.hasNext()) {
            final CheckedItem<GroupItem,ChildItem> checkedItem = iter.next();
            final int[] coord = getCoordFromCheckedItem(checkedItem);
            final GroupItem originalGroupItem = getGroupItem(coord[0]);
            final int originalGroupCheckedStatus = getGroupCheckedMode(originalGroupItem);
            iter.remove();
            final int groupAdapterPosition = getPosition(coord[0]);
            final int adapterPosition = groupAdapterPosition + coord[1] + 1;
            notifyItemChanged(adapterPosition);
            final int currentGroupCheckedStatus = getGroupCheckedMode(originalGroupItem);
            if (coord[1] >= 0 && currentGroupCheckedStatus != originalGroupCheckedStatus) {
                notifyItemChanged(groupAdapterPosition, PAYLOAD_CHECKMODE);
            }
        }
    }

    private int[] getCoordFromCheckedItem(CheckedItem<GroupItem, ChildItem> checkedItem) {
        int[] result = new int[]{-1, -1};
        for (int i = 0; i < getGroupCount(); i++) {
            if (getGroupItem(i).equals(checkedItem.groupItem)) {
                result[0] = i;
                break;
            }
        }
        if (checkedItem.childItem != null) {
            result[1] = getGroupItem(result[0]).getChildren().indexOf(checkedItem.childItem);
        }
        return result;
    }

    private boolean removeFromCheckedList(GroupItem groupItem) {
        return removeFromCheckedList(groupItem, null);
    }

    private boolean removeFromCheckedList(GroupItem groupItem, ChildItem childItem) {
        return mCheckedSet.remove(new CheckedItem<>(groupItem, childItem));
    }

    /******************
     * interface
     ******************/

    public abstract static class BaseCheckableGroupViewHolder extends BaseGroupViewHolder implements Selectable {
        public BaseCheckableGroupViewHolder(View itemView) {
            super(itemView);
        }
    }

    public abstract static class BaseCheckableChildViewHolder extends RecyclerView.ViewHolder implements Selectable {
        public BaseCheckableChildViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface Selectable {
        /**
         * optimize for partial update
         * if an item is switching check mode ,
         * do not need to invalidate whole item,
         * this is the optimized callback
         *
         * @param mode
         */
        void setCheckMode(int mode);

        /**
         * checkable region
         * correspond to the check operation
         * <p>
         * ect.
         * the child item returns itself
         * the group item returns its check icon
         *
         * @return
         */
        View getCheckableRegion();
    }

    public interface CheckableGroupItem<ChildItem> extends GroupNode {
        /**
         * get children list
         *
         * @return
         */
        List<ChildItem> getChildren();
    }

    public static class CheckedItem<GroupItem, ChildItem> {
        @NonNull
        GroupItem groupItem;
        @Nullable
        ChildItem childItem;

        public CheckedItem(@NonNull GroupItem groupItem, @Nullable ChildItem childItem) {
            this.groupItem = groupItem;
            this.childItem = childItem;
        }

        @NonNull
        public GroupItem getGroupItem() {
            return groupItem;
        }

        @Nullable
        public ChildItem getChildItem() {
            return childItem;
        }

        Object getCheckedItem() {
            return childItem != null ? childItem : groupItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CheckedItem that = (CheckedItem) o;

            if (!groupItem.equals(that.groupItem)) {
                return false;
            }
            return childItem != null ? childItem.equals(that.childItem) : that.childItem == null;

        }

        @Override
        public int hashCode() {
            int result = groupItem.hashCode();
            result = 31 * result + (childItem != null ? childItem.hashCode() : 0);
            return result;
        }
    }


    /**
     * Intercept of mode switch
     * <p>
     * returns true means intercept this mode switch
     *
     * @param <GroupItem>
     * @param <ChildItem>
     */
    public interface CheckStatusChangeListener<GroupItem extends BaseCheckableExpandableRecyclerViewAdapter.CheckableGroupItem<ChildItem>, ChildItem> {

        boolean onInterceptGroupCheckStatusChange(GroupItem groupItem, int groupIndex, boolean targetStatus);

        boolean onInterceptChildCheckStatusChange(GroupItem groupItem, int groupIndex, int childIndex, boolean targetStatus);
    }
}