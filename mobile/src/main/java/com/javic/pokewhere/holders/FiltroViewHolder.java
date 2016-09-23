package com.javic.pokewhere.holders;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class FiltroViewHolder extends GroupViewHolder {
    private TextView tv;
    //public ImageView img_filtro;
    private ImageView img_arrow;

    public FiltroViewHolder(View itemView) {
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
