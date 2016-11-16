package com.javic.pokewhere.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.javic.pokewhere.fragments.FragmentPokemonDetail;
import com.javic.pokewhere.models.LocalUserPokemon;

import java.util.List;

/**
 * Created by vagprogrammer on 29/10/16.
 */

public class AdapterPokemonDetail extends FragmentStatePagerAdapter {

    private List<LocalUserPokemon> mLocalUserPokemonList;

    public AdapterPokemonDetail(FragmentManager fm, List<LocalUserPokemon> mLocalUserPokemonList) {
        super(fm);
        this.mLocalUserPokemonList = mLocalUserPokemonList;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentPokemonDetail.newInstance(mLocalUserPokemonList.get(position));
    }

    @Override
    public int getCount() {
        return mLocalUserPokemonList.size();
    }


    @Override
    public int getItemPosition(Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }
}