# ExpandableRecyclerView
Customized recyclerViewAdapter which shows items in a 2-level list.

![demo]()

## Gradle

```groovy
dependencies{
	...
	//todo
	compile ''
}
```

## Usage

```java
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
    public GroupVH onCreateGroupViewHolder(ViewGroup parent) {
    }

    @Override
    public ChildVH onCreateChildViewHolder(ViewGroup parent) {
    }

    @Override
    public void onBindGroupViewHolder(GroupVH holder, SampleGroupBean sampleGroupBean, boolean isExpand) {
    }

    @Override
    public void onBindChildViewHolder(ChildVH holder, SampleGroupBean sampleGroupBean, int childIndex) {
    }
}

//generic classes , below is the sample
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

