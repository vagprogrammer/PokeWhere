package com.javic.pokewhere.util;

/**
 * Created by franciscojimenezjimenez on 28/07/16.
 */

public class Constants {

    public final static Boolean DEBUG_MODE = true;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int ALERT_RESUME_TASK = 1;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    public final static int REQUEST_CODE_ACTIVITY_FILTROS = 0;
    public final static int REQUEST_CODE_ACTIVITY_POKEMON_DETAIL = 10;


    public static final String TAG = "FETCH_ADDRESS";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.javic.pokewhere";
    public static final String PACKAGE_FRAGMENT_NAME = "com.javic.pokewhere.fragments.";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String URL_FEED = "https://pokevision.com/map/data/";

    //DEFAULT VALUES
    public static final int USER_ZOOM =15;

    //Dashboard Actions
    public static final int ACTION_CONNECT_WITH_PG = 0;
    public static final int ACTION_START_SERVICE = 1;
    public static final int ACTION_REFRESH_TOKEN = 2;
    public static final int ACTION_VER_TODOS = 3;
    public static final int ACTION_TRANSFER_POKEMON = 4;
    public static final int ACTION_SET_FAVORITE_POKEMON = 5;
    public static final int ACTION_DELETE_ITEMS = 6;
    public static final int ACTION_GO_TO_DETAIL = 7;
    public static final int ACTION_POWER_UP = 8;
    public static final int ACTION_EVOLVE = 9;
    public static final int ACTION_UPDATE_USER_POKEMON = 10;
    public static final int ACTION_RENAME_USER_POKEMON = 11;
    public static final int ACTION_UPDATE_USER_BAG = 12;

    //Fragments
    public static final int FRAGMENT_BLANK = -1;
    public static final int FRAGMENT_POKEBANK = 0;
    public static final int FRAGMENT_BAG = 1;
    public static final int FRAGMENT_MAPA = 2;
    public static final int FRAGMENT_COMPARE = 3;


    // Preferencias
    public static final String PREFS_POKEWHERE="prefs_pokewhere";
    public static final  String KEY_PREF_REFRESH_TOKEN= "pref_refresh_token";
    public static final  String KEY_PREF_USER_EMAIL= "pref_user_email";
    public static final  String KEY_PREF_USER_PASS= "pref_user_pass";
    public static final  String KEY_PREF_GOOGLE= "pref_google";
    public static final String KEY_PREF_IS_FIRST_TIME_LAUNCH = "pref_isfirsttimelaunch";
    public static final String KEY_PREF_IS_TUTORIAL_COMPLETE = "pref_istutorialcomplete";


    //FILTROS
    public static final String KEY_PREF_ALL_MARKERS="pref_all_markers";
    public static final String KEY_PREF_BUSQUEDA_MARKERS="pref_busqueda_markers";
    public static final String KEY_PREF_NORMAL_POKESTOPS_MARKERS="pref_normal_pokestops";
    public static final String KEY_PREF_LURED_POKESTOPS_MARKERS="pref_lured_pokestops";
    public static final String KEY_PREF_BLUE_GYMS_MARKERS="pref_blue_gyms";
    public static final String KEY_PREF_RED_GYMS_MARKERS="pref_red_gyms";
    public static final String KEY_PREF_YELLOW_GYMS_MARKERS="pref_yellow_gyms";
    public static final String KEY_PREF_WHITE_GYMS_MARKERS="pref_white_gyms";

    //COMAPRE
    public static final int VALUE_IV = 0;
    public static final int VALUE_CP = 1;
    public static final int VALUE_RECENTS = 2;
    public static final int VALUE_NAME = 3;
    public static final int VALUE_NUMBER = 4;
    public static final int VALUE_ATACK = 5;
    public static final int VALUE_DEFENSE = 6;
    public static final int VALUE_STAMINA = 7;
}
