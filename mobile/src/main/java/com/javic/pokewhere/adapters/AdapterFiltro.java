package com.javic.pokewhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.FiltroViewHolder;
import com.javic.pokewhere.holders.OpcionViewHolder;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterFiltro extends ExpandableRecyclerViewAdapter<FiltroViewHolder, OpcionViewHolder> {

    public AdapterFiltro(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public FiltroViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.layout_recycler_item_filtro, parent,false);

        return new FiltroViewHolder(view);
    }

    @Override
    public OpcionViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.layout_recycler_item_opcion, parent,false);

        return new OpcionViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(OpcionViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {

        final Opcion opcion = ((Filtro) group).getItems().get(childIndex);
        holder.setOpcionImage(opcion.getImage());
        holder.setOpcionTitle(opcion.getTitle());
    }

    @Override
    public void onBindGroupViewHolder(FiltroViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setFiltroImagen(group);
    }
}
