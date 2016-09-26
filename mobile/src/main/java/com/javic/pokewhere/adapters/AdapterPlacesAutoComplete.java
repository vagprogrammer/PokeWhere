package com.javic.pokewhere.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.javic.pokewhere.models.PlaceSuggestion;
import com.javic.pokewhere.util.PlaceAPI;

import java.util.ArrayList;

/**
 * Created by vagprogrammer on 24/09/16.
 */

public class AdapterPlacesAutoComplete extends ArrayAdapter<String> implements Filterable {

    private Context mContext;
    private int mResource;

    public ArrayList<PlaceSuggestion> resultList;
    public PlaceAPI mPlaceAPI = new PlaceAPI();

    public AdapterPlacesAutoComplete(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        return resultList.get(position).getBody();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    resultList = mPlaceAPI.autocomplete(constraint.toString());

                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }
}