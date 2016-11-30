package com.javic.pokewhere.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;

import java.util.List;

/**
 * Created by iMac_Vic on 24/11/16.
 */

public class AdapterCatchablePokemon extends PagerAdapter {

    private Context mContext;
    private List<LocalUserPokemon> mLocalPokemonList;
    private LayoutInflater mInflator;

    public AdapterCatchablePokemon(Context mContext, List<LocalUserPokemon> mLocalPokemonList) {
        this.mContext = mContext;
        this.mLocalPokemonList = mLocalPokemonList;
    }


    @Override
    public int getCount() {
        return mLocalPokemonList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        final LocalUserPokemon pokemon = mLocalPokemonList.get(position);

        ImageView img_pokemon;
        TextView tv_name, tv_cp, tv_iv;

        mInflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View itemView = mInflator.inflate(R.layout.layout_catchable_pokemon, container,
                false);;

        if (itemView!=null){
            img_pokemon = (ImageView) itemView.findViewById(R.id.img_pokemon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_cp = (TextView) itemView.findViewById(R.id.cp);
            tv_iv = (TextView) itemView.findViewById(R.id.iv);

            img_pokemon.setImageBitmap(pokemon.getBitmap());
            tv_name.setText(pokemon.getName());
            tv_cp.setText(String.valueOf(pokemon.getCp()));
            tv_iv.setText(String.valueOf(pokemon.getIv()) + "%");
        }

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }
}
