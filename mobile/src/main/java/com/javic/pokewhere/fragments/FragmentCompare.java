package com.javic.pokewhere.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.ActivityPokemonDetail;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FragmentCompare extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, OnViewItemClickListenner {

    private static final String TAG = FragmentCompare.class.getSimpleName();

    private static final int TASK_GETPOKEMON = 0;

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

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Adapter
    private AdapterPokemonBank mAdapter;

    //Listas
    private static List<LocalUserPokemon> mLocalUserPokemonList;

    //Variables
    public boolean favoriteTaskWasCanceled = false;
    private Menu menu;

    public FragmentCompare() {
        // Required empty public constructor
    }

    public static FragmentCompare newInstance(PokemonGo pokemonGo, List<LocalUserPokemon> mList) {
        FragmentCompare fragment = new FragmentCompare();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        mPokemonGo = pokemonGo;
        mLocalUserPokemonList = mList;

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
                    case R.id.tab_attack:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {

                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {

                                return pokemon2.getAttack() - pokemon1.getAttack();
                            }
                        });
                        break;
                    case R.id.tab_defense:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {

                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {

                                return pokemon2.getDefense() - pokemon1.getDefense();
                            }
                        });
                        break;
                    case R.id.tab_stamina:
                        // Sorting
                        Collections.sort(mLocalUserPokemonList, new Comparator<LocalUserPokemon>() {
                            @Override
                            public int compare(LocalUserPokemon pokemon1, LocalUserPokemon pokemon2) {
                                return pokemon2.getStamina() - pokemon1.getStamina(); // Ascending
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
            mToolbar.setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " + mLocalUserPokemonList.get(0).getName());
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

    public void onTaskFinish(int task, boolean cancelled, Object object){

        if (!cancelled){

            switch (task){
                case Constants.ACTION_SET_FAVORITE_POKEMON:
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
                    break;

                /*case Constants.ACTION_TRANSFER_POKEMON:
                    mUserPokemonList = (List<Pokemon>) object;
                    setUpData();
                    mAdapter.upDateAdapter(mLocalUserPokemonList);
                    break;*/

            }
        }
        else{
            switch (task) {
                case Constants.ACTION_SET_FAVORITE_POKEMON:
                    ((LocalUserPokemon)object).setFavorite(!((LocalUserPokemon)object).getFavorite());
                    break;
            }
        }
    }
}
