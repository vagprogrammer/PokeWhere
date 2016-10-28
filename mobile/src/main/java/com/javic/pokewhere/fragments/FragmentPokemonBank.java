package com.javic.pokewhere.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.PokemonCreationTimeComparator;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;

public class FragmentPokemonBank extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback{

    private static final String TAG = FragmentPokemonBank.class.getSimpleName();

    private static final int TASK_GETPOKEMON = 0;

    private OnFragmentCreatedViewListener mListener;

    //Context
    private Context mContext;

    //Fragment UI
    private View mView;
    private BottomBar mBottomBar;
    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Snackbar mSnackBar;
    private MaterialCab mCab;

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Listas
    private List<Pokemon> mUserPokemonList;
    private List<LocalUserPokemon> mLocalUserPokemonList;

    //Variables
    public int pokemonStorage;
    public int totalPokemons;

    //Tasks
    private GetPokemonsTask mGetPokemonsTask;

    //Adapter
    private AdapterPokemonBank mAdapter;

    public FragmentPokemonBank() {
        // Required empty public constructor
    }

    public static FragmentPokemonBank newInstance(PokemonGo pokemonGo) {
        FragmentPokemonBank fragment = new FragmentPokemonBank();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        mPokemonGo = pokemonGo;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_transfer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transferir:
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
        mView = inflater.inflate(R.layout.fragment_pokemon_bank, container, false);


        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);
        mCab = MaterialCab.restoreState(savedInstanceState, (AppCompatActivity) mContext, this);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), ActivityDashboard.mDrawerLayout, mToolbar, R.string.open_location_settings, R.string.open_location_settings);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        ActivityDashboard.mDrawerLayout.addDrawerListener(mDrawerToggle);

        mGridLayoutManager = new GridLayoutManager(mContext, 3);
        // Setup adapter and callbacks
        mLocalUserPokemonList = new ArrayList<>();
        mAdapter = new AdapterPokemonBank(mContext,this, mLocalUserPokemonList);
        // Receives selection updates, recommended to set before restoreInstanceState() so initial reselection is received
        mAdapter.setSelectionListener(this);

        mRecyclerView = (DragSelectRecyclerView) mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mBottomBar = (BottomBar) mView.findViewById(R.id.bottomBar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId){
                    case R.id.tab_iv:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {
                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {
                                return pokemon2.getIv() - pokemon1.getIv(); // Ascending
                            }
                        });
                        break;
                    case R.id.tab_cp:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {
                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {
                                return pokemon2.getCp() - pokemon1.getCp(); // Ascending
                            }
                        });
                        break;
                    case R.id.tab_recents:
                        Collections.sort(mLocalUserPokemonList, new PokemonCreationTimeComparator());
                        break;
                    case R.id.tab_name:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {

                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {

                                return pokemon1.getName().compareTo(pokemon2.getName());
                            }
                        });
                        break;
                    case R.id.tab_number:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {
                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {
                                return pokemon1.getNumber() - pokemon2.getNumber(); // Ascending
                            }
                        });
                        break;

                }

                mAdapter.upDateAdapter(mLocalUserPokemonList);
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo != null) {

            mListener.onFragmentCreatedViewStatus(true);
            mListener.showProgress(false);

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
    public void onClick(int index) {
        // Single click will select or deselect an item

        final int selectedCount = mAdapter.getSelectedCount();

        if (selectedCount>0){
            mAdapter.toggleSelected(index);
        }
        else{
            Toast.makeText(mContext, "Ver detalle", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLongClick(int index) {
        // Long click initializes drag selection, and selects the initial item
        mRecyclerView.setDragSelectActive(true, index);
    }

    @Override
    public void onDragSelectionChanged(int count) {

        if (count > 0) {
            if (mCab == null) {
                mCab = new MaterialCab((AppCompatActivity) mContext, R.id.cab_stub)
                        .setMenu(R.menu.cab)
                        .setCloseDrawableRes(android.R.drawable.ic_menu_close_clear_cancel)
                        .start(this);
            }
            mCab.setTitleRes(R.string.title_fragment_bag, count);
            mAdapter.changeSelectingState(true);
            mBottomBar.setEnabled(false);
        } else if (mCab != null && mCab.isActive()) {
            mCab.reset().finish();
            mCab = null;
            mAdapter.changeSelectingState(false);
            mBottomBar.setEnabled(true);
        }
    }

    // Material CAB Callbacks

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            StringBuilder sb = new StringBuilder();
            int traverse = 0;
            for (Integer index : mAdapter.getSelectedIndices()) {
                if (traverse > 0) sb.append(", ");
                sb.append(mAdapter.getItemId(index));
                traverse++;
            }
            Toast.makeText(mContext,
                    String.format("Selected letters (%d): %s", mAdapter.getSelectedCount(), sb.toString()),
                    Toast.LENGTH_LONG).show();
            mAdapter.clearSelected();
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mAdapter.clearSelected();
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDrawerToggle != null) {
            ActivityDashboard.mDrawerLayout.removeDrawerListener(mDrawerToggle);
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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public class GetPokemonsTask extends AsyncTask<Void, Void, Boolean> {

        private int specificPokemonCount = 0;

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "GET_POKEMON_TASK: onPreExecute");

            //Show the progressBar
            mListener.showProgress(true);

            mUserPokemonList = new ArrayList<>();
            mLocalUserPokemonList = new ArrayList<>();

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

                            LocalUserPokemon localUserPokemon = new LocalUserPokemon();
                            localUserPokemon.setId(pokemon.getId());
                            localUserPokemon.setPokemonId(pokemon.getPokemonId().toString());
                            localUserPokemon.setName(pokemon.getPokemonId().toString());
                            localUserPokemon.setNickname(pokemon.getNickname());
                            localUserPokemon.setBitmap(getBitmapFromAssets(pokemon.getPokemonId().getNumber()));
                            localUserPokemon.setNumber(pokemon.getPokemonId().getNumber());
                            localUserPokemon.setFavorite(pokemon.isFavorite());
                            localUserPokemon.setDead(pokemon.isInjured());
                            localUserPokemon.setCp(pokemon.getCp());
                            localUserPokemon.setIv(((int) (pokemon.getIvRatio() * 100)));
                            localUserPokemon.setAttack(pokemon.getIndividualAttack());
                            localUserPokemon.setDefense(pokemon.getIndividualDefense());
                            localUserPokemon.setStamina(pokemon.getIndividualStamina());
                            localUserPokemon.setMaxCp(pokemon.getMaxCpFullEvolveAndPowerupForPlayer());
                            localUserPokemon.setEvolveCP(pokemon.getCpAfterEvolve());
                            localUserPokemon.setLevel(pokemon.getLevel());
                            localUserPokemon.setCandies(pokemon.getCandy());
                            localUserPokemon.setPowerUpStardust(pokemon.getStardustCostsForPowerup());
                            localUserPokemon.setPoweUpCandies(pokemon.getCandyCostsForPowerup());
                            localUserPokemon.setEvolveCandies(pokemon.getCandiesToEvolve());
                            localUserPokemon.setCreationTime(createDate(pokemon.getCreationTimeMs()));

                            if (!containsEncounteredId(pokemon.getPokemonId())) {
                                specificPokemonCount = mPokemonGo.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId()).size();
                            }

                            mLocalUserPokemonList.add(localUserPokemon);
                        } else {
                            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }
                    }

                    // Sorting
                    Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {
                        @Override
                        public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {
                            return pokemon2.getIv() - pokemon1.getIv(); // Ascending
                        }
                    });

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

            if (succes) {
                setActionBarTitle(String.valueOf(totalPokemons) + "/" + String.valueOf(pokemonStorage) + " pokemons");
                mAdapter.upDateAdapter(mLocalUserPokemonList);
            } else {
                setActionBarTitle(getString(R.string.snack_bar_error_with_pokemon));

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_GETPOKEMON);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_GETPOKEMON);
                }
            }

            //Show the progressBar
            mListener.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "GET_POKEMON_TASK: onCancelled");
            mGetPokemonsTask = null;
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

    public void setActionBarTitle(final String message) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
    }


    public boolean containsEncounteredId(PokemonIdOuterClass.PokemonId enconunteredPokemonId) {

        //If the encontered id exist, return true, if it doesn't exist return false

        for (LocalUserPokemon localUserPokemon : mLocalUserPokemonList) {
            if (localUserPokemon.getId().equals(enconunteredPokemonId.toString())) {
                return true;
            }
        }

        return false;
    }

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {

                            if (task == TASK_GETPOKEMON) {
                                mGetPokemonsTask = new GetPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mGetPokemonsTask.execute();
                                }

                            }
                            /*else if (task == TASK_TRANSFER) {

                                mTransferPokemonsTask = new FragmentPokemon.TransferPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferPokemonsTask.execute();
                                }
                            }*/
                        } else {

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
                });

        mSnackBar.show();
    }

    public Calendar createDate(long timestamp) {

        //Fri Aug 26 19:54:06 CDT 2016
        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Log.i(TAG, date.toString());
        return calendar;

    }

    public Bitmap getBitmapFromAssets(int pokemonIdNumber){
        AssetManager assetManager = mContext.getAssets();

        Bitmap bitmap = null;

        try {
            InputStream is = null;

            if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" +pokemonIdNumber) + ".png");
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
}
