package com.javic.pokewhere.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.ChildItemViewHolder;
import com.javic.pokewhere.holders.GroupItemViewHolder;
import com.javic.pokewhere.models.ChildItem;
import com.javic.pokewhere.models.GroupItem;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by victor on 29/09/16.
 */

public class AdapterChildItem extends ExpandableRecyclerViewAdapter<GroupItemViewHolder, ChildItemViewHolder> {
    private Context mContext;

    public AdapterChildItem(List<? extends ExpandableGroup> groups, Context mContext) {
        super(groups);
        this.mContext = mContext;
    }

    @Override
    public GroupItemViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_group_item, parent, false);
        return new GroupItemViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(GroupItemViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setItemTitle((GroupItem)group);
    }

    @Override
    public ChildItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_child_item, parent, false);
        return new ChildItemViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ChildItemViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {

        final ChildItem childItem = ((GroupItem) group).getItems().get(childIndex);
        holder.setItem(childItem.getTitle());
    }



}
