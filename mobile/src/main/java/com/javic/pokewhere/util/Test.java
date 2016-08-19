package com.javic.pokewhere.util;

/**
 * Created by franciscojimenezjimenez on 09/08/16.
 */
public class Test {


   /* public void getPokemons(String LatLong) {

        Log.i(TAG, "URL request --> " + Constants.URL_FEED + LatLong);

        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                Constants.URL_FEED + LatLong, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

    }

    *//**
     * Parsing json reponse and passing the data to feed view list adapter
     *//*
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("pokemon");

            Log.i(TAG, "user position:" + userPosition.latitude + " Longitude: " + userPosition.longitude);

            mPokemons.clear();

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                LocalPokemon pokemon = new LocalPokemon();
                pokemon.setId(feedObj.getLong("id"));
                //pokemon.setData(feedObj.getString("data"));
                pokemon.setExpiration_time(feedObj.getLong("expiration_time"));
                pokemon.setPokemonId(feedObj.getLong("pokemonId"));
                pokemon.setPokemonName(mPokemonsMap.get(String.valueOf(feedObj.getLong("pokemonId"))));
                pokemon.setLatitude(feedObj.getDouble("latitude"));
                pokemon.setLongitude(feedObj.getDouble("longitude"));
                //pokemon.setUid(feedObj.getString("uid"));
                //pokemon.setIs_alive(feedObj.getBoolean("is_alive"));
                mPokemons.add(pokemon);


                if (!containsEncounteredId(mPokemons, pokemon.getId())) {
                    mPokemons.add(pokemon);
                    Log.i(TAG, pokemon.getPokemonName());

                    drawPokemon(pokemon);
                }

            }

            if (mPokemons.isEmpty()) {
                // notify data changes to list adapater
                showMessage(getString(R.string.message_json_request_empty));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
