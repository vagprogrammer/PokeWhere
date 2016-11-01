package com.javic.pokewhere.fragments;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.pokegoapi.api.PokemonGo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FragmentCompare extends Fragment implements AdapterPokemonBank.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback, OnViewItemClickListenner {

    private static final String TAG = FragmentCompare.class.getSimpleName();

    private static final int TASK_GETPOKEMON = 0;

    //Context
    private Context mContext;

    //Callback
    private OnFragmentListener mListener;

    //Fragment UI
    private View mView;
    private Toolbar mToolbar;
    private MaterialCab mCab;
    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private Snackbar mSnackBar;


    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Adapter
    private AdapterPokemonBank mAdapter;

    //Listas
    private static List<LocalUserPokemon> mLocalUserPokemonList;

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
        mView = inflater.inflate(R.layout.fragment_compare, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);
        mCab = MaterialCab.restoreState(savedInstanceState, (AppCompatActivity) mContext, this);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.valueOf(mLocalUserPokemonList.size()) + " " +mLocalUserPokemonList.get(0).getName());

        mGridLayoutManager = new GridLayoutManager(mContext, 3);

        mAdapter = new AdapterPokemonBank(mContext, this, this, mLocalUserPokemonList);
        // Receives selection updates, recommended to set before restoreInstanceState() so initial reselection is received
        mAdapter.setSelectionListener(this);

        mRecyclerView = (DragSelectRecyclerView) mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
               getActivity().onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo != null) {

            mListener.onFragmentCreatedViewStatus(true);
            mListener.showProgress(false);

        }
    }


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

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {

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
    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return false;
    }

    @Override
    public void onDragSelectionChanged(int count) {

    }

    @Override
    public void onClick(int index) {

    }

    @Override
    public void onLongClick(int index) {

    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        return false;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        return false;
    }

    @Override
    public void OnViewItemClick(Object childItem, View view) {

    }
}
