package com.javic.pokewhere.holders;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.javic.pokewhere.R;
import com.javic.pokewhere.interfaces.OnRecyclerViewItemClickListenner;
import com.javic.pokewhere.models.ChildItem;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by victor on 29/09/16.
 */

public class ChildItemViewHolder extends ChildViewHolder {

    public static OnRecyclerViewItemClickListenner mListener;

    private TextView tv_child_item;
    private ImageView img_child_item;
    private ImageButton imgb_child_actionTrash;
    private ChildItem mChildItem;

    public ChildItemViewHolder(final OnRecyclerViewItemClickListenner mListener, View itemView) {
        super(itemView);
        this.mListener = mListener;
        tv_child_item = (TextView) itemView.findViewById(R.id.tv_child_item);
        img_child_item = (ImageView) itemView.findViewById(R.id.img_child_item);
        imgb_child_actionTrash= (ImageButton) itemView.findViewById(R.id.imgb_child_actionTrash);

        imgb_child_actionTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChildItemViewHolder.mListener.OnViewItemClick(mChildItem);
            }
        });
    }

    public void setUp(ChildItem childItem){
        mChildItem = childItem;
        tv_child_item.setText(childItem.getTitle() + ":" + String.valueOf(childItem.getItemCount()));
        img_child_item.setImageResource(childItem.getImage());

        if (mChildItem.getTitle().contains("INCUBATOR") || mChildItem.getItemCount()==0){
            imgb_child_actionTrash.setVisibility(View.INVISIBLE);
        }else{
            imgb_child_actionTrash.setVisibility(View.VISIBLE);
        }

    }

    public void setChildItemTitle(String name) {
        tv_child_item.setText(name);
    }

    public void setChildItemImage(int resImage) {
        img_child_item.setImageResource(resImage);
    }



}
