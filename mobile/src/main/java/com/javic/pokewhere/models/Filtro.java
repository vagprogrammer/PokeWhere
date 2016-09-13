package com.javic.pokewhere.models;

import android.annotation.SuppressLint;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
@SuppressLint("ParcelCreator")
public class Filtro extends ExpandableGroup<Opcion> {

    public Filtro(String title, List<Opcion> items) {
        super(title, items);
    }
}
