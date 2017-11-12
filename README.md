# ExpandableRecyclerView

[【中文README】](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/README_CN.md)

Customized recyclerViewAdapter which shows items in a 2-level list.

![demo](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/img/expandableRecyclerView.gif)

It encapsulates the **expand and fold operation** in abstract class [BaseExpandableListAdapter](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/expandablerecycleradapter/src/main/java/com/hgdendi/expandablerecycleradapter/ExpandableRecyclerViewAdapter.java),which makes it flexible.

See the usage below , all you need to do is just extending the class and overriding these 6 methods:

* **getGroupCount**
* **getGroupItem**
* onCreateGroupViewHolder
* onCreateChildViewHolder
* onBindGroupViewHolder
* onBindChildViewHolder

As onCreateViewHolder & onBindViewHolder are compulsive methods which need developers to override. All your extra work is overriding getGroupCount & getGroupPosition. In most conditions , it is just a one-line-method.

## Gradle

```groovy
dependencies{
	compile 'com.hgDendi:expandable-recyclerview-adapter:1.0.0'
}
```

## Usage

```java
// !!Notice the generics here
public class SampleAdapter extends BaseExpandableRecyclerViewAdapter
<SampleGroupBean, SampleChildBean, SampleAdapter.GroupVH, SampleAdapter.ChildVH>

    @Override
    public int getGroupCount() {
        //return the size of group
    }

    @Override
    public GroupBean getGroupItem(int groupIndex) {
        //return the bean according to the groupIndex
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
    public void onBindChildViewHolder(ChildVH holder, SampleGroupBean sampleGroupBean, int childIndex) {
    }
}
```

Relating classes:

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

static class GroupVH extends BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder {
    GroupVH(View itemView) {
        super(itemView);
    }

    // this method is used for partial update.Which means when expand status changed,only a part of this view need to invalidate
    @Override
    protected void onExpandStatusChanged(RecyclerView.Adapter relatedAdapter, boolean isExpanding) {
    }
}

static class ChildVH extends RecyclerView.ViewHolder {
    ChildVH(View itemView) {
        super(itemView);
        nameTv = (TextView) itemView.findViewById(R.id.child_item_name);
    }
}
```

## License

**MIT**

https://rem.mit-license.org/