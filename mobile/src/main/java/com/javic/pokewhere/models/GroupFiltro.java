package com.javic.pokewhere.models;

import android.annotation.SuppressLint;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
@SuppressLint("ParcelCreator")
public class GroupFiltro extends MultiCheckExpandableGroup {

    public GroupFiltro(String title, List items) {
        super(title, items);
    }
}
