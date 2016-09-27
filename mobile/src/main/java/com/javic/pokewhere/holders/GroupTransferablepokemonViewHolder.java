package com.javic.pokewhere.holders;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.GroupTransferablePokemon;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by franciscojimenezjimenez on 12/09/16.
 */
public class GroupTransferablePokemonViewHolder extends GroupViewHolder {
    private TextView tv;
    public ImageView img_filtro;
    private ImageView img_arrow;

    public GroupTransferablePokemonViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tv_filtro);
        img_arrow = (ImageView) itemView.findViewById(R.id.img_arrow_filtro);
        img_filtro = (ImageView) itemView.findViewById(R.id.img_filtro);
    }

    public void setFiltroTitle(GroupTransferablePokemon group) {
        tv.setText(group.getTitle());
    }

    public void setFiltroImagen(Bitmap bitmap){
        img_filtro.setImageBitmap(bitmap);
    }
}
