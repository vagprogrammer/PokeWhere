package com.javic.pokewhere.models;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by vagprogrammer on 24/09/16.
 */

public class PlaceSuggestion implements SearchSuggestion {

    private String mPlaceName;

    public PlaceSuggestion(String mPlaceName) {
        this.mPlaceName = mPlaceName;
    }

    public PlaceSuggestion(Parcel source) {
        this.mPlaceName = source.readString();
    }

    @Override
    public String getBody() {
        return mPlaceName;
    }

    public static final Creator<PlaceSuggestion> CREATOR = new Creator<PlaceSuggestion>() {
        @Override
        public PlaceSuggestion createFromParcel(Parcel in) {
            return new PlaceSuggestion(in);
        }

        @Override
        public PlaceSuggestion[] newArray(int size) {
            return new PlaceSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mPlaceName);
    }
}
