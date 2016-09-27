package com.javic.pokewhere;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.games.Player;
import com.javic.pokewhere.fragments.FragmentMap;
import com.javic.pokewhere.fragments.FragmentTransfer;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.services.ServiceFloatingMap;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Data.PlayerDataOuterClass;
import okhttp3.OkHttpClient;

public class ActivityDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentCreatedViewListener {

    private static final String TAG = ActivityDashboard.class.getSimpleName();

    private static final int MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private FragmentMap mFragmentMap;
    private FragmentTransfer mFragmentTransfer;

    private Bundle mExtras;

    public static NavigationView mNavigationView;
    public static DrawerLayout mDrawerLayout;
    private View mHeaderView;

    // API PokemonGO
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo mGO;

    //Variables
    String mUserName="";
    int mUserTeam=0;
    int mUserLevel =0;
    long mUserExperience =0;

    // Activity UI
    private View mView;
    private View mProgressView;
    private View mContainerFormView;
    private TextView mNavHeaderTitle, mNavHeaderSubtitle;
    private ImageView mNavHeaderImage;
    private Snackbar mSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get elemtns of UI
        mView = findViewById(R.id.layout_main_content);
        mContainerFormView = mView.findViewById(R.id.container_form);
        mProgressView = mView.findViewById(R.id.login_progress);

        //Navigation View
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        //HeaderView of Navigation View
        mHeaderView = mNavigationView.getHeaderView(0);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavHeaderTitle = (TextView) mHeaderView.findViewById(R.id.nav_header_title);
        mNavHeaderSubtitle = (TextView) mHeaderView.findViewById(R.id.nav_header_subtitle);
        mNavHeaderImage = (ImageView) mHeaderView.findViewById(R.id.nav_header_image);

        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGO == null) {
            connectWithPokemonGO();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Showing the progress
        showProgress(true);

        if (id == R.id.nav_camera) {
            // Handle the camera action
            setFragment(0);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {
            // Handle the camera action
            setFragment(2);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            deleteCredentials();
            startActivity(new Intent(ActivityDashboard.this, ActivitySelectAccount.class));
            finish();
        } else if (id == R.id.nav_send) {

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFragment(int position) {

        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (position) {
            case 0:

                if (mGO != null) {
                    mFragmentMap = FragmentMap.newInstance(mGO);
                    fragmentTransaction.replace(R.id.content_fragment, mFragmentMap);
                    fragmentTransaction.commit();
                }

                break;
            case 2:
                if (mGO != null) {
                    mFragmentTransfer = mFragmentTransfer.newInstance(mGO);
                    fragmentTransaction.replace(R.id.content_fragment, mFragmentTransfer);
                    fragmentTransaction.commit();
                }
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
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {

            final boolean canShow = showMapHead();
            if (!canShow) {
                Log.w(TAG, "Permiso Denegado");
            }
        }

        if (requestCode == Constants.REQUEST_CODE_ACTIVITY_FILTROS && resultCode == RESULT_OK) {
            mFragmentMap.onActivityResult(requestCode, resultCode, data);
        }


    }


    public void connectWithPokemonGO() {
        if (isDeviceOnline()) {

            showProgress(true);

            new Thread(new Runnable() {
                public void run() {
                    try {

                        if (!getPref(Constants.KEY_PREF_REFRESH_TOKEN).equalsIgnoreCase("")) {
                            //User is logged in with Google Account
                            mGO = new PokemonGo(new GoogleUserCredentialProvider(httpClient, getPref(Constants.KEY_PREF_REFRESH_TOKEN)), httpClient);

                        } else {
                            //Error
                            //User is logged in with username and password
                             mGO = new PokemonGo(new PtcCredentialProvider(httpClient, getPref(Constants.KEY_PREF_USER_EMAIL), getPref(Constants.KEY_PREF_USER_PASS)), httpClient);
                        }


                        if (mGO!=null){

                            final PlayerDataOuterClass.PlayerData playerData = mGO.getPlayerProfile().getPlayerData();
                            final Stats stats = mGO.getPlayerProfile().getStats();

                            mUserName = playerData.getUsername();
                            mUserTeam = playerData.getTeamValue();
                            mUserLevel = stats.getLevel();
                            mUserExperience = stats.getExperience();
                        }


                        runOnUiThread(new Runnable() {
                            public void run() {

                                if (mGO != null) {
                                    //First start (Inbox Fragment)

                                    mNavHeaderTitle.setText(mUserName);

                                    mNavHeaderSubtitle.setText("Nivel: " + String.valueOf(mUserLevel)+ " Experience: " + mUserExperience);

                                    switch (mUserTeam) {
                                        case 1:
                                            mNavHeaderImage.setImageResource(R.drawable.ic_team_yellow);
                                            break;
                                        case 2:
                                            mNavHeaderImage.setImageResource(R.drawable.ic_team_blue);
                                            break;
                                        case 3:
                                            mNavHeaderImage.setImageResource(R.drawable.ic_team_red);
                                            break;
                                        default:
                                            mNavHeaderImage.setImageResource(R.drawable.ic_gym_team_white);
                                            break;
                                    }

                                    setFragment(0);
                                    mNavigationView.getMenu().getItem(0).setChecked(true);
                                } else {
                                    showSnackBar("No pudimos conectar con Pokemon GO", "Reintentar");
                                }
                            }
                        });

                    } catch (LoginFailedException | RemoteServerException e) {
                        e.printStackTrace();

                        showSnackBar("No pudimos conectar con Pokemon GO", "Reintentar");

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               showProgress(false);
                           }
                       });
                    }
                }
            }).start();

        } else {
            showSnackBar("No hay conexión a Internet", "Ir a Configuraciones");
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContainerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mContainerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContainerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
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

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    public void showSnackBar(String snacKMessage, final String buttonTitle) {

        mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(buttonTitle, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {

                        if (buttonTitle.equalsIgnoreCase("Reintentar")) {
                            connectWithPokemonGO();
                        } else {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                            startActivity(intent);
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

        editor.putString(Constants.KEY_PREF_USER_EMAIL, "");
        editor.putString(Constants.KEY_PREF_USER_PASS, "");
        editor.putString(Constants.KEY_PREF_REFRESH_TOKEN, "");

        editor.commit();
    }

    @Override
    public void onFragmentCreatedViewStatus(Boolean status) {
        if (status) {
            showProgress(false);
        }
    }

    @Override
    public void onFragmentActionPerform(int action) {
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
            case Constants.ACTION_REFRESH:
                connectWithPokemonGO();

                break;
            default:
                break;
        }
    }
}
