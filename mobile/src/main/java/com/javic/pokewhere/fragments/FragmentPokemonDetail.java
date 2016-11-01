package com.javic.pokewhere.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;


public class FragmentPokemonDetail extends Fragment implements View.OnClickListener{

    private static final String TAG = FragmentPokemonDetail.class.getSimpleName();
    private LocalUserPokemon mPokemon;
    private PokemonGo mGO;

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


    //Tasks
    private SetFavoriteTask mSetFavoriteTask;

    //Variables
    public boolean favoriteTaskWasCanceled = false;
    public boolean favoriteTaskWasSucces = false;

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
                showToast();
                break;
            case R.id.btnEvolve:
                showToast();
                break;
            case R.id.btnTransfer:
                showToast();
                break;
            case R.id.btnEditName:
                showToast();
                break;
            case R.id.imgFavorite:
                /*if (mSetFavoriteTask == null) {

                    mSetFavoriteTask = new SetFavoriteTask(getUserPokemon(mPokemon.getId()), mPokemon);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mSetFavoriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mSetFavoriteTask.execute();
                    }
                }*/
                showToast();
                break;

        }
    }


    public class SetFavoriteTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "SET_FAVORITE_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(mContext)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        public SetFavoriteTask(Pokemon pokemon, LocalUserPokemon localUserPokemon) {
            this.pokemon = pokemon;
            this.localUserPokemon = localUserPokemon;
            Log.i(TAG, "SET_FAVORITE_TASK: constructor");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "SET_FAVORITE_TASK: doInBackground:start");
            try {
                try {
                    pokemon.setFavoritePokemon(!localUserPokemon.getFavorite());
                    mGO.getInventories().updateInventories(true);
                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: true");
                    favoriteTaskWasSucces = true;
                    return true;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: login or remote server exception");
                    favoriteTaskWasSucces = false;
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: exception");
                favoriteTaskWasSucces = false;
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "SET_FAVORITE_TASK: onPostExecute");
            mSetFavoriteTask = null;
            dialog.dismiss();

            if (succes) {

                if (mListener != null) {
                    mListener.onChangedDetected(true);
                }

                YoYo.with(Techniques.RotateIn)
                        .duration(800)
                        .playOn(imgFavorite);

                if (!localUserPokemon.getFavorite()) {
                    imgFavorite.setImageResource(R.drawable.ic_bookmarked);
                } else {
                    imgFavorite.setImageResource(R.drawable.ic_bookmark);
                }

                localUserPokemon.setFavorite(!localUserPokemon.getFavorite());

            } else {
                Toast.makeText(mContext, getString(R.string.snack_bar_error_with_pokemon), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "SET_FAVORITE_TASK: onCancelled");
            dialog.dismiss();
            mSetFavoriteTask = null;

            favoriteTaskWasCanceled =true;

            if (favoriteTaskWasSucces){
                localUserPokemon.setFavorite(!localUserPokemon.getFavorite());

                if (mListener != null) {
                    mListener.onChangedDetected(true);
                }
            }
        }

    }

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : FragmentPokemonBank.mUserPokemonList) {
            Long id = pokemon.getId();

            if (String.valueOf(id).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
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
        void onFragmentInteraction(Uri uri);
        void onChangedDetected(boolean isChanged);
    }

    public void showToast(){
        Toast.makeText(mContext, getString(R.string.dialog_title_unaviable_service), Toast.LENGTH_SHORT).show();
    }
}
