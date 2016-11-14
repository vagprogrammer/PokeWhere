package com.javic.pokewhere.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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


public class FragmentCompare extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, OnViewItemClickListenner {

    private static final String TAG = FragmentCompare.class.getSimpleName();

    //Context
    private Context mContext;

    //Callback
    private OnFragmentListener mListener;

    //Fragment UI
    private View mView;
    private BottomBar mBottomBar;
    private Toolbar mToolbar;
    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;

    //Adapter
    private AdapterPokemonBank mAdapter;

    //Listas
    private static List<LocalUserPokemon> mLocalUserPokemonList;

    //Variables
    private Menu menu;

    //Variables
    public static LocalUserPokemon localUserPokemon; //Used for ActivityDashboard

    public FragmentCompare() {
        // Required empty public constructor
    }

    public static FragmentCompare newInstance(List<LocalUserPokemon> mList) {
        FragmentCompare fragment = new FragmentCompare();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        mLocalUserPokemonList = mList;

        localUserPokemon = mLocalUserPokemonList.get(0);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        inflater.inflate(R.menu.fragment_transfer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (mAdapter.getSelectedCount() > 0) {
            menu.findItem(R.id.action_transferir).setVisible(true);
        } else {
            menu.findItem(R.id.action_transferir).setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        String conte = "Context: " + context.getClass().getSimpleName();
        mContext = context;

        Log.i(TAG, conte);

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
        mView = inflater.inflate(R.layout.fragment_compare, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());

        mGridLayoutManager = new GridLayoutManager(mContext, 3);

        mAdapter = new AdapterPokemonBank(mContext, this, this, mLocalUserPokemonList);
        mAdapter.setSelectionListener(this);

        mRecyclerView = (DragSelectRecyclerView) mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mBottomBar = (BottomBar) mView.findViewById(R.id.bottomBarCompare);
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
        mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_COMPARE);
        mListener.showProgress(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (canFinish()) {
                    getActivity().onBackPressed();
                }

                break;
            case R.id.action_transferir:
                List<LocalUserPokemon> pokemonTotrasnferList = new ArrayList<>();
                Integer indices[] = mAdapter.getSelectedIndices();

                for (Integer indice : indices) {
                    pokemonTotrasnferList.add(mLocalUserPokemonList.get(indice));
                }

                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.ACTION_TRANSFER_POKEMON, pokemonTotrasnferList);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDragSelectionChanged(int count) {

        if (count > 0) {

            mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            mToolbar.setTitle(String.valueOf(count) + " " + getString(R.string.title_selected));
            menu.findItem(R.id.action_transferir).setVisible(true);
            mAdapter.changeSelectingState(true);
            mBottomBar.setVisibility(View.GONE);
        } else {

            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            if (mLocalUserPokemonList.size() > 0) {
                mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());
            }
            menu.findItem(R.id.action_transferir).setVisible(false);
            mAdapter.changeSelectingState(false);
            mBottomBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(int index) {
        // Single click will select or deselect an item

        final int selectedCount = mAdapter.getSelectedCount();

        if (selectedCount > 0) {
            mAdapter.toggleSelected(index);
        } else {
            if (mListener != null) {
                List<Object> list = new ArrayList();
                list.add(mLocalUserPokemonList);
                list.add(index);
                mListener.onFragmentActionPerform(Constants.ACTION_GO_TO_DETAIL, list);
            }
        }
    }

    @Override
    public void onLongClick(int index) {
        // Long click initializes drag selection, and selects the initial item
        mRecyclerView.setDragSelectActive(true, index);
    }

    @Override
    public void OnViewItemClick(Object childItem, View view) {

        Log.i(TAG, "OnViewItemClick");

        switch (view.getId()) {
            case R.id.imgFavorite:

                LocalUserPokemon localUserPokemon = (LocalUserPokemon) childItem;

                if (mListener != null) {
                    mListener.onFragmentActionPerform(Constants.ACTION_SET_FAVORITE_POKEMON, localUserPokemon);
                }

                break;
        }
    }

    public Boolean canFinish() {
        if (mAdapter.getSelectedCount() > 0) {

            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());

            menu.findItem(R.id.action_transferir).setVisible(false);
            mAdapter.clearSelected();
            mBottomBar.setVisibility(View.VISIBLE);
            return false;
        } else {
            return true;
        }
    }

    public void onTaskFinish(int task, Object object, Object objectList) {

        switch (task) {
            case Constants.ACTION_SET_FAVORITE_POKEMON:

                mLocalUserPokemonList = (List<LocalUserPokemon>) objectList;

                if (mBottomBar != null) {
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

                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

                menu.findItem(R.id.action_transferir).setVisible(false);
                mAdapter.changeSelectingState(false);
                mAdapter.clearSelected();
                mAdapter.upDateAdapter(mLocalUserPokemonList);

                if (mBottomBar != null) {
                    orderList(mBottomBar.getCurrentTabId());
                }

                mBottomBar.setVisibility(View.VISIBLE);

                if (mLocalUserPokemonList.size() > 0) {
                    mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());
                } else {
                    Toast.makeText(mContext, getString(R.string.text_no_pokemon), Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }

                break;
            default:
                mLocalUserPokemonList = (List<LocalUserPokemon>) objectList;

                mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());

                if (mBottomBar!=null){
                    orderList(mBottomBar.getCurrentTabId());
                }

                break;

        }


    }

    private LocalUserPokemon getSpecificPokemon(String id) {

        LocalUserPokemon localUserPokemon = null;

        for (LocalUserPokemon specificPokemon : mLocalUserPokemonList) {
            String specificId = String.valueOf(specificPokemon.getId());

            if (specificId.equals(id)) {
                localUserPokemon = specificPokemon;
            }
        }

        return localUserPokemon;
    }


    private void orderList(@IdRes int tabId) {
        int valueToCompare =0;

        switch (tabId) {
            case R.id.tab_iv:
                valueToCompare = Constants.VALUE_IV;
                break;
            case R.id.tab_cp:
                valueToCompare = Constants.VALUE_CP;
                break;
            case R.id.tab_attack:
                valueToCompare = Constants.VALUE_ATACK;
                break;
            case R.id.tab_defense:
                valueToCompare = Constants.VALUE_DEFENSE;
                break;
            case R.id.tab_stamina:
                valueToCompare = Constants.VALUE_STAMINA;
                break;

        }

        Collections.sort(mLocalUserPokemonList, new PokemonComparator(valueToCompare));
        mAdapter.upDateAdapter(mLocalUserPokemonList);
    }
}
