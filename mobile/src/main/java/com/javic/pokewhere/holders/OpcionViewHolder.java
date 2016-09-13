package com.javic.pokewhere.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class OpcionViewHolder extends ChildViewHolder {
    private ImageView img;
    private TextView tv;

    public OpcionViewHolder(View itemView) {
        super(itemView);
        img= (ImageView) itemView.findViewById(R.id.img_opcion);
        tv = (TextView) itemView.findViewById(R.id.tv_opcion);
    }

    public void setOpcionImage(int imageResource) {
        img.setImageResource(imageResource);
    }

    public void setOpcionTitle(String titleString) {
        tv.setText(titleString);
    }
}
