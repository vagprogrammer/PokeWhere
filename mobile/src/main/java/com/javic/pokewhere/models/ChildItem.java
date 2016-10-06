package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildItem implements Parcelable{

    private String title;
    private int itemCount;
    private int image;

    public ChildItem(int image, String title, int itemCount) {
        this.image = image;
        this.title = title;
        this.itemCount = itemCount;
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    protected ChildItem(Parcel in){
        image = in.readInt();
        title = in.readString();
        itemCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(image);
        parcel.writeString(title);
        parcel.writeInt(itemCount);
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
