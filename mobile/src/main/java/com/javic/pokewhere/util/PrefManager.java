package com.javic.pokewhere.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by vagprogrammer on 15/10/16.
 */

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context mContext;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = Constants.PREFS_POKEWHERE;

    public PrefManager(Context mContext) {
        this.mContext = mContext;
        pref = this.mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(Constants.KEY_PREF_IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(Constants.KEY_PREF_IS_FIRST_TIME_LAUNCH, true);
    }

}
