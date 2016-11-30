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
import com.javic.pokewhere.models.ChildItem;
import com.javic.pokewhere.models.GroupItem;
import com.javic.pokewhere.models.ItemToDelete;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.ProgressTransferPokemon;
import com.javic.pokewhere.services.ServiceFloatingMap;
import com.javic.pokewhere.util.Constants;
import com.javic.pokewhere.util.PrefManager;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Item;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import POGOProtos.Data.PlayerDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;
import okhttp3.OkHttpClient;

import static android.os.Build.VERSION_CODES.M;
import static com.javic.pokewhere.fragments.FragmentCompare.localUserPokemon;
import static com.javic.pokewhere.util.Constants.ACTION_REFRESH_USER_DATA;

public class ActivityDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentListener {


    private static final String TAG = ActivityDashboard.class.getSimpleName();

    private static final int MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    //Instance fragment's
    private FragmentMapa mFragmentMapa;
    private FragmentBag mFragmentBag;
    private FragmentPokemonBank mFragmentPokemonBank;
    private FragmentCompare mFragmentCompare;
    private int visibleFragment = Constants.FRAGMENT_BLANK;

    // API PokemonGO
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo mGO;

    //Variables
    private String mUserName = "";
    private int mUserTeam = 0;
    private int mUserLevel = 0;
    private long mUserExperience = 0;
    private long mUserNextLevelXP = 0;
    private long mUserStardust = 0;
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
    private boolean taskUpdateUserPokemonWasCanceled = false;
    private boolean taskDeleteItemsWasCancelled = false;

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
    private TransferPokemonsTask mTransferPokemonsTask;
    private SetFavoriteTask mSetFavoriteTask;
    private DeleteItemsTask mDeleteItemsTask;
    private RefreshUserDataTask mRefreshUserDataTask;


    //Listas
    private List<Pokemon> mUserPokemonList;
    private List<LocalUserPokemon> mLocalUserPokemonList;
    private List<LocalUserPokemon> specificPokemonList;
    private List<Item> mUserBagItemList;
    private List<GroupItem> mGroupItemList;

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
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (visibleFragment == Constants.FRAGMENT_MAPA) {
                    //mFragmentMapa.showCustomDialog();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                mRefreshUserDataTask = new RefreshUserDataTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mRefreshUserDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mRefreshUserDataTask.execute();
                }

                //Toast.makeText(this, "VISIBLE_FRAGMENT: " + String.valueOf(visibleFragment), Toast.LENGTH_SHORT).show();

