package com.javic.pokewhere.holders;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.GroupItem;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class GroupItemViewHolder extends GroupViewHolder {
    private ImageView img_item;
    private TextView tv_item;
    private ImageView img_arrow;

    public GroupItemViewHolder(View itemView) {
        super(itemView);
        tv_item = (TextView) itemView.findViewById(R.id.tv_item);
        img_item = (ImageView) itemView.findViewById(R.id.img_item);
        img_arrow = (ImageView) itemView.findViewById(R.id.img_arrow_item);
    }

    public void setItemTitle(GroupItem group) {
        tv_item.setText(group.getTitle());
    }

    public void setItemImage(GroupItem group){
        img_item.setImageResource(group.getIconResId());
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
}