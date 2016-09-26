package com.javic.pokewhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.ChildTransferablePokemonViewHolder;
import com.javic.pokewhere.holders.FiltroViewHolder;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterChildTransferablePokemon extends CheckableChildRecyclerViewAdapter<FiltroViewHolder, ChildTransferablePokemonViewHolder> {


    public AdapterChildTransferablePokemon(List<GroupTransferablePokemon> groups) {
        super(groups);
    }

    @Override
    public ChildTransferablePokemonViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_transferable_pokemon_child, parent, false);

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

