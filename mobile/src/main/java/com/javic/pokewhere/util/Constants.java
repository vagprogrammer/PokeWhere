package com.javic.pokewhere.util;

/**
 * Created by franciscojimenezjimenez on 28/07/16.
 */
public class Constants {

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int ALERT_RESUME_TASK = 1;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    public final static int REQUEST_CODE_ACTIVITY_FILTROS = 2000;
    public final static int RESULT_CODE_OK = 1;

    public static final String TAG = "FETCH_ADDRESS";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.javic.pokewhere";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String URL_FEED = "https://pokevision.com/map/data/";

    //DEFAULT VALUES
    public static final int USER_ZOOM =15;

    //Actions
    public static final int ACTION_START_SERVICE = 0;

    // Preferencias
    public static final String PREFS_POKEWHERE="prefs_pokewhere";
    public static final  String KEY_PREF_REFRESH_TOKEN= "pref_refresh_token";
    public static final  String KEY_PREF_USER_EMAIL= "pref_user_email";
    public static final  String KEY_PREF_USER_PASS= "pref_user_pass";
    public static final String KEY_PREF_USER_NAME_KEY="pref_user_name_key";
    public static final String KEY_PREF_USER_TEAM_KEY="pref_user_team_key";
    public static final String KEY_PREF_USER_LEVEL_KEY="pref_user_level_key";


    //FILTROS
    public static final String KEY_PREF_ALL_MARKERS="pref_all_markers";
    public static final String KEY_PREF_BUSQUEDA_MARKERS="pref_busqueda_markers";
    public static final String KEY_PREF_NORMAL_POKESTOPS_MARKERS="pref_normal_pokestops";
    public static final String KEY_PREF_LURED_POKESTOPS_MARKERS="pref_lured_pokestops";

    public static final String KEY_PREF_BLUE_GYMS_MARKERS="pref_blue_gyms";
    public static final String KEY_PREF_RED_GYMS_MARKERS="pref_red_gyms";
    public static final String KEY_PREF_YELLOW_GYMS_MARKERS="pref_yellow_gyms";
    public static final String KEY_PREF_WHITE_GYMS_MARKERS="pref_white_gyms";

}
