package com.javic.pokewhere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.javic.pokewhere.util.Constants;

public class ActivitySplashScreen extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1500;

    private SplashTask mSplashTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mSplashTask = new SplashTask();
        mSplashTask.execute();


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSplashTask!=null){
            if (mSplashTask.getStatus()== AsyncTask.Status.RUNNING){
                mSplashTask.cancel(true);
            }
        }
    }

    /**
     * Al extender una nueva clase de AsyncTask indicaremos tres parámetros de tipo:
     * 1.- El tipo de datos que recibiremos como entrada de la tarea en el método doInBackground().
     * 2.- El tipo de datos con el que actualizaremos el progreso de la tarea,
     *     y que recibiremos como parámetro del método onProgressUpdate()
     *     y que a su vez tendremos que incluir como parémetro del método publishProgress().
     * 3.- El tipo de datos que devolveremos como resultado de nuestra tarea,
     * 	   que será el tipo de retorno del método doInBackground() y el tipo del parámetro recibido
     * 	   en el método onPostExecute().
     */

    public class SplashTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(SPLASH_TIME_OUT);
                return true;
            } catch (Exception e) {
                // TODO: handle exception
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            mSplashTask = null;

            if(result){
                if(isUserLogedIn()){
                    startActivity(new Intent(ActivitySplashScreen.this, ActivityDashboard.class));
                }
                else {
                    startActivity(new Intent(ActivitySplashScreen.this, ActivitySelectAccount.class));
                }
            }

            finish();
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            mSplashTask = null;
        }
    }


    private Boolean isUserLogedIn(){

        SharedPreferences prefsPokeWhere = getSharedPreferences(Constants.PREFS_POKEWHERE, MODE_PRIVATE);

        String mUserEmail = prefsPokeWhere.getString(Constants.KEY_PREF_USER_EMAIL, "");

        String mUserRefreshToken = prefsPokeWhere.getString(Constants.KEY_PREF_REFRESH_TOKEN, "");


        if (mUserEmail!="" || mUserRefreshToken!=""){
         return true;
        }

        return false;
    }

}

