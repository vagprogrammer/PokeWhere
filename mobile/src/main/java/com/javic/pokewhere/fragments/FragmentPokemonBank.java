package com.javic.pokewhere.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.ActivityPokemonDetail;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.ProgressTransferPokemon;
import com.javic.pokewhere.util.Constants;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;

import static android.app.Activity.RESULT_OK;

public class FragmentPokemonBank extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback, OnViewItemClickListenner {

    private static final String TAG = FragmentPokemonBank.class.getSimpleName();

    private static final int TASK_GETPOKEMON = 0;
    private static final int TASK_TRANSFER = 1;

    //Callback
    private OnFragmentListener mListener;

    //Context
    private Context mContext;

    //Fragment UI
    private View mView;
    private BottomBar mBottomBar;
    private Toolbar mToolbar;
    private MaterialCab mCab;
    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private Snackbar mSnackBar;
    private ActionBarDrawerToggle mDrawerToggle;


    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Listas
    private List<Pokemon> mUserPokemonList;
    private List<LocalUserPokemon> mLocalUserPokemonList = new ArrayList<>();
    private List<LocalUserPokemon> specificPokemonList;


    //Variables
    public int pokemonStorage;
    public int totalPokemons;
    public boolean favoriteTaskWasCanceled = false;

    //Tasks
    private GetPokemonsTask mGetPokemonsTask;
    private SetFavoriteTask mSetFavoriteTask;
    private TransferPokemonsTask mTransferPokemonsTask;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentListener) {
            mListener = (OnFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_pokemon_bank, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);

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
        mAdapter = new AdapterPokemonBank(mContext, this, this, mLocalUserPokemonList);
        mAdapter.setSelectionListener(this);

        mRecyclerView = (DragSelectRecyclerView) mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mBottomBar = (BottomBar) mView.findViewById(R.id.bottomBar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ACTIVITY_POKEMON_DETAIL && resultCode == RESULT_OK) {

            boolean isChanged = data.getExtras().getBoolean("resultado");

            if (isChanged) {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDrawerToggle != null) {
            ActivityDashboard.mDrawerLayout.removeDrawerListener(mDrawerToggle);
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (favoriteTaskWasCanceled) {
            favoriteTaskWasCanceled = false;

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSetFavoriteTask != null) {
            mSetFavoriteTask.cancel(true);
        }
    }


    // RecyclerView Callbacks

    @Override
    public void onClick(int index) {
        // Single click will select or deselect an item

        final int selectedCount = mAdapter.getSelectedCount();

        if (selectedCount > 0) {
            mAdapter.toggleSelected(index);
        } else {
            ActivityPokemonDetail.mLocalUserPokemonList = mLocalUserPokemonList;
            Intent i = new Intent(mContext, ActivityPokemonDetail.class);
            i.putExtra("index", index);
            startActivityForResult(i, Constants.REQUEST_CODE_ACTIVITY_POKEMON_DETAIL);
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
                        .setCloseDrawableRes(android.R.drawable.ic_delete)
                        .start(this);
            }

            mCab.setTitle(String.valueOf(count) + " " + getString(R.string.title_selected));
            mAdapter.changeSelectingState(true);
            mBottomBar.setVisibility(View.GONE);
        } else if (mCab != null && mCab.isActive()) {
            mCab.reset().finish();
            mCab = null;
            mAdapter.changeSelectingState(false);
            mBottomBar.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void OnViewItemClick(Object childItem, View view) {

        Log.i(TAG, "OnViewItemClick");

        switch (view.getId()) {
            case R.id.imgFavorite:
                if (mSetFavoriteTask == null) {

                    LocalUserPokemon localPokemon = (LocalUserPokemon) childItem;

                    mSetFavoriteTask = new SetFavoriteTask(getUserPokemon(localPokemon.getId()), localPokemon);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mSetFavoriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mSetFavoriteTask.execute();
                    }
                }
                break;
            case R.id.btnCompare:
                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.FRAGMENT_ACTION_VER_TODOS, getAllPokemonByName(((LocalUserPokemon) childItem).getName()));
                }
                break;
        }
    }

    // Material CAB Callbacks

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }
    @Override
    public boolean onCabItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.action_transferir) {

            if (mTransferPokemonsTask == null) {
                mTransferPokemonsTask = new TransferPokemonsTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mTransferPokemonsTask.execute();
                }
            }
        }
        return true;
    }
    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mAdapter.clearSelected();
        return true;
    }


    /*
    *
    * General Method's
    *
     */

    public boolean isDeviceOnline() {

        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isConnected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                isConnected = true;
            }
        } else {
            // not connected to the internet
            isConnected = false;
        }

        return isConnected;
    }

    public void setActionBarTitle(final String message) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
    }

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : mUserPokemonList) {
            Long id = pokemon.getId();

            if (String.valueOf(id).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
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
                            else if (task == TASK_TRANSFER) {

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

    public Bitmap getBitmapFromAssets(int pokemonIdNumber) {
        AssetManager assetManager = mContext.getAssets();

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

    public boolean canFinish() {
        if (mAdapter.getSelectedCount() > 0) {
            mAdapter.clearSelected();
            return false;
        } else {
            return true;
        }
    }

    private List<LocalUserPokemon> getAllPokemonByName(String name) {
        specificPokemonList = new ArrayList<>();

        for (LocalUserPokemon specificPokemon : mLocalUserPokemonList) {
            if (specificPokemon.getName().equals(name)) {
                specificPokemonList.add(specificPokemon);
            }
        }

        return specificPokemonList;
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    *TASK
    *
     */

    public class GetPokemonsTask extends AsyncTask<Void, Void, Boolean> {

        private int currentTabId;

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "GET_POKEMON_TASK: onPreExecute");

            //Show the progressBar
            mListener.showProgress(true);

            mUserPokemonList = new ArrayList<>();
            mLocalUserPokemonList = new ArrayList<>();

            currentTabId = mBottomBar.getCurrentTabId();

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
                            localUserPokemon.setCreationTimeMillis(pokemon.getCreationTimeMs());

                            mLocalUserPokemonList.add(localUserPokemon);
                        } else {
                            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }
                    }

                    switch (currentTabId) {
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
                setActionBarTitle(String.valueOf(totalPokemons) + "/" + String.valueOf(pokemonStorage) + " " + getString(R.string.text_pokemones));
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

    public class SetFavoriteTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "SET_FAVORITE_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(mContext)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        public SetFavoriteTask(Pokemon pokemon, LocalUserPokemon localUserPokemon) {
            this.pokemon = pokemon;
            this.localUserPokemon = localUserPokemon;
            Log.i(TAG, "SET_FAVORITE_TASK: constructor");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "SET_FAVORITE_TASK: doInBackground:start");
            try {
                try {
                    pokemon.setFavoritePokemon(!localUserPokemon.getFavorite());
                    mPokemonGo.getInventories().updateInventories(true);
                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: true");
                    return true;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "SET_FAVORITE_TASK: onPostExecute");
            mSetFavoriteTask = null;
            dialog.dismiss();

            if (succes) {
                AdapterPokemonBank.PokemonBankViewHolder
                        holder = (AdapterPokemonBank.PokemonBankViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mLocalUserPokemonList.indexOf(localUserPokemon));

                YoYo.with(Techniques.RotateIn)
                        .duration(800)
                        .playOn(holder.imgFavorite);

                if (!localUserPokemon.getFavorite()) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmarked);
                } else {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmark);
                }


                localUserPokemon.setFavorite(!localUserPokemon.getFavorite());
            } else {
                Toast.makeText(mContext, getString(R.string.snack_bar_error_with_pokemon), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "SET_FAVORITE_TASK: onCancelled");
            dialog.dismiss();
            mSetFavoriteTask = null;

            localUserPokemon.setFavorite(!localUserPokemon.getFavorite());
            favoriteTaskWasCanceled = true;
        }

    }

    public class TransferPokemonsTask extends AsyncTask<Void, ProgressTransferPokemon, Boolean> {

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        //Object sended to onProgressUpdate method
        private ProgressTransferPokemon progress;

        private List<LocalUserPokemon> mTransferablePokemonList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onPreExecute");

            progress = new ProgressTransferPokemon();

            builder = new MaterialDialog.Builder(mContext)
                    .title(getString(R.string.dialog_title_transfer_pokemons))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(false, mAdapter.getItemCount(), true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            if (mTransferPokemonsTask != null) {
                                mTransferPokemonsTask.cancel(true);
                            }
                        }
                    });
            dialog = builder.build();
            dialog.show();


            //Initialize the list of pokemon to transfer
            mTransferablePokemonList = new ArrayList<>();

            for (Integer index : mAdapter.getSelectedIndices()) {
                Log.i(TAG, mLocalUserPokemonList.get(index).getName());
                mTransferablePokemonList.add(mLocalUserPokemonList.get(index));
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground");

            try {
                try {

                    for (LocalUserPokemon transferablePokemon : mTransferablePokemonList) {

                        if (!isCancelled()) {

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
                                sleep(900);
                                mPokemonGo.getInventories().updateInventories(true);
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

                mAdapter.clearSelected();

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
            mAdapter.clearSelected();
        }
    }
}
