package com.javic.pokewhere.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.PokemonMove;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import POGOProtos.Data.PokemonDataOuterClass;

/**
 * Created by iMac_Vic on 13/12/16.
 */

public class ServiceMapObjects extends Service {

    private int counter =0;
    private static final String TAG = ServiceMapObjects.class.getSimpleName();

    private final IBinder mBinder = new MyBinder();
    private List<LocalUserPokemon> localUserPokemonList = new ArrayList<>();

    private PokemonsTask mPokemonTask;

    private PokemonGo mPokemonGo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand");

        counter= counter+1;
        //Toast.makeText(this, "onStartCommand called: " + String.valueOf(counter) + " times", Toast.LENGTH_SHORT).show();

        /*if (mCounterToRemoveMarkers == null) {
            mCounterToRemoveMarkers.start();
        }

        if (mPokemonTask == null) {
            mPokemonTask = new PokemonsTask(new LatLng(121212,-121212));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokemonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokemonTask.execute();
            }
        }*/


        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    public class MyBinder extends Binder {

        public ServiceMapObjects getService() {
            return ServiceMapObjects.this;
        }
    }

    public List<LocalUserPokemon> getLocalUserPokemonList() {
        return localUserPokemonList;
    }


    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class PokemonsTask extends AsyncTask<Void, Void, Boolean> {

        private LatLng mSearchPoint;

        public PokemonsTask(LatLng mSearchPoint){
            this.mSearchPoint = mSearchPoint;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
                    try {
                        mPokemonGo.setLocation(mSearchPoint.latitude, mSearchPoint.longitude, 1);

                       List<CatchablePokemon> catchablePokemonList = mPokemonGo.getMap().getCatchablePokemon();

                        if (catchablePokemonList != null) {

                            for (CatchablePokemon catchablePokemon : catchablePokemonList) {

                                // You need to Encounter first.
                                EncounterResult encResult = catchablePokemon.encounterPokemon();

                                if (encResult.wasSuccessful()) {

                                    PokemonDataOuterClass.PokemonData pokemonData = encResult.getPokemonData();

                                    LocalUserPokemon localUserPokemon = new LocalUserPokemon();

                                    localUserPokemon.setId(catchablePokemon.getEncounterId());
                                    localUserPokemon.setExpirationTimeMs(catchablePokemon.getExpirationTimestampMs());
                                    localUserPokemon.setNumber(catchablePokemon.getPokemonId().getNumber());
                                    localUserPokemon.setName(catchablePokemon.getPokemonId().name());
                                    localUserPokemon.setLatitude(catchablePokemon.getLatitude());
                                    localUserPokemon.setLongitude(catchablePokemon.getLongitude());
                                    localUserPokemon.setBitmap(getBitmapFromAssets(catchablePokemon.getPokemonId().getNumber()));
                                    localUserPokemon.setCp(pokemonData.getCp());
                                    Double iv_ratio = ((pokemonData.getIndividualAttack() + pokemonData.getIndividualDefense() + pokemonData.getIndividualStamina()) / 45.0) * (100.0);
                                    localUserPokemon.setIv(iv_ratio.intValue());
                                    localUserPokemon.setAttack(pokemonData.getIndividualAttack());
                                    localUserPokemon.setDefense(pokemonData.getIndividualDefense());
                                    localUserPokemon.setStamina(pokemonData.getIndividualStamina());

                                    PokemonMoveMeta moveMeta1 = PokemonMoveMetaRegistry.getMeta(pokemonData.getMove1());
                                    PokemonMoveMeta moveMeta2 = PokemonMoveMetaRegistry.getMeta(pokemonData.getMove2());
                                    PokemonMove move1 = new PokemonMove(moveMeta1.getMove().name(), moveMeta1.getAccuracy(), moveMeta1.getCritChance(), moveMeta1.getEnergy(),moveMeta1.getPower(), moveMeta1.getTime());
                                    PokemonMove move2 = new PokemonMove(moveMeta2.getMove().name(), moveMeta2.getAccuracy(), moveMeta2.getCritChance(), moveMeta2.getEnergy(),moveMeta2.getPower(), moveMeta2.getTime());
                                    final List<PokemonMove> moves = new ArrayList<>();
                                    moves.add(move1);
                                    moves.add(move2);
                                    localUserPokemon.setMoves(moves);

                                   Boolean isEncountered = containsEncounteredId(localUserPokemon, String.valueOf(localUserPokemon.getId()));

                                    if (!isEncountered) {
                                        Log.i(TAG, encResult.getPokemonData().getPokemonId() + " Encountered..." + " CP: " + encResult.getPokemonData().getCp() + " ExpirationTime: " + String.valueOf(catchablePokemon.getExpirationTimestampMs()));
                                        localUserPokemonList.add(localUserPokemon);
                                    }
                                }
                            }
                        }
                    } catch (LoginFailedException | RemoteServerException | CaptchaActiveException e) {
                        Log.e(TAG, "Failed to get pokemons or server issue Login or RemoteServer exception: ", e);
                    }

            return false;
            }

        @Override
        protected void onPostExecute(Boolean succes) {
            mPokemonTask = null;
        }

        @Override
        protected void onCancelled() {
            mPokemonTask = null;
        }
    }


    private Bitmap getBitmapFromAssets(int pokemonIdNumber) {
        AssetManager assetManager = getAssets();

        Bitmap bitmap = null;

        try {
            InputStream is = null;

            if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" + pokemonIdNumber) + ".png");
            } else if (pokemonIdNumber < 100) {
                is = assetManager.open(String.valueOf("0" + pokemonIdNumber) + ".png");
            } else {
                is = assetManager.open(String.valueOf(pokemonIdNumber) + ".png");
            }

            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        }

        return bitmap;
    }

    public boolean containsEncounteredId(Object object, String enconunteredId) {

        boolean encountered = false;

        if (object instanceof LocalUserPokemon) {
            for (LocalUserPokemon localUserPokemon : localUserPokemonList) {
                if (String.valueOf(localUserPokemon.getId()).equals(enconunteredId)) {
                    encountered = true;
                }
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return encountered;
    }

}
