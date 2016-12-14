package com.javic.pokewhere.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.PokemonMove;
import com.javic.pokewhere.util.Constants;

import java.text.DecimalFormat;
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
        return view == ((RelativeLayout) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        final LocalUserPokemon pokemon = mLocalPokemonList.get(position);

        ImageView img_pokemon;
        TextView tv_name, tv_cp, tv_iv, txtAttack, txtDefense, txtStamina,
                txtAttack1, txtAttack2, txtDamage1, txtDamage2, txtDPS1, txtDPS2, txtDPSTAB1, txtDPSTAB2;

        mInflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View itemView = mInflator.inflate(R.layout.layout_pokemon_catchable, container,
                false);;

        if (itemView!=null){
            img_pokemon = (ImageView) itemView.findViewById(R.id.img_pokemon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_cp = (TextView) itemView.findViewById(R.id.cp);
            tv_iv = (TextView) itemView.findViewById(R.id.iv);

            txtAttack= (TextView) itemView.findViewById(R.id.txtAttack);
            txtDefense= (TextView) itemView.findViewById(R.id.txtDefense);
            txtStamina= (TextView) itemView.findViewById(R.id.txtStamina);

            txtAttack1= (TextView) itemView.findViewById(R.id.txtAttack1);
            txtAttack2= (TextView) itemView.findViewById(R.id.txtAttack2);
            txtDamage1= (TextView) itemView.findViewById(R.id.txtDamage1);
            txtDamage2= (TextView) itemView.findViewById(R.id.txtDamage2);
            txtDPS1= (TextView) itemView.findViewById(R.id.txtDPS1);
            txtDPS2= (TextView) itemView.findViewById(R.id.txtDPS2);
            txtDPSTAB1 = (TextView) itemView.findViewById(R.id.txtDPSTAB1);
            txtDPSTAB2 = (TextView) itemView.findViewById(R.id.txtDPSTAB2);


            final List<PokemonMove> moves = pokemon.getMoves();
            PokemonMove move1= moves.get(0);
            PokemonMove move2= moves.get(1);

            final double dps1 = (1000/(move1.getTime()*1.0))*(move1.getPower());
            final double dps2 = (1000/(move2.getTime()*1.0))*(move2.getPower());


            txtAttack1.setText(move1.getName());
            txtDamage1.setText(String.valueOf(move1.getPower()));
            txtDPS1.setText(new DecimalFormat("##.##").format(dps1));
            txtDPSTAB1.setText(new DecimalFormat("##.##").format(dps1 * Constants.VALUE_STAB));
            txtAttack2.setText(move2.getName());
            txtDamage2.setText(String.valueOf(move2.getPower()));
            txtDPS2.setText(new DecimalFormat("##.##").format(dps2));
            txtDPSTAB2.setText(new DecimalFormat("##.##").format(dps2 * Constants.VALUE_STAB));



            img_pokemon.setImageBitmap(pokemon.getBitmap());
            tv_name.setText(pokemon.getName());
            tv_cp.setText(String.valueOf(pokemon.getCp()));
            tv_iv.setText(String.valueOf(pokemon.getIv()) + "%");

            txtAttack.setText(String.valueOf(pokemon.getAttack()));
            txtDefense.setText(String.valueOf(pokemon.getDefense()));
            txtStamina.setText(String.valueOf(pokemon.getStamina()));

        }

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }
}
