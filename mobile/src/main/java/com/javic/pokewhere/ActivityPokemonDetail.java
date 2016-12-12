package com.javic.pokewhere;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.adapters.AdapterPokemonDetail;
import com.javic.pokewhere.fragments.FragmentPokemonDetail;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.PokemonMove;
import com.javic.pokewhere.models.ProgressTransferPokemon;
import com.javic.pokewhere.util.Constants;
import com.nineoldandroids.animation.Animator;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Evolutions;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;

public class ActivityPokemonDetail extends AppCompatActivity implements FragmentPokemonDetail.OnFragmentInteractionListener, Animator.AnimatorListener {

    private static final String TAG = "ActivityDetail";

    //UI
    private AdapterPokemonDetail mAdapter;
    private ViewPager mViewPager;
    private ImageView leftArrow, rightArrow;

    //Listas
    public static List<LocalUserPokemon> mLocalUserPokemonList;
    public static List<Pokemon> mUserPokemonList;
    public static PokemonGo mGO;

    //Variables
    private int mIndex;
    private boolean isChanged = false;
    private Intent intentFrom;
    private int mUserLevel = 0;
    private long mUserExperience = 0;
    private long mUserNextLevelXP = 0;
    private long mUserStardust = 0;

    private boolean taskSetFavoritePokemonWasCanceled = false;
    private boolean taskPowerUpWasCanceled = false;
    private boolean taskEvolveWasCanceled = false;
    private boolean taskTransferPokemonWasCanceled = false;
    private boolean taskRenameWasCanceled = false;

