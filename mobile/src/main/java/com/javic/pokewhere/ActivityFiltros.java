package com.javic.pokewhere;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class ActivityFiltros extends AppCompatActivity implements GroupExpandCollapseListener,OnCheckChildClickListener {

    private static final String TAG = ActivityFiltros.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterFiltro mAdpaterFiltro;
    private CheckedTextView mCheckedTextView;

    List<Filtro> mFiltros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this);
        mCheckedTextView = (CheckedTextView) findViewById(R.id.checked_tv);
        mCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedTextView.isChecked())
                {mCheckedTextView.setChecked(false);}
                else{ checkAll();
                    mCheckedTextView.setChecked(true);}
            }
        });
        setUpFiltros();

        //instantiate your adapter with the list of bands
        mAdpaterFiltro = new AdapterFiltro(mFiltros);

        mAdpaterFiltro.setOnGroupExpandCollapseListener(this);
        mAdpaterFiltro.setChildClickListener(this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdpaterFiltro);
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
            case R.id.action_aplicar_filtros:
                //saveFiltros();
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

        final Boolean allMarkers = isChecked(Constants.KEY_PREF_ALL_MARKERS);

        if (allMarkers){
            mCheckedTextView.setChecked(true);
        }

        for (int i=0; i<3;i++){

            switch (i){
                case 1:

                    List<Opcion> mPokeStops = new ArrayList<>();
                    mPokeStops.add(new Opcion(R.drawable.ic_pokestop, getString(R.string.op_normal_pokestop)));
                    mPokeStops.add(new Opcion(R.drawable.ic_pokestope_lucky, getString(R.string.op_lured_pokestop)));

                    Filtro filtroPokeStops = new Filtro(getString(R.string.filtro_gyms), mPokeStops);

                    if(allMarkers){
                        for (int j = 0; j<filtroPokeStops.getItems().size(); i++){
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

                case 0:

                    List<Opcion> mGyms= new ArrayList<>();
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_blue, getString(R.string.op_blue_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_red, getString(R.string.op_red_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_yellow, getString(R.string.op_yellow_gym)));
                    mGyms.add(new Opcion(R.drawable.ic_gym_team_white, getString(R.string.op_white_gym)));

                    Filtro filtroGyms = new Filtro(getString(R.string.filtro_gyms), mGyms);

                    if(allMarkers){
                        for (int j = 0; j<filtroGyms.getItems().size(); i++){
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

    private void checkAll(){
        for (Filtro filtro: mFiltros){

            for (int i =0; i<filtro.getItems().size();i++){
                filtro.onChildClicked(i, true);
            }
        }
    }

    public Boolean isChecked(String prefKey){
        SharedPreferences prefs_pokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);

        Boolean result = prefs_pokeWhere.getBoolean(prefKey, false);

        return result;
    }

    public void setPref(String prefKey, Boolean pref){
        SharedPreferences prefs_user = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs_user.edit();

        editor.putBoolean(prefKey, pref);

        editor.commit();
    }

    //Callbacks related to the ExpandableRecyclerView
    @Override
    public void onGroupExpanded(ExpandableGroup group) {
        Log.i(TAG, "Expanded: " + group.getTitle() );
    }

    @Override
    public void onGroupCollapsed(ExpandableGroup group) {
        Log.i(TAG, "Collapsed: " + group.getTitle() );
    }

    @Override
    public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {

        if (mCheckedTextView.isChecked()){
            mCheckedTextView.setChecked(false);
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
