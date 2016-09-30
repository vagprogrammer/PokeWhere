package com.javic.pokewhere.models;

import android.annotation.SuppressLint;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
@SuppressLint("ParcelCreator")
public class GroupItem extends ExpandableGroup<ChildItem> {
    private int iconResId;

    public GroupItem(String title, List items, int iconRes) {
        super(title, items);

        this.iconResId = iconResId;
    }

    public int getIconResId() {
        return iconResId;
    }
}
