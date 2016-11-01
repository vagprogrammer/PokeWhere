package com.javic.pokewhere.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterChildItem;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.interfaces.OnViewItemClickListenner;
import com.javic.pokewhere.models.ChildItem;
import com.javic.pokewhere.models.GroupItem;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class FragmentBag extends Fragment implements OnViewItemClickListenner {

    private static final String TAG = FragmentBag.class.getSimpleName();

    private static final int TASK_ITEMS = 0;
    private static final int TASK_DELETE = 1;

    private OnFragmentListener mListener;

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

    //Tasks
    private GetItemsTask mGetItemsTask;
    private DeleteItemsTask mDeleteItemsTask;

    //Adapters
    private AdapterChildItem mAdapterChildItem;

    //Listas
    private List<Item> mUserBagItemList;
    private List<GroupItem> mGroupItemList = new ArrayList<>();


    //Variables
    private int mItemCount = 0;
    private int mItemStorage = 0;

    public FragmentBag() {
        // Required empty public constructor
    }


    public static FragmentBag newInstance(PokemonGo pokemonGo) {
        FragmentBag fragment = new FragmentBag();
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
        //inflater.inflate(R.menu.fragment_transfer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_:
                break;
            case R.id.action_refresh:
                mListener.onFragmentActionPerform(Constants.FRAGMENT_BAG);
                break;*/
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_bag, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_fragment_bag));

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

            mListener.onFragmentCreatedViewStatus(true);

            if (mGetItemsTask == null) {
                mGetItemsTask = new GetItemsTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mGetItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mGetItemsTask.execute();
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

        if (mGetItemsTask != null) {
            Log.i(TAG, "GET_ITEMS_TASK: cancel:true");
            mListener.showProgress(false);
            mGetItemsTask.cancel(true);
        }

        if (mDeleteItemsTask != null) {
            Log.i(TAG, "DELETE_ITEMS_TASK: cancel:true");
            mListener.showProgress(false);
            mDeleteItemsTask.cancel(true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentListener) {
            mListener = (OnFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void OnViewItemClick(Object childItem, View view) {
        startAction(childItem);
    }

    public class GetItemsTask extends AsyncTask<Void, String, Boolean> {


        @Override
        protected void onPreExecute() {

            Log.i(TAG, "GET_ITEMS_TASK: onPreExecute");

            //Show the progressBar
            mListener.showProgress(true);

            mGroupItemList = new ArrayList<>();

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "GET_ITEMS_TASK: doInBackground: start");

            try {
                try {
                    mPokemonGo.getInventories().updateInventories(true);
                    mUserBagItemList = new ArrayList<>(mPokemonGo.getInventories().getItemBag().getItems());
                    mItemCount = mPokemonGo.getInventories().getItemBag().getItemsCount();
                    mItemStorage = mPokemonGo.getPlayerProfile().getPlayerData().getMaxItemStorage();

                    //publishProgress(String.valueOf(mItemCount) + "/" +String.valueOf(mItemStorage) + " Items");

                    if (mUserBagItemList != null) {

                        String title_group = "";
                        final List<ChildItem> pokeBallTypeList = new ArrayList<>();
                        final List<ChildItem> incenseTypeList = new ArrayList<>();
                        final List<ChildItem> potionTypeList = new ArrayList<>();
                        final List<ChildItem> troyDiskTypeList = new ArrayList<>();
                        final List<ChildItem> eggTypeList = new ArrayList<>();
                        final List<ChildItem> reviveTypeList = new ArrayList<>();
                        final List<ChildItem> berryTypeList = new ArrayList<>();
                        final List<ChildItem> incubatorTypeList = new ArrayList<>();

                        for (Item item : mUserBagItemList) {

                            if (!isCancelled()) {

                                //POKEBALLS
                                if (item.getItemId().toString().contains("BALL")) {
                                    title_group = getString(R.string.group_pokeballs);

                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String ball_type = full_name[1];

                                        if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                                            if (ball_type.equalsIgnoreCase("POKE")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("GREAT")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("ULTRA")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("MASTER")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_master_ball, ball_type + " " + full_name[2], item.getCount()));
                                            }

                                        }

                                        mGroupItemList.add(new GroupItem(title_group, pokeBallTypeList, R.drawable.ic_pokeballs));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String ball_type = full_name[1];

                                        if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                                            if (ball_type.equalsIgnoreCase("POKE")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("GREAT")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("ULTRA")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2], item.getCount()));
                                            } else if (ball_type.equalsIgnoreCase("MASTER")) {
                                                pokeBallTypeList.add(new ChildItem(R.drawable.ic_master_ball, ball_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_pokeballs), pokeBallTypeList, R.drawable.ic_pokeballs));
                                    }
                                }

                                //INCENSES
                                else if (item.getItemId().toString().contains("INCENSE")) {
                                    title_group = getString(R.string.group_incenses);

                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String incense_type = full_name[2];

                                        if (!containsEncounteredChildItem(incense_type, incenseTypeList)) {

                                            if (incense_type.equalsIgnoreCase("ORDINARY")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("COOL")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("FLORAL")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("SPICY")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, incenseTypeList, R.drawable.ic_incenses));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String incense_type = full_name[2];

                                        if (!containsEncounteredChildItem(incense_type, incenseTypeList)) {
                                            if (incense_type.equalsIgnoreCase("ORDINARY")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("COOL")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("FLORAL")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            } else if (incense_type.equalsIgnoreCase("SPICY")) {
                                                incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_incenses), incenseTypeList, R.drawable.ic_incenses));
                                    }
                                }

                                //POTIONS
                                else if (item.getItemId().toString().contains("POTION")) {
                                    title_group = getString(R.string.group_potions);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String potion_type = full_name[1];

                                        if (!containsEncounteredChildItem(potion_type, potionTypeList)) {
                                            if (potion_type.equalsIgnoreCase("POTION")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type, item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("SUPER")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type + " " + full_name[2], item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("HYPER")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type + " " + full_name[2], item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("MAX")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, potionTypeList, R.drawable.ic_potions));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String potion_type = full_name[1];

                                        if (!containsEncounteredChildItem(potion_type, potionTypeList)) {
                                            if (potion_type.equalsIgnoreCase("POTION")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type, item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("SUPER")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type + " " + full_name[2], item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("HYPER")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type + " " + full_name[2], item.getCount()));
                                            } else if (potion_type.equalsIgnoreCase("MAX")) {
                                                potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_potions), potionTypeList, R.drawable.ic_potions));
                                    }
                                }

                                //BAITS
                                else if (item.getItemId().toString().contains("DISK")) {
                                    title_group = getString(R.string.group_troydisks);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String disk_type = full_name[1];

                                        if (!containsEncounteredChildItem(disk_type, troyDiskTypeList)) {
                                            if (disk_type.equalsIgnoreCase("TROY")) {
                                                troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, troyDiskTypeList, R.drawable.ic_baits));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String disk_type = full_name[1];

                                        if (!containsEncounteredChildItem(disk_type, troyDiskTypeList)) {
                                            if (disk_type.equalsIgnoreCase("TROY")) {
                                                troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + " " + full_name[2], item.getCount()));
                                            }
                                        }
                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_troydisks), troyDiskTypeList, R.drawable.ic_baits));
                                    }
                                }

                                //EGGS
                                else if (item.getItemId().toString().contains("EGG")) {
                                    title_group = getString(R.string.group_eggs);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String egg_type = full_name[1];

                                        if (!containsEncounteredChildItem(egg_type, eggTypeList)) {
                                            if (egg_type.equalsIgnoreCase("LUCKY")) {
                                                eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, eggTypeList, R.drawable.ic_eggs));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String egg_type = full_name[1];

                                        if (!containsEncounteredChildItem(egg_type, eggTypeList)) {
                                            if (egg_type.equalsIgnoreCase("LUCKY")) {
                                                eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2], item.getCount()));
                                            }
                                        }
                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_eggs), eggTypeList, R.drawable.ic_eggs));
                                    }
                                }
                                //INCUBATORS
                                else if (item.getItemId().toString().contains("INCUBATOR")) {
                                    title_group = getString(R.string.group_incubators);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String incubator_type = full_name[2];

                                        if (!containsEncounteredChildItem(incubator_type, incubatorTypeList)) {

                                            if (incubator_type.equalsIgnoreCase("BASIC")) {
                                                try {
                                                    incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, full_name[3] + " " + full_name[1], item.getCount()));
                                                } catch (Exception e) {
                                                    incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type + " " + full_name[1], item.getCount()));
                                                }

                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, incubatorTypeList, R.drawable.ic_incubators));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String incubator_type = full_name[1];

                                        if (!containsEncounteredChildItem(incubator_type, incubatorTypeList)) {
                                            if (incubator_type.equalsIgnoreCase("BASIC")) {
                                                try {
                                                    incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, incubator_type + " " + full_name[3] + " " + full_name[1], item.getCount()));
                                                } catch (Exception e) {

                                                    incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type + " " + full_name[1], item.getCount()));
                                                    incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, "UNLIMITED" + " " + full_name[1], 1));
                                                }

                                            }
                                        }
                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_incubators), incubatorTypeList, R.drawable.ic_incubators));
                                    }
                                }


                                //REVIVES
                                else if (item.getItemId().toString().contains("REVIVE")) {
                                    title_group = getString(R.string.group_revives);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String revive_type = full_name[1];

                                        if (!containsEncounteredChildItem(revive_type, reviveTypeList)) {
                                            if (revive_type.equalsIgnoreCase("REVIVE")) {
                                                reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type, item.getCount()));
                                            } else if (revive_type.equalsIgnoreCase("MAX")) {
                                                reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, reviveTypeList, R.drawable.ic_crystal));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String revive_type = full_name[1];

                                        if (!containsEncounteredChildItem(revive_type, reviveTypeList)) {
                                            if (revive_type.equalsIgnoreCase("REVIVE")) {
                                                reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type, item.getCount()));
                                            } else if (revive_type.equalsIgnoreCase("MAX")) {
                                                reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2], item.getCount()));
                                            }
                                        }
                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_revives), reviveTypeList, R.drawable.ic_crystal));
                                    }
                                }

                                //BERRIES
                                else if (item.getItemId().toString().contains("BERRY")) {
                                    title_group = getString(R.string.group_berries);
                                    if (!containsEncounteredGroupItem(title_group)) {

                                        String[] full_name = item.getItemId().toString().split("_");
                                        String berry_type = full_name[1];

                                        if (!containsEncounteredChildItem(berry_type, berryTypeList)) {
                                            if (berry_type.equalsIgnoreCase("RAZZ")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("BLUK")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("NANAB")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("PINAP")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("WEPAR")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            }
                                        }

                                        mGroupItemList.add(new GroupItem(title_group, berryTypeList, R.drawable.ic_berries));

                                    } else {
                                        String[] full_name = item.getItemId().toString().split("_");
                                        String berry_type = full_name[1];

                                        if (!containsEncounteredChildItem(berry_type, berryTypeList)) {
                                            if (berry_type.equalsIgnoreCase("RAZZ")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("BLUK")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("NANAB")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("PINAP")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            } else if (berry_type.equalsIgnoreCase("WEPAR")) {
                                                berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                                            }
                                        }
                                        mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_berries), berryTypeList, R.drawable.ic_berries));
                                    }
                                }
                            }
                            //TASK is Cancelled
                            else {
                                Log.i(TAG, "GET_ITEMS_TASK: doInBackground: task is cancelled");
                                return false;
                            }

                        }//Termina el for

                        // Sorting
                        Collections.sort(mGroupItemList, new Comparator<GroupItem>() {

                            @Override
                            public int compare(GroupItem groupItem1, GroupItem groupItem2) {

                                return groupItem1.getTitle().compareTo(groupItem2.getTitle());
                            }
                        });

                        Log.i(TAG, "GET_ITEMS_TASK: doInBackground: true");
                        return true;
                    }
                    //mUserBagItemList == null;
                    else {
                        Log.i(TAG, "GET_ITEMS_TASK: doInBackground: false");
                        return false;
                    }
                } catch (LoginFailedException | RemoteServerException e) {
                    Log.i(TAG, "GET_ITEMS_TASK: doInBackground: login or remote_server exception");
                    Log.i(TAG, e.toString());
                    return false;
                }

            } catch (Exception e) {
                Log.i(TAG, "GET_ITEMS_TASK: doInBackground: general exception");
                Log.i(TAG, e.toString());
                return false;

            }

        }

        @Override
        protected void onProgressUpdate(String... data) {
            super.onProgressUpdate(data);
            Log.i(TAG, "GET_ITEMS_TASK: onProgressUpdate");
            //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.valueOf(data[0]);
        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "GET_ITEMS_TASK: onPostExecute:" + succes.toString());

            mGetItemsTask = null;

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.valueOf(mItemCount) + "/" + String.valueOf(mItemStorage) + " Items");

            mListener.showProgress(false);

            if (succes) {

                setHasOptionsMenu(true);

                //instantiate your adapter with the list of bands
                mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext, FragmentBag.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapterChildItem);


            } else {
                setHasOptionsMenu(false);

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_ITEMS);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_ITEMS);
                }

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "GET_ITEMS_TASK: onCancelled:");
            mGetItemsTask = null;
        }

    }

    public class DeleteItemsTask extends AsyncTask<Void, String, Boolean> {

        private ItemIdOuterClass.ItemId itemId;
        private int itemsToDelete;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "DELETE_ITEMS_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(mContext)
                    .title(getString(R.string.dialog_title_delete_items))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            if (mDeleteItemsTask != null) {
                                mDeleteItemsTask.cancel(true);
                            }
                        }
                    });
            dialog = builder.build();
            dialog.show();
        }

        public DeleteItemsTask(ItemIdOuterClass.ItemId itemId, int itemsToDelete) {
            this.itemId = itemId;
            this.itemsToDelete = itemsToDelete;

            Log.i(TAG, "DELETE_ITEMS_TASK: constructor");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground:start");
            try {
                try {
                    if (!isCancelled()) {
                        //publishProgress("Borrando Items...");
                        mPokemonGo.getInventories().getItemBag().removeItem(itemId, itemsToDelete);
                        Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: true");
                        return true;
                    } else {
                        Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: task is cancelled");
                        return false;
                    }


                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: exception");
                return false;

            }

        }

        /*@Override
        protected void onProgressUpdate(String... data) {

            super.onProgressUpdate(data);

            Log.i(TAG, "DELETE_ITEMS_TASK: onProgressUpdate: " + data[0]);

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(data[0]);

        }*/

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "DELETE_ITEMS_TASK: onPostExecute");
            mDeleteItemsTask = null;
            dialog.dismiss();

            if (succes) {

                if (mGetItemsTask == null) {
                    mGetItemsTask = new GetItemsTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mGetItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mGetItemsTask.execute();
                    }
                }

            } else {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.snack_bar_error_with_pokemon));
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "DELETE_ITEMS_TASK: onCancelled");
            dialog.dismiss();
            mDeleteItemsTask = null;
        }

    }

    public GroupItem getGroupItem(String titleGroup) {

        for (GroupItem groupItem : mGroupItemList) {
            if (String.valueOf(groupItem.getTitle()).equalsIgnoreCase(String.valueOf(titleGroup))) {
                return groupItem;
            }
        }
        return null;
    }

    public boolean containsEncounteredGroupItem(String enconunteredItemId) {

        for (GroupItem groupItem : mGroupItemList) {
            if (groupItem.getTitle().equalsIgnoreCase(enconunteredItemId)) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }

    public boolean containsEncounteredChildItem(String enconunteredItemId, List<ChildItem> specificItemList) {

        for (ChildItem childItem : specificItemList) {
            if (childItem.getTitle().equalsIgnoreCase(enconunteredItemId)) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
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
        }
        else {
            // not connected to the internet
            isConnected = false;
        }

        return isConnected;
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {

                            if (task == TASK_ITEMS) {
                                mGetItemsTask = new GetItemsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mGetItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mGetItemsTask.execute();
                                }
                            } else if (task == TASK_DELETE) {

                            }

                        } else {

                            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
                });

        mSnackBar.show();

    }

    public void startAction(Object childItem) {

        final ChildItem mChildItem = (ChildItem) childItem;

        final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(mContext)
                .minValue(1)
                .maxValue(mChildItem.getItemCount())
                .defaultValue(1)
                .backgroundColor(Color.WHITE)
                .separatorColor(Color.TRANSPARENT)
                .textColor(Color.BLACK)
                .textSize(20)
                .enableFocusability(false)
                .wrapSelectorWheel(true)
                .build();



        new MaterialDialog.Builder(mContext)
                .title(getString(R.string.dialog_title_select_items_pre) + " " + mChildItem.getTitle() + " " + getString(R.string.dialog_title_select_items_post) + " " + getString(R.string.dialog_title_select_items_total) + " " + String.valueOf(mChildItem.getItemCount()))
                .customView(numberPicker, false)
                .positiveText(getString(R.string.dialog_positive_btn_items))
                .negativeText(getString(R.string.location_alert_neg_btn))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mDeleteItemsTask == null) {
                            mDeleteItemsTask = new DeleteItemsTask(getItemId(mChildItem), numberPicker.getValue());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                mDeleteItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                mDeleteItemsTask.execute();
                            }
                        }
                    }
                }).show();
    }

    public ItemIdOuterClass.ItemId getItemId(ChildItem mChildItem) {
        ItemIdOuterClass.ItemId item = null;

        if (mChildItem.getTitle().contains("BALL")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("POKE")) {
                item = ItemIdOuterClass.ItemId.ITEM_POKE_BALL;
            } else if (item_type.equalsIgnoreCase("GREAT")) {
                item = ItemIdOuterClass.ItemId.ITEM_GREAT_BALL;
            } else if (item_type.equalsIgnoreCase("ULTRA")) {
                item = ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL;
            } else if (item_type.equalsIgnoreCase("MASTER")) {
                item = ItemIdOuterClass.ItemId.ITEM_MASTER_BALL;
            }
        } else if (mChildItem.getTitle().contains("POTION")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("POTION")) {
                item = ItemIdOuterClass.ItemId.ITEM_POTION;
            } else if (item_type.equalsIgnoreCase("SUPER")) {
                item = ItemIdOuterClass.ItemId.ITEM_SUPER_POTION;
            } else if (item_type.equalsIgnoreCase("HYPER")) {
                item = ItemIdOuterClass.ItemId.ITEM_HYPER_POTION;
            } else if (item_type.equalsIgnoreCase("MAX")) {
                item = ItemIdOuterClass.ItemId.ITEM_MAX_POTION;
            }
        } else if (mChildItem.getTitle().contains("INCENSE")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("ORDINARY")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY;
            } else if (item_type.equalsIgnoreCase("COOL")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCENSE_COOL;
            } else if (item_type.equalsIgnoreCase("FLORAL")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCENSE_FLORAL;
            } else if (item_type.equalsIgnoreCase("SPICY")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCENSE_SPICY;
            }
        } else if (mChildItem.getTitle().contains("BERRY")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("RAZZ")) {
                item = ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY;
            } else if (item_type.equalsIgnoreCase("BLUK")) {
                item = ItemIdOuterClass.ItemId.ITEM_BLUK_BERRY;
            } else if (item_type.equalsIgnoreCase("NANAB")) {
                item = ItemIdOuterClass.ItemId.ITEM_NANAB_BERRY;
            } else if (item_type.equalsIgnoreCase("PINAP")) {
                item = ItemIdOuterClass.ItemId.ITEM_PINAP_BERRY;
            } else if (item_type.equalsIgnoreCase("WEPAR")) {
                item = ItemIdOuterClass.ItemId.ITEM_WEPAR_BERRY;
            }
        } else if (mChildItem.getTitle().contains("DISK")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("TROY")) {
                item = ItemIdOuterClass.ItemId.ITEM_TROY_DISK;
            }
        } else if (mChildItem.getTitle().contains("REVIVE")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("REVIVE")) {
                item = ItemIdOuterClass.ItemId.ITEM_REVIVE;
            } else if (item_type.equalsIgnoreCase("MAX")) {
                item = ItemIdOuterClass.ItemId.ITEM_MAX_REVIVE;
            }
        } else if (mChildItem.getTitle().contains("INCUBATOR")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("BASIC")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC;
            } else if (item_type.equalsIgnoreCase("UNLIMITED")) {
                item = ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC_UNLIMITED;
            }
        } else if (mChildItem.getTitle().contains("EGG")) {
            String[] fullItemName = mChildItem.getTitle().split(" ");
            String item_type = fullItemName[0];

            if (item_type.equalsIgnoreCase("LUCKY")) {
                item = ItemIdOuterClass.ItemId.ITEM_LUCKY_EGG;
            }
        }


        return item;
    }

}
