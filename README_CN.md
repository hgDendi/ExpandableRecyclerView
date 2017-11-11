# ExpanableRecyclerView

![demo](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/img/expandableRecyclerView.gif)

github:https://github.com/hgDendi/ExpandableRecyclerView



自定义支持二级菜单的RecyclerViewAdapter。

将展开闭合操作封装在了[BaseExpandableRecyclerViewAdapter](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/expandable-recyclerview-adapter/src/main/java/com/hgdendi/expandablerecycleradapter/BaseExpandableRecyclerViewAdapter.java)中，使整个使用方式充满弹性。

下方有具体使用方法，一般需要override 6个方法：

*   **getGroupCount**
*   **getGroupItem**
*   onCreateGroupViewHolder
*   onCreateChildViewHolder
*   onBindGroupViewHolder
*   onBindChildViewHolder

因为onCreateViewHolder和onBindViewHolder本来即是RecyclerViewAdapter需要强制Override的方法，这里按父子关系拆分成了两个方法。而getGroupCount和getGroupItem在大概率情况下都是基于List的简单一行代码即可实现，故而使用起来十分简便。

## Gradle

```Groovy
dependencies{
	compile 'com.hgDendi:expandable-recyclerview-adapter:0.0.2'
}
```

## 优点

1.  使用便捷、简洁明了
2.  最大程度保留RecyclerView的原生机制，滑动到具体条目才进行渲染，不会滑到另一个Group渲染另一个Group下所有子Item
3.  itemView的部分刷新，可以自定义展开、闭合时的刷新机制，避免GroupItem在展开闭合时刷新整个GroupItem（比如只是简单的箭头指向改变）
4.  采用泛型，用户自定义传入参数，扩展性更高

## 使用方法

### 定义父子数据结构

其中GroupBean需要继承自BaseGroupBean并override三个方法。

*   getChildCount
    *   获取子节点个数
*   isExpandable
    *   是否为可展开的节点
    *   默认实现可以是判断子节点是否为0，但是也可以做其他处理
*   getChildAt
    *   根据index获取对应的子节点数据结构

```java
class SampleGroupBean implements BaseExpandableRecyclerViewAdapter.BaseGroupBean<SampleChildBean> {
    @Override
    public int getChildCount() {
        return mList.size();
    }

    // whether this group is expandable
    @Override
    public boolean isExpandable() {
        return getChildCount() > 0;
    }
  
  	@Override
    public SampleChildBean getChildAt(int index) {
        return mList.size() <= index ? null : mList.get(index);
    }
}

public class SampleChildBean {
}
```

### 定义对应的ViewHolder

其中Group对应的ViewHolder要继承BaseGroupViewHolder并改写onExpandStatusChanged.

该方法是实现item局部刷新的方法，在展开、闭合时会回调，比如对于大多数情况，开关闭合状态只需要修改左边箭头指向，就无需刷新itemView的其他部分。

实现原理是使用RecyclerView的payload机制实现局部监听刷新。

```java
static class GroupVH extends BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder {
    GroupVH(View itemView) {
        super(itemView);
    }

    // this method is used for partial update.Which means when expand status changed,only a part of this view need to invalidate
    @Override
    protected void onExpandStatusChanged(RecyclerView.Adapter relatedAdapter, boolean isExpanding) {
      // 1.只更新左侧展开、闭合箭头
      foldIv.setImageResource(isExpanding ? R.drawable.ic_arrow_expanding : R.drawable.ic_arrow_folding);
      
      // 2.默认刷新整个Item
      relatedAdapter.notifyItemChanged(getAdapterPosition());
    }
}

static class ChildVH extends RecyclerView.ViewHolder {
    ChildVH(View itemView) {
        super(itemView);
    }
}
```

### 使用自定义Adapter继承基类

```java
// !!注意这里继承时候使用的泛型，分别为上面提到的Bean和ViewHolder
public class SampleAdapter extends BaseExpandableRecyclerViewAdapter
<SampleGroupBean, SampleChildBean, SampleAdapter.GroupVH, SampleAdapter.ChildVH>

    @Override
    public int getGroupCount() {
        // 父节点个数
    }

    @Override
    public GroupBean getGroupItem(int groupIndex) {
        // 获取父节点
    }

    @Override
    public GroupVH onCreateGroupViewHolder(ViewGroup parent, int groupViewType) {
    }

    @Override
    public ChildVH onCreateChildViewHolder(ViewGroup parent, int childViewType) {
    }

    @Override
    public void onBindGroupViewHolder(GroupVH holder, SampleGroupBean sampleGroupBean, boolean isExpand) {
    }

    @Override
    public void onBindChildViewHolder(ChildVH holder, SampleGroupBean sampleGroupBean, SampleChildBean sampleChildBean) {
    }
}
```

