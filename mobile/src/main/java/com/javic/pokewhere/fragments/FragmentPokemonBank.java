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
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.ActivityPokemonDetail;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.Constants;
import com.javic.pokewhere.util.PokemonComparator;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    //Adapter
    private AdapterPokemonBank mAdapter;

    //Variables
    private static int mUserPokeBankSpace = 0;

    public FragmentPokemonBank() {
        // Required empty public constructor
    }

    public static FragmentPokemonBank newInstance(List<LocalUserPokemon> localUserPokemonList, int userPokeBankSpace) {
        FragmentPokemonBank fragment = new FragmentPokemonBank();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mLocalUserPokemonList = localUserPokemonList;
        mUserPokeBankSpace = userPokeBankSpace;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_pokemon_bank, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + "/" + String.valueOf(mUserPokeBankSpace) + " " + getString(R.string.text_pokemones));

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
                orderList(tabId);
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                mGridLayoutManager.scrollToPositionWithOffset(0,0);
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
                        .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
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

                                    for (Integer indice : indices) {
                                        pokemonTotrasnferList.add(mLocalUserPokemonList.get(indice));
                                    }

                                    if (mListener != null) {
                                        mListener.onFragmentActionPerform(Constants.ACTION_TRANSFER_POKEMON, pokemonTotrasnferList);
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

                //Variables
                final LocalUserPokemon localUserPokemon = (LocalUserPokemon) childItem;

                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.ACTION_SET_FAVORITE_POKEMON, localUserPokemon);
                }

                break;
            case R.id.btnCompare:
                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.ACTION_VER_TODOS, childItem);
                }
                break;
        }
    }

    /*
    *
    * General Method's
    *
    */

    public boolean canFinish() {
        if (mAdapter.getSelectedCount() > 0) {
            mAdapter.clearSelected();
            return false;
        } else {
            return true;
        }
    }


    public void onTaskFinish(int task, Object object, Object objectList) {

        switch (task) {
            case Constants.ACTION_SET_FAVORITE_POKEMON:

                mLocalUserPokemonList = (List<LocalUserPokemon>) objectList;

                if (mBottomBar!=null){
                    orderList(mBottomBar.getCurrentTabId());
                }

                /*LocalUserPokemon local = getSpecificPokemon((LocalUserPokemon) object);

                AdapterPokemonBank.PokemonBankViewHolder
                        holder = (AdapterPokemonBank.PokemonBankViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mLocalUserPokemonList.indexOf(local));

                YoYo.with(Techniques.RotateIn)
                        .duration(800)
                        .playOn(holder.imgFavorite);

                if (local.getFavorite()) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmarked);
                } else {
                    holder.imgFavorite.setImageResource(R.drawable.ic_bookmark);
                }*/

                break;

            case Constants.ACTION_TRANSFER_POKEMON:
                mLocalUserPokemonList = (List<LocalUserPokemon>) objectList;
                mAdapter.clearSelected();
                mAdapter.upDateAdapter(mLocalUserPokemonList);

                if (mBottomBar!=null){
                    orderList(mBottomBar.getCurrentTabId());
                }

                mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + "/" + String.valueOf(mUserPokeBankSpace) + " " + getString(R.string.text_pokemones));

                break;
        }
    }

    private LocalUserPokemon getSpecificPokemon(LocalUserPokemon pokemon) {

        LocalUserPokemon localUserPokemon = null;

        for (LocalUserPokemon specificPokemon : mLocalUserPokemonList) {
            String specificId = String.valueOf(specificPokemon.getId());

            if (specificId.equals(String.valueOf(pokemon.getId()))) {
                localUserPokemon = specificPokemon;
            }
        }

        return localUserPokemon;
    }

    private void orderList(@IdRes int tabId){
        int valueToCompare =0;
        switch (tabId) {
            case R.id.tab_iv:
                valueToCompare = Constants.VALUE_IV;
                break;
            case R.id.tab_cp:
                valueToCompare = Constants.VALUE_CP;
                break;
            case R.id.tab_recents:
                valueToCompare = Constants.VALUE_RECENTS;
                break;
            case R.id.tab_name:
                valueToCompare = Constants.VALUE_NAME;
                break;
            case R.id.tab_number:
                valueToCompare = Constants.VALUE_NUMBER;
                break;
        }

        Collections.sort(mLocalUserPokemonList, new PokemonComparator(valueToCompare));
        mAdapter.upDateAdapter(mLocalUserPokemonList);
    }

}
