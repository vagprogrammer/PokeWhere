package com.javic.pokewhere;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class ActivityDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentMap.OnFragmentInteractionListener {

    private static final String TAG = ActivityDashboard.class.getSimpleName();
    private static final int MAPHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    private FragmentMap mFragmentMap;
    private Bundle mExtras;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String mParamUser;
    private String mParamPass;
    private String mParamRefreshToken;
    private String mUserName;
    private String mUserTeam;
    private String mUserLevel;


    private TextView mNavHeaderTitle, mNavHeaderSubtitle;
    private ImageView mNavHeaderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get elemtns of UI
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        //View headerView = navigationView.inflateHeaderView(R.layout.layout_nav_header_activity_main);

        mNavHeaderTitle = (TextView) headerView.findViewById(R.id.nav_header_title);
        mNavHeaderSubtitle = (TextView) headerView.findViewById(R.id.nav_header_subtitle);
        mNavHeaderImage = (ImageView) headerView.findViewById(R.id.nav_header_image);

        mExtras = getIntent().getExtras();

        if (mExtras != null) {

            if (mExtras.getString(Constants.ARG_REFRESHTOKEN) == null) {
                mParamUser = mExtras.getString(Constants.ARG_USER);
                mParamPass = mExtras.getString(Constants.ARG_PASS);
            } else {
                mParamRefreshToken = mExtras.getString(Constants.ARG_REFRESHTOKEN);
            }
        }
        else{
            if (getPref(Constants.KEY_PREF_REFRESH_TOKEN).equalsIgnoreCase("")){
                mParamUser = getPref(Constants.KEY_PREF_USER_EMAIL);
                mParamPass = getPref(Constants.KEY_PREF_USER_PASS);

            }
            else{
                mParamRefreshToken = getPref(Constants.KEY_PREF_REFRESH_TOKEN);
            }
        }

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


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {

                //since the drawer might have opened as a results of
                //a click on the left menu, we need to make sure
                //to close it right after the drawer opens, so that
                //it is closed when the drawer is  closed.
                FragmentMap.mSearchView.setLeftMenuOpen(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                FragmentMap.mSearchView.setLeftMenuOpen(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);

        //First start (Inbox Fragment)
        setFragment(0);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void showMessage(String message) {

        Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

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

                if (mFragmentMap == null) {

                    if (mParamRefreshToken == null) {
                        mFragmentMap = FragmentMap.newInstance(mParamUser, mParamPass);
                    } else {
                        mFragmentMap = FragmentMap.newInstance(mParamRefreshToken);
                    }

                }

                fragmentTransaction.replace(R.id.content_fragment, mFragmentMap);
                fragmentTransaction.commit();

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

    @Override
    public void onFragmentInteraction(int action) {

        Log.i(TAG, String.valueOf(action));

        switch (action) {
            case Constants.ACTION_OPEN_DRAWER:

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (!drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                }

                break;
        }
    }

    private String getPref(String KEY_PREF) {

        SharedPreferences prefsPokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);

        if(KEY_PREF.equalsIgnoreCase(Constants.KEY_PREF_USER_TEAM_KEY)|| KEY_PREF.equalsIgnoreCase(Constants.KEY_PREF_USER_LEVEL_KEY)){
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
}
