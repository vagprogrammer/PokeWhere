package com.javic.pokewhere;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.app.AppTutorial;
import com.javic.pokewhere.util.PrefManager;

public class ActivitySplashScreen extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1500;

    private SplashTask mSplashTask = null;
    private boolean splashTaskWasCanceled = false;
    private PrefManager prefmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefmanager = new PrefManager(this);

        if (prefmanager.isUserLogedIn()) {

            if (prefmanager.isTutorialComplete()){
                startActivity(new Intent(ActivitySplashScreen.this, ActivityDashboard.class));
            }
            else {
                startActivity(new Intent(ActivitySplashScreen.this, AppTutorial.class));
            }

            finish();
        } else {
            setContentView(R.layout.activity_splash_screen);

            YoYo.with(Techniques.FadeInDown)
                    .duration(1300)
                    .playOn(findViewById(R.id.image_login));

            mSplashTask = new SplashTask();
            mSplashTask.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSplashTask!=null){
            mSplashTask.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (splashTaskWasCanceled){
            mSplashTask = new SplashTask();
            mSplashTask.execute();
        }

    }

    /**
     * Al extender una nueva clase de AsyncTask indicaremos tres parámetros de tipo:
     * 1.- El tipo de datos que recibiremos como entrada de la tarea en el método doInBackground().
     * 2.- El tipo de datos con el que actualizaremos el progreso de la tarea,
     * y que recibiremos como parámetro del método onProgressUpdate()
     * y que a su vez tendremos que incluir como parémetro del método publishProgress().
     * 3.- El tipo de datos que devolveremos como resultado de nuestra tarea,
     * que será el tipo de retorno del método doInBackground() y el tipo del parámetro recibido
     * en el método onPostExecute().
     */

    public class SplashTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                Thread.sleep(SPLASH_TIME_OUT);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mSplashTask = null;
            if (result) {
                startActivity(new Intent(ActivitySplashScreen.this, ActivitySelectAccount.class));
            }
            finish();
        }

        @Override
        protected void onCancelled() {
            mSplashTask = null;
        }
    }

}

