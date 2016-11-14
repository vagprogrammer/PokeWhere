package com.javic.pokewhere.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import com.javic.pokewhere.models.ItemToDelete;
import com.javic.pokewhere.util.Constants;

import java.util.List;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;


public class FragmentBag extends Fragment implements OnViewItemClickListenner {

    private static final String TAG = FragmentBag.class.getSimpleName();

    private OnFragmentListener mListener;

    //Fragment UI
    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    //Context
    private Context mContext;

    //Adapters
    private AdapterChildItem mAdapterChildItem;

    //Listas
    private static List<GroupItem> mGroupItemList;


    //Variables
    private static int mUserBagSpace = 0;

    public FragmentBag() {
        // Required empty public constructor
    }

    public static FragmentBag newInstance(List<GroupItem> groupItemList, int userBagSpace) {
        FragmentBag fragment = new FragmentBag();
        mGroupItemList = groupItemList;
        mUserBagSpace =  userBagSpace;
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
                    + " must implement OnFragmentListener");
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
        mView = inflater.inflate(R.layout.fragment_bag, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);

        mToolbar = (Toolbar) mView.findViewById(R.id.appbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.valueOf(getItemCount()) + "/" + String.valueOf(mUserBagSpace) + " Items");


        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), ActivityDashboard.mDrawerLayout, mToolbar, R.string.open_location_settings, R.string.open_location_settings);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        ActivityDashboard.mDrawerLayout.addDrawerListener(mDrawerToggle);

        mLayoutManager = new LinearLayoutManager(mContext);

        //instantiate your adapter with the list of bands
        mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext, FragmentBag.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapterChildItem);


        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_BAG);
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
    public void OnViewItemClick(Object childItem, View view) {
        startAction(childItem);
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

                        mListener.onFragmentActionPerform(Constants.ACTION_DELETE_ITEMS, new ItemToDelete(getItemId(mChildItem), numberPicker.getValue()));

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


    public void onTaskFinish(int task, Object object, Object objectList) {

        switch (task) {
            case Constants.ACTION_DELETE_ITEMS:

                mGroupItemList = (List<GroupItem>) objectList;

                //mAdapterChildItem.upDateAdapter(mGroupItemList);

                //instantiate your adapter with the list of bands
                mAdapterChildItem = new AdapterChildItem(mGroupItemList, mContext, FragmentBag.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapterChildItem);

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.valueOf(getItemCount()) + "/" + String.valueOf(mUserBagSpace) + " " + getString(R.string.title_items));

                break;
        }
    }


    public int getItemCount(){

        int itemCount =0;

        for (GroupItem group: mGroupItemList) {
            for (ChildItem item: group.getItems()) {
                itemCount = itemCount + item.getItemCount();
            }
        }

        return itemCount;
    }
}
