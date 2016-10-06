package com.javic.pokewhere.fragments;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterChildItem;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.ChildItem;
import com.javic.pokewhere.models.GroupItem;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class FragmentBag extends Fragment {

    private static final String TAG = FragmentBag.class.getSimpleName();
    private static final int TASK_ITEMS = 0;
    private static final int TASK_DELETE = 1;

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

    //Tasks
    private GetItemsTask mGetItemsTask;
    private DeleteItemsTask mDeleteItemsTask;

    //Adapters
    private AdapterChildItem mAdapterChildItem;

    //Listas
    private List<Item> mUserBagItemList;
    private List<GroupItem> mGroupItemList = new ArrayList<>();

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
            mGetItemsTask.cancel(true);
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


    public class GetItemsTask extends AsyncTask<Void, String, Boolean> {


        @Override
        protected void onPreExecute() {
            mGroupItemList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                mPokemonGo.getInventories().updateInventories(true);
                mUserBagItemList = new ArrayList<>(mPokemonGo.getInventories().getItemBag().getItems());

                int itemCount =  mPokemonGo.getInventories().getItemBag().getItemsCount();
                int itemStorage = mPokemonGo.getPlayerProfile().getPlayerData().getMaxItemStorage();

                publishProgress(String.valueOf(itemCount) + "/" +String.valueOf(itemStorage) + " Items");

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

                        //POKEBALLS
                        if (item.getItemId().toString().contains("BALL")) {
                            title_group = getString(R.string.group_pokeballs);

                            if (!containsEncounteredGroupItem(title_group)) {

                                String[] full_name = item.getItemId().toString().split("_");
                                String ball_type = full_name[1];

                                if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                                    if (ball_type.equalsIgnoreCase("POKE")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (ball_type.equalsIgnoreCase("GREAT")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (ball_type.equalsIgnoreCase("ULTRA")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
                                    }

                                }

                                mGroupItemList.add(new GroupItem(title_group, pokeBallTypeList, R.drawable.ic_pokeballs));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String ball_type = full_name[1];

                                if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                                    if (ball_type.equalsIgnoreCase("POKE")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (ball_type.equalsIgnoreCase("GREAT")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (ball_type.equalsIgnoreCase("ULTRA")){
                                        pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2] + ":" + item.getCount()));
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
                                    if (incense_type.equalsIgnoreCase("ORDINARY")){
                                        incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type  + " " + full_name[1] + ":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, incenseTypeList, R.drawable.ic_incenses));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String incense_type = full_name[2];

                                if (!containsEncounteredChildItem(incense_type, incenseTypeList)) {
                                    if (incense_type.equalsIgnoreCase("ORDINARY")){
                                        incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type  + " " + full_name[1] + ":" + item.getCount()));
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
                                    if (potion_type.equalsIgnoreCase("POTION")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("SUPER")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type  + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("HYPER")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type  + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("MAX")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type  + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, potionTypeList, R.drawable.ic_potions));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String potion_type = full_name[1];

                                if (!containsEncounteredChildItem(potion_type, potionTypeList)) {
                                    if (potion_type.equalsIgnoreCase("POTION")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("SUPER")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type  + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("HYPER")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type  + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                    else if (potion_type.equalsIgnoreCase("MAX")){
                                        potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type  + " " + full_name[2] + ":" + item.getCount()));
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
                                    if (disk_type.equalsIgnoreCase("TROY")){
                                        troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + " " + full_name[1] + ":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, troyDiskTypeList, R.drawable.ic_baits));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String disk_type = full_name[1];

                                if (!containsEncounteredChildItem(disk_type, troyDiskTypeList)) {
                                    if (disk_type.equalsIgnoreCase("TROY")){
                                        troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + ":" + item.getCount()));
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
                                    if (egg_type.equalsIgnoreCase("LUCKY")){
                                        eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2] + ":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, eggTypeList, R.drawable.ic_eggs));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String egg_type = full_name[1];

                                if (!containsEncounteredChildItem(egg_type, eggTypeList)) {
                                    if (egg_type.equalsIgnoreCase("LUCKY")){
                                        eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2] + ":" + item.getCount()));
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

                                    if (incubator_type.equalsIgnoreCase("BASIC")){
                                        try{
                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, incubator_type+ " " +full_name[3]+ " "+full_name[1]+":" + item.getCount()));
                                        }
                                        catch (Exception e){

                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type+ " "+full_name[1]+":" + item.getCount()));
                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, "UNLIMITED"+ " " + full_name[1]+":" + 1));
                                        }

                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, incubatorTypeList, R.drawable.ic_incubators));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String incubator_type = full_name[1];

                                if (!containsEncounteredChildItem(incubator_type, incubatorTypeList)) {
                                    if (incubator_type.equalsIgnoreCase("BASIC")){
                                        try{
                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, incubator_type+ " " +full_name[3]+ " "+full_name[1]+":" + item.getCount()));
                                        }
                                        catch (Exception e){

                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type+ " "+full_name[1]+":" + item.getCount()));
                                            incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, "UNLIMITED"+ " " + full_name[1]+":" + 1));
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
                                    if (revive_type.equalsIgnoreCase("REVIVE")){
                                        reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + ":" + item.getCount()));
                                    }
                                    else if(revive_type.equalsIgnoreCase("MAX")){
                                        reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2] +":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, reviveTypeList, R.drawable.ic_crystal));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String revive_type = full_name[1];

                                if (!containsEncounteredChildItem(revive_type, reviveTypeList)) {
                                    if (revive_type.equalsIgnoreCase("REVIVE")){
                                        reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + ":" + item.getCount()));
                                    }
                                    else if(revive_type.equalsIgnoreCase("MAX")){
                                        reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2] +":" + item.getCount()));
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
                                    if (berry_type.equalsIgnoreCase("RAZZ")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("BLUK")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("NANAB")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("PINAP")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("WEPAR")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                }

                                mGroupItemList.add(new GroupItem(title_group, berryTypeList, R.drawable.ic_berries));

                            } else {
                                String[] full_name = item.getItemId().toString().split("_");
                                String berry_type = full_name[1];

                                if (!containsEncounteredChildItem(berry_type, berryTypeList)) {
                                    if (berry_type.equalsIgnoreCase("RAZZ")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("BLUK")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("NANAB")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("PINAP")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                    else if (berry_type.equalsIgnoreCase("WEPAR")){
                                        berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2] +":" + item.getCount()));
                                    }
                                }
                                mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_berries), berryTypeList, R.drawable.ic_berries));
                            }
                        }

                    }

                }
                return true;

            } catch (Exception e) {
                Log.i(TAG, e.toString());

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

            mGetItemsTask = null;

            if (succes) {

                setHasOptionsMenu(true);

                //instantiate your adapter with the list of bands
                mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapterChildItem);

                mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_BAG);

            } else {
                setHasOptionsMenu(false);

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_ITEMS);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_ITEMS);
                }

                mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_TRANSFER);
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
            mGetItemsTask = null;
        }

    }

    public class DeleteItemsTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                //To update the list
                mListener.onFragmentCreatedViewStatus(true, Constants.FRAGMENT_BAG);

                return true;

            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

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

            mDeleteItemsTask = null;

            if (succes) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapterChildItem.notifyDataSetChanged();
                        mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_BAG);
                    }
                });
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
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

}
