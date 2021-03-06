package com.javic.pokewhere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;

import com.javic.pokewhere.adapters.AdapterChildFiltro;
import com.javic.pokewhere.models.ChildFiltro;
import com.javic.pokewhere.models.GroupFiltro;
import com.javic.pokewhere.util.Constants;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class ActivityFiltros extends AppCompatActivity implements OnCheckChildClickListener {

    private static final String TAG = ActivityFiltros.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterChildFiltro mAdpaterFiltro;
    private CheckedTextView mCheckedFilterAll, mCheckedFilterBusqueda;
    private List<GroupFiltro> mGroupFiltros = new ArrayList<>();

    private Boolean mAllMarkers;
    private SharedPreferences mPrefsUser;
    private SharedPreferences.Editor mEditor;
    private Intent intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);


        intentFrom = getIntent();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");

        mAllMarkers = isChecked(Constants.KEY_PREF_ALL_MARKERS);
        mPrefsUser = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        mEditor= mPrefsUser.edit();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mCheckedFilterAll = (CheckedTextView) findViewById(R.id.checked_tv_filtro_show_all);
        mCheckedFilterBusqueda = (CheckedTextView) findViewById(R.id.checked_tv_filtro_busqueda);


        mCheckedFilterBusqueda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckedFilterBusqueda.isChecked())
                {
                    setPref(Constants.KEY_PREF_BUSQUEDA_MARKERS, false);

                    if (mCheckedFilterAll.isChecked()){
                        mCheckedFilterAll.setChecked(false);
                        setPref(Constants.KEY_PREF_ALL_MARKERS, false);
                    }
                    mCheckedFilterBusqueda.setChecked(false);
                }
                else
                {
                    setPref(Constants.KEY_PREF_BUSQUEDA_MARKERS, true);
                    mCheckedFilterBusqueda.setChecked(true);
                }
            }
        });


        mCheckedFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedFilterAll.isChecked())
                {
                    setPref(Constants.KEY_PREF_ALL_MARKERS, false);
                    checkAll(false);
                    mCheckedFilterAll.setChecked(false);
                }
                else
                {
                    setPref(Constants.KEY_PREF_ALL_MARKERS, true);
                    checkAll(true);
                    mCheckedFilterAll.setChecked(true);
                }
            }
        });

        if (isChecked(Constants.KEY_PREF_BUSQUEDA_MARKERS)){
            mCheckedFilterBusqueda.setChecked(true);
        }

        if (mAllMarkers){
            mCheckedFilterAll.setChecked(true);
        }

        //instantiate your adapter with the list of bands
        mAdpaterFiltro = new AdapterChildFiltro(mGroupFiltros);

        mAdpaterFiltro.setChildClickListener(ActivityFiltros.this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdpaterFiltro);

        new Thread(new Runnable() {
            public void run() {

                setUpFiltros();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdpaterFiltro.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_filtros, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdpaterFiltro.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_aplicar:
                mEditor.commit();
                setResult(RESULT_OK, intentFrom);
                finish();
              break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdpaterFiltro.onRestoreInstanceState(savedInstanceState);
    }

    private void setUpFiltros(){

        for (int i=0; i<3;i++){

            switch (i){
                case 0:

                    List<ChildFiltro> mPokeStops = new ArrayList<>();
                    mPokeStops.add(new ChildFiltro(R.drawable.ic_pokestop, getString(R.string.op_normal_pokestop)));
                    mPokeStops.add(new ChildFiltro(R.drawable.ic_pokestope_lucky, getString(R.string.op_lured_pokestop)));

                    GroupFiltro groupFiltroPokeStops = new GroupFiltro(getString(R.string.filtro_pokestops), mPokeStops);

                    if(mAllMarkers){
                        for (int j = 0; j< groupFiltroPokeStops.getItems().size(); j++){
                            groupFiltroPokeStops.onChildClicked(j,true);
                        }
                    }
                    else{
                        if (isChecked(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS)){
                            groupFiltroPokeStops.onChildClicked(0,true);
                        }
                        if (isChecked(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS)){
                            groupFiltroPokeStops.onChildClicked(1,true);
                        }
                    }

                    mGroupFiltros.add(groupFiltroPokeStops);

                    break;

                case 1:

                    List<ChildFiltro> mGyms= new ArrayList<>();
                    mGyms.add(new ChildFiltro(R.drawable.ic_gym_team_blue, getString(R.string.op_blue_gym)));
                    mGyms.add(new ChildFiltro(R.drawable.ic_gym_team_red, getString(R.string.op_red_gym)));
                    mGyms.add(new ChildFiltro(R.drawable.ic_gym_team_yellow, getString(R.string.op_yellow_gym)));
                    mGyms.add(new ChildFiltro(R.drawable.ic_gym_team_white, getString(R.string.op_white_gym)));

                    GroupFiltro groupFiltroGyms = new GroupFiltro(getString(R.string.filtro_gyms), mGyms);

                    if(mAllMarkers){
                        for (int j = 0; j< groupFiltroGyms.getItems().size(); j++){
                            groupFiltroGyms.onChildClicked(j,true);
                        }
                    }

                    else{
                        if (isChecked(Constants.KEY_PREF_BLUE_GYMS_MARKERS)){
                            groupFiltroGyms.onChildClicked(0,true);
                        }
                        if (isChecked(Constants.KEY_PREF_RED_GYMS_MARKERS)){
                            groupFiltroGyms.onChildClicked(1,true);
                        }
                        if (isChecked(Constants.KEY_PREF_YELLOW_GYMS_MARKERS)){
                            groupFiltroGyms.onChildClicked(2,true);
                        }
                        if (isChecked(Constants.KEY_PREF_WHITE_GYMS_MARKERS)){
                            groupFiltroGyms.onChildClicked(3,true);
                        }
                    }

                    mGroupFiltros.add(groupFiltroGyms);

                    break;
            }
        }
    }

    private void checkAll(Boolean check){

        for (GroupFiltro groupFiltro : mGroupFiltros){

            for (int i = 0; i< groupFiltro.getItems().size(); i++){
                groupFiltro.onChildClicked(i, check);
            }

            mAdpaterFiltro.notifyDataSetChanged();
        }

        mCheckedFilterAll.setChecked(check);
        mCheckedFilterBusqueda.setChecked(check);

        setPref(Constants.KEY_PREF_ALL_MARKERS, check);
        setPref(Constants.KEY_PREF_BUSQUEDA_MARKERS, check);
        setPref(Constants.KEY_PREF_BLUE_GYMS_MARKERS, check);
        setPref(Constants.KEY_PREF_RED_GYMS_MARKERS, check);
        setPref(Constants.KEY_PREF_YELLOW_GYMS_MARKERS, check);
        setPref(Constants.KEY_PREF_WHITE_GYMS_MARKERS, check);
        setPref(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS, check);
        setPref(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS, check);


    }

    public Boolean isChecked(String prefKey){
        SharedPreferences prefs_pokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);

        Boolean result = prefs_pokeWhere.getBoolean(prefKey, false);

        return result;
    }

    public void setPref(String prefKey, Boolean pref){
        mEditor.putBoolean(prefKey, pref);
    }

    @Override
    public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {

        if (mCheckedFilterAll.isChecked()){
            mCheckedFilterAll.setChecked(false);
            setPref(Constants.KEY_PREF_ALL_MARKERS, false);
        }

        if (group.getTitle().equalsIgnoreCase(getString(R.string.filtro_pokestops))){
            switch (childIndex){
                case 0:
                    setPref(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS, checked);
                    break;
                case 1:
                    setPref(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS, checked);
                    break;
            }
        }
        else if (group.getTitle().equalsIgnoreCase(getString(R.string.filtro_gyms))){
            switch (childIndex){
                case 0:
                    setPref(Constants.KEY_PREF_BLUE_GYMS_MARKERS, checked);
                    break;
                case 1:
                    setPref(Constants.KEY_PREF_RED_GYMS_MARKERS, checked);
                    break;
                case 2:
                    setPref(Constants.KEY_PREF_YELLOW_GYMS_MARKERS, checked);
                    break;
                case 3:
                    setPref(Constants.KEY_PREF_WHITE_GYMS_MARKERS, checked);
                    break;
            }
        }
    }
}
