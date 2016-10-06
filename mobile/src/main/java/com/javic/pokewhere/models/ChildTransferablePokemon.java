package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildTransferablePokemon implements Parcelable{

    private Long id;
    private Boolean isFavorite;
    private int cp;
    private int iv;
    private String title;

    public ChildTransferablePokemon(Long id, Boolean isFavorite, int cp,int iv, String title) {
        this.id = id;
        this.isFavorite= isFavorite;
        this.cp= cp;
        this.iv = iv;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected ChildTransferablePokemon(Parcel in){
        id = in.readLong();
        isFavorite = in.readByte() != 0;
        cp = in.readInt();
        iv = in.readInt();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeByte((byte) (isFavorite ? 1 : 0));
        parcel.writeInt(cp);
        parcel.writeInt(iv);
        parcel.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChildTransferablePokemon> CREATOR = new Creator<ChildTransferablePokemon>(){

        @Override
        public ChildTransferablePokemon createFromParcel(Parcel parcel) {
            return new ChildTransferablePokemon(parcel);
        }

        @Override
        public ChildTransferablePokemon[] newArray(int size) {
            return new ChildTransferablePokemon[size];
        }
    };
}
