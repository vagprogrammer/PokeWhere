package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildTransferablePokemon implements Parcelable{

    private Long id;
    private int cp;
    private String title;

    public ChildTransferablePokemon(Long id, int cp, String title) {
        this.id = id;
        this.cp= cp;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected ChildTransferablePokemon(Parcel in){
        id = in.readLong();
        cp = in.readInt();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeInt(cp);
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
