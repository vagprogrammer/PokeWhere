package com.javic.pokewhere.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.PokemonMove;
import com.javic.pokewhere.util.Constants;

import java.text.DecimalFormat;
import java.util.List;


public class FragmentPokemonDetail extends Fragment implements View.OnClickListener{

    private static final String TAG = FragmentPokemonDetail.class.getSimpleName();

    //Callbacks
    private OnFragmentInteractionListener mListener;

    //Context
    private Context mContext;

    //Fragment UI
    private View mView, mLayoutPowerUp, mLayoutEvolve;

    private ImageView imgPokemon, imgFavorite;
    private TextView txtCP, txtName, txtIv, txtAttack,txtDefense,txtStamina,txtMaxCP,
            txtMultCP, txtLevel, txtCandies, txtStardustToPowerup, txtCandiesToPowerUp,
            txtCandiesToEvolve, txtStardust,  txtAttack1, txtAttack2, txtEnergy1, txtEnergy2, txtDamage1, txtDamage2, txtDPS1, txtDPS2, txtDPSTAB1, txtDPSTAB2;

    private Button btnPowerUp, btnEvolve, btnTransfer;
    private ImageButton btnEditName;


    //variables
    private LocalUserPokemon mPokemon;
    private long mUserStardust;

    public FragmentPokemonDetail() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static FragmentPokemonDetail newInstance(LocalUserPokemon pokemon, long mUserStardust) {
        FragmentPokemonDetail fragment = new FragmentPokemonDetail();

        Bundle args = new Bundle();
        args.putParcelable("pokemon", pokemon);
        args.putLong("stardust", mUserStardust);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPokemon = getArguments().getParcelable("pokemon");
            mUserStardust = getArguments().getLong("stardust");
        }
    }

    /*@Override
    public void onResume() {
        super.onResume();

        if (txtStardust!=null){
            txtStardust.setText(String.valueOf(mUserStardust));
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_pokemon_detail, container, false);
        mLayoutPowerUp = mView.findViewById(R.id.layoutPowerup);
        mLayoutEvolve = mView.findViewById(R.id.layoutEvolve);

        imgPokemon = (ImageView) mView.findViewById(R.id.imgPokemon);
        imgFavorite = (ImageView) mView.findViewById(R.id.imgFavorite);
        txtCP = (TextView) mView.findViewById(R.id.txtCP);
        txtName = (TextView) mView.findViewById(R.id.txtName);
        txtIv = (TextView) mView.findViewById(R.id.txtIV);
        txtAttack= (TextView) mView.findViewById(R.id.txtAttack);
        txtDefense= (TextView) mView.findViewById(R.id.txtDefense);
        txtStamina= (TextView) mView.findViewById(R.id.txtStamina);
        txtMaxCP= (TextView) mView.findViewById(R.id.txtMaxCP);
        txtMultCP= (TextView) mView.findViewById(R.id.txtMultCP);
        txtLevel = (TextView) mView.findViewById(R.id.txtLevel);
        txtCandies = (TextView) mView.findViewById(R.id.txtCandies);
        txtStardustToPowerup= (TextView) mView.findViewById(R.id.txtStardustToPowerup);
        txtCandiesToPowerUp= (TextView) mView.findViewById(R.id.txtCandiesToPowerUp);
        txtCandiesToEvolve= (TextView) mView.findViewById(R.id.txtCandiesToEvolve);
        txtStardust= (TextView) mView.findViewById(R.id.txtStardust);
        txtAttack1= (TextView) mView.findViewById(R.id.txtAttack1);
        txtAttack2= (TextView) mView.findViewById(R.id.txtAttack2);
        txtEnergy1 = (TextView) mView.findViewById(R.id.txtEnergy1);
        txtEnergy2= (TextView) mView.findViewById(R.id.txtEnergy2);
        txtDamage1= (TextView) mView.findViewById(R.id.txtDamage1);
        txtDamage2= (TextView) mView.findViewById(R.id.txtDamage2);
        txtDPS1= (TextView) mView.findViewById(R.id.txtDPS1);
        txtDPS2= (TextView) mView.findViewById(R.id.txtDPS2);
        txtDPSTAB1 = (TextView) mView.findViewById(R.id.txtDPSTAB1);
        txtDPSTAB2 = (TextView) mView.findViewById(R.id.txtDPSTAB2);

        btnPowerUp = (Button) mView.findViewById(R.id.btnPowerUp);
        btnEvolve = (Button) mView.findViewById(R.id.btnEvolve);
        btnTransfer = (Button) mView.findViewById(R.id.btnTransfer);
        btnEditName = (ImageButton) mView.findViewById(R.id.btnEditName);

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgPokemon.setImageBitmap(mPokemon.getBitmap());
        txtCP.setText(String.valueOf(mPokemon.getCp()));

        if (mPokemon.getNickname().equals("")){
            txtName.setText(mPokemon.getName());
        }else{
            txtName.setText(mPokemon.getNickname());
        }

        if (mPokemon.getFavorite()){
            imgFavorite.setImageResource(R.drawable.ic_bookmarked);
        }
        else{
            imgFavorite.setImageResource(R.drawable.ic_bookmark);
        }


        txtIv.setText(String.valueOf(mPokemon.getIv())+ "%");
        txtAttack.setText(String.valueOf(mPokemon.getAttack()));
        txtDefense.setText(String.valueOf(mPokemon.getDefense()));
        txtStamina.setText(String.valueOf(mPokemon.getStamina()));
        txtMaxCP.setText(String.valueOf(mPokemon.getMaxCp()));
        txtMultCP.setText(String.valueOf(mPokemon.getEvolveCP()));
        txtLevel.setText(String.valueOf(mPokemon.getLevel()));
        txtStardust.setText(String.valueOf(mUserStardust));
        txtCandies.setText(String.valueOf(mPokemon.getCandies()));

        imgFavorite.setOnClickListener(this);
        btnEditName.setOnClickListener(this);
        btnTransfer.setOnClickListener(this);

        txtStardustToPowerup.setText(String.valueOf(mPokemon.getPowerUpStardust()));
        txtCandiesToPowerUp.setText(String.valueOf(mPokemon.getPoweUpCandies()));
        txtCandiesToEvolve.setText(String.valueOf(mPokemon.getEvolveCandies()));

        final List<PokemonMove> moves = mPokemon.getMoves();
        PokemonMove move1= moves.get(0);
        PokemonMove move2= moves.get(1);

        final double dps1 = (1000/(move1.getTime()*1.0))*(move1.getPower());
        final double dps2 = (1000/(move2.getTime()*1.0))*(move2.getPower());


        txtAttack1.setText(move1.getName());
        txtEnergy1.setText(String.valueOf(move1.getEnergy()));
        txtDamage1.setText(String.valueOf(move1.getPower()));
        txtDPS1.setText(new DecimalFormat("##.##").format(dps1));
        txtDPSTAB1.setText(new DecimalFormat("##.##").format(dps1 * Constants.VALUE_STAB));
        txtAttack2.setText(move2.getName());
        txtEnergy2.setText(String.valueOf(move2.getEnergy()));
        txtDamage2.setText(String.valueOf(move2.getPower()));
        txtDPS2.setText(new DecimalFormat("##.##").format(dps2));
        txtDPSTAB2.setText(new DecimalFormat("##.##").format(dps2 * Constants.VALUE_STAB));

        if (mPokemon.getCandies()>=mPokemon.getPoweUpCandies() && mUserStardust>=mPokemon.getPowerUpStardust()){
            btnPowerUp.setOnClickListener(this);
        }
        else {
            btnPowerUp.setBackground(getResources().getDrawable(R.drawable.buttonshape_disable));
            btnPowerUp.setTextColor(getResources().getColor(R.color.color_background_stroke_user_profile));
            btnPowerUp.setEnabled(false);
        }

        if (mPokemon.getEvolveCandies()==0){
            mLayoutEvolve.setVisibility(View.GONE);
        }
        else if (mPokemon.getCandies()>=mPokemon.getEvolveCandies()){
            btnEvolve.setOnClickListener(this);
        }
        else {
            btnEvolve.setBackground(getResources().getDrawable(R.drawable.buttonshape_disable));
            btnEvolve.setTextColor(getResources().getColor(R.color.color_background_stroke_user_profile));
            btnEvolve.setEnabled(false);
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPowerUp:
                if (mListener!=null){
                    mListener.onFragmentActionPerform(Constants.ACTION_POWER_UP, mPokemon);
                }
                break;
            case R.id.btnEvolve:
                if (mListener!=null){
                    mListener.onFragmentActionPerform(Constants.ACTION_EVOLVE, mPokemon);
                }
                break;
            case R.id.btnTransfer:
                if (mListener!=null){
                    mListener.onFragmentActionPerform(Constants.ACTION_TRANSFER_POKEMON, mPokemon);
                }
                break;
            case R.id.btnEditName:
                if (mListener!=null){
                    mListener.onFragmentActionPerform(Constants.ACTION_RENAME_USER_POKEMON, mPokemon);
                }
                break;
            case R.id.imgFavorite:

                if (mListener!=null){
                    mListener.onFragmentActionPerform(Constants.ACTION_SET_FAVORITE_POKEMON, mPokemon);
                }

                break;

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentActionPerform(int action, Object object);
    }

    public void showToast(){
        Toast.makeText(mContext, getString(R.string.dialog_title_unaviable_service), Toast.LENGTH_SHORT).show();
    }
}