### 其他用法

#### 增加父子的种类

通过改写getChildType和getGroupType方法进行控制type，该type会在onCreateGroupViewHolder和onCreateChildViewHolder时回传。

```java
protected int getGroupType(GroupBean groupBean) {
    return 0;
}

abstract public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int groupViewType);
    
protected int getChildType(GroupBean groupBean, ChildBean childBean) {
    return 0;
}

abstract public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int childViewType);
```

#### 增加列表为空时候的EmptyView

```java
adapter.setEmptyViewProducer(new ViewProducer() {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty, parent, false);
        return new DefaultEmptyViewHolder(emptyView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder) {
    }
});
```

#### 增加HeaderView

```java
adapter.setEmptyViewProducer(new ViewProducer() {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);
        return new DefaultEmptyViewHolder(emptyView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder) {
    }
},false);
```

#### 监听事件

可以通过setListener的方式设置监听事件。

```java
public interface ExpandableRecyclerViewOnClickListener<GroupBean extends BaseGroupBean, ChildBean> {

        /**
         * 长按时的操作
         *
         * @param groupItem
         * @return
         */
        boolean onGroupLongClicked(GroupBean groupItem);

        /**
         * 在可展开group被点击的时候触发的回调，返回布尔值表示是否拦截该操作
         *
         * @param groupItem
         * @param isExpand
         * @return true 使点击无效。false 正常执行展开、闭合操作。
         */
        boolean onInterceptGroupExpandEvent(GroupBean groupItem, boolean isExpand);

        /**
         * 点击GroupView时的操作(该Group的isExpandable返回false才会触发这个回调)
         *
         * @param groupItem
         */
        void onGroupClicked(GroupBean groupItem);

        /**
         * 点击子View时的操作
         *
         * @param groupItem
         * @param childItem
         */
        void onChildClicked(GroupBean groupItem, ChildBean childItem);
    }
```

## 实现原理

### 父子结构划分

1.  通过getItemType判断所处类型，在基类中就先划分为四种类型的View。

```java
private static final int TYPE_EMPTY = ViewProducer.VIEW_TYPE_EMPTY;
private static final int TYPE_HEADER = ViewProducer.VIEW_TYPE_HEADER;
private static final int TYPE_GROUP = ViewProducer.VIEW_TYPE_EMPTY >> 2;
private static final int TYPE_CHILD = ViewProducer.VIEW_TYPE_EMPTY >> 3;
private static final int TYPE_MASK = TYPE_GROUP | TYPE_CHILD | TYPE_EMPTY | TYPE_HEADER;

// 通过getItemView判断类型，这里默认是使用上面定义的MASK，可以重载使Group和Child中再划分子类，但是不允许和TYPE_MASK冲突，否则会报Exception
@Override
public final int getItemViewType(int position) {
    if (mIsEmpty) {
        return position == 0 && mShowHeaderViewWhenEmpty ? TYPE_HEADER : TYPE_EMPTY;
    }
    if (position == 0 && mHeaderViewProducer != null) {
        return TYPE_HEADER;
    }
    int[] coord = translateToDoubleIndex(position);
    GroupBean groupBean = getGroupItem(coord[0]);
    if (coord[1] < 0) {
        int groupType = getGroupType(groupBean);
        if ((groupType & TYPE_MASK) == 0) {
            return groupType | TYPE_GROUP;
        } else {
            throw new IllegalStateException(
                String.format(Locale.getDefault(), "GroupType [%d] conflits with MASK [%d]", groupType, TYPE_MASK));
        }
    } else {
        int childType = getChildType(groupBean, groupBean.getChildAt(coord[1]));
        if ((childType & TYPE_MASK) == 0) {
            return childType | TYPE_CHILD;
        } else {
            throw new IllegalStateException(
                String.format(Locale.getDefault(), "ChildType [%d] conflits with MASK [%d]", childType, TYPE_MASK));
        }
    }
}
```

