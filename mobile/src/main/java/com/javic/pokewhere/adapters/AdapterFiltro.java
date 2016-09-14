package com.javic.pokewhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.FiltroViewHolder;
import com.javic.pokewhere.holders.OpcionViewHolder;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterFiltro extends CheckableChildRecyclerViewAdapter<FiltroViewHolder, OpcionViewHolder> {

    public AdapterFiltro(List<Filtro> groups) {
        super(groups);
    }

    @Override
    public OpcionViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recycler_item_opcion, parent, false);

        return new OpcionViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(OpcionViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final Opcion opcion = (Opcion) group.getItems().get(childIndex);
        holder.setOpcionTitle(opcion.getTitle());
        holder.setOpcionImage(opcion.getImage());
    }

    @Override
    public FiltroViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recycler_item_filtro, parent, false);
        return new FiltroViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(FiltroViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setFiltroTitle(group);
    }
}
