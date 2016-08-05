package com.javic.pokewhere;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;

import okhttp3.OkHttpClient;

import static android.Manifest.permission.READ_CONTACTS;

public class ActivitySelectAccount extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ActivitySelectAccount.class.getSimpleName();

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_AUTHORIZATION = 2;

    static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    //https://accounts.google.com/o/oauth2/auth?client_id=848232511240-73ri3t7plvk96pj4f85uj8otdat2alem.apps.googleusercontent.com&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&response_type=code&scope=openid%20email%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email
    // https://accounts.google.com/o/oauth2/auth?client_id=848232511240-73ri3t7plvk96pj4f85uj8otdat2alem.apps.googleusercontent.com
    private View mProgressView;
    private View mLoginFormView;
    private String mEmail;
    private String mUserName;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private GoogleTokenTask mGTokenTask= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account);
        // Set up the login form.
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        final Button mEmailLogInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailLogInButton.setOnClickListener(this);

        final Button mGoogleLogInButton = (Button)findViewById(R.id.google_sign_in_button);
        mGoogleLogInButton.setOnClickListener(this);

    }

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {

                if (mGTokenTask == null) {
                    showProgress(true);
                    mGTokenTask = new GoogleTokenTask(ActivitySelectAccount.this, mEmail, SCOPE);
                    mGTokenTask.execute();
                }

            } else {
                Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
            }
        }
        // Handle the result from exceptions
    }


    /**
     * Represents an asynchronous Gets an authentication token from Google
     */
    public class GoogleTokenTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mScope;
        private final Activity mActivity;

        GoogleTokenTask(Activity activity, String email, String scope) {
            mEmail = email;
            mScope = scope;
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String token = null;

            try {

                token = GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                Log.i(TAG, userRecoverableException.toString());
                startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
            } catch (GoogleAuthException fatalException) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                Log.i(TAG, fatalException.toString());
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }

            if (token==null){
                return false;
            }else{

                final OkHttpClient httpClient = new OkHttpClient();

                try {
                    final GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient);

                    provider.login(token);

                    saveRefreshToken(provider.getRefreshToken());

                    PokemonGo go = new PokemonGo(provider, httpClient);

                    mUserName = go.getPlayerProfile().getPlayerData().getUsername();

                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                    return false;
                }


                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGTokenTask = null;
            showProgress(false);

            if (success) {

                Intent intent = new Intent(ActivitySelectAccount.this, ActivityMain.class);
                intent.putExtra(Constants.EXTRA_USERNAME_KEY, mUserName);
                startActivity(intent);
            } else {
                showMessage(getString(R.string.message_json_request_error));
            }
        }

        @Override
        protected void onCancelled() {
            mGTokenTask = null;
            showProgress(false);
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public void saveRefreshToken(String refreshToken){

        SharedPreferences prefs_user = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs_user.edit();

        editor.putString(Constants.KEY_PREF_REFRESH_TOKEN, refreshToken);

        editor.commit();

    }

    public boolean isDeviceOnline() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

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

    public void showMessage(String message) {

        Snackbar.make(mLoginFormView, R.string.message_json_request_error, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.action_snack_intentarlo_ahora), new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {

                        getUsername();

                    }
                }).show();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.email_sign_in_button:
                startActivity(new Intent(this, ActivityLogin.class));
            break;

            case R.id.google_sign_in_button:
                getUsername();
                break;
        }

    }
}
