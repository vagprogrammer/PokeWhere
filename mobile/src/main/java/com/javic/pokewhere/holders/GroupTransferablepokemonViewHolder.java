package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class GroupTransferablepokemonViewHolder extends GroupViewHolder {
    private TextView tv;
    public ImageView img_filtro;
    private ImageView img_arrow;

    public GroupTransferablepokemonViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tv_filtro);
        img_arrow = (ImageView) itemView.findViewById(R.id.img_arrow_filtro);
    }

    public void setFiltroTitle(ExpandableGroup group) {

        tv.setText(group.getTitle());
    }


    /*public void setFiltroImage(int res) {

        img_filtro.setImageResource(res);
    }*/
}
