package com.javic.pokewhere.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.holders.ChildTransferablePokemonViewHolder;
import com.javic.pokewhere.holders.GroupTransferablePokemonViewHolder;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class AdapterChildTransferablePokemon extends CheckableChildRecyclerViewAdapter<GroupTransferablePokemonViewHolder, ChildTransferablePokemonViewHolder> {

    private Context mContext;

    public AdapterChildTransferablePokemon(List<GroupTransferablePokemon> groups, Context mContext) {
        super(groups);
        this.mContext = mContext;
    }

    @Override
    public ChildTransferablePokemonViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_child_transferable_pokemon, parent, false);

        return new ChildTransferablePokemonViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(ChildTransferablePokemonViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final ChildTransferablePokemon child = (ChildTransferablePokemon) group.getItems().get(childIndex);
        holder.setUp(child);
    }

    @Override
    public GroupTransferablePokemonViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_group_transferable_pokemon, parent, false);
        return new GroupTransferablePokemonViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(GroupTransferablePokemonViewHolder holder, int flatPosition, final ExpandableGroup group) {
        final GroupTransferablePokemon g = (GroupTransferablePokemon) group;
        final int pokemonIdNumber = g.getPokemonIdNumber();
        holder.setTexts(g);

        AssetManager assetManager = mContext.getAssets();

        try {
            InputStream is = null;

            /*if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" +pokemonIdNumber) + ".ico");
            } else if (pokemonIdNumber < 100) {
                is = assetManager.open(String.valueOf("0" + pokemonIdNumber) + ".ico");
            } else {
                is = assetManager.open(String.valueOf(pokemonIdNumber) + ".ico");
            }*/

            if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" +pokemonIdNumber) + ".png");
            } else if (pokemonIdNumber < 100) {
                is = assetManager.open(String.valueOf("0" + pokemonIdNumber) + ".png");
            } else {
                is = assetManager.open(String.valueOf(pokemonIdNumber) + ".png");
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            holder.setFiltroImagen(bitmap);
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        }
    }
}

