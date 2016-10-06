package com.javic.pokewhere.holders;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class GroupTransferablePokemonViewHolder2 extends GroupViewHolder implements GroupExpandCollapseListener {
    private ImageView img_filtro;
    private TextView tv;
    private TextView tv_candies;
    private TextView tv_child_count;
    private ImageView img_arrow;

    public GroupTransferablePokemonViewHolder2(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tv_filtro);
        tv_candies = (TextView) itemView.findViewById(R.id.tv_candies);
        tv_child_count= (TextView) itemView.findViewById(R.id.tv_child_count);
        img_filtro = (ImageView) itemView.findViewById(R.id.img_filtro);
        img_arrow = (ImageView) itemView.findViewById(R.id.img_arrow_filtro);
    }

    public void setTexts(GroupTransferablePokemon group){
        tv_child_count.setText(String.valueOf(group.getChildCount()));
        tv.setText(group.getTitle());
        tv_candies.setText(String.valueOf(group.getPokemonCandies()));
    }
    public void setFiltroImagen(Bitmap bitmap){
        img_filtro.setImageBitmap(bitmap);
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        img_arrow.setAnimation(rotate);
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        img_arrow.setAnimation(rotate);
    }

    @Override
    public void onGroupExpanded(ExpandableGroup group) {
        animateExpand();
    }

    @Override
    public void onGroupCollapsed(ExpandableGroup group) {
        animateCollapse();
    }
}