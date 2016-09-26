package com.javic.pokewhere.models;

import android.annotation.SuppressLint;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
@SuppressLint("ParcelCreator")
public class GroupTransferablePokemon extends MultiCheckExpandableGroup {

    private String pokemonId;
    private int childCount;

    public GroupTransferablePokemon(String pokemonId, int childCount, String title, List items) {
        super(title, items);
        this.childCount= childCount;
        this.pokemonId = pokemonId;
    }

    public String getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(String pokemonId) {
        this.pokemonId = pokemonId;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}
