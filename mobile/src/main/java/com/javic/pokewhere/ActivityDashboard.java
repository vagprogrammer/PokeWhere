package com.javic.pokewhere;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javic.pokewhere.fragments.FragmentBag;
import com.javic.pokewhere.fragments.FragmentBlank;
import com.javic.pokewhere.fragments.FragmentCompare;
import com.javic.pokewhere.fragments.FragmentMapa;
import com.javic.pokewhere.fragments.FragmentPokemonBank;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.ProgressTransferPokemon;
import com.javic.pokewhere.services.ServiceFloatingMap;
import com.javic.pokewhere.util.Constants;
import com.javic.pokewhere.util.PrefManager;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import POGOProtos.Data.PlayerDataOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;
import okhttp3.OkHttpClient;

import static android.os.Build.VERSION_CODES.M;

public class ActivityDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentListener {


    private static final String TAG = ActivityDashboard.class.getSimpleName();

    public static final int TASK_CONNECT_WITH_POKEMON_GO = 0;
    public static final int TASK_GET_POKEMON = 1;
    public static final int TASK_TRANSFER = 2;
    public static final int TASK_SET_FAVORITE = 3;


    private static final int MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    //Instance fragment's
    private FragmentMapa mFragmentMapa;
    private FragmentBag mFragmentBag;
    private FragmentPokemonBank mFragmentPokemonBank;
    private FragmentCompare mFragmentCompare;
    private int visibleFragment = Constants.FRAGMENT_POKEBANK;

    // API PokemonGO
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo mGO;

    //Variables
    private String mUserName = "";
    private int mUserTeam = 0;
    private int mUserLevel = 0;
    private long mUserExperience = 0;
    private long mUserNextLevelXP = 0;
    public static long mUserStardust = 0;
    private int mUserBagSpace = 0;
    private int mUserPokeBankSpace = 0;
    private long mCreationTime = 0;

    private Map<String, String> mRewardsMap = new HashMap<String, String>();
    private PrefManager prefmanager;

    private boolean isGoogleAccount;
    private boolean taskConnectWithPGoWasCancelled = false;
    private boolean taskGetPokemonWasCanceled = false;
    private boolean taskTransferPokemonWasCanceled = false;
    private boolean taskSetFavoritePokemonWasCanceled = false;

    // Activity UI
    private View mView;
    public static NavigationView mNavigationView;
    public static DrawerLayout mDrawerLayout;
    private View mProgressView;
    private View mContainerFormView;
    private TextView mNavHeaderUserName, mNavHeaderUserLevel, mNavHeaderUserXP, mNavHeaderUserStardust, mNavHeaderUserAntiquity, mNavHeaderUserBagSpace, mNavHeaderUserPokeBankSpace;
    private SeekBar mNavHeaderXpBar;
    private ImageView mNavHeaderImage;
    private Snackbar mSnackBar;

    //Task
    private ConnectWithPokemonGoTask mConnectTask;
    private GetPokemonsTask mGetPokemonsTask;
    private TransferPokemonsTask mTransferPokemonsTask;
    private SetFavoriteTask mSetFavoriteTask;

    //Listas
    private List<Pokemon> mUserPokemonList;
    private List<LocalUserPokemon> mLocalUserPokemonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefmanager = new PrefManager(this);

        isGoogleAccount = prefmanager.getGooglePref();

        //Get elemtns of UI
        mView = findViewById(R.id.layout_main_content);
        mContainerFormView = mView.findViewById(R.id.container_form);
        mProgressView = mView.findViewById(R.id.login_progress);

        //Navigation View
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        //HeaderView of Navigation View
        View mHeaderView = mNavigationView.getHeaderView(0);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavHeaderImage = (ImageView) mHeaderView.findViewById(R.id.nav_header_image);
        mNavHeaderUserName = (TextView) mHeaderView.findViewById(R.id.nav_header_user_name);
        mNavHeaderUserLevel = (TextView) mHeaderView.findViewById(R.id.nav_header_user_level);
        mNavHeaderUserXP = (TextView) mHeaderView.findViewById(R.id.nav_header_user_xp);
        mNavHeaderUserStardust = (TextView) mHeaderView.findViewById(R.id.nav_header_user_stardust);
        mNavHeaderUserAntiquity = (TextView) mHeaderView.findViewById(R.id.nav_header_user_antiquity);
        mNavHeaderUserBagSpace = (TextView) mHeaderView.findViewById(R.id.nav_header_user_bag_space);
        mNavHeaderUserPokeBankSpace = (TextView) mHeaderView.findViewById(R.id.nav_header_user_pokebank_space);
        mNavHeaderXpBar = (SeekBar) mHeaderView.findViewById(R.id.bar_xp);

