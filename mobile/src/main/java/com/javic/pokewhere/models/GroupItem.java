package com.javic.pokewhere.models;

import android.annotation.SuppressLint;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
@SuppressLint("ParcelCreator")
public class GroupItem extends ExpandableGroup<ChildItem> {
    private int iconRes;

    public GroupItem(String title, List items, int iconRes) {
        super(title, items);

        this.iconRes = iconRes;
    }

    public int getIconResId() {
        return iconRes;
    }
}
