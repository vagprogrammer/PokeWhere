package com.javic.pokewhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.ChildTransferablePokemonViewHolder;
import com.javic.pokewhere.holders.FiltroViewHolder;
import com.javic.pokewhere.holders.OpcionViewHolder;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterChildTransferablePokemon extends CheckableChildRecyclerViewAdapter<FiltroViewHolder, ChildTransferablePokemonViewHolder> {


    public AdapterChildTransferablePokemon(List<Filtro> groups) {
        super(groups);
    }

    @Override
    public ChildTransferablePokemonViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recycler_item_child_transferable_pokemon, parent, false);

        return new ChildTransferablePokemonViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(ChildTransferablePokemonViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final ChildTransferablePokemon opcion = (ChildTransferablePokemon) group.getItems().get(childIndex);
        holder.setOpcionTitle(opcion.getTitle());
    }

    @Override
    public FiltroViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recycler_item_filtro, parent, false);
        return new FiltroViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(FiltroViewHolder holder, int flatPosition, final ExpandableGroup group) {
        holder.setFiltroTitle(group);
    }
}

