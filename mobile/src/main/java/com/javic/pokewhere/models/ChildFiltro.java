package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildFiltro implements Parcelable{

    private int image;
    private String title;

    public ChildFiltro(int image, String title) {
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

    protected ChildFiltro(Parcel in){
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

    public static final Parcelable.Creator<ChildFiltro> CREATOR = new Parcelable.Creator<ChildFiltro>(){

        @Override
        public ChildFiltro createFromParcel(Parcel parcel) {
            return new ChildFiltro(parcel);
        }

        @Override
        public ChildFiltro[] newArray(int size) {
            return new ChildFiltro[size];
        }
    };
}
