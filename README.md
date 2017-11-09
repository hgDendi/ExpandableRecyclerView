# ExpandableRecyclerView
Customized recyclerViewAdapter which shows items in a 2-level list.

![demo](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/expandableRecyclerView.gif)

It encapsulates the **expand and fold operation** in abstract class [ExpandableListAdapter](https://github.com/hgDendi/ExpandableRecyclerView/blob/master/expandablerecycleradapter/src/main/java/com/hgdendi/expandablerecycleradapter/ExpandableRecyclerViewAdapter.java),which makes it flexible.

See the usage below , all you need to do is just extending the class and overriding these 5 methods:

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
	compile 'com.hgDendi:expandable-recyclerview-adapter:0.0.2'
}
```

## Usage

```java
// !!Notice the generics here
public class SampleAdapter extends ExpandableRecyclerViewAdapter
        <GroupBean, SampleAdapter.GroupVH, SampleAdapter.ChildVH> {

    @Override
    public int getGroupCount() {
        //return the size of group
    }

    @Override
    public GroupBean getGroupItem(int position) {
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
class GroupVH extends RecyclerView.ViewHolder {
    //customize
}

class ChildVH extends RecyclerView.ViewHolder {
    //customize
}
    
class GroupBean implements ExpandableRecyclerViewAdapter.GroupNode {
    @Override
    public int getChildCount() {
    }
}
```

## License

**MIT**

https://rem.mit-license.org/