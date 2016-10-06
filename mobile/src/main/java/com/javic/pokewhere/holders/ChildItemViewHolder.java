package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by victor on 29/09/16.
 */

public class ChildItemViewHolder extends ChildViewHolder {

    private TextView tv_child_item;
    private ImageView img_child_item;
    private ImageButton imgb_child_actionTrash;

    public ChildItemViewHolder(View itemView) {
        super(itemView);
        tv_child_item = (TextView) itemView.findViewById(R.id.tv_child_item);
        img_child_item = (ImageView) itemView.findViewById(R.id.img_child_item);
        imgb_child_actionTrash= (ImageButton) itemView.findViewById(R.id.imgb_child_actionTrash);

        imgb_child_actionTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void setChildItemTitle(String name) {
        tv_child_item.setText(name);
    }

    public void setChildItemImage(int resImage) {
        img_child_item.setImageResource(resImage);
    }
}
