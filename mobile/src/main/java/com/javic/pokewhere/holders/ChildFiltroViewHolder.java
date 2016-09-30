package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildFiltroViewHolder extends CheckableChildViewHolder {
    private ImageView img;
    private CheckedTextView tv;

    public ChildFiltroViewHolder(View itemView) {
        super(itemView);
        img= (ImageView) itemView.findViewById(R.id.img_opcion);
        tv = (CheckedTextView) itemView.findViewById(R.id.tv_opcion);
    }

    @Override
    public Checkable getCheckable() {
        return tv;
    }

    public void setOpcionImage(int imageResource) {
        img.setImageResource(imageResource);
    }

    public void setOpcionTitle(String titleString) {
        tv.setText(titleString);
    }
}