                break;
        }
        return super.onOptionsItemSelected(item);
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

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            switch (visibleFragment) {
                case Constants.FRAGMENT_POKEBANK:
                    //Verificamos que no haya elementos seleccionados en la lista de pokémon
                    if (mFragmentPokemonBank != null) {
                        if (mFragmentPokemonBank.canFinish()) {
                            finish();
                        }
                    } else {
                        finish();
                    }
                    break;
                case Constants.FRAGMENT_COMPARE:
                    //Verificamos que no haya elementos seleccionados en la lista de pokémon
                    if (mFragmentCompare != null) {
                        if (mFragmentCompare.canFinish()) {
                            super.onBackPressed();
                        }
                    }
                    break;
                default:
                    if (mSnackBar != null) {
                        if (mSnackBar.isShown()) {
                            mSnackBar.dismiss();
                        }
                    }
                    super.onBackPressed();
                    break;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        mDrawerLayout.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_fragment_pokebank:
                // Handle the camera action
                setFragment(Constants.FRAGMENT_POKEBANK, null);
                break;

            case R.id.nav_fragment_bag:
                setFragment(Constants.FRAGMENT_BAG, null);
                break;

            case R.id.nav_fragment_map:
                setFragment(Constants.FRAGMENT_MAPA, null);
                break;

            case R.id.nav_sing_out:
                deleteCredentials();
                startActivity(new Intent(ActivityDashboard.this, ActivitySelectAccount.class));
                finish();
                break;
            case R.id.nav_contact:
                goToAppDetail();
                break;
        }

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
            case Constants.FRAGMENT_POKEBANK:
                if (mFragmentPokemonBank == null) {
                    //mFragmentPokemon = mFragmentPokemon.newInstance(mGO);
                    mFragmentPokemonBank = FragmentPokemonBank.newInstance(getLocalUserpokemonList(), mUserPokeBankSpace);
                }
                if (visibleFragment != Constants.FRAGMENT_POKEBANK) {
                    replaceFragment(mFragmentPokemonBank);
                }
                break;

            case Constants.FRAGMENT_COMPARE:
                mFragmentCompare = FragmentCompare.newInstance(getLocalSpecificPokemonList(((LocalUserPokemon) object).getName()));
                mFragmentCompare.setTargetFragment(mFragmentPokemonBank, Constants.FRAGMENT_POKEBANK);
                replaceFragment(mFragmentCompare);
                break;

            case Constants.FRAGMENT_BAG:

                if (mFragmentBag == null) {
                    mFragmentBag = FragmentBag.newInstance(getLocalItems(), mUserBagSpace);
                }

                if (visibleFragment != Constants.FRAGMENT_BAG) {
                    replaceFragment(mFragmentBag);
                }

                break;

            case Constants.FRAGMENT_MAPA:
                if (mFragmentMapa == null) {
                    mFragmentMapa = FragmentMapa.newInstance(mGO);
                }
                if (visibleFragment != Constants.FRAGMENT_MAPA) {
                    replaceFragment(mFragmentMapa);
                }
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        FragmentTransaction ft;

        if (!fragmentPopped) { //fragment not in back stack, create it.

            showProgressView(true);

            Log.i(TAG, "fragment not in back stack, create it");
            ft = manager.beginTransaction();
            ft.add(R.id.content_fragment, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    private void updateNavigationView(Fragment fragment) {
        String fragClassName = fragment.getClass().getName();

        switch (fragClassName) {
            case Constants.PACKAGE_FRAGMENT_NAME + "FragmentPokemonBank":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                visibleFragment = Constants.FRAGMENT_POKEBANK;
                break;
            case Constants.PACKAGE_FRAGMENT_NAME + "FragmentCompare":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                visibleFragment = Constants.FRAGMENT_COMPARE;
                break;
            case Constants.PACKAGE_FRAGMENT_NAME + "FragmentBag":
                mNavigationView.getMenu().getItem(Constants.FRAGMENT_BAG).setChecked(true);
                visibleFragment = Constants.FRAGMENT_BAG;
                break;
            case Constants.PACKAGE_FRAGMENT_NAME + "FragmentMapa":
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

                /*switch (visibleFragment){
                    case Constants.FRAGMENT_POKEBANK:
                        mFragmentPokemonBank.onTaskFinish(-1, null, getLocalUserpokemonList());
                        break;
                    case Constants.FRAGMENT_COMPARE:
                        LocalUserPokemon localPokemon = data.getExtras().getParcelable("pokemon");

                        mFragmentCompare.onTaskFinish(Constants.ACTION_SET_FAVORITE_POKEMON, localPokemon, getLocalSpecificPokemonList(localPokemon.getName()));

                        if (mFragmentPokemonBank != null) {
                            mFragmentPokemonBank.onTaskFinish(Constants.ACTION_SET_FAVORITE_POKEMON, localPokemon, getLocalUserpokemonList());
                        }

                        break;
                }*/
                mUserLevel = data.getExtras().getInt("level");
                mUserExperience = data.getExtras().getLong("expirience");
                mUserNextLevelXP = data.getExtras().getLong("nextLevelXp");
                mUserStardust = data.getExtras().getLong("stardust");
                setUpHeaderNavigationView();

                mRefreshUserDataTask = new RefreshUserDataTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mRefreshUserDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mRefreshUserDataTask.execute();
                }

            }
        } else {
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

    public void showSnackBar(String snacKMessage, final String buttonTitle, final int task, final Object obj) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(M)
                    public void onClick(View v) {
                        if (buttonTitle.equalsIgnoreCase(getString(R.string.snack_bar_error_with_pokemon_positive_btn))) {

                            switch (task) {
                                case Constants.ACTION_CONNECT_WITH_PG:
                                    mConnectTask = new ConnectWithPokemonGoTask();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        mConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        mConnectTask.execute();
                                    }
                                    break;
                                case Constants.ACTION_TRANSFER_POKEMON:

                                    final List<LocalUserPokemon> list = (List<LocalUserPokemon>) obj;

                                    new MaterialDialog.Builder(ActivityDashboard.this)
                                            .title(getString(R.string.dialog_title_multiple_transfer) + " " +  String.valueOf(list.size()) +" " + getString(R.string.text_pokemones) +"?")
                                            .content(getString(R.string.dialog_content_multiple_transfer_1))
                                            .positiveText(R.string.dialog_positive_btn_powerup)
                                            .negativeText(R.string.dialog_negative_btn_powerup)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    if (visibleFragment == Constants.FRAGMENT_COMPARE) {
                                                        mTransferPokemonsTask = new TransferPokemonsTask(list, list.get(0));
                                                    } else {
                                                        mTransferPokemonsTask = new TransferPokemonsTask(list, null);
                                                    }

                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                        mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    } else {
                                                        mTransferPokemonsTask.execute();
                                                    }
                                                }
                                            })
                                            .show();

                                    if (visibleFragment == Constants.FRAGMENT_COMPARE) {

                                        mTransferPokemonsTask = new TransferPokemonsTask(list, list.get(0));
                                    } else {
                                        mTransferPokemonsTask = new TransferPokemonsTask(list, null);
                                    }


                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        mTransferPokemonsTask.execute();
                                    }

                                    break;
                                case ACTION_REFRESH_USER_DATA:
                                    mRefreshUserDataTask = new RefreshUserDataTask();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        mRefreshUserDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        mRefreshUserDataTask.execute();
                                    }
                                    break;
                                case Constants.ACTION_DELETE_ITEMS:
                                    break;
                            }
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

            case Constants.ACTION_SET_FAVORITE_POKEMON:

                mSetFavoriteTask = new SetFavoriteTask((LocalUserPokemon) object);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mSetFavoriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mSetFavoriteTask.execute();
                }
                break;

            case Constants.ACTION_TRANSFER_POKEMON:
                final List<LocalUserPokemon> list = (List<LocalUserPokemon>) object;


                new MaterialDialog.Builder(ActivityDashboard.this)
                        .title(getString(R.string.dialog_title_multiple_transfer) + " " +  String.valueOf(list.size()) +" " + getString(R.string.text_pokemones) +"?")
                        .content(getString(R.string.dialog_content_multiple_transfer_1))
                        .positiveText(R.string.dialog_positive_btn_powerup)
                        .negativeText(R.string.dialog_negative_btn_powerup)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (visibleFragment == Constants.FRAGMENT_COMPARE) {
                                    mTransferPokemonsTask = new TransferPokemonsTask(list, list.get(0));
                                } else {
                                    mTransferPokemonsTask = new TransferPokemonsTask(list, null);
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    mTransferPokemonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    mTransferPokemonsTask.execute();
                                }
                            }
                        })
                        .show();

                break;

            case Constants.ACTION_VER_TODOS:

                setFragment(Constants.FRAGMENT_COMPARE, object);

                break;
            case Constants.ACTION_DELETE_ITEMS:

                ItemToDelete itemToDelete = (ItemToDelete) object;

                mDeleteItemsTask = new DeleteItemsTask(itemToDelete.getmChildItemId(), itemToDelete.getCount());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mDeleteItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mDeleteItemsTask.execute();
                }
                break;
            case Constants.ACTION_GO_TO_DETAIL:

                ActivityPokemonDetail.mGO = mGO;
                ActivityPokemonDetail.mUserPokemonList = mUserPokemonList;
                ActivityPokemonDetail.mLocalUserPokemonList = (List<LocalUserPokemon>) (((List<Object>) object).get(0));

                Intent i = new Intent(ActivityDashboard.this, ActivityPokemonDetail.class);
                i.putExtra("index", (Integer) (((List<Object>) object).get(1)));
                startActivityForResult(i, Constants.REQUEST_CODE_ACTIVITY_POKEMON_DETAIL);

                break;
            case ACTION_REFRESH_USER_DATA:
                mRefreshUserDataTask = new RefreshUserDataTask();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mRefreshUserDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mRefreshUserDataTask.execute();
                }
                break;
        }
    }

    /**
     * Represents an asynchronous connect with pokemon go servers
     * with a location.
     */
    public class ConnectWithPokemonGoTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "CONNECT_WITH_POKEMON_TASK: onPreExecute");
            super.onPreExecute();
            showProgressView(true);
            mUserPokemonList = new ArrayList<>();
            mUserBagItemList = new ArrayList<>();
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

                        //Actualizando lista de pokemon
                        //publishProgress(getString(R.string.message_update_pokemon));
                        mUserPokemonList = mGO.getInventories().getPokebank().getPokemons();

                        //Actualizando lista de objetos
                        //publishProgress(getString(R.string.message_update_objects));
                        mUserBagItemList = new ArrayList<>(mGO.getInventories().getItemBag().getItems());


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

                setUpHeaderNavigationView();

                switch (visibleFragment) {
                    case Constants.FRAGMENT_BLANK:
                        setFragment(Constants.FRAGMENT_POKEBANK, mUserPokemonList);
                        mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                        break;
                    case Constants.FRAGMENT_POKEBANK:

                        if (mFragmentPokemonBank!=null){
                            mFragmentPokemonBank.onTaskFinish(Constants.ACTION_CONNECT_WITH_PG, null, getLocalUserpokemonList());
                        }
                        else{
                            setFragment(Constants.FRAGMENT_POKEBANK, mUserPokemonList);
                            mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                        }
                        break;
                    case Constants.FRAGMENT_COMPARE:

                        if (mFragmentPokemonBank != null) {
                            mFragmentPokemonBank.onTaskFinish(Constants.ACTION_CONNECT_WITH_PG, null, getLocalUserpokemonList());
                        }

                        if (mFragmentCompare!=null){
                            mFragmentCompare.onTaskFinish(Constants.ACTION_CONNECT_WITH_PG, null, getLocalSpecificPokemonList(localUserPokemon.getName()));
                        }
                        else{
                            setFragment(Constants.FRAGMENT_POKEBANK, mUserPokemonList);
                            mNavigationView.getMenu().getItem(Constants.FRAGMENT_POKEBANK).setChecked(true);
                        }

                        break;
                    case Constants.FRAGMENT_BAG:
                        if (mFragmentBag!=null){
                            mFragmentBag.onTaskFinish(Constants.ACTION_DELETE_ITEMS, null, getLocalItems());
                        }
                        else{
                            setFragment(Constants.FRAGMENT_BAG, mUserBagItemList);
                            mNavigationView.getMenu().getItem(Constants.FRAGMENT_BAG).setChecked(true);
                        }
                        break;

                }

                if (prefmanager.isFirstTimeLaunch()) {
                    mDrawerLayout.openDrawer(mNavigationView);
                    prefmanager.setFirstTimeLaunch(false);
                }
            } else {

                setFragment(Constants.FRAGMENT_BLANK, null);

                if (visibleFragment != Constants.FRAGMENT_BLANK) {
                    mNavigationView.getMenu().getItem(visibleFragment).setChecked(false);
                }


                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), Constants.ACTION_CONNECT_WITH_PG, null);

                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), Constants.ACTION_CONNECT_WITH_PG, null);
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

    private void setUpHeaderNavigationView() {
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
    }

    public class TransferPokemonsTask extends AsyncTask<Void, ProgressTransferPokemon, Boolean> {

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        //Object sended to onProgressUpdate method
        private ProgressTransferPokemon progress;


        private List<LocalUserPokemon> mTransferablePokemonList;
        private LocalUserPokemon localUserPokemon;

        public TransferPokemonsTask(List<LocalUserPokemon> mTransferablePokemonList, LocalUserPokemon localUserPokemon) {
            this.mTransferablePokemonList = mTransferablePokemonList;
            this.localUserPokemon = localUserPokemon;
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
                                progress.setProgressMessage(getString(R.string.message_text_transfering) + " " + pokemonToTransfer.getPokemonId().toString());
                                progress.setUpdateProgress(false);
                                publishProgress(progress);
                                //pokemonToTransfer.debug();
                                ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result result = pokemonToTransfer.transferPokemon();
                                progress.setProgressMessage(result.toString());
                                progress.setUpdateProgress(true);
                                publishProgress(progress);
                                sleep(500);
                                mGO.getInventories().updateInventories(true);
                            }
                        } else {
                            Log.i(TAG, "TRANSFER_POKEMON_TASK: doInBackground: task is cancelled");
                            progress.setProgressMessage(getString(R.string.message_text_canceling));
                            progress.setUpdateProgress(false);
                            publishProgress(progress);
                            sleep(500);
                            mGO.getInventories().updateInventories(true);
                            return false;
                        }

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

                if (visibleFragment == Constants.FRAGMENT_POKEBANK) {
                    mFragmentPokemonBank.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalUserpokemonList());
                } else if (visibleFragment == Constants.FRAGMENT_COMPARE) {
                    mFragmentCompare.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalSpecificPokemonList(localUserPokemon.getName()));

                    if (mFragmentPokemonBank != null) {
                        mFragmentPokemonBank.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalUserpokemonList());
                    }
                }
            } else {

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), Constants.ACTION_TRANSFER_POKEMON, mTransferablePokemonList);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), Constants.ACTION_TRANSFER_POKEMON, mTransferablePokemonList);
                }

            }

            //Dismissing the dialog
            dialog.dismiss();
        }


        @Override
        protected void onCancelled() {
            Log.i(TAG, "TRANSFER_POKEMON_TASK: onCancelled");
            mTransferPokemonsTask = null;
            taskTransferPokemonWasCanceled = true;

            sleep(500);
            try {
                mGO.getInventories().updateInventories(true);
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
            mUserPokemonList = mGO.getInventories().getPokebank().getPokemons();

            if (mUserPokemonList.size() != 0) {
                if (visibleFragment == Constants.FRAGMENT_POKEBANK) {
                    mFragmentPokemonBank.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalUserpokemonList());
                } else if (visibleFragment == Constants.FRAGMENT_COMPARE) {
                    mFragmentCompare.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalSpecificPokemonList(localUserPokemon.getName()));

                    if (mFragmentPokemonBank != null) {
                        mFragmentPokemonBank.onTaskFinish(Constants.ACTION_TRANSFER_POKEMON, null, getLocalUserpokemonList());
                    }
                }
                dialog.dismiss();
            } else {
                dialog.dismiss();
                finish();
            }
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


            if (succes) {

                if (visibleFragment == Constants.FRAGMENT_POKEBANK) {
                    mFragmentPokemonBank.onTaskFinish(Constants.ACTION_SET_FAVORITE_POKEMON, localUserPokemon, getLocalUserpokemonList());
                } else if (visibleFragment == Constants.FRAGMENT_COMPARE) {
                    mFragmentCompare.onTaskFinish(Constants.ACTION_SET_FAVORITE_POKEMON, localUserPokemon, getLocalSpecificPokemonList(localUserPokemon.getName()));

                    if (mFragmentPokemonBank != null) {
                        mFragmentPokemonBank.onTaskFinish(Constants.ACTION_SET_FAVORITE_POKEMON, localUserPokemon, getLocalUserpokemonList());
                    }
                }

                if (!localUserPokemon.getFavorite()) {
                    Toast.makeText(ActivityDashboard.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.message_favorite_pokemon), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityDashboard.this, "¡" + localUserPokemon.getName() + " " + getString(R.string.message_unfavorite_pokemon), Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(ActivityDashboard.this, getString(R.string.message_un_power_up), Toast.LENGTH_SHORT).show();
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

    public class RefreshUserDataTask extends AsyncTask<Void, String, Boolean> {

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "UPDATE_USER_POKEMON_TASK: onPreExecute");

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

            Log.i(TAG, "UPDATE_USER_POKEMON_TASK: doInBackground:start");
            try {
                try {
                    mGO.getInventories().updateInventories(true);
                    mUserPokemonList = mGO.getInventories().getPokebank().getPokemons();
                    mUserBagItemList = new ArrayList<>(mGO.getInventories().getItemBag().getItems());

                    final Stats stats = mGO.getPlayerProfile().getStats();
                    mUserLevel = stats.getLevel();
                    mUserExperience = stats.getExperience();
                    mUserNextLevelXP = stats.getNextLevelXp();

                    Log.i(TAG, "UPDATE_USER_POKEMON_TASK: doInBackground: true");
                    return true;
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "UPDATE_USER_POKEMON_TASK: doInBackground: login or remote server exception");
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "UPDATE_USER_POKEMON_TASK: doInBackground: exception");
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "UPDATE_USER_POKEMON_TASK: onPostExecute");
            mRefreshUserDataTask = null;

            if (succes) {
                switch (visibleFragment) {
                    case Constants.FRAGMENT_POKEBANK:
                        mFragmentPokemonBank.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalUserpokemonList());

                        if (mFragmentBag!=null){
                            mFragmentBag.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalItems());
                        }
                        break;
                    case Constants.FRAGMENT_COMPARE:

                        mFragmentCompare.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalSpecificPokemonList(mFragmentCompare.localUserPokemon.getName()));

                        if (mFragmentPokemonBank != null) {
                            mFragmentPokemonBank.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalUserpokemonList());
                        }

                        if (mFragmentBag!=null){
                            mFragmentBag.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalItems());
                        }

                        break;
                    case Constants.FRAGMENT_BAG:

                        mFragmentBag.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalItems());

                        if (mFragmentPokemonBank != null) {
                            mFragmentPokemonBank.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalUserpokemonList());
                        }

                        if (mFragmentCompare!=null){
                            mFragmentCompare.onTaskFinish(ACTION_REFRESH_USER_DATA, visibleFragment, getLocalSpecificPokemonList(mFragmentCompare.localUserPokemon.getName()));
                        }

                        break;
                }

                setUpHeaderNavigationView();

            } else {

                showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), ACTION_REFRESH_USER_DATA, null);
            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "UPDATE_USER_POKEMON_TASK: onCancelled");
            mRefreshUserDataTask = null;
            taskUpdateUserPokemonWasCanceled = true;

            dialog.dismiss();

        }

    }

    public class DeleteItemsTask extends AsyncTask<Void, String, Boolean> {

        private ItemIdOuterClass.ItemId itemId;
        private int itemsToDelete;

        private MaterialDialog.Builder builder;
        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "DELETE_ITEMS_TASK: onPreExecute");

            builder = new MaterialDialog.Builder(ActivityDashboard.this)
                    .title(getString(R.string.dialog_title_delete_items))
                    .content(getString(R.string.dialog_content_please_wait))
                    .cancelable(false)
                    .negativeText(getString(R.string.location_alert_neg_btn))
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            if (mDeleteItemsTask != null) {
                                mDeleteItemsTask.cancel(true);
                            }
                        }
                    });
            dialog = builder.build();
            dialog.show();
        }

        public DeleteItemsTask(ItemIdOuterClass.ItemId itemId, int itemsToDelete) {
            this.itemId = itemId;
            this.itemsToDelete = itemsToDelete;

            Log.i(TAG, "DELETE_ITEMS_TASK: constructor");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground:start");

            boolean result = false;

            try {
                try {
                    if (!isCancelled()) {
                        mGO.getInventories().getItemBag().removeItem(itemId, itemsToDelete);
                        mGO.getInventories().updateInventories(true);
                        result = true;
                        Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: true");

                    } else {
                        Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: task is cancelled");
                        result = false;
                    }


                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: exception");
                    result = false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.i(TAG, "DELETE_ITEMS_TASK: doInBackground: exception");
                result = false;

            }

            mUserBagItemList = new ArrayList<>(mGO.getInventories().getItemBag().getItems());

            return result;

        }

        @Override
        protected void onPostExecute(Boolean succes) {
            Log.i(TAG, "DELETE_ITEMS_TASK: onPostExecute");
            mDeleteItemsTask = null;

            if (succes) {

                if (mFragmentBag != null) {
                    mFragmentBag.onTaskFinish(Constants.ACTION_DELETE_ITEMS, null, getLocalItems());

                    Toast.makeText(ActivityDashboard.this, "¡" + String.valueOf(itemsToDelete) + " " + itemId.toString() + " " + getString(R.string.message_items_deleted) + "!", Toast.LENGTH_SHORT).show();
                }


            } else {

                if (isDeviceOnline()) {
                    showSnackBar(getString(R.string.snack_bar_error_with_pokemon), getString(R.string.snack_bar_error_with_pokemon_positive_btn), Constants.ACTION_DELETE_ITEMS, mGroupItemList);
                } else {
                    showSnackBar(getString(R.string.snack_bar_error_with_internet_acces), getString(R.string.snack_bar_error_with_internet_acces_positive_btn), Constants.ACTION_DELETE_ITEMS, mGroupItemList);
                }

            }

            //Dismissing the dialog
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "DELETE_ITEMS_TASK: onCancelled");
            dialog.dismiss();
            mDeleteItemsTask = null;
            taskDeleteItemsWasCancelled = true;
        }

    }

    public List<LocalUserPokemon> getLocalUserpokemonList() {

        mLocalUserPokemonList = new ArrayList<>();

        for (Pokemon pokemon : mUserPokemonList) {
            LocalUserPokemon localUserPokemon = new LocalUserPokemon();
            localUserPokemon.setId(pokemon.getId());
            localUserPokemon.setName(pokemon.getPokemonId().name());
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
            localUserPokemon.setPokemonCount(mGO.getInventories().getPokebank().getPokemonByPokemonId(pokemon.getPokemonId()).size());
            mLocalUserPokemonList.add(localUserPokemon);
        }

        return mLocalUserPokemonList;
    }

    private List<LocalUserPokemon> getLocalSpecificPokemonList(String name) {
        specificPokemonList = new ArrayList<>();

        for (Pokemon specificPokemon : mUserPokemonList) {
            if (specificPokemon.getPokemonId().name().equals(name)) {

                LocalUserPokemon localUserPokemon = new LocalUserPokemon();
                localUserPokemon.setId(specificPokemon.getId());
                localUserPokemon.setName(specificPokemon.getPokemonId().name());
                localUserPokemon.setNickname(specificPokemon.getNickname());
                localUserPokemon.setBitmap(getBitmapFromAssets(specificPokemon.getPokemonId().getNumber()));
                localUserPokemon.setNumber(specificPokemon.getPokemonId().getNumber());
                localUserPokemon.setFavorite(specificPokemon.isFavorite());
                localUserPokemon.setDead(specificPokemon.isInjured());
                localUserPokemon.setCp(specificPokemon.getCp());
                localUserPokemon.setIv(((int) (specificPokemon.getIvRatio() * 100)));
                localUserPokemon.setAttack(specificPokemon.getIndividualAttack());
                localUserPokemon.setDefense(specificPokemon.getIndividualDefense());
                localUserPokemon.setStamina(specificPokemon.getIndividualStamina());
                localUserPokemon.setMaxCp(specificPokemon.getMaxCpFullEvolveAndPowerupForPlayer());
                localUserPokemon.setEvolveCP(specificPokemon.getCpAfterEvolve());

                localUserPokemon.setEvolveCP(specificPokemon.getCpAfterEvolve());


                localUserPokemon.setLevel(specificPokemon.getLevel());
                localUserPokemon.setCandies(specificPokemon.getCandy());
                localUserPokemon.setPowerUpStardust(specificPokemon.getStardustCostsForPowerup());
                localUserPokemon.setPoweUpCandies(specificPokemon.getCandyCostsForPowerup());
                localUserPokemon.setEvolveCandies(specificPokemon.getCandiesToEvolve());
                localUserPokemon.setCreationTimeMillis(specificPokemon.getCreationTimeMs());
                localUserPokemon.setPokemonCount(mGO.getInventories().getPokebank().getPokemonByPokemonId(specificPokemon.getPokemonId()).size());
                specificPokemonList.add(localUserPokemon);
            }
        }

        return specificPokemonList;
    }

    private List<GroupItem> getLocalItems() {

        mGroupItemList = new ArrayList<>();

        String title_group = "";

        final List<ChildItem> pokeBallTypeList = new ArrayList<>();
        final List<ChildItem> incenseTypeList = new ArrayList<>();
        final List<ChildItem> potionTypeList = new ArrayList<>();
        final List<ChildItem> troyDiskTypeList = new ArrayList<>();
        final List<ChildItem> eggTypeList = new ArrayList<>();
        final List<ChildItem> reviveTypeList = new ArrayList<>();
        final List<ChildItem> berryTypeList = new ArrayList<>();
        final List<ChildItem> incubatorTypeList = new ArrayList<>();

        for (Item item : mUserBagItemList) {

            //POKEBALLS
            if (item.getItemId().toString().contains("BALL")) {
                title_group = getString(R.string.group_pokeballs);

                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String ball_type = full_name[1];

                    if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                        if (ball_type.equalsIgnoreCase("POKE")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("GREAT")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("ULTRA")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("MASTER")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_master_ball, ball_type + " " + full_name[2], item.getCount()));
                        }

                    }

                    mGroupItemList.add(new GroupItem(title_group, pokeBallTypeList, R.drawable.ic_pokeballs));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String ball_type = full_name[1];

                    if (!containsEncounteredChildItem(ball_type, pokeBallTypeList)) {
                        if (ball_type.equalsIgnoreCase("POKE")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_poke_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("GREAT")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_great_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("ULTRA")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_ultra_ball, ball_type + " " + full_name[2], item.getCount()));
                        } else if (ball_type.equalsIgnoreCase("MASTER")) {
                            pokeBallTypeList.add(new ChildItem(R.drawable.ic_master_ball, ball_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_pokeballs), pokeBallTypeList, R.drawable.ic_pokeballs));
                }
            }

            //INCENSES
            else if (item.getItemId().toString().contains("INCENSE")) {
                title_group = getString(R.string.group_incenses);

                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String incense_type = full_name[2];

                    if (!containsEncounteredChildItem(incense_type, incenseTypeList)) {

                        if (incense_type.equalsIgnoreCase("ORDINARY")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("COOL")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("FLORAL")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("SPICY")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, incenseTypeList, R.drawable.ic_incenses));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String incense_type = full_name[2];

                    if (!containsEncounteredChildItem(incense_type, incenseTypeList)) {
                        if (incense_type.equalsIgnoreCase("ORDINARY")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("COOL")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("FLORAL")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        } else if (incense_type.equalsIgnoreCase("SPICY")) {
                            incenseTypeList.add(new ChildItem(R.drawable.ic_incense_ordinary, incense_type + " " + full_name[1], item.getCount()));
                        }
                    }

                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_incenses), incenseTypeList, R.drawable.ic_incenses));
                }
            }

            //POTIONS
            else if (item.getItemId().toString().contains("POTION")) {
                title_group = getString(R.string.group_potions);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String potion_type = full_name[1];

                    if (!containsEncounteredChildItem(potion_type, potionTypeList)) {
                        if (potion_type.equalsIgnoreCase("POTION")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type, item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("SUPER")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type + " " + full_name[2], item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("HYPER")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type + " " + full_name[2], item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("MAX")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, potionTypeList, R.drawable.ic_potions));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String potion_type = full_name[1];

                    if (!containsEncounteredChildItem(potion_type, potionTypeList)) {
                        if (potion_type.equalsIgnoreCase("POTION")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_potion, potion_type, item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("SUPER")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_great_potion, potion_type + " " + full_name[2], item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("HYPER")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_hyper_potion, potion_type + " " + full_name[2], item.getCount()));
                        } else if (potion_type.equalsIgnoreCase("MAX")) {
                            potionTypeList.add(new ChildItem(R.drawable.ic_potions, potion_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_potions), potionTypeList, R.drawable.ic_potions));
                }
            }

            //BAITS
            else if (item.getItemId().toString().contains("DISK")) {
                title_group = getString(R.string.group_troydisks);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String disk_type = full_name[1];

                    if (!containsEncounteredChildItem(disk_type, troyDiskTypeList)) {
                        if (disk_type.equalsIgnoreCase("TROY")) {
                            troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, troyDiskTypeList, R.drawable.ic_baits));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String disk_type = full_name[1];

                    if (!containsEncounteredChildItem(disk_type, troyDiskTypeList)) {
                        if (disk_type.equalsIgnoreCase("TROY")) {
                            troyDiskTypeList.add(new ChildItem(R.drawable.ic_bait, disk_type + " " + full_name[2], item.getCount()));
                        }
                    }
                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_troydisks), troyDiskTypeList, R.drawable.ic_baits));
                }
            }

            //EGGS
            else if (item.getItemId().toString().contains("EGG")) {
                title_group = getString(R.string.group_eggs);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String egg_type = full_name[1];

                    if (!containsEncounteredChildItem(egg_type, eggTypeList)) {
                        if (egg_type.equalsIgnoreCase("LUCKY")) {
                            eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, eggTypeList, R.drawable.ic_eggs));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String egg_type = full_name[1];

                    if (!containsEncounteredChildItem(egg_type, eggTypeList)) {
                        if (egg_type.equalsIgnoreCase("LUCKY")) {
                            eggTypeList.add(new ChildItem(R.drawable.ic_egg, egg_type + " " + full_name[2], item.getCount()));
                        }
                    }
                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_eggs), eggTypeList, R.drawable.ic_eggs));
                }
            }
            //INCUBATORS
            else if (item.getItemId().toString().contains("INCUBATOR")) {
                title_group = getString(R.string.group_incubators);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String incubator_type = full_name[2];

                    if (!containsEncounteredChildItem(incubator_type, incubatorTypeList)) {

                        if (incubator_type.equalsIgnoreCase("BASIC")) {
                            try {
                                incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, full_name[3] + " " + full_name[1], item.getCount()));
                            } catch (Exception e) {
                                incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type + " " + full_name[1], item.getCount()));
                            }

                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, incubatorTypeList, R.drawable.ic_incubators));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String incubator_type = full_name[1];

                    if (!containsEncounteredChildItem(incubator_type, incubatorTypeList)) {
                        if (incubator_type.equalsIgnoreCase("BASIC")) {
                            try {
                                incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, incubator_type + " " + full_name[3] + " " + full_name[1], item.getCount()));
                            } catch (Exception e) {

                                incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_incubator, incubator_type + " " + full_name[1], item.getCount()));
                                incubatorTypeList.add(new ChildItem(R.drawable.ic_basic_unlimited_incubator, "UNLIMITED" + " " + full_name[1], 1));
                            }

                        }
                    }
                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_incubators), incubatorTypeList, R.drawable.ic_incubators));
                }
            }


            //REVIVES
            else if (item.getItemId().toString().contains("REVIVE")) {
                title_group = getString(R.string.group_revives);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String revive_type = full_name[1];

                    if (!containsEncounteredChildItem(revive_type, reviveTypeList)) {
                        if (revive_type.equalsIgnoreCase("REVIVE")) {
                            reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type, item.getCount()));
                        } else if (revive_type.equalsIgnoreCase("MAX")) {
                            reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, reviveTypeList, R.drawable.ic_crystal));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String revive_type = full_name[1];

                    if (!containsEncounteredChildItem(revive_type, reviveTypeList)) {
                        if (revive_type.equalsIgnoreCase("REVIVE")) {
                            reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type, item.getCount()));
                        } else if (revive_type.equalsIgnoreCase("MAX")) {
                            reviveTypeList.add(new ChildItem(R.drawable.ic_crystal, revive_type + full_name[2], item.getCount()));
                        }
                    }
                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_revives), reviveTypeList, R.drawable.ic_crystal));
                }
            }

            //BERRIES
            else if (item.getItemId().toString().contains("BERRY")) {
                title_group = getString(R.string.group_berries);
                if (!containsEncounteredGroupItem(title_group)) {

                    String[] full_name = item.getItemId().toString().split("_");
                    String berry_type = full_name[1];

                    if (!containsEncounteredChildItem(berry_type, berryTypeList)) {
                        if (berry_type.equalsIgnoreCase("RAZZ")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("BLUK")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("NANAB")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("PINAP")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("WEPAR")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        }
                    }

                    mGroupItemList.add(new GroupItem(title_group, berryTypeList, R.drawable.ic_berries));

                } else {
                    String[] full_name = item.getItemId().toString().split("_");
                    String berry_type = full_name[1];

                    if (!containsEncounteredChildItem(berry_type, berryTypeList)) {
                        if (berry_type.equalsIgnoreCase("RAZZ")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("BLUK")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("NANAB")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("PINAP")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        } else if (berry_type.equalsIgnoreCase("WEPAR")) {
                            berryTypeList.add(new ChildItem(R.drawable.ic_berrie, berry_type + " " + full_name[2], item.getCount()));
                        }
                    }
                    mGroupItemList.set(mGroupItemList.indexOf(getGroupItem(title_group)), new GroupItem(getString(R.string.group_berries), berryTypeList, R.drawable.ic_berries));
                }
            }

        }//Termina el for

        // Sorting
        Collections.sort(mGroupItemList, new Comparator<GroupItem>() {

            @Override
            public int compare(GroupItem groupItem1, GroupItem groupItem2) {

                return groupItem1.getTitle().compareTo(groupItem2.getTitle());
            }
        });

        return mGroupItemList;
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

    public GroupItem getGroupItem(String titleGroup) {

        for (GroupItem groupItem : mGroupItemList) {
            if (String.valueOf(groupItem.getTitle()).equalsIgnoreCase(String.valueOf(titleGroup))) {
                return groupItem;
            }
        }
        return null;
    }

    public boolean containsEncounteredGroupItem(String enconunteredItemId) {

        for (GroupItem groupItem : mGroupItemList) {
            if (groupItem.getTitle().equalsIgnoreCase(enconunteredItemId)) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }

    public boolean containsEncounteredChildItem(String enconunteredItemId, List<ChildItem> specificItemList) {

        for (ChildItem childItem : specificItemList) {
            if (childItem.getTitle().equalsIgnoreCase(enconunteredItemId)) {
                return true;
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }

}

