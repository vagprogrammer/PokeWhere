package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildItem implements Parcelable{

    private int image;
    private String title;

    public ChildItem(int image, String title) {
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

    protected ChildItem(Parcel in){
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

    public static final Creator<ChildItem> CREATOR = new Creator<ChildItem>(){

        @Override
        public ChildItem createFromParcel(Parcel parcel) {
            return new ChildItem(parcel);
        }

        @Override
        public ChildItem[] newArray(int size) {
            return new ChildItem[size];
        }
    };
}