    //Task
    private SetFavoriteTask mSetFavoriteTask;
    private PowerUpTask mPowerUpTask;
    private EvolveTask mEvolveTask;
    private TransferPokemonsTask mTransferPokemonsTask;
    private RenameTask mRenameTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        intentFrom = getIntent();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_pokemon_detail));

        Bundle args = getIntent().getExtras();

        if (args != null) {
            mIndex = args.getInt("index");

            mUserLevel = args.getInt("level");
            mUserExperience = args.getLong("expirience");
            mUserNextLevelXP = args.getLong("nextLevelXp");
            mUserStardust = args.getLong("stardust");

        } else {
            mIndex = 0;
        }

        leftArrow = (ImageView) findViewById(R.id.leftArrow);
        rightArrow = (ImageView) findViewById(R.id.rightArrow);

        mViewPager = (ViewPager) findViewById(R.id.vp_slider);
        mViewPager.setClipToPadding(false);
        mAdapter = new AdapterPokemonDetail(this.getSupportFragmentManager(), mLocalUserPokemonList, mUserStardust);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, new BackgroundToForegroundTransformer());
        mViewPager.setCurrentItem(mIndex);

        if (mIndex > 0) {
            YoYo.with(Techniques.Flash)
                    .withListener(this)
                    .duration(1000)
                    .playOn(leftArrow);
        } else {
            leftArrow.setVisibility(View.GONE);
        }

        if ((mLocalUserPokemonList.size() - 1) > mIndex) {
            YoYo.with(Techniques.Flash)
                    .withListener(this)
                    .duration(1000)
                    .playOn(rightArrow);
        } else {
            rightArrow.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isChanged) {
                    if (intentFrom != null) {
                        intentFrom.putExtra("resultado", true);
                        if (mLocalUserPokemonList.size()>0){
                            intentFrom.putExtra("pokemon", mLocalUserPokemonList.get(mViewPager.getCurrentItem()));
                        }
                        intentFrom.putExtra("stardust", mUserStardust);
                        intentFrom.putExtra("level", mUserLevel);
                        intentFrom.putExtra("expirience", mUserExperience);
                        intentFrom.putExtra("nextLevelXp", mUserNextLevelXP);
                        setResult(RESULT_OK, intentFrom);
                        finish();
                    }
                }
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (isChanged) {

            if (intentFrom != null) {
                intentFrom.putExtra("resultado", true);
                if (mLocalUserPokemonList.size()>0){
                    intentFrom.putExtra("pokemon", mLocalUserPokemonList.get(mViewPager.getCurrentItem()));
                }
                intentFrom.putExtra("stardust", mUserStardust);
                intentFrom.putExtra("level", mUserLevel);
                intentFrom.putExtra("expirience", mUserExperience);
                intentFrom.putExtra("nextLevelXp", mUserNextLevelXP);
                setResult(RESULT_OK, intentFrom);
                finish();
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onFragmentActionPerform(int action, Object object) {

        final LocalUserPokemon localUserPokemon = (LocalUserPokemon) object;

        switch (action) {
            case Constants.ACTION_SET_FAVORITE_POKEMON:
                mSetFavoriteTask = new SetFavoriteTask(localUserPokemon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mSetFavoriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mSetFavoriteTask.execute();
                }
                break;

            case Constants.ACTION_RENAME_USER_POKEMON:
                String name = "";

                if (localUserPokemon.getNickname().equals("")) {
                    name = ((LocalUserPokemon) object).getName();
                } else {
                    name = localUserPokemon.getNickname();
                }

                final String actualName = name;

                new MaterialDialog.Builder(this)
                        .title(R.string.dialog_title_rename)
                        .negativeText(getString(R.string.location_alert_neg_btn))
                        .positiveText(getString(R.string.dialog_positive_btn_renames))
                        .inputRange(1, 12)
                        .input(getString(R.string.dialog_title_rename), actualName, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                // Do something
                                mRenameTask = new RenameTask(localUserPokemon, input.toString());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mRenameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mRenameTask.execute();
                                }
                            }
                        }).show();
                break;
            case Constants.ACTION_POWER_UP:
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.dialog_title_powerup) + " " + localUserPokemon.getName() + "?")
                        .content(getString(R.string.dialog_content_powerup_1) + " " + String.valueOf(localUserPokemon.getCp()) + getString(R.string.dialog_content_powerup_2)+ " " + String.valueOf(getUserPokemon(localUserPokemon.getId()).getCpAfterPowerup()))
                        .positiveText(R.string.dialog_positive_btn_powerup)
                        .negativeText(R.string.dialog_negative_btn_powerup)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mPowerUpTask = new PowerUpTask(localUserPokemon);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mPowerUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mPowerUpTask.execute();
                                }
                            }
                        })
                        .show();
                break;
            case Constants.ACTION_EVOLVE:
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.dialog_title_evolve_1)+ " " + localUserPokemon.getName() + " " + getString(R.string.dialog_title_evolve_2))
                        .content(getString(R.string.dialog_content_evolve_1) + " " + String.valueOf(localUserPokemon.getCp()) + getString(R.string.dialog_content_evolve_2)+ " " + String.valueOf(localUserPokemon.getEvolveCP()))
                        .positiveText(R.string.dialog_positive_btn_powerup)
                        .negativeText(R.string.dialog_negative_btn_powerup)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mEvolveTask = new EvolveTask(localUserPokemon);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mEvolveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mEvolveTask.execute();
                                }
                            }
                        })
                        .show();
                break;
            case Constants.ACTION_TRANSFER_POKEMON:

                new MaterialDialog.Builder(this)
                        .title(getString(R.string.dialog_title_transfer) + " " + localUserPokemon.getName() +"?")
                        .content(getString(R.string.dialog_content_transfer_1) + getString(R.string.dialog_content_transfer_2) + " " + localUserPokemon.getName()  + " " + getString(R.string.dialog_content_transfer_3) + String.valueOf(localUserPokemon.getCandies() + 1))
                        .positiveText(R.string.dialog_positive_btn_powerup)
                        .negativeText(R.string.dialog_negative_btn_powerup)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mTransferPokemonsTask = new TransferPokemonsTask(localUserPokemon);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferPokemonsTask.execute();
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        leftArrow.setVisibility(View.GONE);
        rightArrow.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : mUserPokemonList) {
            Long id = pokemon.getId();

            if (String.valueOf(id).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
    }

    private Bitmap getBitmapFromAssets(int pokemonIdNumber) {
        AssetManager assetManager = getAssets();

        Bitmap bitmap = null;

        try {
            InputStream is = null;

            if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" + pokemonIdNumber) + ".png");
            } else if (pokemonIdNumber < 100) {
                is = assetManager.open(String.valueOf("0" + pokemonIdNumber) + ".png");
            } else {
                is = assetManager.open(String.valueOf(pokemonIdNumber) + ".png");
            }

            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        }

        return bitmap;
    }

    public class SetFavoriteTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        public SetFavoriteTask(LocalUserPokemon localUserPokemon) {
            this.localUserPokemon = localUserPokemon;
            Log.i(TAG, "SET_FAVORITE_TASK: constructor");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "SET_FAVORITE_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(ActivityPokemonDetail.this)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            pokemon = getUserPokemon(localUserPokemon.getId());

            Log.i(TAG, "SET_FAVORITE_TASK: doInBackground:start");
            try {
                try {
                    pokemon.setFavoritePokemon(!localUserPokemon.getFavorite());
                    mGO.getInventories().updateInventories(true);

                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: true");
                    return true;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "SET_FAVORITE_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "SET_FAVORITE_TASK: onPostExecute");
            mSetFavoriteTask = null;

            if (succes) {

                int index = mLocalUserPokemonList.indexOf(localUserPokemon);

                if (!localUserPokemon.getFavorite()) {
                    Toast.makeText(ActivityPokemonDetail.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.message_favorite_pokemon), Toast.LENGTH_SHORT).show();
                    localUserPokemon.setFavorite(true);
                } else {
                    Toast.makeText(ActivityPokemonDetail.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.message_unfavorite_pokemon), Toast.LENGTH_SHORT).show();
                    localUserPokemon.setFavorite(false);
                }


                mLocalUserPokemonList.set(index, localUserPokemon);

                mAdapter.notifyDataSetChanged();

                isChanged = true;

            } else {
                Toast.makeText(ActivityPokemonDetail.this, getString(R.string.message_un_power_up), Toast.LENGTH_SHORT).show();
            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "SET_FAVORITE_TASK: onCancelled");
            mSetFavoriteTask = null;
            taskSetFavoritePokemonWasCanceled = true;
            dialog.dismiss();

        }

    }
    //Actual S: 85531;

    public class PowerUpTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;

        private LocalUserPokemon localPokemon;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        public PowerUpTask(LocalUserPokemon localUserPokemon) {
            this.localUserPokemon = localUserPokemon;
            Log.i(TAG, "POWER_UP_TASK: constructor");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "POWER_UP_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(ActivityPokemonDetail.this)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            pokemon = getUserPokemon(localUserPokemon.getId());

            Log.i(TAG, "POWER_UP_TASK: doInBackground:start");
            try {
                try {

                    if (pokemon.canPowerUp(true)) {
                        pokemon.powerUp();
                        mGO.getInventories().updateInventories(true);

                        //mUserStardust = mGO.getPlayerProfile().getCurrency(PlayerProfile.Currency.STARDUST);

                        mUserStardust = mUserStardust - localUserPokemon.getPowerUpStardust();

                        localPokemon = new LocalUserPokemon();
                        localPokemon.setId(pokemon.getId());
                        localPokemon.setName(pokemon.getPokemonId().name());
                        localPokemon.setNickname(pokemon.getNickname());
                        localPokemon.setBitmap(getBitmapFromAssets(pokemon.getPokemonId().getNumber()));
                        localPokemon.setNumber(pokemon.getPokemonId().getNumber());
                        localPokemon.setFavorite(pokemon.isFavorite());
                        localPokemon.setDead(pokemon.isInjured());
                        localPokemon.setCp(pokemon.getCp());
                        localPokemon.setIv(((int) (pokemon.getIvRatio() * 100)));
                        localPokemon.setAttack(pokemon.getIndividualAttack());
                        localPokemon.setDefense(pokemon.getIndividualDefense());
                        localPokemon.setStamina(pokemon.getIndividualStamina());

                        //MOVES
                        PokemonMoveMeta moveMeta1 = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
                        PokemonMoveMeta moveMeta2 = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
                        PokemonMove move1 = new PokemonMove(moveMeta1.getMove().name(), moveMeta1.getAccuracy(), moveMeta1.getCritChance(), moveMeta1.getEnergy(),moveMeta1.getPower(), moveMeta1.getTime());
                        PokemonMove move2 = new PokemonMove(moveMeta2.getMove().name(), moveMeta2.getAccuracy(), moveMeta2.getCritChance(), moveMeta2.getEnergy(),moveMeta2.getPower(), moveMeta2.getTime());
                        final List<PokemonMove> moves = new ArrayList<>();
                        moves.add(move1);
                        moves.add(move2);
                        localPokemon.setMoves(moves);

                        //CP
                        List<PokemonIdOuterClass.PokemonId> evolutions = Evolutions.getEvolutions(pokemon.getPokemonId());
                        if (evolutions.size() > 0) {
                            if (pokemon.getPokemonId() != PokemonIdOuterClass.PokemonId.EEVEE){
                                localPokemon.setEvolveCP(pokemon.getCpAfterEvolve(evolutions.get(0)));
                            }
                            else{
                                localPokemon.setEvolveCP(-1);
                            }
                        }
                        List<PokemonIdOuterClass.PokemonId> highest = Evolutions.getHighest(pokemon.getPokemonId());
                        if (highest.size() > 0) {
                            if (pokemon.getPokemonId() != PokemonIdOuterClass.PokemonId.EEVEE){
                                localPokemon.setMaxCp(pokemon.getMaxCpFullEvolveAndPowerupForPlayer(highest.get(0)));
                            }else{
                                localPokemon.setMaxCp(-1);
                            }
                        }

                        localPokemon.setLevel(pokemon.getLevel());
                        localPokemon.setCandies(pokemon.getCandy());
                        localPokemon.setPowerUpStardust(pokemon.getStardustCostsForPowerup());
                        localPokemon.setPoweUpCandies(pokemon.getCandyCostsForPowerup());
                        localPokemon.setEvolveCandies(pokemon.getCandiesToEvolve());
                        localPokemon.setCreationTimeMillis(pokemon.getCreationTimeMs());
                        localPokemon.setPokemonCount(mGO.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId()).size());

                        Log.i(TAG, "POWER_UP_TASK: doInBackground: true");
                        return true;
                    }


                    Log.i(TAG, "POWER_UP_TASK: doInBackground: false");
                    return false;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "POWER_UP_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "POWER_UP_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "POWER_UP_TASK: onPostExecute");
            mPowerUpTask = null;

            if (succes) {

                int index = mLocalUserPokemonList.indexOf(localUserPokemon);

                Toast.makeText(ActivityPokemonDetail.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.text_result_power_up), Toast.LENGTH_SHORT).show();

                mLocalUserPokemonList.set(index, localPokemon);

                mAdapter.mUserStardust = mUserStardust;

                mAdapter.notifyDataSetChanged();

                isChanged = true;

            } else {
                Toast.makeText(ActivityPokemonDetail.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.message_un_powerup), Toast.LENGTH_SHORT).show();
            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "POWER_UP_TASK: onCancelled");
            mPowerUpTask = null;
            taskPowerUpWasCanceled = true;
            dialog.dismiss();

        }

    }

    public class EvolveTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;
        private LocalUserPokemon localPokemon;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        public EvolveTask(LocalUserPokemon localUserPokemon) {
            this.localUserPokemon = localUserPokemon;
            Log.i(TAG, "EVOLVE_TASK: constructor");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "EVOLVE_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(ActivityPokemonDetail.this)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            pokemon = getUserPokemon(localUserPokemon.getId());

            Log.i(TAG, "EVOLVE_TASK: doInBackground:start");
            try {
                try {

                    if (pokemon.canEvolve()) {
                        EvolutionResult result = pokemon.evolve();

                        if (result.isSuccessful()) {
                            Pokemon pokemon = result.getEvolvedPokemon();
                            mGO.getInventories().updateInventories(true);

                            final Stats stats = mGO.getPlayerProfile().getStats();
                            mUserLevel = stats.getLevel();
                            mUserExperience = stats.getExperience();
                            mUserNextLevelXP = stats.getNextLevelXp();

                            localPokemon = new LocalUserPokemon();
                            localPokemon.setId(pokemon.getId());
                            localPokemon.setName(pokemon.getPokemonId().name());
                            localPokemon.setNickname(pokemon.getNickname());
                            localPokemon.setBitmap(getBitmapFromAssets(pokemon.getPokemonId().getNumber()));
                            localPokemon.setNumber(pokemon.getPokemonId().getNumber());
                            localPokemon.setFavorite(pokemon.isFavorite());
                            localPokemon.setDead(pokemon.isInjured());
                            localPokemon.setCp(pokemon.getCp());
                            localPokemon.setIv(((int) (pokemon.getIvRatio() * 100)));
                            localPokemon.setAttack(pokemon.getIndividualAttack());
                            localPokemon.setDefense(pokemon.getIndividualDefense());
                            localPokemon.setStamina(pokemon.getIndividualStamina());

                            //MOVES
                            PokemonMoveMeta moveMeta1 = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
                            PokemonMoveMeta moveMeta2 = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
                            PokemonMove move1 = new PokemonMove(moveMeta1.getMove().name(), moveMeta1.getAccuracy(), moveMeta1.getCritChance(), moveMeta1.getEnergy(),moveMeta1.getPower(), moveMeta1.getTime());
                            PokemonMove move2 = new PokemonMove(moveMeta2.getMove().name(), moveMeta2.getAccuracy(), moveMeta2.getCritChance(), moveMeta2.getEnergy(),moveMeta2.getPower(), moveMeta2.getTime());
                            final List<PokemonMove> moves = new ArrayList<>();
                            moves.add(move1);
                            moves.add(move2);
                            localPokemon.setMoves(moves);


                            //CP
                            List<PokemonIdOuterClass.PokemonId> evolutions = Evolutions.getEvolutions(pokemon.getPokemonId());
                            if (evolutions.size() > 0) {
                                if (pokemon.getPokemonId() != PokemonIdOuterClass.PokemonId.EEVEE){
                                    localPokemon.setEvolveCP(pokemon.getCpAfterEvolve(evolutions.get(0)));
                                }
                                else{
                                    localPokemon.setEvolveCP(-1);
                                }
                            }
                            List<PokemonIdOuterClass.PokemonId> highest = Evolutions.getHighest(pokemon.getPokemonId());
                            if (highest.size() > 0) {
                                if (pokemon.getPokemonId() != PokemonIdOuterClass.PokemonId.EEVEE){
                                    localPokemon.setMaxCp(pokemon.getMaxCpFullEvolveAndPowerupForPlayer(highest.get(0)));
                                }else{
                                    localPokemon.setMaxCp(-1);
                                }
                            }

                            localPokemon.setLevel(pokemon.getLevel());
                            localPokemon.setCandies(pokemon.getCandy());
                            localPokemon.setPowerUpStardust(pokemon.getStardustCostsForPowerup());
                            localPokemon.setPoweUpCandies(pokemon.getCandyCostsForPowerup());
                            localPokemon.setEvolveCandies(pokemon.getCandiesToEvolve());
                            localPokemon.setCreationTimeMillis(pokemon.getCreationTimeMs());
                            localPokemon.setPokemonCount(mGO.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId()).size());
                        }

                        Log.i(TAG, "EVOLVE_TASK: doInBackground: true");
                        return true;
                    } else {
                        Log.i(TAG, "EVOLVE_TASK: doInBackground: false");
                        return false;
                    }

                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "EVOLVE_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "EVOLVE_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "EVOLVE_TASK: onPostExecute");
            mEvolveTask = null;

            if (succes) {

                Toast.makeText(ActivityPokemonDetail.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.text_result_evolve) + " " + localPokemon.getName(), Toast.LENGTH_SHORT).show();

                int index = mLocalUserPokemonList.indexOf(localUserPokemon);

                mLocalUserPokemonList.set(index, localPokemon);

                mAdapter.notifyDataSetChanged();

                isChanged = true;

            } else {
                Toast.makeText(ActivityPokemonDetail.this, localUserPokemon.getName() + " " + getString(R.string.message_un_evolve), Toast.LENGTH_SHORT).show();
            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "EVOLVE_TASK: onCancelled");
            mEvolveTask = null;
            taskEvolveWasCanceled = true;

            dialog.dismiss();

        }

    }

    public class TransferPokemonsTask extends AsyncTask<Void, ProgressTransferPokemon, Boolean> {

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        //Object sended to onProgressUpdate method
        private ProgressTransferPokemon progress;

        private LocalUserPokemon localUserPokemon;

        public TransferPokemonsTask(LocalUserPokemon localUserPokemon) {
            this.localUserPokemon = localUserPokemon;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onPreExecute");

            progress = new ProgressTransferPokemon();

            builder = new MaterialDialog.Builder(ActivityPokemonDetail.this)
                    .title(getString(R.string.dialog_title_transfer_pokemons))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(false, 1, true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            if (mTransferPokemonsTask != null) {
                                mTransferPokemonsTask.cancel(true);
                            }
                        }
                    });
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground");
            try {
                try {

                    Pokemon pokemonToTransfer = getUserPokemon(localUserPokemon.getId());

                    if (pokemonToTransfer != null) {
                        progress.setProgressMessage(getString(R.string.message_text_transfering) + " " + pokemonToTransfer.getPokemonId().toString());
                        progress.setUpdateProgress(false);
                        publishProgress(progress);
                        //pokemonToTransfer.debug();
                        ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                        progress.setProgressMessage(result.toString());
                        progress.setUpdateProgress(true);
                        publishProgress(progress);
                        mGO.getInventories().updateInventories(true);
                    }


                    mUserPokemonList = mGO.getInventories().getPokebank().getPokemons();
                    Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: true");
                    return true;

                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: exception");
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(ProgressTransferPokemon... data) {

            super.onProgressUpdate(data);
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onProgressUpdate: " + data[0]);

            // Increment the dialog's progress by 1 after sleeping for 50ms
            dialog.setContent(data[0].getProgressMessage());

            if (data[0].getUpdateProgress()) {
                dialog.incrementProgress(1);
            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            Log.i(TAG, "TRANSFER_POKEMON_TASK: onProgressUpdate: " + succes.toString());
            mTransferPokemonsTask = null;

            if (succes) {

                Toast.makeText(ActivityPokemonDetail.this, getString(R.string.message_succes_transfer) + " "+ localUserPokemon.getName() + "!", Toast.LENGTH_SHORT).show();

                mLocalUserPokemonList.remove(localUserPokemon);

                isChanged = true;

                if (mLocalUserPokemonList.size()==0){
                    if (intentFrom != null) {
                        intentFrom.putExtra("resultado", true);
                        if (mLocalUserPokemonList.size()>0){
                            intentFrom.putExtra("pokemon", mLocalUserPokemonList.get(mViewPager.getCurrentItem()));
                        }
                        intentFrom.putExtra("stardust", mUserStardust);
                        intentFrom.putExtra("level", mUserLevel);
                        intentFrom.putExtra("expirience", mUserExperience);
                        intentFrom.putExtra("nextLevelXp", mUserNextLevelXP);
                        setResult(RESULT_OK, intentFrom);
                        finish();
                    }
                }
                else {
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                Toast.makeText(ActivityPokemonDetail.this, getString(R.string.message_un_power_up), Toast.LENGTH_SHORT).show();
            }

            //Dismissing the dialog
            dialog.dismiss();
        }


        @Override
        protected void onCancelled() {
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onCancelled");
            mTransferPokemonsTask = null;
            taskTransferPokemonWasCanceled = true;
        }
    }

    public class RenameTask extends AsyncTask<Void, String, Boolean> {

        private Pokemon pokemon;
        private LocalUserPokemon localUserPokemon;
        private String newName;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        public RenameTask(LocalUserPokemon localUserPokemon, String newName) {
            this.localUserPokemon = localUserPokemon;
            this.newName = newName;

            Log.i(TAG, "SET_NAME_TASK: constructor");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "SET_NAME_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(ActivityPokemonDetail.this)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            pokemon = getUserPokemon(localUserPokemon.getId());

            Log.i(TAG, "SET_NAME_TASK: doInBackground:start");
            try {
                try {
                    pokemon.renamePokemon(newName);
                    mGO.getInventories().updateInventories(true);

                    Log.i(TAG, "SET_NAME_TASK: doInBackground: true");
                    return true;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "SET_NAME_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "SET_NAME_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "SET_NAME_TASK: onPostExecute");
            mRenameTask = null;

            if (succes) {

                int index = mLocalUserPokemonList.indexOf(localUserPokemon);

                Toast.makeText(ActivityPokemonDetail.this, "¡" + getString(R.string.text_done_button) + "!", Toast.LENGTH_SHORT).show();
                localUserPokemon.setNickname(newName);

                mLocalUserPokemonList.set(index, localUserPokemon);

                mAdapter.notifyDataSetChanged();

                isChanged = true;

            } else {
                Toast.makeText(ActivityPokemonDetail.this, getString(R.string.message_un_power_up), Toast.LENGTH_SHORT).show();
            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "SET_NAME_TASK: onCancelled");
            mRenameTask = null;
            taskRenameWasCanceled = true;
            dialog.dismiss();

        }

    }

}
