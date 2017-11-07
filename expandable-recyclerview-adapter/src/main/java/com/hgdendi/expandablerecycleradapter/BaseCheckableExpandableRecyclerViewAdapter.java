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
        <GroupBean extends BaseCheckableExpandableRecyclerViewAdapter.CheckableGroupItem<ChildBean>,
                ChildBean,
                GroupViewHolder extends BaseCheckableExpandableRecyclerViewAdapter.BaseCheckableGroupViewHolder,
                ChildViewHolder extends BaseCheckableExpandableRecyclerViewAdapter.BaseCheckableChildViewHolder>
        extends BaseExpandableRecyclerViewAdapter<GroupBean, ChildBean, GroupViewHolder, ChildViewHolder> {

    private static final String TAG = BaseCheckableExpandableRecyclerViewAdapter.class.getSimpleName();

    private final Object PAYLOAD_CHECKMODE = this;
    public static final int CHECK_MODE_NONE = 0;
    public static final int CHECK_MODE_PARTIAL = CHECK_MODE_NONE + 1;
    public static final int CHECK_MODE_ALL = CHECK_MODE_NONE + 2;

    private final Set<CheckedItem<GroupBean, ChildBean>> mCheckedSet = new HashSet<>();
    private CheckStatusChangeListener<GroupBean, ChildBean> mOnCheckStatusChangeListener;

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

    public final Set<CheckedItem<GroupBean, ChildBean>> getCheckedSet() {
        return mCheckedSet;
    }

    public final int getSelectedCount() {
        return mCheckedSet.size();
    }

    public final void setOnCheckStatusChangeListener(CheckStatusChangeListener<GroupBean, ChildBean> onCheckStatusChangeListener) {
        mOnCheckStatusChangeListener = onCheckStatusChangeListener;
    }

    public final void setCheckedSet(List<CheckedItem<GroupBean, ChildBean>> checkedSet) {
        clearCheckedListAndUpdateUI();
        if (checkedSet == null || checkedSet.size() <= 0) {
            return;
        }
        for (CheckedItem<GroupBean, ChildBean> checkedItem : checkedSet) {
            addToCheckedList(checkedItem);
        }
    }

    @Override
    public void onBindGroupViewHolder(final GroupViewHolder groupViewHolder, final GroupBean groupBean, boolean isExpand) {
        if (groupViewHolder.getCheckableRegion() != null) {
            groupViewHolder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onGroupChecked(
                            groupBean,
                            groupViewHolder,
                            translateToDoubleIndex(groupViewHolder.getAdapterPosition())[0]);
                }
            });
        }
    }

    @Override
    protected void onBindGroupViewHolder(GroupViewHolder groupViewHolder, GroupBean groupBean, boolean isExpand, List<Object> payload) {
        if (payload != null && payload.size() != 0) {
            if (payload.contains(PAYLOAD_CHECKMODE)) {
                groupViewHolder.setCheckMode(getGroupCheckedMode(groupBean));
            }
            return;
        }

        onBindGroupViewHolder(groupViewHolder, groupBean, isExpand);
    }

    @Override
    public void onBindChildViewHolder(final ChildViewHolder holder, final GroupBean groupBean, final ChildBean childBean) {
        holder.setCheckMode(getChildCheckedMode(childBean));
        if (holder.getCheckableRegion() != null) {
            holder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onChildChecked(
                            holder,
                            groupBean,
                            childBean);
                }
            });
        }
    }

    @Override
    protected void onBindChildViewHolder(ChildViewHolder holder, GroupBean groupBean, ChildBean childBean, List<Object> payload) {
        if (payload != null && payload.size() != 0) {
            if (payload.contains(PAYLOAD_CHECKMODE)) {
                holder.setCheckMode(getChildCheckedMode(childBean));
            }
            return;
        }
        onBindChildViewHolder(holder, groupBean, childBean);
    }

    @Override
    protected void bindChildViewHolder(final ChildViewHolder holder, final GroupBean groupBean, final ChildBean childBean, List<Object> payload) {
        super.bindChildViewHolder(holder, groupBean, childBean, payload);
        holder.setCheckMode(getChildCheckedMode(childBean));
        if (holder.getCheckableRegion() != null) {
            holder.getCheckableRegion().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onChildChecked(holder, groupBean, childBean);
                }
            });
        }
    }

    private int getGroupCheckedMode(GroupBean groupBean) {
        if (!groupBean.isExpandable()) {
            return isItemSelected(groupBean) ? CHECK_MODE_ALL : CHECK_MODE_NONE;
        } else {
            int checkedCount = 0;
            for (ChildBean childBean : groupBean.getChildren()) {
                if (isItemSelected(childBean)) {
                    checkedCount++;
                }
            }
            if (checkedCount == 0) {
                return CHECK_MODE_NONE;
            } else if (checkedCount == groupBean.getChildCount()) {
                return CHECK_MODE_ALL;
            } else {
                return CHECK_MODE_PARTIAL;
            }
        }
    }

    private int getChildCheckedMode(ChildBean childBean) {
        return isItemSelected(childBean) ? CHECK_MODE_ALL : CHECK_MODE_NONE;
    }

    private void onGroupChecked(GroupBean groupBean, GroupViewHolder holder, int groupIndex) {
        int checkedMode = getGroupCheckedMode(groupBean);
        if (groupBean.isExpandable()) {
            //有子菜单
            switch (checkedMode) {
                case CHECK_MODE_NONE:
                case CHECK_MODE_PARTIAL:
                    selectAllInGroup(holder, groupBean, groupIndex, true);
                    break;
                case CHECK_MODE_ALL:
                default:
                    selectAllInGroup(holder, groupBean, groupIndex, false);
                    break;
            }
        } else {
            //无子菜单
            if (isItemSelected(groupBean)) {
                if (!onInterceptGroupCheckStatusChanged(groupBean, false)
                        && removeFromCheckedList(groupBean)) {
                    holder.setCheckMode(getGroupCheckedMode(groupBean));
                }
            } else {
                if (!onInterceptGroupCheckStatusChanged(groupBean, true)
                        && addToCheckedList(groupBean)) {
                    holder.setCheckMode(getGroupCheckedMode(groupBean));
                }
            }
        }
    }

    private void selectAllInGroup(GroupViewHolder holder, GroupBean groupBean, int groupIndex, boolean selectAll) {
        if (selectAll && !isGroupExpand(groupIndex)) {
            expandGroup(groupIndex);
        }
        final List<ChildBean> children = groupBean.getChildren();
        final int groupAdapterPosition = holder.getAdapterPosition();
        final int originalGroupCheckedMode = getGroupCheckedMode(groupBean);
        for (int i = 0; i < children.size(); i++) {
            ChildBean childBean = children.get(i);
            if (selectAll) {
                if (isItemSelected(childBean)) {
                    continue;
                }
                if (!onInterceptChildCheckStatusChanged(groupBean, childBean, true)) {
                    addToCheckedList(groupBean, childBean);
                    notifyItemChanged(groupAdapterPosition + i + 1, PAYLOAD_CHECKMODE);
                }
            } else {
                if (!isItemSelected(childBean)) {
                    continue;
                }
                if (!onInterceptChildCheckStatusChanged(groupBean, childBean, false)
                        && removeFromCheckedList(groupBean, childBean)) {
                    notifyItemChanged(groupAdapterPosition + i + 1, PAYLOAD_CHECKMODE);
                }
            }

        }
        final int currentGroupCheckedMode = getGroupCheckedMode(groupBean);
        if (currentGroupCheckedMode != originalGroupCheckedMode) {
            holder.setCheckMode(currentGroupCheckedMode);
        }
    }

    private void onChildChecked(ChildViewHolder holder, GroupBean groupBean, ChildBean childBean) {
        final int originalGroupMode = getGroupCheckedMode(groupBean);
        boolean changeFlag = false;
        if (getChildCheckedMode(childBean) == CHECK_MODE_ALL) {
            if (!onInterceptChildCheckStatusChanged(groupBean, childBean, false)
                    && removeFromCheckedList(groupBean, childBean)) {
                holder.setCheckMode(getChildCheckedMode(childBean));
                changeFlag = true;
            }
        } else {
            if (!onInterceptChildCheckStatusChanged(groupBean, childBean, true)
                    && addToCheckedList(groupBean, childBean)) {
                holder.setCheckMode(getChildCheckedMode(childBean));
                changeFlag = true;
            }
        }

        if (changeFlag && getGroupCheckedMode(groupBean) != originalGroupMode) {
            notifyItemChanged(getPosition(getGroupIndex(groupBean)), PAYLOAD_CHECKMODE);
        }
    }

    private boolean onInterceptGroupCheckStatusChanged(GroupBean groupBean, boolean targetStatus) {
        return mOnCheckStatusChangeListener != null
                && mOnCheckStatusChangeListener.onInterceptGroupCheckStatusChange(groupBean, targetStatus);
    }

    private boolean onInterceptChildCheckStatusChanged(GroupBean groupBean, ChildBean childBean, boolean targetStatus) {
        return mOnCheckStatusChangeListener != null
                && mOnCheckStatusChangeListener.onInterceptChildCheckStatusChange(groupBean, childBean, targetStatus);
    }

    private boolean isItemSelected(GroupBean groupBean) {
        for (CheckedItem checkedItem : mCheckedSet) {
            if (checkedItem.getCheckedItem().equals(groupBean)) {
                return true;
            }
        }
        return false;
    }

    private boolean isItemSelected(ChildBean childBean) {
        for (CheckedItem checkedItem : mCheckedSet) {
            if (checkedItem.getCheckedItem().equals(childBean)) {
                return true;
            }
        }
        return false;
    }

    private boolean addToCheckedList(GroupBean groupBean) {
        return addToCheckedList(groupBean, null);
    }

    private boolean addToCheckedList(GroupBean groupBean, ChildBean childBean) {
        return addToCheckedList(new CheckedItem<>(groupBean, childBean));
    }

    private boolean addToCheckedList(CheckedItem<GroupBean, ChildBean> checkedItem) {
        if (mMaxCheckedNum == 1) {
            clearCheckedListAndUpdateUI();
        } else if (mMaxCheckedNum <= mCheckedSet.size()) {
            return false;
        }
        return mCheckedSet.add(checkedItem);
    }

    private void clearCheckedListAndUpdateUI() {
        Iterator<CheckedItem<GroupBean, ChildBean>> iter = mCheckedSet.iterator();
        while (iter.hasNext()) {
            final CheckedItem<GroupBean, ChildBean> checkedItem = iter.next();
            final int[] coord = getCoordFromCheckedItem(checkedItem);
            final GroupBean originalGroupBean = getGroupItem(coord[0]);
            final int originalGroupCheckedStatus = getGroupCheckedMode(originalGroupBean);
            iter.remove();
            final int groupAdapterPosition = getPosition(coord[0]);
            final int adapterPosition = groupAdapterPosition + coord[1] + 1;
            notifyItemChanged(adapterPosition);
            final int currentGroupCheckedStatus = getGroupCheckedMode(originalGroupBean);
            if (coord[1] >= 0 && currentGroupCheckedStatus != originalGroupCheckedStatus) {
                notifyItemChanged(groupAdapterPosition, PAYLOAD_CHECKMODE);
            }
        }
    }

    private int[] getCoordFromCheckedItem(CheckedItem<GroupBean, ChildBean> checkedItem) {
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

    private boolean removeFromCheckedList(GroupBean groupBean) {
        return removeFromCheckedList(groupBean, null);
    }

    private boolean removeFromCheckedList(GroupBean groupBean, ChildBean childBean) {
        return mCheckedSet.remove(new CheckedItem<>(groupBean, childBean));
    }

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

    public interface CheckableGroupItem<ChildItem> extends BaseGroupBean<ChildItem> {
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

        boolean onInterceptGroupCheckStatusChange(GroupItem groupItem, boolean targetStatus);

        boolean onInterceptChildCheckStatusChange(GroupItem groupItem, ChildItem childItem, boolean targetStatus);
    }
}