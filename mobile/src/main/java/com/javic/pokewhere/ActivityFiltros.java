package com.javic.pokewhere;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.javic.pokewhere.adapters.AdapterFiltro;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;

import java.util.ArrayList;
import java.util.List;

public class ActivityFiltros extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    AdapterFiltro mAdpaterFiltro;

    List<Filtro> mFiltros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this);


        setUpFiltros();
        //instantiate your adapter with the list of bands
        mAdpaterFiltro = new AdapterFiltro(mFiltros);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdpaterFiltro);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpFiltros(){

        List<Opcion> mOpciones = new ArrayList<>();

        for (int i=0; i<3;i++){

            switch (i){
                case 0:
                    mOpciones.add(new Opcion(R.drawable.ic_pokestop, "PokeStop"));
                    mOpciones.add(new Opcion(R.drawable.ic_pokestope_lucky, "Lured PokeStop"));

                    Filtro filtro = new Filtro("PokeStops", mOpciones);

                    mFiltros.add(filtro);

                    break;
            }
        }
    }
}
