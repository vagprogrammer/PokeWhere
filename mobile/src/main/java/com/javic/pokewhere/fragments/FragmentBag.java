package com.javic.pokewhere.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.javic.pokewhere.adapters.AdapterChildTransferablePokemon;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.models.ChildItem;
import com.javic.pokewhere.models.GroupItem;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.javic.pokewhere.models.TransferablePokemon;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.pokemon.Pokemon;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;

public class FragmentBag extends Fragment  {

    private static final String TAG = FragmentBag.class.getSimpleName();

    private OnFragmentCreatedViewListener mListener;

    // API PokemonGO
    private static PokemonGo mPokemonGo;

    //Fragment UI
    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

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
        setHasOptionsMenu(true);
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
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.title_fragment_bag));

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), ActivityDashboard.mDrawerLayout, mToolbar, R.string.open_location_settings,  R.string.open_location_settings);
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
            //instantiate your adapter with the list of bands
            mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext);

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapterChildItem);

            if (mGetItemsTask==null){
                mGetItemsTask = new GetItemsTask();
                mGetItemsTask.execute();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDrawerToggle!=null){
            ActivityDashboard.mDrawerLayout.removeDrawerListener(mDrawerToggle);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGetItemsTask!=null){
            mGetItemsTask.cancel(true);
            mGetItemsTask = null;
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

    //

    public class GetItemsTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            mGroupItemList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
               mUserBagItemList = new ArrayList<>(mPokemonGo.getInventories().getItemBag().getItems());

                if (mUserBagItemList!=null){

                    String title_group ="";
                    final List<ChildItem> pokeBallTypeList = new ArrayList<>();
                    final List<ChildItem> incenseTypeList = new ArrayList<>();

                    for (Item item: mUserBagItemList){

                        if (item.getItemId().toString().contains("BALL")){
                            title_group = getString(R.string.group_pokeballs);

                            if (!containsEncounteredGroupItem(title_group)){

                                String[] full_name = item.getItemId().toString().split("_");
                                String ball_type = full_name[1];

                                if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)){
                                    pokeBallTypeList.add(new ChildItem(R.drawable.ic_pokeball, full_name[1]+" "+full_name[2] + ":" + item.getCount()));
                                }

                                mGroupItemList.add(new GroupItem(title_group, pokeBallTypeList, R.drawable.ic_pokeball));

                            }else{
                                String[] full_name = item.getItemId().toString().split("_");
                                String ball_type = full_name[1];

                                if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)){
                                    pokeBallTypeList.add(new ChildItem(R.drawable.ic_pokeball, full_name[1]+" "+full_name[2] + ":" + item.getCount()));
                                }

                                mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_pokeballs), pokeBallTypeList, R.drawable.ic_pokeball)   );
                            }
                        } else
                        if(item.getItemId().toString().contains("INCENSE")){
                            title_group = getString(R.string.group_incenses);

                            if (!containsEncounteredGroupItem(title_group)){

                                String[] full_name = item.getItemId().toString().split("_");
                                String incense_type = full_name[1];

                                if (!containsEncounteredChildItem(incense_type, pokeBallTypeList)){
                                    incenseTypeList.add(new ChildItem(R.drawable.ic_pokeball, full_name[1]+" "+full_name[2] + ":" + item.getCount()));
                                }

                                mGroupItemList.add(new GroupItem(title_group, incenseTypeList, R.drawable.ic_pokeball));

                            }else{
                                String[] full_name = item.getItemId().toString().split("_");
                                String incense_type = full_name[1];

                                if (!containsEncounteredChildItem(incense_type, pokeBallTypeList)){
                                    incenseTypeList.add(new ChildItem(R.drawable.ic_pokeball, full_name[1]+" "+full_name[2] + ":" + item.getCount()));
                                }

                                mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_incenses), incenseTypeList, R.drawable.ic_pokeball)   );
                            }
                        }


                    }

                }
                return true;

            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

                return false;
            }

        }

        @Override
        protected void onProgressUpdate(String... data) {

            super.onProgressUpdate(data);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mGetItemsTask = null;

            if (succes) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext);
                        mRecyclerView.setAdapter(mAdapterChildItem);
                        //mAdapterChildItem.notifyDataSetChanged();
                        mListener.onFragmentCreatedViewStatus(false, Constants.FRAGMENT_BAG);
                    }
                });
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "tarea cancelada");
            mGetItemsTask = null;
        }

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

            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(data[0]);

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


    public GroupItem getGroupItem(String titleGroup) {

        for (GroupItem groupItem: mGroupItemList) {
            if (String.valueOf(groupItem.getTitle()).equalsIgnoreCase(String.valueOf(titleGroup))) {
                return groupItem;
            }
        }
        return null;
    }
}
