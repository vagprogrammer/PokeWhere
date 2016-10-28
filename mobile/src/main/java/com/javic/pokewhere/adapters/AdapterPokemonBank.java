package com.javic.pokewhere.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;

import java.util.List;

/**
 * Created by franciscojimenezjimenez on 26/10/16.
 */

public class AdapterPokemonBank extends DragSelectRecyclerViewAdapter<AdapterPokemonBank.PokemonBankViewHolder> {

    private final Context mContext;
    private final ClickListener mCallback;
    private List<LocalUserPokemon> mLocalUserPokemonList;

    public boolean isSelecting = false;

    // Constructor takes click listener callback
    public AdapterPokemonBank(Context mContext, ClickListener mCallback, List<LocalUserPokemon> mLocalUserPokemonList) {
        super();
        this.mContext = mContext;
        this.mCallback = mCallback;
        this.mLocalUserPokemonList= mLocalUserPokemonList;
    }

    @Override
    public PokemonBankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pokemon, parent, false);
        return new PokemonBankViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PokemonBankViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        LocalUserPokemon pokemon = mLocalUserPokemonList.get(position);
        holder.cp.setText(String.valueOf(pokemon.getCp()));
        holder.iv.setText(String.valueOf(pokemon.getIv()));
        holder.attack.setText(String.valueOf(pokemon.getAttack()));
        holder.defense.setText(String.valueOf(pokemon.getDefense()));
        holder.stamina.setText(String.valueOf(pokemon.getStamina()));

        if (pokemon.getNickname().equals("")){
            holder.name.setText(pokemon.getName());
        }else {
            holder.name.setText(pokemon.getNickname());
        }

        if (pokemon.getBitmap()!=null){
            holder.imgPokemon.setImageBitmap(pokemon.getBitmap());
        }

        holder.imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (pokemon.getFavorite()){
            holder.imgFavorite.setImageResource(R.drawable.ic_bookmarked);
        }
        else{
            holder.imgFavorite.setImageResource(R.drawable.ic_bookmark);
        }

        if (isSelecting){
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        }

        if (isIndexSelected(position)) {
            // Item is selected, change it somehow
            holder.mContainer.setVisibility(View.VISIBLE);
            holder.mCheckBox.setChecked(true);
        } else {
            // Item is not selected, reset it to a non-selected state
            holder.mContainer.setVisibility(View.GONE);
            holder.mCheckBox.setChecked(false);
        }
    }

    @Override
    protected boolean isIndexSelectable(int index) {
        // This method is OPTIONAL, returning false will prevent the item at the specified index from being selected.
        // Both initial selection, and drag selection.

        LocalUserPokemon pokemon = mLocalUserPokemonList.get(index);

        if (pokemon.getFavorite()){

            showToast(mContext.getString(R.string.message_untrasferable_favorite), 500);

            return false;
        }
        else{
            return true;
        }

    }

    @Override
    public int getItemCount() {
        return mLocalUserPokemonList.size();
    }

    public class PokemonBankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        private final View mContainer;
        private final CheckBox mCheckBox;
        private final TextView name, cp,iv,attack,defense,stamina;
        private final ImageView imgPokemon, imgFavorite;


        private PokemonBankViewHolder(View itemView) {
            super(itemView);
            this.mContainer = itemView.findViewById(R.id.layoutContainer);
            this.mCheckBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            this.name = (TextView) itemView.findViewById(R.id.txtName);
            this.cp = (TextView) itemView.findViewById(R.id.txtCP);
            this.iv = (TextView) itemView.findViewById(R.id.txtIV);
            this.attack = (TextView) itemView.findViewById(R.id.txtAttack);
            this.defense = (TextView) itemView.findViewById(R.id.txtDefense);
            this.stamina = (TextView) itemView.findViewById(R.id.txtStamina);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            this.imgPokemon = (ImageView) itemView.findViewById(R.id.imgPokemon);
            this.imgFavorite = (ImageView) itemView.findViewById(R.id.imgFavorite);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null)
            {
                mCallback.onClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null)
            {
                mCallback.onLongClick(getAdapterPosition());
            }
            return true;
        }
    }

    public void upDateAdapter(List<LocalUserPokemon> mLocalUserPokemonList){
        //this.mLocalUserPokemonList.clear();
        //this.mLocalUserPokemonList.addAll(mLocalUserPokemonList);
        this.mLocalUserPokemonList = mLocalUserPokemonList;
        this.notifyDataSetChanged();
    }

    public void changeSelectingState(boolean isSelecting){
        this.isSelecting = isSelecting;
        this.notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(int index);

        void onLongClick(int index);
    }

    private void showToast(String message, int millisecons) {
        final Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, millisecons);
    }

}
