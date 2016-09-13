package com.javic.pokewhere.holders;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.Filtro;
import com.javic.pokewhere.models.Opcion;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class FiltroViewHolder extends GroupViewHolder {
    private TextView tv;
    private ImageView img_arrow;

    public FiltroViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tv_filtro);
        img_arrow = (ImageView) itemView.findViewById(R.id.img_arrow_filtro);
    }

    public void setFiltroTitle(ExpandableGroup group) {
        tv.setText(group.getTitle());
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand()
    {
        RotateAnimation rotate = new RotateAnimation(180,360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        img_arrow.setAnimation(rotate);
    }

    private void animateCollapse(){
        RotateAnimation rotate = new RotateAnimation(360,180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        img_arrow.setAnimation(rotate);
    }
}
