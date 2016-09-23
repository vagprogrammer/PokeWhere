package com.javic.pokewhere.models;

import POGOProtos.Enums.PokemonIdOuterClass;

/**
 * Created by victor on 22/09/16.
 */

public class TransferablePokemon {

    private PokemonIdOuterClass.PokemonId pokemonId;
    private Long id;
    private int cp;
    private Boolean transfer = false;
    private Boolean isFavorite;
    private Boolean isDead;


    public TransferablePokemon() {

    }

    public PokemonIdOuterClass.PokemonId getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(PokemonIdOuterClass.PokemonId pokemonId) {
        this.pokemonId = pokemonId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public Boolean getTransfer() {
        return transfer;
    }

    public void setTransfer(Boolean transfer) {
        this.transfer = transfer;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public Boolean getDead() {
        return isDead;
    }

    public void setDead(Boolean dead) {
        isDead = dead;
    }
}
