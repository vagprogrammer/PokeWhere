package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class LocalUserPokemon implements Parcelable{

    private Long id;
    private String pokemonId;
    private String name;
    private int number;
    private String nickname;
    private Boolean isFavorite;
    private Boolean isDead;
    private int cp;
    private int iv;
    private int attack;
    private int defense;
    private int stamina;
    private int maxCp;
    private int evolveCP;
    private float level;
    private int candies;
    private int powerUpStardust;
    private int poweUpCandies;
    private int evolveCandies;

    public LocalUserPokemon(){
        //Empty constructor is needed
    }

    public LocalUserPokemon(Long id,String pokemonId, String name, int number, String nickname, Boolean isFavorite, Boolean isDead,int cp, int iv, int attack, int defense, int stamina, int maxCp, int evolveCP, float level, int candies, int powerUpStardust, int poweUpCandies, int evolveCandies) {
        this.id = id;
        this.pokemonId = pokemonId;
        this.name = name;
        this.number = number;
        this.nickname = nickname;
        this.isFavorite = isFavorite;
        this.isDead = isDead;
        this.cp = cp;
        this.iv = iv;
        this.attack = attack;
        this.defense = defense;
        this.stamina = stamina;
        this.maxCp = maxCp;
        this.evolveCP = evolveCP;
        this.level = level;
        this.candies = candies;
        this.powerUpStardust = powerUpStardust;
        this.poweUpCandies = poweUpCandies;
        this.evolveCandies = evolveCandies;
    }

    protected LocalUserPokemon(Parcel in){
        id = in.readLong();
        name = in.readString();
        number = in.readInt();
        nickname = in.readString();
        isFavorite = in.readByte() != 0;
        cp = in.readInt();
        iv = in.readInt();
        attack = in.readInt();
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeByte((byte) (isFavorite ? 1 : 0));
        parcel.writeInt(cp);
        parcel.writeInt(iv);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalUserPokemon> CREATOR = new Creator<LocalUserPokemon>(){

        @Override
        public LocalUserPokemon createFromParcel(Parcel parcel) {
            return new LocalUserPokemon(parcel);
        }

        @Override
        public LocalUserPokemon[] newArray(int size) {
            return new LocalUserPokemon[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(String pokemonId) {
        this.pokemonId = pokemonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public void setDead(Boolean isDead) {
        isDead = isDead;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public int getIv() {
        return iv;
    }

    public void setIv(int iv) {
        this.iv = iv;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getMaxCp() {
        return maxCp;
    }

    public void setMaxCp(int maxCp) {
        this.maxCp = maxCp;
    }

    public int getEvolveCP() {
        return evolveCP;
    }

    public void setEvolveCP(int evolveCP) {
        this.evolveCP = evolveCP;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public int getCandies() {
        return candies;
    }

    public void setCandies(int candies) {
        this.candies = candies;
    }

    public int getPowerUpStardust() {
        return powerUpStardust;
    }

    public void setPowerUpStardust(int powerUpStardust) {
        this.powerUpStardust = powerUpStardust;
    }

    public int getPoweUpCandies() {
        return poweUpCandies;
    }

    public void setPoweUpCandies(int poweUpCandies) {
        this.poweUpCandies = poweUpCandies;
    }

    public int getEvolveCandies() {
        return evolveCandies;
    }

    public void setEvolveCandies(int evolveCandies) {
        this.evolveCandies = evolveCandies;
    }
}
