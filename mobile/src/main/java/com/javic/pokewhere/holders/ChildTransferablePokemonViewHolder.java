package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildTransferablePokemonViewHolder extends CheckableChildViewHolder {
    private CheckedTextView tv;

    public ChildTransferablePokemonViewHolder(View itemView) {
        super(itemView);
        tv = (CheckedTextView) itemView.findViewById(R.id.tv_opcion);
    }

    @Override
    public Checkable getCheckable() {
        return tv;
    }

    public void setOpcionTitle(String titleString) {
        tv.setText(titleString);
    }
}