2.  在onCreateViewHolder和onBindViewHolder中进行类型判断，调用不同的方法，这些方法在子类中进行重载。注意这里将这三个方法都置为final，防止子类重载，子类只能重载对应不同type的具体方法。

```java
@Override
public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
        case TYPE_EMPTY:
            return mEmptyViewProducer.onCreateViewHolder(parent);
        case TYPE_HEADER:
            return mHeaderViewProducer.onCreateViewHolder(parent);
        case TYPE_CHILD:
            return onCreateChildViewHolder(parent, viewType ^ TYPE_CHILD);
        case TYPE_GROUP:
            return onCreateGroupViewHolder(parent, viewType ^ TYPE_GROUP);
        default:
            throw new IllegalStateException(
                String.format(Locale.getDefault(), "Illegal view type : viewType[%d]", viewType));
    }
}

@Override
public final void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    onBindViewHolder(holder, position, null);
}

@Override
public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    switch (holder.getItemViewType() & TYPE_MASK) {
        case TYPE_EMPTY:
            mEmptyViewProducer.onBindViewHolder(holder);
            break;
        case TYPE_HEADER:
            mHeaderViewProducer.onBindViewHolder(holder);
            break;
        case TYPE_CHILD:
            final int[] childCoord = translateToDoubleIndex(position);
            GroupBean groupBean = getGroupItem(childCoord[0]);
            bindChildViewHolder((ChildViewHolder) holder, groupBean, groupBean.getChildAt(childCoord[1]), payloads);
            break;
        case TYPE_GROUP:
            bindGroupViewHolder((GroupViewHolder) holder,
                getGroupItem(translateToDoubleIndex(position)[0]), payloads);
            break;
        default:
            throw new IllegalStateException(
                String.format(Locale.getDefault(), "Illegal view type : position [%d] ,itemViewType[%d]", position, holder.getItemViewType()));
    }
}
```

### 展开闭合操作

#### 操作

当groupBean的isExpandable返回true的时候，为itemView设置点击事件，进行展开闭合。

展开闭合的具体原理是在Set中记录展开闭合情况，当发生展开、闭合操作的时候进行更新，并使用notifyItemChange接口进行列表的局部刷新。

```java
private Set<GroupBean> mExpandGroupSet;

protected void bindGroupViewHolder(final GroupViewHolder holder, final GroupBean groupBean, List<Object> payload) {
  	// ...
    if (!groupBean.isExpandable()) {
      // ...
    } else {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isExpand = mExpandGroupSet.contains(groupBean);
                if (mListener == null || !mListener.onInterceptGroupExpandEvent(groupBean, isExpand)) {
                final int adapterPosition = holder.getAdapterPosition();
                holder.onExpandStatusChanged(BaseExpandableRecyclerViewAdapter.this, !isExpand);
                    if (isExpand) {
                        mExpandGroupSet.remove(groupBean);
                        notifyItemRangeRemoved(adapterPosition + 1, groupBean.getChildCount());
                    } else {
                        mExpandGroupSet.add(groupBean);
                        notifyItemRangeInserted(adapterPosition + 1, groupBean.getChildCount());
                    }
                }
            }
        });
    }
  	// 子类实现
    onBindGroupViewHolder(holder, groupBean, isGroupExpand(groupBean));
}
```

#### 局部刷新原理

定义了Payload，采用Payload机制进行局部刷新。

在notifyItemChange时传入payload，在onBindViewHolder操作中判断是否带有payload，若带有payload，则执行部分刷新的操作。

```java
private static final Object EXPAND_PAYLOAD = new Object();

// 局部刷新时调用的接口(已封装好，用户无需调用)
notifyItemChanged(position, EXPAND_PAYLOAD);

// 处理payload
protected void bindGroupViewHolder(final GroupViewHolder holder, final GroupBean groupBean, List<Object> payload) {
    if (payload != null && payload.size() != 0) {
        if (payload.contains(EXPAND_PAYLOAD)) {
          	// holder方法有抽象方法，在此方法中实现具体的展开、闭合逻辑
            holder.onExpandStatusChanged(BaseExpandableRecyclerViewAdapter.this, isGroupExpand(groupBean));
            if (payload.size() == 1) {
                return;
            }
        }
    onBindGroupViewHolder(holder, groupBean, isGroupExpand(groupBean), payload);
    return;
    }
}
```

## License

**MIT**

https://rem.mit-license.org/