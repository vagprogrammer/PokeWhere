package com.javic.pokewhere;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

import static android.Manifest.permission.READ_CONTACTS;

//https://security.google.com/settings/security/apppasswords
//Keep in mind, it requires 2 step verification to be enabled
/**
 * A login screen that offers login via email/password.
 */
public class ActivityLogin extends AppCompatActivity{

    private static final String TAG = ActivityLogin.class.getSimpleName();

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_PERMISSION_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask= null;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private ContactsTask mContactsTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mTVTitle;

    //Variables
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo mGO;
    private Boolean isGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();

        if (extras!= null){
            isGoogleAccount =  (extras.getString("login").equalsIgnoreCase("Google")) ? true : false;
        }

        // Set up the login form.
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mTVTitle = (TextView) findViewById(R.id.tv_login_title);

        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        final ImageButton mBackButton = (ImageButton) findViewById(R.id.back_button);

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id ==  EditorInfo.IME_ACTION_NEXT) {
                    if (!TextUtils.isEmpty(textView.getText().toString()))
                    {mPasswordView.requestFocus();}
                    return true;
                }
                return false;
            }
        });

        mEmailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() != 0){
                    mPasswordView.setEnabled(true);
                    mEmailSignInButton.setEnabled(true);

                }else {
                    mPasswordView.setEnabled(false);
                    mEmailSignInButton.setEnabled(false);

                }

            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id ==  EditorInfo.IME_ACTION_DONE) {

                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (isGoogleAccount){
            mTVTitle.setText(getString(R.string.google_account_credentials));
            mEmailView.setHint(getString(R.string.prompt_email));
        }
        else {
            mTVTitle.setText(getString(R.string.ptc_account));
            mEmailView.setHint(getString(R.string.prompt_nickname));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isGoogleAccount) {
            populateAutoComplete();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAuthTask!=null){
            Log.i(TAG, "USER_LOGIN_TASK: cancel:true");
            mAuthTask.cancel(true);
        }

        if(mContactsTask!=null){
            Log.i(TAG, "CONTACTS_TASK: cancel:true");
            mContactsTask.cancel(true);
        }
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        mContactsTask = new ContactsTask();
        mContactsTask.execute((Void) null);
    }

    private boolean mayRequestContacts() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mLoginFormView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_PERMISSION_READ_CONTACTS);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_PERMISSION_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        //HIde the Keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check if the user entered a password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }else if(isGoogleAccount){
            // Check for a valid password.
            if (!isPasswordValid(password)){
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
        }

        // Check for a empty textView.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (isGoogleAccount) {
            // Check for a valid email address.
            if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            if (isDeviceOnline()){
                showProgress(true);
                mAuthTask = new UserLoginTask(email, password);
                mAuthTask.execute((Void) null);
            }
            else {
                Toast.makeText(this, R.string.snack_bar_error_with_internet_acces, Toast.LENGTH_LONG).show();
            }

        }
    }

    private boolean isEmailValid(String email) {

        if (isGoogleAccount){
            return email.contains("@");
        }
        else{
            return !email.isEmpty();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {

            Log.i(TAG, "USER_LOGIN_TASK: constructor");

            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(TAG, "USER_LOGIN_TASK: doInBackground:start");

            try {
                try {

                    while (!isCancelled()){
                        mGO = new PokemonGo(httpClient);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (isGoogleAccount){
                            mGO.login(new GoogleAutoCredentialProvider(httpClient, mEmail, mPassword));
                        }else{
                            mGO.login(new PtcCredentialProvider(httpClient, mEmail, mPassword));
                        }

                        Log.i(TAG, "USER_LOGIN_TASK: doInBackground:true");
                        return true;
                    }

                } catch (LoginFailedException | RemoteServerException e) {
                    Log.i(TAG, "USER_LOGIN_TASK: doInBackground: login or remote_server exception");
                    Log.i(TAG, e.toString());
                    return false;
                }

            } catch (Exception e) {
                Log.i(TAG, "USER_LOGIN_TASK: doInBackground: general exception");
                Log.i(TAG, e.toString());
                return false;

            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.i(TAG, "USER_LOGIN_TASK: onPostExecute");
            mAuthTask = null;
            showProgress(false);

            if (success) {
                saveUserCredentials(mEmail, mPassword);
                saveUserData();
                Intent intent = new Intent(ActivityLogin.this, ActivityDashboard.class);
                intent.putExtra("login", "Google");
                startActivity(intent);

                //finish activity to select
                ActivitySelectAccount.activitySelectAccount.finish();

                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "USER_LOGIN_TASK: onCancelled");
            mAuthTask = null;
            showProgress(false);
        }
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

    /**
     * Represents an asynchronous get user contacts task used to suggest emails to
     * the user.
     */
    public class ContactsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            Log.i(TAG, "CONTACTS_TASK: doInBackground");

            //ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> emails = new ArrayList<String>();
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor cur1 = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (cur1.moveToNext()) {
                        //to get the contact names
                        //String name=cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        //Log.e("Name :", name);

                        //to get the contact emails
                        String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        //Log.i(TAG, "Email: "+email);
                        if(email!=null){
                            emails.add(email);
                        }
                    }
                    cur1.close();
                }
            }
            cur.close();

            return emails;
        }

        @Override
        protected void onPostExecute(ArrayList<String> emails) {

            Log.i(TAG, "CONTACTS_TASK: onPostExecute");
            mContactsTask = null;

            //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
            ArrayAdapter<String> adapter= new ArrayAdapter<>(ActivityLogin.this,
                                android.R.layout.simple_dropdown_item_1line, emails);


            mEmailView.setAdapter(adapter);
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "CONTACTS_TASK: onCancelled");
            mContactsTask = null;
        }
    }


    public void saveUserCredentials(String userEmail, String userPass){

        SharedPreferences prefs_user = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs_user.edit();

        editor.putString(Constants.KEY_PREF_USER_EMAIL, userEmail);
        editor.putString(Constants.KEY_PREF_USER_PASS, userPass);

        editor.commit();

    }

    public void saveUserData(){

        SharedPreferences prefs_user = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs_user.edit();

        //Pref to show all the markers in teh map <def value is true>
        editor.putBoolean(Constants.KEY_PREF_GOOGLE, isGoogleAccount);
        editor.putBoolean(Constants.KEY_PREF_ALL_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_BUSQUEDA_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_BLUE_GYMS_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_RED_GYMS_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_YELLOW_GYMS_MARKERS, true);
        editor.putBoolean(Constants.KEY_PREF_WHITE_GYMS_MARKERS, true);

        editor.commit();

    }




}

