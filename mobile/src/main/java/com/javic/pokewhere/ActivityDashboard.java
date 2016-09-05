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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.javic.pokewhere.fragments.FragmentMap;
import com.javic.pokewhere.services.ServiceFloatingMap;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import okhttp3.OkHttpClient;

public class ActivityDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentCreatedViewListener{

    private static final String TAG = ActivityDashboard.class.getSimpleName();
    private static final int MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    private FragmentMap mFragmentMap;
    private Bundle mExtras;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String mUserName;
    private String mUserTeam;
    private String mUserLevel;

    private NavigationView mNavigationView;
    public static DrawerLayout mDrawerLayout;
    private View mHeaderView;

    // API PokemonGO
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo mGO;


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

        //Config UI
        mUserName = getPref(Constants.KEY_PREF_USER_NAME_KEY);
        mUserTeam = getPref(Constants.KEY_PREF_USER_TEAM_KEY);
        mUserLevel = getPref(Constants.KEY_PREF_USER_LEVEL_KEY);


        mNavHeaderTitle.setText(mUserName);
        mNavHeaderSubtitle.setText("Nivel: " + String.valueOf(mUserLevel));

        switch (Integer.parseInt(mUserTeam)) {
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

        mNavigationView.setNavigationItemSelectedListener(this);

        connectWithPokemonGO();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_service) {

            final boolean canShow = showMapHead();
            if (!canShow) {
                // 広告トリガーのFloatingViewの表示許可設定
                @SuppressLint("InlinedApi")
                final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ActivityDashboard.this.getPackageName()));
                startActivityForResult(intent, MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
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

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            deleteCredentials();
            startActivity(new Intent(ActivityDashboard.this, ActivitySelectAccount.class));
            finish();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;

        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();

                if (mGO!=null){
                    mFragmentMap = FragmentMap.newInstance(mGO);
                    fragmentTransaction.replace(R.id.content_fragment, mFragmentMap);
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
    }


    public void connectWithPokemonGO() {
        if (isDeviceOnline()){

            showProgress(true);

            new Thread(new Runnable() {
                public void run() {
                    try {

                        if (!getPref(Constants.KEY_PREF_REFRESH_TOKEN).equalsIgnoreCase("")){
                            //User is logged in with Google Account
                            mGO = new PokemonGo(new GoogleUserCredentialProvider(httpClient, getPref(Constants.KEY_PREF_REFRESH_TOKEN)), httpClient);

                        }
                        else{
                            //User is logged in with username and password
                            mGO = new PokemonGo(new PtcCredentialProvider(httpClient, getPref(Constants.KEY_PREF_USER_EMAIL), getPref(Constants.KEY_PREF_USER_PASS)), httpClient);
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {

                                if (mGO!=null){
                                    //First start (Inbox Fragment)
                                    setFragment(0);
                                    mNavigationView.getMenu().getItem(0).setChecked(true);
                                }
                                else{
                                    showSnackBar("No pudimos conectar con Pokemon GO", "Reintentar");
                                }
                            }
                        });

                    } catch (LoginFailedException | RemoteServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        else{
            showSnackBar("No hay conexión a Internet", "Conectar");
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

    public void showSnackBar(String snacKMessage, String buttonTitle){

            mSnackBar = Snackbar.make(mView, snacKMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(buttonTitle, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {

                        }
                    });

    }

    private String getPref(String KEY_PREF) {

        SharedPreferences prefsPokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);

        if(KEY_PREF.equalsIgnoreCase(Constants.KEY_PREF_USER_TEAM_KEY) || KEY_PREF.equalsIgnoreCase(Constants.KEY_PREF_USER_LEVEL_KEY)){
            String pref = String.valueOf(prefsPokeWhere.getInt(KEY_PREF, -1));
            return  pref;
        }else{
            String pref = prefsPokeWhere.getString(KEY_PREF, "");
            return  pref;

        }

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
        if (status){
            showProgress(false);
        }
    }
}
