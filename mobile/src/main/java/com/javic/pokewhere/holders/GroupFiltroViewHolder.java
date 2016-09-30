package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class GroupFiltroViewHolder extends GroupViewHolder {
    private TextView tv;

    public GroupFiltroViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tv_filtro);
    }

    public void setFiltroTitle(ExpandableGroup group) {

        tv.setText(group.getTitle());
    }

}
