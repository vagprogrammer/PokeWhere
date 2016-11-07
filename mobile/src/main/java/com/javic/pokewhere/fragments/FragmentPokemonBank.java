package com.javic.pokewhere.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
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

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.ActivityPokemonDetail;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.Constants;
import com.javic.pokewhere.util.PokemonCreationTimeComparator;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.javic.pokewhere.ActivityDashboard.TASK_SET_FAVORITE;
import static com.javic.pokewhere.util.Constants.ACTION_FRAGMENT_SET_FAVORITE_POKEMON;

public class FragmentPokemonBank extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, OnViewItemClickListenner {

    private static final String TAG = FragmentPokemonBank.class.getSimpleName();

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
    private ActionBarDrawerToggle mDrawerToggle;

    //Listas
    private static List<LocalUserPokemon> mLocalUserPokemonList;
    private List<LocalUserPokemon> specificPokemonList;


    //Adapter
    private AdapterPokemonBank mAdapter;

    public FragmentPokemonBank() {
        // Required empty public constructor
    }

    public static FragmentPokemonBank newInstance(List<LocalUserPokemon> localUserPokemonList) {
        FragmentPokemonBank fragment = new FragmentPokemonBank();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        mLocalUserPokemonList = localUserPokemonList;

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

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), ActivityDashboard.mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_POKEBANK);
        mListener.showProgress(false);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();

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
                        .start(new MaterialCab.Callback() {
                            @Override
                            public boolean onCabCreated(MaterialCab cab, Menu menu) {
                                return true;
                            }
                            @Override
                            public boolean onCabItemClicked(MenuItem item) {
                                if (item.getItemId() == R.id.action_transferir) {

                                    List<LocalUserPokemon> pokemonTotrasnferList = new ArrayList<>();
                                    Integer indices[] = mAdapter.getSelectedIndices();

                                    for (Integer indice: indices) {
                                        pokemonTotrasnferList.add(mLocalUserPokemonList.get(indice));
                                    }

                                    if (mListener != null) {
                                        mListener.onFragmentActionPerform(Constants.ACTION_FRAGMENT_TRANSFER_POKEMON, pokemonTotrasnferList);
                                    }
                                }
                                return true;
                            }
                            @Override
                            public boolean onCabFinished(MaterialCab cab) {
                                mAdapter.clearSelected();
                                return true;
                            }
                        });
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

                LocalUserPokemon localUserPokemon = (LocalUserPokemon) childItem;

                if (mListener != null) {
                    mListener.onFragmentActionPerform(ACTION_FRAGMENT_SET_FAVORITE_POKEMON, localUserPokemon);
                }

                break;
            case R.id.btnCompare:
                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.ACTION_FRAGMENT_VER_TODOS, getAllPokemonByName(((LocalUserPokemon) childItem).getName()));
                }
                break;
        }
    }

    /*
    *
    * General Method's
    *
    */
    public void setActionBarTitle(final String message) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
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

    public void onTaskFinish(int task, boolean cancelled, Object object){

        if (!cancelled){
            if (task == ACTION_FRAGMENT_SET_FAVORITE_POKEMON){
                AdapterPokemonBank.PokemonBankViewHolder
                        holder = (AdapterPokemonBank.PokemonBankViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mLocalUserPokemonList.indexOf((LocalUserPokemon)object));

                YoYo.with(Techniques.RotateIn)
                        .duration(800)
                        .playOn(holder.imgFavorite);

                if (!((LocalUserPokemon) object).getFavorite()) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmarked);
                } else {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmark);
                }

                ((LocalUserPokemon)object).setFavorite(!((LocalUserPokemon)object).getFavorite());
            }
        }
        else{
            if (task == TASK_SET_FAVORITE){
                ((LocalUserPokemon)object).setFavorite(!((LocalUserPokemon)object).getFavorite());
            }
        }

    }

}
