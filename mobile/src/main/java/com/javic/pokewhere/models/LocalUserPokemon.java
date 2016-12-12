package com.javic.pokewhere.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */

public class LocalUserPokemon implements Parcelable{

    private Long id;
    private String name;
    private Bitmap bitmap;
    private int number;
    private String nickname;
    private Boolean isFavorite;
    private Boolean isDead = false;
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
    private Long creationTimeMillis;
    private int pokemonCount;
    private double latitude;
    private double longitude;
    private long expirationTimeMs;
    private List<PokemonMove> moves;

    public LocalUserPokemon(){
        //Empty constructor is needed
    }

    public LocalUserPokemon(Long id, String name, Bitmap bitmap, int number, String nickname, Boolean isFavorite, Boolean isDead,int cp, int iv, int attack, int defense, int stamina, int maxCp, int evolveCP, float level, int candies, int powerUpStardust, int poweUpCandies, int evolveCandies, Calendar creationTime, Long creationTimeMillis, int pokemonCount, ArrayList<PokemonMove> moves) {
        this.id = id;
        this.name = name;
        this.bitmap = bitmap;
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
        this.creationTimeMillis = creationTimeMillis;
        this.pokemonCount = pokemonCount;
        this.moves = moves;
    }


    protected LocalUserPokemon(Parcel in){
        id = in.readLong();
        name = in.readString();
        bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        number = in.readInt();
        nickname = in.readString();
        isFavorite = in.readByte() != 0;
        isDead = in.readByte() != 0;
        cp = in.readInt();
        iv = in.readInt();
        attack = in.readInt();
        defense = in.readInt();
        stamina = in.readInt();
        maxCp = in.readInt();
        evolveCP = in.readInt();
        level = in.readFloat();
        candies = in.readInt();
        powerUpStardust = in.readInt();
        poweUpCandies = in.readInt();
        evolveCandies = in.readInt();
        creationTimeMillis = in.readLong();
        pokemonCount = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        expirationTimeMs = in.readLong();

        moves= new ArrayList<PokemonMove>();
        in.readList(moves, null);

        //moves = in.readArrayList(null);
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeValue(bitmap);
        parcel.writeInt(number);
        parcel.writeString(nickname);
        parcel.writeByte((byte) (isFavorite ? 1 : 0));
        parcel.writeByte((byte) (isDead ? 1 : 0));
        parcel.writeInt(cp);
        parcel.writeInt(iv);
        parcel.writeInt(attack);
        parcel.writeInt(defense);
        parcel.writeInt(stamina);
        parcel.writeInt(maxCp);
        parcel.writeInt(evolveCP);
        parcel.writeFloat(level);
        parcel.writeInt(candies);
        parcel.writeInt(powerUpStardust);
        parcel.writeInt(poweUpCandies);
        parcel.writeInt(evolveCandies);
        parcel.writeLong(creationTimeMillis);
        parcel.writeInt(pokemonCount);

        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeLong(expirationTimeMs);

        parcel.writeList(moves);

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
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

    public Long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    public void setCreationTimeMillis(Long creationTimeMillis) {
        this.creationTimeMillis = creationTimeMillis;
    }

    public int getPokemonCount() {
        return pokemonCount;
    }

    public void setPokemonCount(int pokemonCount) {
        this.pokemonCount = pokemonCount;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getExpirationTimeMs() {
        return expirationTimeMs;
    }

    public void setExpirationTimeMs(long expirationTimeMs) {
        this.expirationTimeMs = expirationTimeMs;
    }

    public List<PokemonMove> getMoves() {
        return moves;
    }

    public void setMoves(List<PokemonMove> moves) {
        this.moves = moves;
    }
}
