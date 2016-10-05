package com.javic.pokewhere.fragments;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Toast;

import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterChildTransferablePokemon;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.javic.pokewhere.models.TransferablePokemon;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;


public class FragmentTransfer extends Fragment implements OnCheckChildClickListener {

    private static final String TAG = FragmentTransfer.class.getSimpleName();

    private static final int TASK_FILTROS = 0;
    private static final int TASK_TRANSFER = 1;

    private OnFragmentCreatedViewListener mListener;

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Fragment UI
    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Snackbar mSnackBar;

    //Context
    private Context mContext;

    //Variables
    private Boolean isTransfering = true;

    //Tasks
    private FiltrosTask mFiltrosTask;
    private TransferTask mTransferTask;


    //Adapters
    private AdapterChildTransferablePokemon mAdpaterChildTransferablePokemon;

    //Listas
    private List<Pokemon> mUserPokemons;
    private List<TransferablePokemon> mTransferablePokemons = new ArrayList<>();
    private List<GroupTransferablePokemon> mFiltrosPokemonList = new ArrayList<>();

    public FragmentTransfer() {
        // Required empty public constructor
    }

    public static FragmentTransfer newInstance(PokemonGo pokemonGo) {
        FragmentTransfer fragment = new FragmentTransfer();

        mPokemonGo = pokemonGo;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_transfer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transferir:

                if (mTransferTask == null) {
                    mTransferTask = new TransferTask(true);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mTransferTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mTransferTask.execute();
                    }
                }

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
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), ActivityDashboard.mDrawerLayout, mToolbar, R.string.open_location_settings, R.string.open_location_settings);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        ActivityDashboard.mDrawerLayout.addDrawerListener(mDrawerToggle);

        mLayoutManager = new LinearLayoutManager(mContext);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo != null) {

            if (mFiltrosTask==null){
                mFiltrosTask = new FiltrosTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mFiltrosTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mFiltrosTask.execute();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDrawerToggle != null) {
            ActivityDashboard.mDrawerLayout.removeDrawerListener(mDrawerToggle);
        }

    }


    @Override
    public void onPause() {
        super.onPause();

        if (mTransferTask != null) {
            mTransferTask.cancel(true);
            mTransferTask = null;
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
    public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {

        ChildTransferablePokemon childTransferablePokemon = (ChildTransferablePokemon) group.getItems().get(childIndex);
        TransferablePokemon transferablePokemon = getTransferablePokemon(childTransferablePokemon.getId());

        if (transferablePokemon != null) {

            if (!transferablePokemon.getFavorite()) {

                if (transferablePokemon.getTransfer()) {
                    transferablePokemon.setTransfer(checked);
                    mTransferablePokemons.set(mTransferablePokemons.indexOf(getTransferablePokemon(childTransferablePokemon.getId())), transferablePokemon);
                    group.onChildClicked(childIndex, false);
                } else {
                    transferablePokemon.setTransfer(checked);
                    mTransferablePokemons.set(mTransferablePokemons.indexOf(getTransferablePokemon(childTransferablePokemon.getId())), transferablePokemon);
                    group.onChildClicked(childIndex, true);
                }

                showToast(countTransferablePokemons() + " pokemons to transfer", 500);
            } else {
                showToast("No puedes transferir un pokémon favorito", Toast.LENGTH_SHORT);
                group.onChildClicked(childIndex, false);
            }
        } else {
            showToast("Ocurrio un error, intentalo más tarde", Toast.LENGTH_SHORT);
            group.onChildClicked(childIndex, false);
        }

    }

    public class FiltrosTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

            mUserPokemons = new ArrayList<>();
            mFiltrosPokemonList = new ArrayList<>();
            mTransferablePokemons = new ArrayList<>();

            super.onPreExecute();
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                mPokemonGo.getInventories().updateInventories(true);
                mUserPokemons = mPokemonGo.getInventories().getPokebank().getPokemons();

                for (Pokemon pokemon : mUserPokemons) {

                    if (!containsEncounteredId(pokemon.getPokemonId())) {
                        //Obtenemos todos los pokemons para este id
                        List<Pokemon> specificPokemons = mPokemonGo.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId());
                        List<ChildTransferablePokemon> mChildTransferablePokemonList = new ArrayList<>();

                        for (Pokemon specificPokemon : specificPokemons) {

                            TransferablePokemon transferablePokemon = new TransferablePokemon();
                            transferablePokemon.setPokemonId(specificPokemon.getPokemonId());
                            transferablePokemon.setId(specificPokemon.getId());
                            transferablePokemon.setCp(specificPokemon.getCp());
                            transferablePokemon.setFavorite(specificPokemon.isFavorite());
                            transferablePokemon.setDead(specificPokemon.isInjured());
                            mTransferablePokemons.add(transferablePokemon);

                            mChildTransferablePokemonList.add(new
                                    ChildTransferablePokemon(specificPokemon.getId(),
                                    transferablePokemon.getCp(),
                                    specificPokemon.getNickname()
                                            + " CP: " + String.valueOf(transferablePokemon.getCp())
                                            + " IV: " + String.valueOf((int) (specificPokemon.getIvRatio() * 100)) + "%"));

                        }

                        // Sorting
                        Collections.sort(mChildTransferablePokemonList, new Comparator<ChildTransferablePokemon>() {

                            @Override
                            public int compare(ChildTransferablePokemon childTransferablePokemon1, ChildTransferablePokemon childTransferablePokemon2) {
                                return childTransferablePokemon1.getCp() - childTransferablePokemon2.getCp(); // Ascending
                            }
                        });

                        final String pokemonId = pokemon.getPokemonId().toString();
                        final int pokemonIdNumber = pokemon.getPokemonId().getNumber();
                        final int childCount = mChildTransferablePokemonList.size();

                        mFiltrosPokemonList.add(new GroupTransferablePokemon(pokemonId, pokemonIdNumber, childCount, pokemonId + " " + String.valueOf(childCount) + " Can:" + String.valueOf(pokemon.getCandy()), mChildTransferablePokemonList));

                        // Sorting
                        Collections.sort(mFiltrosPokemonList, new Comparator<GroupTransferablePokemon>() {

                            @Override
                            public int compare(GroupTransferablePokemon filtro1, GroupTransferablePokemon filtro2) {

                                return filtro1.getPokemonId().compareTo(filtro2.getPokemonId());
                            }
                        });
                    }
                }

                return true;

            } catch (Exception e) {
                Log.i(TAG, e.toString());

                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mFiltrosTask = null;
            setActionBarTitle("Total: " + String.valueOf(mFiltrosPokemonList.size()));

            if (succes) {
                setHasOptionsMenu(true);
                //instantiate your adapter with the list of bands
                mAdpaterChildTransferablePokemon = new AdapterChildTransferablePokemon(mFiltrosPokemonList, mContext);
                mAdpaterChildTransferablePokemon.setChildClickListener(FragmentTransfer.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdpaterChildTransferablePokemon);

                mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_TRANSFER);
            } else {
                setHasOptionsMenu(false);

                if (isDeviceOnline()) {
                    setActionBarTitle(getString(R.string.snack_bar_error_with_pokemon));
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_FILTROS);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_FILTROS);
                }

                mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_TRANSFER);

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
            mFiltrosTask = null;
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

                for (TransferablePokemon transferablePokemon : mTransferablePokemons) {

                    if (isTransfering) {
                        if (transferablePokemon.getTransfer()) {
                            Pokemon pokemonToTransfer = getUserPokemon(transferablePokemon.getId());

                            if (pokemonToTransfer != null) {
                                publishProgress(pokemonToTransfer.getPokemonId().toString());
                                pokemonToTransfer.debug();
                                ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                                publishProgress(result.toString());
                                sleep(300);
                                mPokemonGo.getInventories().updateInventories(true);
                            }
                        }
                    } else {
                        mTransferTask.cancel(true);
                    }
                }
                return true;

            } catch (Exception e) {
                Log.e(TAG, e.toString());

                return false;
            }


        }

        @Override
        protected void onProgressUpdate(String... data) {

            super.onProgressUpdate(data);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(data[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mTransferTask = null;
            isTransfering = false;

            if (succes) {

                if (mFiltrosTask == null) {
                    mFiltrosTask = new FiltrosTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mFiltrosTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mFiltrosTask.execute();
                    }
                }

            } else {

                if (isDeviceOnline()) {

                    setActionBarTitle(getString(R.string.snack_bar_error_with_pokemon));
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_TRANSFER);

                } else {

                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_TRANSFER);
                }

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
            mTransferTask = null;
            isTransfering = false;
        }

    }

    public boolean containsEncounteredId(PokemonIdOuterClass.PokemonId enconunteredPokemonId) {

        for (GroupTransferablePokemon filtroPokemon : mFiltrosPokemonList) {
            if (filtroPokemon.getPokemonId().equalsIgnoreCase(enconunteredPokemonId.toString())) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }

    public TransferablePokemon getTransferablePokemon(Long idPokemon) {

        for (TransferablePokemon transferablePokemon : mTransferablePokemons) {
            if (String.valueOf(transferablePokemon.getId()).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return transferablePokemon;
            }
        }
        return null;
    }

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : mUserPokemons) {
            if (String.valueOf(pokemon.getId()).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
    }

    public String countTransferablePokemons() {
        int size = 0;

        for (TransferablePokemon transferablePokemon : mTransferablePokemons) {
            if (transferablePokemon.getTransfer()) {
                size++;
            }
        }

        return String.valueOf(size);
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

    public void showToast(String message, int millisecons) {
        final Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, millisecons);
    }

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {

                            if (task == TASK_FILTROS) {
                                mFiltrosTask = new FiltrosTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mFiltrosTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mFiltrosTask.execute();
                                }

                            } else if (task == TASK_TRANSFER) {

                                mTransferTask = new TransferTask(true);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferTask.execute();
                                }
                            }

                        } else {

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
                });

        mSnackBar.show();

    }

    public void setActionBarTitle(final String message) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
            }
        });
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
