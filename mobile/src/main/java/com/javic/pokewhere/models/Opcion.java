package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class Opcion implements Parcelable{

    private int image;
    private String title;

    public Opcion(int image, String title) {
        this.image = image;
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected Opcion(Parcel in){
        image = in.readInt();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(image);
        parcel.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Opcion> CREATOR = new Parcelable.Creator<Opcion>(){

        @Override
        public Opcion createFromParcel(Parcel parcel) {
            return new Opcion(parcel);
        }

        @Override
        public Opcion[] newArray(int size) {
            return new Opcion[size];
        }
    };
}
