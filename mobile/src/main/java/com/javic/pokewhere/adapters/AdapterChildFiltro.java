package com.javic.pokewhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.GroupFiltroViewHolder;
import com.javic.pokewhere.holders.ChildFiltroViewHolder;
import com.javic.pokewhere.models.GroupFiltro;
import com.javic.pokewhere.models.ChildFiltro;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterChildFiltro extends CheckableChildRecyclerViewAdapter<GroupFiltroViewHolder, ChildFiltroViewHolder> {


    public AdapterChildFiltro(List<GroupFiltro> groups) {
        super(groups);
    }

    @Override
    public ChildFiltroViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_child_filtro, parent, false);

        return new ChildFiltroViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(ChildFiltroViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final ChildFiltro childFiltro = (ChildFiltro) group.getItems().get(childIndex);
        holder.setOpcionTitle(childFiltro.getTitle());
        holder.setOpcionImage(childFiltro.getImage());
    }

    @Override
    public GroupFiltroViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_group_filtro, parent, false);
        return new GroupFiltroViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(GroupFiltroViewHolder holder, int flatPosition, final ExpandableGroup group) {
        holder.setFiltroTitle(group);

    }
}

