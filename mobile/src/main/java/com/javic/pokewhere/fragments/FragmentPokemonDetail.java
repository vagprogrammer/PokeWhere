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

import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.Constants;


public class FragmentPokemonDetail extends Fragment implements View.OnClickListener{

    private static final String TAG = FragmentPokemonDetail.class.getSimpleName();
    private LocalUserPokemon mPokemon;

    private OnFragmentInteractionListener mListener;

    //Context
    private Context mContext;

    //Fragment UI
    private View mView;
    private ImageView imgPokemon, imgFavorite;
    private TextView txtCP, txtName, txtIv, txtAttack,txtDefense,txtStamina,txtMaxCP,
            txtMultCP, txtLevel, txtCandies, txtStardustToPowerup, txtCandiesToPowerUp,
            txtCandiesToEvolve, txtStardust;
    private Button btnPowerUp, btnEvolve, btnTransfer;
    private ImageButton btnEditName;

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

    public static FragmentPokemonDetail newInstance(LocalUserPokemon pokemon) {
        FragmentPokemonDetail fragment = new FragmentPokemonDetail();

        Bundle args = new Bundle();
        args.putParcelable("pokemon", pokemon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPokemon = getArguments().getParcelable("pokemon");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_pokemon_detail, container, false);
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

        btnPowerUp = (Button) mView.findViewById(R.id.btnPowerUp);
        btnEvolve = (Button) mView.findViewById(R.id.btnEvolve);
        btnTransfer = (Button) mView.findViewById(R.id.btnTransfer);
        btnEditName = (ImageButton) mView.findViewById(R.id.btnEditName);

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mGO = ActivityDashboard.mGO;

        imgPokemon.setImageBitmap(mPokemon.getBitmap());
        txtCP.setText(String.valueOf(mPokemon.getCp()));

        if (mPokemon.getNickname().equals("")){
            txtName.setText(mPokemon.getName());
        }else{
            txtName.setText(mPokemon.getNickname());
        }

        txtIv.setText(String.valueOf(mPokemon.getIv())+ "%");
        txtAttack.setText(String.valueOf(mPokemon.getAttack()));
        txtDefense.setText(String.valueOf(mPokemon.getDefense()));
        txtStamina.setText(String.valueOf(mPokemon.getStamina()));
        txtMaxCP.setText(String.valueOf(mPokemon.getMaxCp()));
        txtMultCP.setText(String.valueOf(mPokemon.getEvolveCP()));
        txtLevel.setText(String.valueOf(mPokemon.getLevel()));
        txtCandies.setText(String.valueOf(mPokemon.getCandies()));
        txtStardustToPowerup.setText(String.valueOf(mPokemon.getPowerUpStardust()));
        txtCandiesToPowerUp.setText(String.valueOf(mPokemon.getPoweUpCandies()));
        txtCandiesToEvolve.setText(String.valueOf(mPokemon.getEvolveCandies()));
        txtStardust.setText(String.valueOf(ActivityDashboard.mUserStardust));

        if (mPokemon.getFavorite()){
            imgFavorite.setImageResource(R.drawable.ic_bookmarked);
        }
        else{
            imgFavorite.setImageResource(R.drawable.ic_bookmark);
        }

        imgFavorite.setOnClickListener(this);
        btnPowerUp.setOnClickListener(this);
        btnEvolve.setOnClickListener(this);
        btnTransfer.setOnClickListener(this);
        btnEditName.setOnClickListener(this);

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
                showToast();
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
