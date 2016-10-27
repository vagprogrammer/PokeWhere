package com.javic.pokewhere.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 26/10/16.
 */

public class AdapterPokemonBank extends DragSelectRecyclerViewAdapter<AdapterPokemonBank.PokemonBankViewHolder> {

    private final ClickListener mCallback;
    private List<LocalUserPokemon> mLocalUserPokemonList;

    // Constructor takes click listener callback
    public AdapterPokemonBank(ClickListener callback, List<LocalUserPokemon> mLocalUserPokemonList) {
        super();
        mCallback = callback;
        this.mLocalUserPokemonList= mLocalUserPokemonList;
    }

    @Override
    public PokemonBankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pokemon_bank_item, parent, false);
        return new PokemonBankViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PokemonBankViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        // Sets position + 1 to a label view
        //holder.label.setText(String.format("%d", position + 1));
        holder.label.setText(mLocalUserPokemonList.get(position).getName());

        if (isIndexSelected(position)) {
            // Item is selected, change it somehow
        } else {
            // Item is not selected, reset it to a non-selected state
        }
    }

    @Override
    protected boolean isIndexSelectable(int index) {
        // This method is OPTIONAL, returning false will prevent the item at the specified index from being selected.
        // Both initial selection, and drag selection.
        return true;
    }

    @Override
    public int getItemCount() {
        return mLocalUserPokemonList.size();
    }

    public class PokemonBankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public final TextView label;

        public PokemonBankViewHolder(View itemView) {
            super(itemView);
            this.label = (TextView) itemView.findViewById(R.id.label);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onLongClick(getAdapterPosition());
            return true;
        }
    }

    public void upDateAdapter(List<LocalUserPokemon> mLocalUserPokemonList){
        this.mLocalUserPokemonList.clear();
        this.mLocalUserPokemonList.addAll(mLocalUserPokemonList);
        //this.mLocalUserPokemonList = mLocalUserPokemonList;
        this.notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(int index);

        void onLongClick(int index);
    }


}