        //Disabel the drag
        mNavHeaderXpBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);


        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(View drawerView) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
            @Override
            public void onDrawerClosed(View drawerView) {
                if (visibleFragment == Constants.FRAGMENT_MAPA) {
                    mFragmentMapa.showCustomDialog();
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        // Update your UI here.
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
                        if (f != null) {
                            Log.i(TAG, "llamamos a UpdateTitle");
                            updateNavigationView(f);
                        } else {
                            Log.i(TAG, "El fragment es = null");
                        }

                    }
                });

        setUpData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGO == null) {
            mConnectTask = new ConnectWithPokemonGoTask();
            mConnectTask.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mConnectTask != null) {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: cancel:true");
            mConnectTask.cancel(true);
        }

        if (mSetFavoriteTask != null) {
            Log.i(TAG, "SET_FAVORITE_TASK: cancel:true");
            mSetFavoriteTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {

        switch (visibleFragment){
            case Constants.FRAGMENT_POKEBANK:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    //Verificamos que no haya elementos seleccionados en la lista de pokémon
                    if (mFragmentPokemonBank!=null){
                        if (mFragmentPokemonBank.canFinish()){
                            finish();
                        }
                    }
                }
                break;
            case Constants.FRAGMENT_COMPARE:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    //Verificamos que no haya elementos seleccionados en la lista de pokémon
                    if (mFragmentCompare!=null){
                        if (mFragmentCompare.canFinish()){
                            super.onBackPressed();
                        }
                    }
                }
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean show = false;

        switch (id){
            case R.id.nav_fragment_pokebank:
                // Handle the camera action
                setFragment(Constants.FRAGMENT_POKEBANK, null);
                show= true;
                break;

            case R.id.nav_fragment_bag:
                setFragment(Constants.FRAGMENT_BAG, null);
                show= true;
                break;

            case R.id.nav_fragment_map:
                setFragment(Constants.FRAGMENT_MAPA, null);
                show= true;
                break;

            case R.id.nav_sing_out:
                deleteCredentials();
                startActivity(new Intent(ActivityDashboard.this, ActivitySelectAccount.class));
                finish();
                break;
            case R.id.nav_contact:
                goToAppDetail();
                break;
            default:
                show = false;
                break;
        }

        showProgress(show);
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public void setFragment(int position, Object object) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (position) {
            case Constants.FRAGMENT_BLANK:

                FragmentBlank mFragmentBlank = FragmentBlank.newInstance();
                fragmentTransaction.add(R.id.content_fragment, mFragmentBlank);
                fragmentTransaction.commit();
                break;
            case Constants.FRAGMENT_MAPA:
                if (mGO != null) {
                    if (mFragmentMapa==null){
                        mFragmentMapa = FragmentMapa.newInstance(mGO);
                        replaceFragment(mFragmentMapa);
                    }else{
                        showProgress(false);
                    }
                }
                break;
            case Constants.FRAGMENT_BAG:
                if (mGO != null) {
                    if (mFragmentBag==null){
                        mFragmentBag = FragmentBag.newInstance(mGO);
                        replaceFragment(mFragmentBag);
                    }
                    else{
                        showProgress(false);
                    }
                }
                break;
            case Constants.FRAGMENT_POKEBANK:
                if (mGO != null) {
                    if (mFragmentPokemonBank==null){
                        //mFragmentPokemon = mFragmentPokemon.newInstance(mGO);
                        if (mLocalUserPokemonList!=null){
                            mFragmentPokemonBank = FragmentPokemonBank.newInstance(mLocalUserPokemonList);
                            replaceFragment(mFragmentPokemonBank);
                        }
                    }else{
                        showProgress(false);
                    }
                }
                break;

            case Constants.FRAGMENT_COMPARE:
                if (mGO != null) {
                    mFragmentCompare = FragmentCompare.newInstance(mGO, (List<LocalUserPokemon>) object);
                    replaceFragment(mFragmentCompare);

                }
                break;

        }
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getSimpleName();
        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        FragmentTransaction ft;

        if (!fragmentPopped) { //fragment not in back stack, create it.

            Log.i(TAG, "fragment not in back stack, create it");
            ft = manager.beginTransaction();
            ft.add(R.id.content_fragment, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    private void updateNavigationView(Fragment fragment) {
        String fragClassName = fragment.getClass().getSimpleName();

        switch (fragClassName){
            case "FragmentPokemonBank":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                visibleFragment = Constants.FRAGMENT_POKEBANK;
                break;
            case "FragmentBag":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_BAG).setChecked(true);
                visibleFragment = Constants.FRAGMENT_BAG;
                break;
            case "FragmentMapa":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_MAPA).setChecked(true);
                visibleFragment = Constants.FRAGMENT_MAPA;
                break;
        }
    }

    /**
     * @return Si se puede visualizar true, si no se puede visualizar false
     */
    @SuppressLint("NewApi")
    private boolean showMapHead() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startService(new Intent(ActivityDashboard.this, ServiceFloatingMap.class));
            return true;
        }

        if (Settings.canDrawOverlays(this)) {
            startService(new Intent(ActivityDashboard.this, ServiceFloatingMap.class));
            return true;
        }

        return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            final boolean canShow = showMapHead();
            if (!canShow) {
                Log.i(TAG, "Permiso Denegado");
            }
        } else if (requestCode == Constants.REQUEST_CODE_ACTIVITY_POKEMON_DETAIL && resultCode == RESULT_OK) {

            boolean isChanged = data.getExtras().getBoolean("resultado");

            if (isChanged) {
                if (mGetPokemonsTask == null) {
                    mGetPokemonsTask = new GetPokemonsTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mGetPokemonsTask.execute();
                    }
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgressView(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            //mContainerFormView.setVisibility(show ? View.VISIBLE:View.GONE  );
            mContainerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContainerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            //mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mContainerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public boolean isDeviceOnline() {

        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isConnected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                isConnected = true;
            }
        } else {
            // not connected to the internet
            isConnected = false;
        }

        return isConnected;
    }

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {

                            if (task == TASK_CONNECT_WITH_POKEMON_GO){

                                mConnectTask = new ConnectWithPokemonGoTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mConnectTask.execute();
                                }
                            } else if (task == TASK_GET_POKEMON) {
                                mGetPokemonsTask = new GetPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mGetPokemonsTask.execute();
                                }

                            }
                            /*else if (task == TASK_TRANSFER) {

                                mTransferPokemonsTask = new FragmentPokemonBank.TransferPokemonsTask();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferPokemonsTask.execute();
                                }
                            }*/
                        } else {

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }
                });

        mSnackBar.show();
    }

    private String getPref(String KEY_PREF) {
        SharedPreferences prefsPokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        String pref = prefsPokeWhere.getString(KEY_PREF, "");
        return pref;
    }

    public void deleteCredentials() {
        SharedPreferences prefs_user = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs_user.edit();

        editor.putBoolean(Constants.KEY_PREF_GOOGLE, false);
        editor.putString(Constants.KEY_PREF_REFRESH_TOKEN, "");
        editor.putBoolean(Constants.KEY_PREF_IS_FIRST_TIME_LAUNCH, true);

        editor.putString(Constants.KEY_PREF_USER_EMAIL, "");
        editor.putString(Constants.KEY_PREF_USER_PASS, "");

        editor.apply();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void goToAppDetail() {
        final String packageName = Constants.PACKAGE_NAME;
        String url = "";

        try {
            //Check whether Google Play store is installed or not:
            this.getPackageManager().getPackageInfo("com.android.vending", 0);

            url = "market://details?id=" + packageName;
        } catch (final Exception e) {
            url = "https://play.google.com/store/apps/details?id=" + packageName;
        }


        //Open the app page in Google Play store:
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
    }

    public void setUpData() {

        mRewardsMap.put("1", "1000");
        mRewardsMap.put("2", "2000");
        mRewardsMap.put("3", "3000");
        mRewardsMap.put("4", "4000");
        mRewardsMap.put("5", "5000");
        mRewardsMap.put("6", "6000");
        mRewardsMap.put("7", "7000");
        mRewardsMap.put("8", "8000");
        mRewardsMap.put("9", "9000");
        mRewardsMap.put("10", "10000");
        mRewardsMap.put("11", "10000");
        mRewardsMap.put("12", "10000");
        mRewardsMap.put("13", "10000");
        mRewardsMap.put("14", "15000");
        mRewardsMap.put("15", "20000");
        mRewardsMap.put("16", "20000");
        mRewardsMap.put("17", "20000");
        mRewardsMap.put("18", "25000");
        mRewardsMap.put("19", "25000");
        mRewardsMap.put("20", "50000");
        mRewardsMap.put("21", "75000");
        mRewardsMap.put("22", "100000");
        mRewardsMap.put("23", "125000");
        mRewardsMap.put("24", "150000");
        mRewardsMap.put("25", "190000");
        mRewardsMap.put("26", "200000");
        mRewardsMap.put("27", "250000");
        mRewardsMap.put("28", "300000");
        mRewardsMap.put("29", "350000");
        mRewardsMap.put("30", "500000");
        mRewardsMap.put("31", "500000");
        mRewardsMap.put("32", "750000");
        mRewardsMap.put("33", "1000000");
        mRewardsMap.put("34", "1250000");
        mRewardsMap.put("35", "1500000");
        mRewardsMap.put("36", "2000000");
        mRewardsMap.put("37", "2500000");
        mRewardsMap.put("38", "3000000");
        mRewardsMap.put("39", "5000000");
        mRewardsMap.put("40", "5000000");
        mRewardsMap.put("41", "5000000");
        mRewardsMap.put("42", "5000000");
        mRewardsMap.put("43", "5000000");
        mRewardsMap.put("44", "5000000");
        mRewardsMap.put("45", "5000000");
        mRewardsMap.put("46", "5000000");
        mRewardsMap.put("47", "5000000");
        mRewardsMap.put("48", "5000000");
        mRewardsMap.put("49", "5000000");
        mRewardsMap.put("50", "5000000");

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy", cal).toString();
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

    public Pokemon getUserPokemon(Long idPokemon) {

        for (Pokemon pokemon : mUserPokemonList) {
            Long id = pokemon.getId();

            if (String.valueOf(id).equalsIgnoreCase(String.valueOf(idPokemon))) {
                return pokemon;
            }
        }
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation change
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            super.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * >Communicating activity with Other Fragments</a> for more information.
     */
    @Override
    public void onFragmentCreatedViewStatus(int visibleFragment) {

        this.visibleFragment = visibleFragment;

    }

    @Override
    public void showProgress(Boolean show) {
        showProgressView(show);
    }

    @Override
    public void onFragmentActionPerform(int action, Object object) {
        switch (action) {

            case Constants.ACTION_START_SERVICE:
                final boolean canShow = showMapHead();

                if (!canShow) {
                    // 広告トリガーのFloatingViewの表示許可設定
                    @SuppressLint("InlinedApi")
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ActivityDashboard.this.getPackageName()));
                    startActivityForResult(intent, MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
                }

                break;


            case Constants.ACTION_REFRESH_TOKEN:

                mConnectTask = new ConnectWithPokemonGoTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mConnectTask.execute();
                }

                break;

            case Constants.ACTION_FRAGMENT_VER_TODOS:
                setFragment(Constants.FRAGMENT_COMPARE, object);
                break;

            case Constants.ACTION_FRAGMENT_SET_FAVORITE_POKEMON:

                mSetFavoriteTask = new SetFavoriteTask((LocalUserPokemon) object);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mSetFavoriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mSetFavoriteTask.execute();
                }
                break;
            case Constants.ACTION_FRAGMENT_TRANSFER_POKEMON:
                mTransferPokemonsTask = new TransferPokemonsTask((List<LocalUserPokemon>) object);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mTransferPokemonsTask.execute();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class ConnectWithPokemonGoTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: onPreExecute");
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: doInBackground: start");
            try {
                try {
                    publishProgress(getString(R.string.message_connecting_with_pokemon_go));

                    mGO = new PokemonGo(httpClient);

                    if (!getPref(Constants.KEY_PREF_REFRESH_TOKEN).equalsIgnoreCase("")) {
                        //User is logged in with Google Account
                        mGO.login(new GoogleUserCredentialProvider(httpClient, getPref(Constants.KEY_PREF_REFRESH_TOKEN)));
                    } else {
                        //User is logged in with username and password
                        if (isGoogleAccount) {
                            mGO.login(new GoogleAutoCredentialProvider(httpClient, getPref(Constants.KEY_PREF_USER_EMAIL), getPref(Constants.KEY_PREF_USER_PASS)));
                        } else {
                            mGO.login(new PtcCredentialProvider(httpClient, getPref(Constants.KEY_PREF_USER_EMAIL), getPref(Constants.KEY_PREF_USER_PASS)));
                        }

                    }

                    if (mGO != null) {

                        publishProgress(getString(R.string.message_get_user_information));
                        sleep(1000);
                        final PlayerDataOuterClass.PlayerData playerData = mGO.getPlayerProfile().getPlayerData();

                        publishProgress(getString(R.string.message_get_account_information));
                        sleep(1000);
                        final Stats stats = mGO.getPlayerProfile().getStats();

                        sleep(1000);
                        mUserStardust = mGO.getPlayerProfile().getCurrency(PlayerProfile.Currency.STARDUST);

                        mUserName = playerData.getUsername();
                        mUserTeam = playerData.getTeamValue();
                        mUserBagSpace = playerData.getMaxItemStorage();
                        mUserPokeBankSpace = playerData.getMaxPokemonStorage();
                        mCreationTime = playerData.getCreationTimestampMs();
                        mUserLevel = stats.getLevel();
                        mUserExperience = stats.getExperience();
                        mUserNextLevelXP = stats.getNextLevelXp();

                        mGetPokemonsTask = new GetPokemonsTask();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            mGetPokemonsTask.execute();
                        }

                        return true;
                    }
                } catch (LoginFailedException | RemoteServerException e) {
                    Log.i(TAG, "GET_ITEMS_TASK: doInBackground: login or remote_server exception");
                    Log.i(TAG, e.toString());
                    return false;
                }

            } catch (Exception e) {
                Log.i(TAG, "GET_ITEMS_TASK: doInBackground: general exception");
                Log.i(TAG, e.toString());
                return false;

            }

            return false;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            super.onProgressUpdate(message);
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: onProgressUpdate: " + message[0]);
            Toast.makeText(ActivityDashboard.this, message[0], Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: onPostExecute: " + succes.toString());

            mConnectTask = null;

            if (succes) {

                mNavHeaderUserName.setText(mUserName);
                mNavHeaderUserAntiquity.setText(getDate(mCreationTime));
                mNavHeaderUserLevel.setText(getString(R.string.nav_header_user_level) + " " + String.valueOf(mUserLevel));
                mNavHeaderUserXP.setText(String.valueOf(Long.valueOf(mRewardsMap.get(String.valueOf(mUserLevel))) - (mUserNextLevelXP - mUserExperience)) + " / " + mRewardsMap.get(String.valueOf(mUserLevel)));

                mNavHeaderXpBar.setMax(Integer.parseInt(mRewardsMap.get(String.valueOf(mUserLevel))));
                mNavHeaderXpBar.setProgress(Integer.parseInt(mRewardsMap.get(String.valueOf(mUserLevel))) - Long.valueOf(mUserNextLevelXP - mUserExperience).intValue());

                mNavHeaderUserStardust.setText(getString(R.string.nav_header_user_stardust) + " " + String.valueOf(mUserStardust));

                mNavHeaderUserBagSpace.setText(getString(R.string.nav_header_user_bag_space) + " " + String.valueOf(mUserBagSpace));
                mNavHeaderUserPokeBankSpace.setText(getString(R.string.nav_header_user_pokebank_space) + " " + String.valueOf(mUserPokeBankSpace));

                switch (mUserTeam) {
                    case 1:
                        mNavHeaderImage.setImageResource(R.drawable.ic_team_blue);
                        break;
                    case 2:
                        mNavHeaderImage.setImageResource(R.drawable.ic_team_red);
                        break;
                    case 3:
                        mNavHeaderImage.setImageResource(R.drawable.ic_team_yellow);
                        break;
                    default:
                        mNavHeaderImage.setImageResource(R.drawable.ic_gym_team_white);
                        break;
                }
                setFragment(visibleFragment, null);
                mNavigationView.getMenu().getItem(visibleFragment).setChecked(true);

                if (prefmanager.isFirstTimeLaunch()) {
                    mDrawerLayout.openDrawer(mNavigationView);
                    prefmanager.setFirstTimeLaunch(false);
                }
            } else {

                setFragment(Constants.FRAGMENT_BLANK, null);
                mNavigationView.getMenu().getItem(visibleFragment).setChecked(false);

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_CONNECT_WITH_POKEMON_GO);

                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_CONNECT_WITH_POKEMON_GO);
                }

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: onCancelled");
            mConnectTask = null;
            taskConnectWithPGoWasCancelled = true;
        }
    }

    public class GetPokemonsTask extends AsyncTask<Void, Void, Boolean> {

        private int totalPokemons=0;
        private int pokemonStorage=0;

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "GET_POKEMON_TASK: onPreExecute");

            //Show the progressBar
            showProgress(true);

            mUserPokemonList = new ArrayList<>();
            mLocalUserPokemonList = new ArrayList<>();

            super.onPreExecute();
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: start");

            try {
                try {
                    mGO.getInventories().updateInventories(true);
                    mUserPokemonList = mGO.getInventories().getPokebank().getPokemons();
                    totalPokemons = mUserPokemonList.size() + mGO.getInventories().getHatchery().getEggs().size();
                    pokemonStorage = mGO.getPlayerProfile().getPlayerData().getMaxPokemonStorage();

                    for (Pokemon pokemon : mUserPokemonList) {

                        if (!isCancelled()) {

                            LocalUserPokemon localUserPokemon = new LocalUserPokemon();
                            localUserPokemon.setId(pokemon.getId());
                            localUserPokemon.setName(pokemon.getPokemonId().toString());
                            localUserPokemon.setNickname(pokemon.getNickname());
                            localUserPokemon.setBitmap(getBitmapFromAssets(pokemon.getPokemonId().getNumber()));
                            localUserPokemon.setNumber(pokemon.getPokemonId().getNumber());
                            localUserPokemon.setFavorite(pokemon.isFavorite());
                            localUserPokemon.setDead(pokemon.isInjured());
                            localUserPokemon.setCp(pokemon.getCp());
                            localUserPokemon.setIv(((int) (pokemon.getIvRatio() * 100)));
                            localUserPokemon.setAttack(pokemon.getIndividualAttack());
                            localUserPokemon.setDefense(pokemon.getIndividualDefense());
                            localUserPokemon.setStamina(pokemon.getIndividualStamina());
                            localUserPokemon.setMaxCp(pokemon.getMaxCpFullEvolveAndPowerupForPlayer());
                            localUserPokemon.setEvolveCP(pokemon.getCpAfterEvolve());
                            localUserPokemon.setLevel(pokemon.getLevel());
                            localUserPokemon.setCandies(pokemon.getCandy());
                            localUserPokemon.setPowerUpStardust(pokemon.getStardustCostsForPowerup());
                            localUserPokemon.setPoweUpCandies(pokemon.getCandyCostsForPowerup());
                            localUserPokemon.setEvolveCandies(pokemon.getCandiesToEvolve());
                            localUserPokemon.setCreationTimeMillis(pokemon.getCreationTimeMs());

                            mLocalUserPokemonList.add(localUserPokemon);
                        } else {
                            Log.i(TAG, "GET_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }
                    }

                    Log.i(TAG, "GET_POKEMON_TASK: doInBackground: true");
                    return true;

                } catch (LoginFailedException | RemoteServerException e) {
                    Log.i(TAG, "GET_POKEMON_TASK: doInBackground: login or remote_server exception");
                    Log.i(TAG, e.toString());
                    return false;
                }

            } catch (Exception e) {
                Log.i(TAG, "GET_POKEMON_TASK: doInBackground: general exception");
                Log.i(TAG, e.toString());
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            Log.i(TAG, "GET_POKEMON_TASK: onPostExecute: " + succes.toString());
            mGetPokemonsTask = null;

            if (succes) {
                //setActionBarTitle(String.valueOf(totalPokemons) + "/" + String.valueOf(pokemonStorage) + " " + getString(R.string.text_pokemones));
                //mAdapter.upDateAdapter(mLocalUserPokemonList);
            } else {
                //setActionBarTitle(getString(R.string.snack_bar_error_with_pokemon));

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_GET_POKEMON);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_GET_POKEMON);
                }
            }

            //Show the progressBar
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "GET_POKEMON_TASK: onCancelled");
            taskGetPokemonWasCanceled = true;
            mGetPokemonsTask = null;
        }

    }

    public class TransferPokemonsTask extends AsyncTask<Void, ProgressTransferPokemon, Boolean> {

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        //Object sended to onProgressUpdate method
        private ProgressTransferPokemon progress;

        private List<LocalUserPokemon> mTransferablePokemonList;

        public TransferPokemonsTask(List<LocalUserPokemon> mTransferablePokemonList){
            this.mTransferablePokemonList = mTransferablePokemonList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onPreExecute");

            progress = new ProgressTransferPokemon();

            builder = new MaterialDialog.Builder(ActivityDashboard.this)
                    .title(getString(R.string.dialog_title_transfer_pokemons))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(false, mTransferablePokemonList.size(), true)
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

                    for (LocalUserPokemon transferablePokemon : mTransferablePokemonList) {

                        if (!isCancelled()) {

                            Pokemon pokemonToTransfer = getUserPokemon(transferablePokemon.getId());

                            if (pokemonToTransfer != null) {
                                progress.setProgressMessage(pokemonToTransfer.getPokemonId().toString());
                                progress.setUpdateProgress(false);
                                publishProgress(progress);
                                //pokemonToTransfer.debug();
                                ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                                progress.setProgressMessage(result.toString());
                                progress.setUpdateProgress(true);
                                publishProgress(progress);
                                sleep(900);
                                mGO.getInventories().updateInventories(true);
                            }
                        } else {
                            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: task is cancelled");
                            return false;
                        }

                    }

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

            //Dismissing the dialog
            dialog.dismiss();

            if (succes) {

                if (mGetPokemonsTask == null) {
                    mGetPokemonsTask = new GetPokemonsTask();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mGetPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mGetPokemonsTask.execute();
                    }
                }

            } else {

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), TASK_TRANSFER);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), TASK_TRANSFER);
                }

            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onCancelled");
            mTransferPokemonsTask = null;
            taskTransferPokemonWasCanceled = true;
        }
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

            pokemon = getUserPokemon(localUserPokemon.getId());

            builder = new MaterialDialog.Builder(ActivityDashboard.this)
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);
            dialog = builder.build();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

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
            dialog.dismiss();

            if (succes) {

                if (visibleFragment == Constants.FRAGMENT_POKEBANK){
                    mFragmentPokemonBank.onTaskFinish(Constants.ACTION_FRAGMENT_SET_FAVORITE_POKEMON, false, localUserPokemon);
                }

            } else {
                Toast.makeText(ActivityDashboard.this, getString(R.string.snack_bar_error_with_pokemon), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "SET_FAVORITE_TASK: onCancelled");
            mSetFavoriteTask = null;
            taskSetFavoritePokemonWasCanceled = true;

            dialog.dismiss();

            if (visibleFragment == Constants.FRAGMENT_POKEBANK){
                mFragmentPokemonBank.onTaskFinish(Constants.ACTION_FRAGMENT_SET_FAVORITE_POKEMON, true, localUserPokemon);
            }

        }

    }
}

