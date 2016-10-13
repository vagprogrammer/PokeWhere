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
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterChildTransferablePokemon;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.javic.pokewhere.models.ProgressTransferPokemon;
import com.javic.pokewhere.models.TransferablePokemon;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;

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
    public int pokemonStorage;
    public int totalPokemons;

    //Tasks
    private GetPokemonsTask mGetPokemonsTask;
    private TransferPokemonsTask mTransferPokemonsTask;


    //Adapters
    private AdapterChildTransferablePokemon mAdpaterChildTransferablePokemon;

    //Listas
    private List<Pokemon> mUserPokemonList;
    private List<TransferablePokemon> mTransferablePokemonList = new ArrayList<>();
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

                if (mTransferPokemonsTask == null) {
                    mTransferPokemonsTask = new TransferPokemonsTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mTransferPokemonsTask.execute();
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

            mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_TRANSFER);

            if (mGetPokemonsTask == null) {
                mGetPokemonsTask = new GetPokemonsTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mGetPokemonsTask.execute();
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

        if (mGetPokemonsTask != null) {
            Log.i(TAG, "FILTROS_TASK: cancel:true");
            mListener.showProgress(false);
            mGetPokemonsTask.cancel(true);
        }

        if (mTransferPokemonsTask != null) {
            Log.i(TAG, "TRANSFER_TASK: cancel:true");
            mListener.showProgress(false);
            mTransferPokemonsTask.cancel(true);
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
                    mTransferablePokemonList.set(mTransferablePokemonList.indexOf(getTransferablePokemon(childTransferablePokemon.getId())), transferablePokemon);
                    group.onChildClicked(childIndex, false);
                } else {
                    transferablePokemon.setTransfer(checked);
                    mTransferablePokemonList.set(mTransferablePokemonList.indexOf(getTransferablePokemon(childTransferablePokemon.getId())), transferablePokemon);
                    group.onChildClicked(childIndex, true);
                }
                if (countTransferablePokemons() > 0) {
                    setActionBarTitle(String.valueOf(countTransferablePokemons()) + " selected");
                    showToast(String.valueOf(countTransferablePokemons()) + " pokemons to transfer", 700);
                    setHasOptionsMenu(true);
                } else {
                    setActionBarTitle(String.valueOf(totalPokemons) + "/" + String.valueOf(pokemonStorage) + " pokemons");
                    setHasOptionsMenu(false);
                }

            } else {
                showToast("No puedes transferir un pokémon favorito", 8500);
                group.onChildClicked(childIndex, false);
            }
        } else {
            showToast("Ahora no puedes transferir este pokémon, intentalo más tarde", Toast.LENGTH_SHORT);
            group.onChildClicked(childIndex, false);
        }

    }

    public class GetPokemonsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "GET_POKEMON_TASK: onPreExecute");

            //Show the progressBar
            mListener.showProgress(true);

            mUserPokemonList = new ArrayList<>();
            mFiltrosPokemonList = new ArrayList<>();
            mTransferablePokemonList = new ArrayList<>();


            super.onPreExecute();
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: start");

            try {
                try {
                    mPokemonGo.getInventories().updateInventories(true);
                    mUserPokemonList = mPokemonGo.getInventories().getPokebank().getPokemons();
                    totalPokemons = mUserPokemonList.size() + mPokemonGo.getInventories().getHatchery().getEggs().size();
                    pokemonStorage = mPokemonGo.getPlayerProfile().getPlayerData().getMaxPokemonStorage();

                    for (Pokemon pokemon : mUserPokemonList) {

                        if (!isCancelled()) {

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
                                    mTransferablePokemonList.add(transferablePokemon);

                                    String specificPokemonNick = specificPokemon.getNickname();

                                    if (specificPokemonNick.equals("")) {
                                        specificPokemonNick = specificPokemon.getPokemonId().toString();
                                    }

                                    mChildTransferablePokemonList.add(new
                                            ChildTransferablePokemon(specificPokemon.getId(),
                                            transferablePokemon.getFavorite(),
                                            transferablePokemon.getCp(),
                                            ((int) (specificPokemon.getIvRatio() * 100)),
                                            specificPokemonNick));

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

                                mFiltrosPokemonList.add(new GroupTransferablePokemon(pokemonId, pokemonIdNumber, childCount, pokemon.getCandy(), pokemonId, mChildTransferablePokemonList));

                                // Sorting
                                Collections.sort(mFiltrosPokemonList, new Comparator<GroupTransferablePokemon>() {

                                    @Override
                                    public int compare(GroupTransferablePokemon filtro1, GroupTransferablePokemon filtro2) {

                                        return filtro1.getPokemonId().compareTo(filtro2.getPokemonId());
                                    }
                                });
                            }
                        } else {
                            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }
                    }

                    Log.i(TAG, "GET_POKEMON_TASK: doInBackground: true");
                    return true;

                } catch (LoginFailedException | RemoteServerException e) {
                    Log.i(TAG, "GET_POKEMON_TASK: doInBackground: login or remote_server exception");
                    Log.i(TAG, e.toString());
                    return false;
                }

            } catch (Exception e) {
                Log.i(TAG, "GET_POKEMON_TASK: doInBackground: general exception");
                Log.i(TAG, e.toString());
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            Log.i(TAG, "GET_POKEMON_TASK: onPostExecute: " + succes.toString());

            mGetPokemonsTask = null;
            setActionBarTitle(String.valueOf(totalPokemons) + "/" + String.valueOf(pokemonStorage) + " pokemons");

            //Show the progressBar
            mListener.showProgress(false);

            if (succes) {
                //instantiate your adapter with the list of bands
                mAdpaterChildTransferablePokemon = new AdapterChildTransferablePokemon(mFiltrosPokemonList, mContext);
                mAdpaterChildTransferablePokemon.setChildClickListener(FragmentTransfer.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdpaterChildTransferablePokemon);

            } else {
                if (isDeviceOnline()) {
                    setActionBarTitle(getString(R.string.snack_bar_error_with_pokemon));
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_FILTROS);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_FILTROS);
                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "GET_POKEMON_TASK: onCancelled");
            mGetPokemonsTask = null;
        }

    }

    public class TransferPokemonsTask extends AsyncTask<Void, ProgressTransferPokemon, Boolean> {

        private Boolean showMinMax;
        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        //Object sended to onProgressUpdate method
        private ProgressTransferPokemon progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onPreExecute");

            showMinMax = true;
            progress = new ProgressTransferPokemon();
            builder = new MaterialDialog.Builder(mContext)
                    .title(getString(R.string.dialog_title_transfer_pokemons))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(false, countTransferablePokemons(), showMinMax)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            if (mTransferPokemonsTask!=null){
                                mTransferPokemonsTask.cancel(true);
                            }
                        }
                    });
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground");

            try {
                try {

                    for (TransferablePokemon transferablePokemon : mTransferablePokemonList) {

                        if (!isCancelled()) {

                            if (transferablePokemon.getTransfer()) {
                                Pokemon pokemonToTransfer = getUserPokemon(transferablePokemon.getId());

                                if (pokemonToTransfer != null) {
                                    progress.setProgressMessage(pokemonToTransfer.getPokemonId().toString());
                                    progress.setUpdateProgress(false);
                                    publishProgress(progress);
                                    //pokemonToTransfer.debug();
                                    ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                                    progress.setProgressMessage(result.toString());
                                    progress.setUpdateProgress(true);
                                    publishProgress(progress);
                                    sleep(1000);
                                    mPokemonGo.getInventories().updateInventories(true);
                                }
                            }
                        } else {
                            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }

                    }

                    Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: true");
                    return true;


                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: exception");
                return false;

            }


        }

        @Override
        protected void onProgressUpdate(ProgressTransferPokemon... data) {

            super.onProgressUpdate(data);
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onProgressUpdate: " + data[0]);

            // Increment the dialog's progress by 1 after sleeping for 50ms
            dialog.setContent(data[0].getProgressMessage());

            if (data[0].getUpdateProgress()) {
                dialog.incrementProgress(1);
            }


        }

        @Override
        protected void onPostExecute(Boolean succes) {

            Log.i(TAG, "TRANSFER_POKEMON_TASK: onProgressUpdate: " + succes.toString());
            mTransferPokemonsTask = null;

            //Hiding the menu
            setHasOptionsMenu(false);

            //Dismissing the dialog
            dialog.dismiss();

            if (succes) {

                if (mGetPokemonsTask == null) {
                    mGetPokemonsTask = new GetPokemonsTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mGetPokemonsTask.execute();
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
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onCancelled");
            mTransferPokemonsTask = null;
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

        for (TransferablePokemon transferablePokemon : mTransferablePokemonList) {
            if (String.valueOf(transferablePokemon.getId()).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return transferablePokemon;
            }
        }
        return null;
    }

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : mUserPokemonList) {
            if (String.valueOf(pokemon.getId()).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
    }

    public int countTransferablePokemons() {
        int size = 0;

        for (TransferablePokemon transferablePokemon : mTransferablePokemonList) {
            if (transferablePokemon.getTransfer()) {
                size++;
            }
        }

        return size;
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
                                mGetPokemonsTask = new GetPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mGetPokemonsTask.execute();
                                }

                            } else if (task == TASK_TRANSFER) {

                                mTransferPokemonsTask = new TransferPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferPokemonsTask.execute();
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
