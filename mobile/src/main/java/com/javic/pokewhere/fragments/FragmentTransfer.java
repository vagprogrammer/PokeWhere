package com.javic.pokewhere.fragments;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.LocalPokeStop;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;


public class FragmentTransfer extends Fragment implements View.OnClickListener{


    private OnFragmentCreatedViewListener mListener;

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Fragment UI
    private View mView;
    private TextView tvStatus;
    private Button btnTransferir;

    //Context
    private Context mContext;

    //Variables
    private Boolean isTransfering = true;

    //Tasks
    private TransferTask mTransferTask;

    public FragmentTransfer() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentTransfer newInstance(PokemonGo pokemonGo) {
        FragmentTransfer fragment = new FragmentTransfer();

        mPokemonGo = pokemonGo;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_transfer, container, false);
        tvStatus = (TextView) mView.findViewById(R.id.textViewStatus);
        btnTransferir = (Button)mView.findViewById(R.id.buttonTransferir);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo!=null){
            btnTransferir.setOnClickListener(this);
            mListener.onFragmentCreatedViewStatus(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isTransfering != null) {
            if (isTransfering) {
                isTransfering = false;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentCreatedViewListener) {
            mListener = (OnFragmentCreatedViewListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentCreatedViewListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonTransferir:

                if (isDeviceOnline()){
                    if (mTransferTask == null) {
                        mTransferTask = new TransferTask(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            mTransferTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            mTransferTask.execute();
                        }
                    }
                }

                break;
        }
    }

    public class TransferTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        TransferTask(Boolean isEnabled) {
            isTransfering = isEnabled;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {


                    List<Pokemon> pokemons = mPokemonGo.getInventories().getPokebank().getPokemons();

                    for (Pokemon pokemonToTransfer: pokemons){

                            if (pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.PIDGEY || pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.RATTATA ||  pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.EKANS) {
                                publishProgress("Transfering " + pokemonToTransfer.getPokemonId() + "...");
                                pokemonToTransfer.debug();
                                ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                                publishProgress("Transfered result: " + result);
                            }

                            sleep(600);
                    }

            } catch (Exception e){

            }

            return false;
        }

        @Override
        protected void onProgressUpdate(String... data) {

            super.onProgressUpdate(data);

            tvStatus.setText(data[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            if (succes) {
                mTransferTask = null;
                isTransfering = false;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Transfering complete");
                    }
                });
            }
        }

        @Override
        protected void onCancelled() {
            mTransferTask = null;
            isTransfering = false;
        }

    }

    public boolean isDeviceOnline() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
