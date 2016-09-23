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

import com.javic.pokewhere.adapters.AdapterFiltro;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;
import com.javic.pokewhere.util.Constants;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class ActivityFiltros extends AppCompatActivity implements OnCheckChildClickListener {

    private static final String TAG = ActivityFiltros.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterFiltro mAdpaterFiltro;
    private CheckedTextView mCheckedFilterAll, mCheckedFilterBusqueda;
    private List<Filtro> mFiltros = new ArrayList<>();

    private Boolean mAllMarkers;
    private SharedPreferences mPrefsUser;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);
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
        mAdpaterFiltro = new AdapterFiltro(mFiltros);

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
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
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

                    List<Opcion> mPokeStops = new ArrayList<>();
                    mPokeStops.add(new Opcion(R.drawable.ic_pokestop, getString(R.string.op_normal_pokestop)));
                    mPokeStops.add(new Opcion(R.drawable.ic_pokestope_lucky, getString(R.string.op_lured_pokestop)));

                    Filtro filtroPokeStops = new Filtro(getString(R.string.filtro_pokestops), mPokeStops);

                    if(mAllMarkers){
                        for (int j = 0; j<filtroPokeStops.getItems().size(); j++){
                            filtroPokeStops.onChildClicked(j,true);
                        }
                    }
                    else{
                        if (isChecked(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS)){
                            filtroPokeStops.onChildClicked(0,true);
                        }
                        if (isChecked(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS)){
                            filtroPokeStops.onChildClicked(1,true);
                        }
                    }

                    mFiltros.add(filtroPokeStops);

                    break;

                case 1:

                    List<Opcion> mGyms= new ArrayList<>();
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_blue, getString(R.string.op_blue_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_red, getString(R.string.op_red_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_yellow, getString(R.string.op_yellow_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_white, getString(R.string.op_white_gym)));

                    Filtro filtroGyms = new Filtro(getString(R.string.filtro_gyms), mGyms);

                    if(mAllMarkers){
                        for (int j = 0; j<filtroGyms.getItems().size(); j++){
                            filtroGyms.onChildClicked(j,true);
                        }
                    }

                    else{
                        if (isChecked(Constants.KEY_PREF_BLUE_GYMS_MARKERS)){
                            filtroGyms.onChildClicked(0,true);
                        }
                        if (isChecked(Constants.KEY_PREF_RED_GYMS_MARKERS)){
                            filtroGyms.onChildClicked(1,true);
                        }
                        if (isChecked(Constants.KEY_PREF_YELLOW_GYMS_MARKERS)){
                            filtroGyms.onChildClicked(2,true);
                        }
                        if (isChecked(Constants.KEY_PREF_WHITE_GYMS_MARKERS)){
                            filtroGyms.onChildClicked(3,true);
                        }
                    }

                    mFiltros.add(filtroGyms);

                    break;
            }
        }
    }

    private void checkAll(Boolean check){

        for (Filtro filtro: mFiltros){

            for (int i =0; i<filtro.getItems().size();i++){
                filtro.onChildClicked(i, check);
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
