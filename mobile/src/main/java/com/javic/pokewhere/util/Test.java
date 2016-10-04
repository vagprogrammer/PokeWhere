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


    //Errror BAN
   /* 10-01 09:13:09.192 30359-31379/com.javic.pokewhere E/FragmentMapa: Failed to get pokemons or server issue General exception:
    com.pokegoapi.exceptions.AsyncPokemonGoException: Unknown exception occurred.
    at com.pokegoapi.util.AsyncHelper.toBlocking(AsyncHelper.java:46)
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.useItem(CatchablePokemon.java:595)
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:454)
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:265)
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:820)
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:768)
    at android.os.AsyncTask$2.call(AsyncTask.java:295)
    at java.util.concurrent.FutureTask.run(FutureTask.java:237)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)
    at java.lang.Thread.run(Thread.java:818)
    Caused by: java.lang.RuntimeException: java.util.concurrent.ExecutionException: com.pokegoapi.exceptions.RemoteServerException: Your account may be banned! please try from the official client.
    at rx.exceptions.Exceptions.propagate(Exceptions.java:58)
    at rx.observables.BlockingObservable.blockForSingle(BlockingObservable.java:465)
    at rx.observables.BlockingObservable.first(BlockingObservable.java:168)
    at com.pokegoapi.util.AsyncHelper.toBlocking(AsyncHelper.java:38)
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.useItem(CatchablePokemon.java:595) 
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:454) 
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:265) 
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:820) 
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:768) 
    at android.os.AsyncTask$2.call(AsyncTask.java:295) 
    at java.util.concurrent.FutureTask.run(FutureTask.java:237) 
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113) 
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588) 
    at java.lang.Thread.run(Thread.java:818) 
    Caused by: java.util.concurrent.ExecutionException: com.pokegoapi.exceptions.RemoteServerException: Your account may be banned! please try from the official client.
    at com.pokegoapi.main.RequestHandler$1.get(RequestHandler.java:109)
    at com.pokegoapi.main.RequestHandler$1.get(RequestHandler.java:86)
    at rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:74)
    at rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:43)
    at rx.Observable.unsafeSubscribe(Observable.java:9861)
    at rx.internal.operators.OnSubscribeMap.call(OnSubscribeMap.java:48)
    at rx.internal.operators.OnSubscribeMap.call(OnSubscribeMap.java:33)
    at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
    at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
    at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:48)
    at rx.internal.operators.OnSubscribeLift.call(OnSubscribeLift.java:30)
    at rx.Observable.subscribe(Observable.java:9957)
    at rx.Observable.subscribe(Observable.java:9924)
    at rx.observables.BlockingObservable.blockForSingle(BlockingObservable.java:445)
    at rx.observables.BlockingObservable.first(BlockingObservable.java:168) 
    at com.pokegoapi.util.AsyncHelper.toBlocking(AsyncHelper.java:38) 
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.useItem(CatchablePokemon.java:595) 
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:454) 
    at com.pokegoapi.api.map.pokemon.CatchablePokemon.catchPokemon(CatchablePokemon.java:265) 
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:820) 
    at com.javic.pokewhere.fragments.FragmentMapa$PokemonsTask.doInBackground(FragmentMapa.java:768) 
    at android.os.AsyncTask$2.call(AsyncTask.java:295) 
    at java.util.concurrent.FutureTask.run(FutureTask.java:237) 
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113) 
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588) 
    at java.lang.Thread.run(Thread.java:818) 
    Caused by: com.pokegoapi.exceptions.RemoteServerException: Your account may be banned! please try from the official client.
    at com.pokegoapi.main.RequestHandler.internalSendServerRequests(RequestHandler.java:223)
    at com.pokegoapi.main.RequestHandler.run(RequestHandler.java:293)
    at java.lang.Thread.run(Thread.java:818) */
}
