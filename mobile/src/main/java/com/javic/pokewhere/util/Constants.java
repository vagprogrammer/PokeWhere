package com.javic.pokewhere.util;

/**
 * Created by franciscojimenezjimenez on 28/07/16.
 */
public class Constants {



    public static final String TAG = "FETCH_ADDRESS";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.javic.pokewhere";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final String URL_FEED = "https://pokevision.com/map/data/";

    public static final int USER_ZOOM =15;

    public static final int ACTION_OPEN_DRAWER = 0;
    public static final int ACTION_CLOSE_DRAWER = 1;


    // Preferencias
    public static final String PREFS_POKEWHERE="prefs_pokewhere";
    public static final  String KEY_PREF_REFRESH_TOKEN= "pref_refresh_token";
    public static final  String KEY_PREF_USER_EMAIL= "pref_user_email";
    public static final  String KEY_PREF_USER_PASS= "pref_user_pass";
    public static final String KEY_PREF_USER_NAME_KEY="extra_user_name_key";
    public static final String KEY_PREF_USER_TEAM_KEY="extra_user_team_key";
    public static final String KEY_PREF_USER_LEVEL_KEY="extra_user_level_key";


    //Fragments Params
    public static final String ARG_USER = "paramUser";
    public static final String ARG_PASS = "paramPass";
    public static final String ARG_REFRESHTOKEN = "paramRefreshToken";
}
