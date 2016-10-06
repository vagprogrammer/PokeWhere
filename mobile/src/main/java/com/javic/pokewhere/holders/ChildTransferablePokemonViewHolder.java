package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.ChildTransferablePokemon;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class ChildTransferablePokemonViewHolder extends CheckableChildViewHolder {

    private ImageView img_bookmark;
    private TextView tv_title;
    private TextView tv_cp;
    private CheckedTextView tv;


    public ChildTransferablePokemonViewHolder(View itemView) {
        super(itemView);

        img_bookmark = (ImageView) itemView.findViewById(R.id.img_bookmark);
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        tv_cp = (TextView) itemView.findViewById(R.id.tv_cp);
        tv = (CheckedTextView) itemView.findViewById(R.id.tv_opcion);
    }

    @Override
    public Checkable getCheckable() {
        return tv;
    }

    public void setUp(ChildTransferablePokemon child) {
        if (child.getFavorite()){
            img_bookmark.setVisibility(View.VISIBLE);
        }
        else {
            img_bookmark.setVisibility(View.INVISIBLE);
        }
        tv_title.setText(child.getTitle());
        tv_cp.setText(String.valueOf(child.getCp()));
        tv.setText("IV:"+String.valueOf(child.getIv()) + "%");
    }
}