package com.javic.pokewhere.util;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.javic.pokewhere.models.PlaceSuggestion;

import java.util.ArrayList;

/**
 * Created by vagprogrammer on 24/09/16.
 */
public class PlayServicesPlaceAPI {

    private GoogleApiClient mGoogleApiClient;

    Place placeToReturn;

    public PlayServicesPlaceAPI(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public ArrayList<PlaceSuggestion> getSuggestions(String query )
    {
        final ArrayList<PlaceSuggestion> resultList = new ArrayList<>();

        //Southwest corner to Northeast corner.
        LatLngBounds bounds = new LatLngBounds( new LatLng(16.0744386,-90.4435153),new LatLng(31.8110563,-106.5646006) );

        //Filter: https://developers.google.com/places/supported_types#table3
        //List<Integer> filterTypes = new ArrayList<Integer>();
        //filterTypes.add( Place.TYPE_ESTABLISHMENT );
        //AutocompleteFilter.create(filterTypes);

        Places.GeoDataApi.getAutocompletePredictions( mGoogleApiClient, query, bounds, null)
                .setResultCallback (
                        new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult( AutocompletePredictionBuffer buffer ) {

                                if( buffer == null )
                                    return;

                                if( buffer.getStatus().isSuccess() ) {
                                    for( AutocompletePrediction prediction : buffer ) {
                                        //Add as a new item to avoid IllegalArgumentsException when buffer is released
                                        resultList.add(new PlaceSuggestion(prediction.getFullText(null).toString()));
                                    }
                                }

                                //Prevent memory leak by releasing buffer
                                buffer.release();
                            }
                        });

        return resultList;
    }
}
