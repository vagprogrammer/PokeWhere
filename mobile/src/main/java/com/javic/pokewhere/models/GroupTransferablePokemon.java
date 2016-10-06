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
    private int pokemonIdNumber;

    private int pokemonCandies;

    public GroupTransferablePokemon(String pokemonId, int pokemonIdNumber, int childCount, int pokemonCandies, String title, List items) {
        super(title, items);
        this.pokemonId = pokemonId;
        this.childCount= childCount;
        this.pokemonCandies = pokemonCandies;
        this.pokemonIdNumber = pokemonIdNumber;

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

    public int getPokemonIdNumber() {
        return pokemonIdNumber;
    }

    public void setPokemonIdNumber(int pokemonIdNumber) {
        this.pokemonIdNumber = pokemonIdNumber;
    }

    public int getPokemonCandies() {
        return pokemonCandies;
    }

    public void setPokemonCandies(int pokemonCandies) {
        this.pokemonCandies = pokemonCandies;
    }
}
