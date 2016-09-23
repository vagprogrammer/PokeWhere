package com.javic.pokewhere.fragments;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.javic.pokewhere.ActivityFiltros;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterFiltro;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.LocalGym;
import com.javic.pokewhere.models.LocalPokeStop;
import com.javic.pokewhere.models.LocalPokemon;
import com.javic.pokewhere.models.Opcion;
import com.javic.pokewhere.models.TransferablePokemon;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;

import static com.javic.pokewhere.R.id.toolbar;


public class FragmentTransfer extends Fragment implements View.OnClickListener, GroupExpandCollapseListener, OnCheckChildClickListener {

    private static final String TAG = FragmentTransfer.class.getSimpleName();

    private OnFragmentCreatedViewListener mListener;

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Fragment UI
    private View mView;
    private TextView tvStatus;
    private Button btnTransferir;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;

    //Context
    private Context mContext;

    //Variables
    private Boolean isTransfering = true;

    //Tasks
    private TransferTask mTransferTask;

    //Adapters
    private AdapterFiltro mAdpaterFiltro;

    //Listas
    private List<Pokemon> mUserPokemons;
    private List<TransferablePokemon> mTransferablePokemons = new ArrayList<>();;
    private List<Filtro> mFiltrosPokemons = new ArrayList<>();


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
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        switch (item.getItemId()) {
            case R.id.action_aplicar:

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_transfer, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        mToolbar.inflateMenu(R.menu.fragment_transfer);



        mLayoutManager = new LinearLayoutManager(mContext);
        tvStatus = (TextView) mView.findViewById(R.id.textViewStatus);
        btnTransferir = (Button) mView.findViewById(R.id.buttonTransferir);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo != null) {
            btnTransferir.setOnClickListener(this);

            //instantiate your adapter with the list of bands
            mAdpaterFiltro = new AdapterFiltro(mFiltrosPokemons);

            mAdpaterFiltro.setOnGroupExpandCollapseListener(this);
            mAdpaterFiltro.setChildClickListener(this);

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdpaterFiltro);

            new Thread(new Runnable() {
                public void run() {

                    setUpFiltros();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdpaterFiltro.notifyDataSetChanged();
                            mListener.onFragmentCreatedViewStatus(true);
                        }
                    });
                }
            }).start();
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
        switch (view.getId()) {
            case R.id.buttonTransferir:

                if (isDeviceOnline()) {
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

    @Override
    public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {

    }

    @Override
    public void onGroupExpanded(ExpandableGroup group) {

    }

    @Override
    public void onGroupCollapsed(ExpandableGroup group) {

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

                for (Pokemon pokemonToTransfer : pokemons) {

                    if (isTransfering) {
                        if (pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.PIDGEY ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.RATTATA ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.EKANS ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.MAGNEMITE ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.SANDSHREW ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.GEODUDE ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.MANKEY ||
                                pokemonToTransfer.getPokemonId() == PokemonIdOuterClass.PokemonId.VENONAT) {
                            publishProgress("Transfering " + pokemonToTransfer.getPokemonId() + " with CP: " + String.valueOf(pokemonToTransfer.getCp()) + "... ");
                            pokemonToTransfer.debug();
                            ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                            publishProgress("Transfered result: " + result);
                            sleep(500);
                        }
                    } else {
                        mTransferTask.cancel(true);
                    }

                }

                return true;

            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

                return false;
            }


        }

        @Override
        protected void onProgressUpdate(String... data) {

            super.onProgressUpdate(data);

            tvStatus.setText(data[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mTransferTask = null;
            isTransfering = false;

            if (succes) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Transfering complete");
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Ocurrio un error, por favor intentalo de nuevo.");
                    }
                });
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
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


    public void setUpFiltros() {

        try {
            mUserPokemons = mPokemonGo.getInventories().getPokebank().getPokemons();

            for (Pokemon pokemon : mUserPokemons) {

                if (!containsEncounteredId(pokemon.getPokemonId())){
                    //Obtenemos todos los pokemons para este id
                    List<Pokemon> specificPokemons = mPokemonGo.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId());
                    List<Opcion> mOpcionPokemons = new ArrayList<>();

                    for (Pokemon specificPokemon:specificPokemons){

                        TransferablePokemon transferablePokemon = new TransferablePokemon();
                        transferablePokemon.setPokemonId(specificPokemon.getPokemonId());
                        transferablePokemon.setId(specificPokemon.getId());
                        transferablePokemon.setCp(specificPokemon.getCp());
                        transferablePokemon.setFavorite(specificPokemon.isFavorite());
                        transferablePokemon.setDead(specificPokemon.isInjured());
                        mTransferablePokemons.add(transferablePokemon);

                        mOpcionPokemons.add(new Opcion(R.drawable.ic_pokeball,specificPokemon.getPokemonId().toString() + " CP: " + String.valueOf(transferablePokemon.getCp() )));

                    }

                    mFiltrosPokemons.add(new Filtro(pokemon.getPokemonId().toString(), mOpcionPokemons));
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    public boolean containsEncounteredId(PokemonIdOuterClass.PokemonId enconunteredId) {

        for (Filtro filtroPokemon : mFiltrosPokemons) {
            if (filtroPokemon.getTitle() == enconunteredId.toString()) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }
}
