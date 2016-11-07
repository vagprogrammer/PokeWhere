package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.ActivityFiltros;
import com.javic.pokewhere.R;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.models.LocalGym;
import com.javic.pokewhere.models.LocalPokeStop;
import com.javic.pokewhere.models.LocalPokemon;
import com.javic.pokewhere.models.PlaceSuggestion;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class FragmentMapa extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FragmentMapa.class.getSimpleName();

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng userPosition;

    // FragmentMapa UI
    private View mView;
    private GoogleMap mGoogleMap;
    private MapView mapView;
    private FloatingSearchView mSearchView;
    private FloatingActionButton mGetPokemonsButton;
    private ImageView mUserMarker;
    private Snackbar mSnackBar;
    private Snackbar mSnackBarPermisions;

    // API PokemonGO
    private static PokemonGo mPokemonGo;
    private PokemonsTask mPokemonTask;
    private PokeStopsTask mPokeStopsTask;
    private GymsTask mGymsTask;

    //Marcadores y listas
    private Map<String, String> mPokemonsMap = new HashMap<String, String>();
    private List<LocalPokemon> mLocalPokemons = new ArrayList<>();
    private List<LocalPokeStop> mLocalPokeStops = new ArrayList<>();
    private List<LocalGym> mLocalGyms = new ArrayList<>();
    private List<Marker> mMarkers = new ArrayList();
    private ArrayList<PlaceSuggestion> mPlaceSuggestionList = new ArrayList<>();

    //Filtros
    private Boolean mAllMarkers;
    private Boolean mBusquedaMarkers;
    private Boolean mNormalPokeStopMarkers;
    private Boolean mLuredPokeStopMarkers;
    private Boolean mBlueGymMarkers;
    private Boolean mRedGymMarkers;
    private Boolean mYellowGymsMarkers;
    private Boolean mWhiteGymsMarkers;


    //Variables
    private Context mContext;
    private Boolean isSearching = true;
    private LatLng ltn = null;
    private String mLastQuery = "";

    //Class and interface
    private CounterToRemoveMarkers mCounterToRemoveMarkers;
    private OnFragmentListener mListener;

    //Handlres
    HandlerThread mHandlerThread;
    Handler mThreadHandler;

    public FragmentMapa() {
        // Required empty public constructor

        if (mThreadHandler == null) {
            // Initialize and start the HandlerThread
            // which is basically a Thread with a Looper
            // attached (hence a MessageQueue)
            mHandlerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {

                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {


                        /*ArrayList<String> results = mAdapter.resultList;

                        if (results != null && results.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        }
                        else {
                            mAdapter.notifyDataSetInvalidated();
                        }*/

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //this will swap the data and
                                //render the collapse/expand animations as necessary
                                mSearchView.swapSuggestions(mPlaceSuggestionList);

                                //let the users know that the background
                                //process has completed
                                mSearchView.hideProgress();
                            }
                        });
                    }
                }
            };
        }
    }

    /**
     * @param
     * @return A new instance of fragment FragmentMapa.
     */
    public static FragmentMapa newInstance(PokemonGo pokemonGo) {
        FragmentMapa fragment = new FragmentMapa();

        mPokemonGo = pokemonGo;

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(mContext);
        setUpData();
        getFiltros();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPokemonGo!=null){

            mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_MAPA);

            mapView = (MapView) mView.findViewById(R.id.map);
            mSearchView = (FloatingSearchView) mView.findViewById(R.id.floating_search_view);
            mUserMarker = (ImageView) mView.findViewById(R.id.user_marker);
            mGetPokemonsButton = (FloatingActionButton) mView.findViewById(R.id.fab);

            mGetPokemonsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    attemptSearch();
                }
            });

            // First we need to check availability of play services
            if (checkPlayServices()) {

                // Building the GoogleApi client
                buildGoogleApiClient();

                //Biulding the GoogleMap
                if (mapView != null) {
                    // Initialise the MapView
                    mapView.onCreate(null);
                    mapView.onResume();

                    // Set the map ready callback to receive the GoogleMap object
                    mapView.getMapAsync(this);
                }
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentListener) {
            mListener = (OnFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        //First we need to check if the GoogleMap was not created in OnCreate
        if (mGoogleMap == null) {

            // We need to check availability of play services
            if (checkPlayServices()) {

                if (mGoogleApiClient == null) {
                    // Building the GoogleApi client
                    buildGoogleApiClient();
                }
                //Biulding the GoogleMap
                if (mapView != null) {
                    // Initialise the MapView
                    mapView.onCreate(null);
                    mapView.onResume();

                    // Set the map ready callback to receive the GoogleMap object
                    mapView.getMapAsync(this);
                }
            }
        }

        //GoogleMap exist
        else {
            /*
            if (!mayRequestLocation()) {
                return;
            }*/
        }


    }

    @Override
    public void onPause() {
        super.onPause();

        //OnlyCnacel
        cancelTask(true);

        if (mGoogleMap != null) {

            if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_CODE_ACTIVITY_FILTROS && resultCode == getActivity().RESULT_OK) {
            getFiltros();

            showAlert(Constants.ALERT_RESUME_TASK);
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(mContext);

        mGoogleMap = googleMap;

        setUpGoogleMap();
        setUpSearchView();

        //show progressBar
        mListener.showProgress(false);
        mGoogleApiClient.connect();

    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);

        ltn = null;
    }

    /**
     * Method to display the location on UI
     */
    private void getUserLocation() {

        if (!mayRequestLocation()) {

            userPosition = new LatLng(34.0089919, -118.4996126);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {

            userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

        } else {
            showAlert(Constants.ALERT_ADDRESS_RESULT_RECIVER);
        }

    }

    private boolean mayRequestLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }

            return true;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {

            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, Constants.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });
                mSnackBarPermisions.show();
            } else {
                if (!mSnackBarPermisions.isShown()) {
                    mSnackBarPermisions.show();
                }
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == Constants.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mSnackBarPermisions = null;
                //getUserLocation();
            }
        } else {
            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, Constants.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });

            }
        }


    }

    public void drawPokemon(LocalPokemon localPokemon) {

        AssetManager assetManager = mContext.getAssets();

        try {
            InputStream is = null;

           /* if (localPokemon.getPokemonId() < 10) {
                is = assetManager.open(String.valueOf("00" + localPokemon.getPokemonId()) + ".ico");
            } else if (localPokemon.getPokemonId() < 100) {
                is = assetManager.open(String.valueOf("0" + localPokemon.getPokemonId()) + ".ico");
            } else {
                is = assetManager.open(String.valueOf(localPokemon.getPokemonId()) + ".ico");
            }*/

            if (localPokemon.getPokemonId() < 10) {
                is = assetManager.open(String.valueOf("00" + localPokemon.getPokemonId()) + ".png");
            } else if (localPokemon.getPokemonId() < 100) {
                is = assetManager.open(String.valueOf("0" + localPokemon.getPokemonId()) + ".png");
            } else {
                is = assetManager.open(String.valueOf(localPokemon.getPokemonId()) + ".png");
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(localPokemon.getLatitude(), localPokemon.getLongitude()))
                    .title(localPokemon.getPokemonName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .snippet(createDate(localPokemon.getExpiration_time()))
            );

            mMarker.setTag(localPokemon);
            mMarkers.add(mMarker);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void drawPokeStop(LocalPokeStop localPokeStop) {

        Log.i(TAG, "DRAW: " + localPokeStop.getName());

        int resourceId;

        if (!localPokeStop.getHasLure()) {

            if (mNormalPokeStopMarkers) {
                resourceId = R.drawable.ic_pokestop;

                Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(localPokeStop.getLatitude(), localPokeStop.getLongitude()))
                        .title(localPokeStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(resourceId))
                        .snippet(localPokeStop.getDescription())
                );

                mMarker.setTag(localPokeStop);
                mMarkers.add(mMarker);
            }

        } else {

            if (mLuredPokeStopMarkers) {
                resourceId = R.drawable.ic_pokestope_lucky;

                Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(localPokeStop.getLatitude(), localPokeStop.getLongitude()))
                        .title(localPokeStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(resourceId))
                        .snippet(localPokeStop.getDescription())
                );

                mMarker.setTag(localPokeStop);
                mMarkers.add(mMarker);
            }

        }
    }

    public void drawGym(LocalGym localGym) {

        int icon_gym;

        switch (localGym.getTeam()) {
            case 1:
                if (mBlueGymMarkers) {
                    icon_gym = R.drawable.ic_gym_team_blue;

                    Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(localGym.getLatitude(), localGym.getLongitude()))
                            .title(localGym.getName())
                            .icon(BitmapDescriptorFactory.fromResource(icon_gym))
                            .snippet(localGym.getDescription())
                    );

                    mMarker.setTag(localGym);
                    mMarkers.add(mMarker);
                }

                break;
            case 2:
                if (mRedGymMarkers) {
                    icon_gym = R.drawable.ic_gym_team_red;
                    Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(localGym.getLatitude(), localGym.getLongitude()))
                            .title(localGym.getName())
                            .icon(BitmapDescriptorFactory.fromResource(icon_gym))
                            .snippet(localGym.getDescription())
                    );

                    mMarker.setTag(localGym);
                    mMarkers.add(mMarker);
                }

                break;
            case 3:

                if (mYellowGymsMarkers) {
                    icon_gym = R.drawable.ic_gym_team_yellow;
                    Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(localGym.getLatitude(), localGym.getLongitude()))
                            .title(localGym.getName())
                            .icon(BitmapDescriptorFactory.fromResource(icon_gym))
                            .snippet(localGym.getDescription())
                    );

                    mMarker.setTag(localGym);
                    mMarkers.add(mMarker);
                }


                break;
            default:

                if (mWhiteGymsMarkers) {
                    icon_gym = R.drawable.ic_gym_team_white;
                    Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(localGym.getLatitude(), localGym.getLongitude()))
                            .title(localGym.getName())
                            .icon(BitmapDescriptorFactory.fromResource(icon_gym))
                            .snippet(localGym.getDescription())
                    );

                    mMarker.setTag(localGym);
                    mMarkers.add(mMarker);
                }


                break;
        }


    }

    public void drawLocation(LatLng position) {

        if (mBusquedaMarkers) {

            Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(position.latitude, position.longitude)));

            Long currentTime = System.currentTimeMillis();
            mMarker.setTitle("Custom Location");
            mMarker.setTitle("At: " + createDate(currentTime));
            mMarker.setTag(currentTime);
            mMarkers.add(mMarker);

        }

    }


    /**
     * Google api callback methods
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        //getUserLocation();
        //showCustomDialog();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    /**
     * Attempts to start the search ok pokemons, gyms and pokestops.
     */
    private void attemptSearch() {

        if (mPokemonTask == null) {
            mPokemonTask = new PokemonsTask(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokemonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokemonTask.execute();
            }
        }

        if (mPokeStopsTask == null) {
            mPokeStopsTask = new PokeStopsTask(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokeStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokeStopsTask.execute();
            }
        }

        /*if (mGymsTask == null) {
            mGymsTask = new GymsTask(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mGymsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mGymsTask.execute();
            }
        }*/

        if (mCounterToRemoveMarkers==null){

            mCounterToRemoveMarkers = new CounterToRemoveMarkers(60000, 10000);
            mCounterToRemoveMarkers.start();
        }


        mGetPokemonsButton.setVisibility(View.GONE);

        mSnackBar = Snackbar.make(mView, R.string.message_snackbar_searching_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snack_bar_neg_btn, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {

                        //onlyCancel
                        cancelTask(true);
                    }
                });

        mSnackBar.show();

        mSearchView.showProgress();
    }

    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class PokemonsTask extends AsyncTask<Void, LocalPokemon, Boolean> {


        PokemonsTask(Boolean isEnabled) {
            isSearching = isEnabled;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                while (isSearching) {

                    if (isDeviceOnline()) {

                        if (ltn == null) {
                            ltn = new LatLng(userPosition.longitude, userPosition.latitude);
                        } else {
                            ltn = getRandomeLocation(userPosition.longitude, userPosition.latitude, 100);
                        }

                        mPokemonGo.setLocation(ltn.latitude, ltn.longitude, 1);

                        try {
                            List<CatchablePokemon> chablePokemons = mPokemonGo.getMap().getCatchablePokemon();

                            List<Pokeball> pokeBallsList = new ArrayList<>();

                            int pokeballs = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_POKE_BALL).getCount();
                            int greatBalls = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL).getCount();
                            int ultraBalls = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL).getCount();

                            Log.i(TAG,"POKEBALL:" + String.valueOf(pokeballs)+ " GREAT_BALL: " + String.valueOf(greatBalls)+ " ULTRA_BALL: " + String.valueOf(ultraBalls));

                            if (chablePokemons != null) {
                                for (CatchablePokemon cachablePokemon : chablePokemons) {

                                    // You need to Encounter first.
                                    EncounterResult encResult = cachablePokemon.encounterPokemon();

                                    Log.i(TAG, " Encountered Result: " + encResult.getStatus().toString());

                                    // if encounter was succesful, catch
                                    if (encResult.wasSuccessful()) {
                                        Log.i(TAG, " Encountered: " + cachablePokemon.getPokemonId());

                                        CatchOptions options = new CatchOptions(mPokemonGo);

                                        if(options.getRazzberries()>0){
                                            options.maxRazzberries(0);
                                        }

                                        if (options.getMaxPokeballs()>0){
                                            options.useBestBall(true);
                                            options.noMasterBall(true);
                                        }

                                        CatchResult result = cachablePokemon.catchPokemon(options);

                                        Log.i(TAG, "Attempt to catch: " + cachablePokemon.getPokemonId() + " " + result.getStatus());

                                        showToast(cachablePokemon.getPokemonId() + " " + result.getStatus());

                                    }

                                    LocalPokemon localPokemon = new LocalPokemon();
                                    localPokemon.setId(cachablePokemon.getEncounterId());
                                    localPokemon.setExpiration_time(cachablePokemon.getExpirationTimestampMs());
                                    localPokemon.setPokemonId(cachablePokemon.getPokemonId().getNumber());
                                    localPokemon.setPokemonName(mPokemonsMap.get(String.valueOf(localPokemon.getPokemonId())));
                                    localPokemon.setLatitude(cachablePokemon.getLatitude());
                                    localPokemon.setLongitude(cachablePokemon.getLongitude());

                                    Boolean isEncountered = containsEncounteredId(localPokemon, String.valueOf(localPokemon.getId()));

                                    if (!isEncountered) {
                                        mLocalPokemons.add(localPokemon);
                                        publishProgress(localPokemon);
                                    }

                                }

                                sleep(10000);
                            }
                        } catch (LoginFailedException | RemoteServerException e) {
                            Log.e(TAG, "Failed to get pokemons or server issue Login or RemoteServer exception: ", e);
                            cancelTask(false);
                        }
                    }

                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get pokemons or server issue General exception: ", e);
                cancelTask(false);
            }

            return false;

        }

        @Override
        protected void onProgressUpdate(LocalPokemon... localPokemon) {

            super.onProgressUpdate(localPokemon);
            drawPokemon(localPokemon[0]);
        }

        @Override
        protected void onPostExecute(Boolean succes) {
            mPokemonTask = null;
            isSearching = false;
        }

        @Override
        protected void onCancelled() {
            mPokemonTask = null;
            isSearching = false;
        }
    }

    /**
     * Represents an asynchronous get poke stops
     * with a location.
     */
    public class PokeStopsTask extends AsyncTask<Void, LocalPokeStop, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        PokeStopsTask(Boolean isEnabled) {
            isSearching = isEnabled;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                while (isSearching) {

                    if (isDeviceOnline()) {
                        mPokemonGo.setLocation(userPosition.latitude, userPosition.longitude, 1);

                        try {
                            List<Pokestop> pokeStops = new ArrayList<>(mPokemonGo.getMap().getMapObjects().getPokestops());

                            if (pokeStops != null) {

                                for (Pokestop pkStop : pokeStops) {

                                    LocalPokeStop localPokeStop = new LocalPokeStop();
                                    localPokeStop.setId(pkStop.getId());
                                    localPokeStop.setLatitude(pkStop.getLatitude());
                                    localPokeStop.setLatitude(pkStop.getLatitude());
                                    localPokeStop.setLongitude(pkStop.getLongitude());

                                    //Check pokeStop to Add or Loot
                                    if (!containsEncounteredId(localPokeStop, localPokeStop.getId())) {

                                        //Check if the pokestop is in range to be drawed (< 150 [m])
                                        if (!shouldMarkerRemove(localPokeStop)) {

                                            Log.i(TAG, " Encountered PokeStop: " + pkStop.getDetails().getName());

                                            //Check if the pokestop is in range to be Loot (< 90 [m])
                                            if (isinRange(localPokeStop)) {
                                                Log.i(TAG, "Attempt to Loot...");
                                                mPokemonGo.setLocation(pkStop.getLatitude(), pkStop.getLongitude(), 1);
                                               // if (pkStop.canLoot()) {
                                                    PokestopLootResult result = pkStop.loot();

                                                    if (result.wasSuccessful()){
                                                        String items="";

                                                        for (ItemAwardOuterClass.ItemAward itemAward: result.getItemsAwarded()){
                                                            items = items + "\n* " + itemAward.getItemId().name();
                                                        }

                                                        String message = "Items obtained: " + String.valueOf(result.getItemsAwarded().size()+ items);
                                                        Log.i(TAG, message);
                                                        showToast(message);
                                                    }

                                                    Log.i(TAG, "Result: " + result.getResult().toString());
                                               // }
                                            }

                                            //Add PokeStop
                                            sleep(1000);
                                            localPokeStop.setHasLure(pkStop.hasLure());
                                            localPokeStop.setName(pkStop.getDetails().getName());
                                            localPokeStop.setDescription(pkStop.getDetails().getDescription());

                                            mLocalPokeStops.add(localPokeStop);
                                            publishProgress(localPokeStop);
                                        }
                                    }
                                    else{
                                        //Check if the pokestop is in range to be Loot (< 90 [m])
                                        if (isinRange(localPokeStop)) {
                                            Log.i(TAG, "Attempt to Loot " + pkStop.getDetails().getName());
                                            mPokemonGo.setLocation(pkStop.getLatitude(), pkStop.getLongitude(), 1);

                                            if (pkStop.canLoot()) {
                                            PokestopLootResult result = pkStop.loot();

                                            if (result.wasSuccessful()){
                                                String items="";

                                                for (ItemAwardOuterClass.ItemAward itemAward: result.getItemsAwarded()){
                                                    items = items + "\n* " + itemAward.getItemId().name();
                                                }

                                                String message = "Items obtained: " + String.valueOf(result.getItemsAwarded().size()+ items);
                                                Log.i(TAG, message);
                                                showToast(message);
                                            }

                                            Log.i(TAG, "Result: " + result.getResult().toString());
                                            }
                                        }
                                        sleep(1000);
                                    }
                                }

                            }
                        } catch (LoginFailedException | RemoteServerException e) {
                            Log.e(TAG, "Failed to get pokestops or server issue Login or RemoteServer exception: ", e);
                            cancelTask(false);
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to get pokestops or server issue General exception: ", e);
                cancelTask(false);
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(LocalPokeStop... localPokeStop) {

            super.onProgressUpdate(localPokeStop);
            drawPokeStop(localPokeStop[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mPokeStopsTask = null;
            isSearching = false;
        }

        @Override
        protected void onCancelled() {
            mPokeStopsTask = null;
            isSearching = false;
        }

    }

    /**
     * Represents an asynchronous get Gyms
     * with a location.
     */
    public class GymsTask extends AsyncTask<Void, LocalGym, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        GymsTask(Boolean isEnabled) {
            isSearching = isEnabled;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {

            try {

                while (isSearching) {

                    if (isDeviceOnline()) {

                        mPokemonGo.setLocation(userPosition.latitude, userPosition.longitude, 1);

                        try {
                            List<Gym> gyms = new ArrayList<>(mPokemonGo.getMap().getGyms());

                            if (gyms != null) {

                                for (Gym gym : gyms) {
                                    LocalGym localGym = new LocalGym();
                                    localGym.setId(gym.getId());

                                    Boolean isEncountered = containsEncounteredId(localGym, localGym.getId());

                                    if (!isEncountered) {

                                        sleep(1000);
                                        localGym.setName(gym.getName());
                                        localGym.setTeam(gym.getOwnedByTeam().getNumber());
                                        localGym.setInBattle(gym.getIsInBattle());
                                        localGym.setPoints(gym.getPoints());
                                        localGym.setLatitude(gym.getLatitude());
                                        localGym.setLongitude(gym.getLongitude());
                                        localGym.setDescription(gym.getDescription());

                                        mLocalGyms.add(localGym);
                                        publishProgress(localGym);

                                    }

                                }
                            }
                            sleep(10000);
                        } catch (LoginFailedException | RemoteServerException e) {
                            Log.e(TAG, "Failed to get gyms or server issue Login or RemoteServer exception: ", e);
                            cancelTask(false);
                        }
                    }
                }

            } catch (Exception e){
                Log.e(TAG, "Failed to get gyms or server issue General exception: ", e);
                cancelTask(false);
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(LocalGym... localGym) {

            super.onProgressUpdate(localGym);
            drawGym(localGym[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mGymsTask = null;
            isSearching = false;
        }

        @Override
        protected void onCancelled() {
            mGymsTask = null;
            isSearching = false;
        }
    }


    public void cancelTask(Boolean onlyCancel){

        if (isSearching != null) {
            isSearching = false;
        }

        if (mPokemonTask!=null){
            mPokemonTask.cancel(true);
            mPokemonTask = null;
        }

        if (mPokeStopsTask!=null){
            mPokeStopsTask.cancel(true);
            mPokeStopsTask = null;
        }

        if (mGymsTask!=null){
            mGymsTask.cancel(true);
            mGymsTask = null;
        }

        if (mCounterToRemoveMarkers != null) {
            mCounterToRemoveMarkers.cancel();
            mCounterToRemoveMarkers = null;
        }

        if (onlyCancel){
            Log.i(TAG, "ONLY CANCEL TASK");

            if (mSnackBar != null && mSearchView != null && mGetPokemonsButton != null) {
                mSnackBar.dismiss();
                mGetPokemonsButton.setVisibility(View.VISIBLE);
                mSearchView.hideProgress();
            }
        }
        else {
            if (!Constants.DEBUG_MODE){
                Log.i(TAG, "DEBUG MODE IS DISABLE");
                //Show snackBar to let user restart the tasks
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mSnackBar != null && mSearchView != null) {
                            mSnackBar.dismiss();
                            mSnackBar = null;
                            mSearchView.hideProgress();
                        }

                        if (mSnackBar == null) {
                            mSnackBar = Snackbar.make(mView, R.string.snack_bar_error_with_pokemon, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.snack_bar_error_with_pokemon_negative_btn, new View.OnClickListener() {
                                        @Override
                                        @TargetApi(Build.VERSION_CODES.M)
                                        public void onClick(View v) {
                                            mGetPokemonsButton.setVisibility(View.VISIBLE);
                                        }
                                    })
                                    .setAction(R.string.snack_bar_error_with_pokemon_positive_btn, new View.OnClickListener() {
                                        @Override
                                        @TargetApi(Build.VERSION_CODES.M)
                                        public void onClick(View v) {

                                            attemptSearch();
                                        }
                                    });
                            mSnackBar.show();
                        }
                    }
                });

            }
            else{

                sleep(5000);
                Log.i(TAG, "DEBUG MODE IS ENABLED, SO RESTART ALL THE TASKS");
                //Restart all the Tasks
                attemptSearch();
            }
        }

    }

    public void setUpSearchView() {

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mGetPokemonsButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                mSearchView.setSearchBarTitle(mLastQuery);
                mGetPokemonsButton.setVisibility(View.VISIBLE);
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    mSearchView.showProgress();


                    //final String value = s.toString();

                    final String value = newQuery;

                    // Remove all callbacks and messages
                    mThreadHandler.removeCallbacksAndMessages(null);

                    // Now add a new one
                    mThreadHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Background thread
                            // mAdapter.resultList = mAdapter.mPlaceAPI.autocomplete(value);

                            mPlaceSuggestionList = new ArrayList<>();

                            //Southwest corner to Northeast corner.
                            LatLngBounds bounds = new LatLngBounds(new LatLng(18.7887956,-98.3336627), new LatLng(19.0110955,-97.0607641));

                            Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, value, bounds, null)
                                    .setResultCallback(
                                            new ResultCallback<AutocompletePredictionBuffer>() {
                                                @Override
                                                public void onResult(AutocompletePredictionBuffer buffer) {

                                                    if (buffer == null)
                                                        return;

                                                    if (buffer.getStatus().isSuccess()) {
                                                        for (AutocompletePrediction prediction : buffer) {
                                                            //Add as a new item to avoid IllegalArgumentsException when buffer is released
                                                            mPlaceSuggestionList.add(new PlaceSuggestion(prediction.getFullText(null).toString()));
                                                        }
                                                    }

                                                    //Prevent memory leak by releasing buffer
                                                    buffer.release();

                                                    // Post to Main Thread
                                                    mThreadHandler.sendEmptyMessage(1);
                                                }
                                            });
                        }
                    }, 500);


                }

                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                PlaceSuggestion placeSuggestion = (PlaceSuggestion) item;

                leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_search_black_24dp, null));
                leftIcon.setAlpha(.36f);

                textView.setTextColor(Color.parseColor("#0B3861"));

                String text = placeSuggestion.getBody()
                        .replaceFirst(mSearchView.getQuery(),
                                "<font color=\"" + "#d67601" + "\">" + mSearchView.getQuery() + "</font>");

                textView.setText(Html.fromHtml(text));
            }
        });

        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_location:

                        if (!mayRequestLocation()) {
                            break;
                        } else {
                            View myLocationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent())
                                    .findViewById(Integer.parseInt("2"));

                            myLocationButton.performClick();
                        }
                        break;
                    case R.id.action_start_service:
                        mListener.onFragmentActionPerform(Constants.ACTION_START_SERVICE, null);
                        break;
                    default:
                        Intent intent = new Intent(mContext, ActivityFiltros.class);
                        getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_ACTIVITY_FILTROS);
                        break;
                }
            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHamburger"
       /* mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {

                Log.i(TAG, "onMenuOpened()");
            }

            @Override
            public void onMenuClosed() {

                Log.i(TAG, "onMenuClosed()");
            }
        });*/

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                mLastQuery = searchSuggestion.getBody();
                // Determine whether a Geocoder is available.
                if (!Geocoder.isPresent()) {
                    Snackbar.make(mView, getString(R.string.no_geocoder_available), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    Geocoder gc = new Geocoder(mContext);

                    try {
                        List<Address> list = gc.getFromLocationName(mLastQuery, 1);
                        Address address = list.get(0);

                        userPosition = new LatLng(address.getLatitude(), address.getLongitude());

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));


                    } catch (Exception e) {

                    }


                }

                Log.i(TAG, "onSuggestionClicked()");
            }

            @Override
            public void onSearchAction(String query) {

                // Determine whether a Geocoder is available.
                if (!Geocoder.isPresent()) {
                    Snackbar.make(mView, getString(R.string.no_geocoder_available), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    Geocoder gc = new Geocoder(mContext);

                    try {
                        List<Address> list = gc.getFromLocationName(query, 1);
                        Address address = list.get(0);

                        userPosition = new LatLng(address.getLatitude(), address.getLongitude());

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

                    } catch (Exception e) {

                    }


                }

                Log.i(TAG, "onSearchAction()");
            }
        });

        mSearchView.attachNavigationDrawerToMenuButton(ActivityDashboard.mDrawerLayout);
    }

    public void setUpGoogleMap() {

        if (mGoogleMap != null) {

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.setOnCameraChangeListener(this);
            //mGoogleMap.setPadding(0, 0, 0, 150);

            mSearchView.setVisibility(View.VISIBLE);
            mUserMarker.setVisibility(View.VISIBLE);
            mGetPokemonsButton.setVisibility(View.VISIBLE);
        }
    }

    public void showAlert(final int action) {

        String title, message, positive_btn_title, negative_btn_title;

        if (action == Constants.ALERT_ADDRESS_RESULT_RECIVER) {
            title = mContext.getResources().getString(R.string.location_alert_title);
            message = mContext.getResources().getString(R.string.location_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.location_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        } else if (action == Constants.ALERT_RESUME_TASK) {
            title = mContext.getResources().getString(R.string.resume_alert_title);
            message = mContext.getResources().getString(R.string.resume_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.resume_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.resume_alert_neg_btn);

        } else {
            title = mContext.getResources().getString(R.string.permission_alert_title);
            message = mContext.getResources().getString(R.string.permission_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.open_location_settings);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton(positive_btn_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

                if (action == Constants.ALERT_ADDRESS_RESULT_RECIVER) {
                    //getUserLocation();
                }
                if (action == Constants.ALERT_RESUME_TASK) {
                    attemptSearch();
                } else {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);
                }

            }
        });

        dialog.setNegativeButton(negative_btn_title, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

                if (action == Constants.ALERT_RESUME_TASK) {
                    mGoogleMap.clear();
                    mLocalPokemons = new ArrayList<>();
                    mLocalPokeStops = new ArrayList<>();
                    mLocalGyms = new ArrayList<>();
                    mMarkers = new ArrayList();

                    attemptSearch();
                }
            }
        });
        dialog.show();
    }

    public boolean isDeviceOnline() {

        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isConnected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                isConnected = true;
            }
        }
        else {
            // not connected to the internet
            isConnected = false;
        }

        return isConnected;
    }

    public static String createDate(long timestamp) {

        //Fri Aug 26 19:54:06 CDT 2016
        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String time = "Desaparecerá a las: " + String.valueOf(hours) + ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds);
        return time;

    }

    public LatLng getRandomeLocation(double x0, double y0, int radius) {
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLongitude = new_x + x0;
        double foundLatitude = y + y0;
        final LatLng foundLocation = new LatLng(foundLatitude, foundLongitude);

        //Log.i(TAG, "Longitude: " + foundLongitude + "  Latitude: " + foundLatitude);

        Log.i(TAG, "New Location");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawLocation(foundLocation);
            }
        });

        return foundLocation;
    }

    public void setUpData() {

        mPokemonsMap.put("1", "Bulbasaur");
        mPokemonsMap.put("2", "Ivysaur");
        mPokemonsMap.put("3", "Venusaur");
        mPokemonsMap.put("4", "Charmander");
        mPokemonsMap.put("5", "Charmeleon");
        mPokemonsMap.put("6", "Charizard");
        mPokemonsMap.put("7", "Squirtle");
        mPokemonsMap.put("8", "Wartortle");
        mPokemonsMap.put("9", "Blastoise");
        mPokemonsMap.put("10", "Caterpie");
        mPokemonsMap.put("11", "Metapod");
        mPokemonsMap.put("12", "Butterfree");
        mPokemonsMap.put("13", "Weedle");
        mPokemonsMap.put("14", "Kakuna");
        mPokemonsMap.put("15", "Beedrill");
        mPokemonsMap.put("16", "Pidgey");
        mPokemonsMap.put("17", "Pidgeotto");
        mPokemonsMap.put("18", "Pidgeot");
        mPokemonsMap.put("19", "Rattata");
        mPokemonsMap.put("20", "Raticate");
        mPokemonsMap.put("21", "Spearow");
        mPokemonsMap.put("22", "Fearow");
        mPokemonsMap.put("23", "Ekans");
        mPokemonsMap.put("24", "Arbok");
        mPokemonsMap.put("25", "Pikachu");
        mPokemonsMap.put("26", "Raichu");
        mPokemonsMap.put("27", "Sandshrew");
        mPokemonsMap.put("28", "Sandslash");
        mPokemonsMap.put("29", "Nidoran♀");
        mPokemonsMap.put("30", "Nidorina");
        mPokemonsMap.put("31", "Nidoqueen");
        mPokemonsMap.put("32", "Nidoran♂");
        mPokemonsMap.put("33", "Nidorino");
        mPokemonsMap.put("34", "Nidoking");
        mPokemonsMap.put("35", "Clefairy");
        mPokemonsMap.put("36", "Clefable");
        mPokemonsMap.put("37", "Vulpix");
        mPokemonsMap.put("38", "Ninetales");
        mPokemonsMap.put("39", "Jigglypuff");
        mPokemonsMap.put("40", "Wigglytuff");
        mPokemonsMap.put("41", "Zubat");
        mPokemonsMap.put("42", "Golbat");
        mPokemonsMap.put("43", "Oddish");
        mPokemonsMap.put("44", "Gloom");
        mPokemonsMap.put("45", "Vileplume");
        mPokemonsMap.put("46", "Paras");
        mPokemonsMap.put("47", "Parasect");
        mPokemonsMap.put("48", "Venonat");
        mPokemonsMap.put("49", "Venomoth");
        mPokemonsMap.put("50", "Diglett");
        mPokemonsMap.put("51", "Dugtrio");
        mPokemonsMap.put("52", "Meowth");
        mPokemonsMap.put("53", "Persian");
        mPokemonsMap.put("54", "Psyduck");
        mPokemonsMap.put("55", "Golduck");
        mPokemonsMap.put("56", "Mankey");
        mPokemonsMap.put("57", "Primeape");
        mPokemonsMap.put("58", "Growlithe");
        mPokemonsMap.put("59", "Arcanine");
        mPokemonsMap.put("60", "Poliwag");
        mPokemonsMap.put("61", "Poliwhirl");
        mPokemonsMap.put("62", "Poliwrath");
        mPokemonsMap.put("63", "Abra");
        mPokemonsMap.put("64", "Kadabra");
        mPokemonsMap.put("65", "Alakazam");
        mPokemonsMap.put("66", "Machop");
        mPokemonsMap.put("67", "Machoke");
        mPokemonsMap.put("68", "Machamp");
        mPokemonsMap.put("69", "Bellsprout");
        mPokemonsMap.put("70", "Weepinbell");
        mPokemonsMap.put("71", "Victreebel");
        mPokemonsMap.put("72", "Tentacool");
        mPokemonsMap.put("73", "Tentacruel");
        mPokemonsMap.put("74", "Geodude");
        mPokemonsMap.put("75", "Graveler");
        mPokemonsMap.put("76", "Golem");
        mPokemonsMap.put("77", "Ponyta");
        mPokemonsMap.put("78", "Rapidash");
        mPokemonsMap.put("79", "Slowpoke");
        mPokemonsMap.put("80", "Slowbro");
        mPokemonsMap.put("81", "Magnemite");
        mPokemonsMap.put("82", "Magneton");
        mPokemonsMap.put("83", "Farfetch'd");
        mPokemonsMap.put("84", "Doduo");
        mPokemonsMap.put("85", "Dodrio");
        mPokemonsMap.put("86", "Seel");
        mPokemonsMap.put("87", "Dewgong");
        mPokemonsMap.put("88", "Grimer");
        mPokemonsMap.put("89", "Muk");
        mPokemonsMap.put("90", "Shellder");
        mPokemonsMap.put("91", "Cloyster");
        mPokemonsMap.put("92", "Gastly");
        mPokemonsMap.put("93", "Haunter");
        mPokemonsMap.put("94", "Gengar");
        mPokemonsMap.put("95", "Onix");
        mPokemonsMap.put("96", "Drowzee");
        mPokemonsMap.put("97", "Hypno");
        mPokemonsMap.put("98", "Krabby");
        mPokemonsMap.put("99", "Kingler");
        mPokemonsMap.put("100", "Voltorb");
        mPokemonsMap.put("101", "Electrode");
        mPokemonsMap.put("102", "Exeggcute");
        mPokemonsMap.put("103", "Exeggutor");
        mPokemonsMap.put("104", "Cubone");
        mPokemonsMap.put("105", "Marowak");
        mPokemonsMap.put("106", "Hitmonlee");
        mPokemonsMap.put("107", "Hitmonchan");
        mPokemonsMap.put("108", "Lickitung");
        mPokemonsMap.put("109", "Koffing");
        mPokemonsMap.put("110", "Weezing");
        mPokemonsMap.put("111", "Rhyhorn");
        mPokemonsMap.put("112", "Rhydon");
        mPokemonsMap.put("113", "Chansey");
        mPokemonsMap.put("114", "Tangela");
        mPokemonsMap.put("115", "Kangaskhan");
        mPokemonsMap.put("116", "Horsea");
        mPokemonsMap.put("117", "Seadra");
        mPokemonsMap.put("118", "Goldeen");
        mPokemonsMap.put("119", "Seaking");
        mPokemonsMap.put("120", "Staryu");
        mPokemonsMap.put("121", "Starmie");
        mPokemonsMap.put("122", "Mr. Mime");
        mPokemonsMap.put("123", "Scyther");
        mPokemonsMap.put("124", "Jynx");
        mPokemonsMap.put("125", "Electabuzz");
        mPokemonsMap.put("126", "Magmar");
        mPokemonsMap.put("127", "Pinsir");
        mPokemonsMap.put("128", "Tauros");
        mPokemonsMap.put("129", "Magikarp");
        mPokemonsMap.put("130", "Gyarados");
        mPokemonsMap.put("131", "Lapras");
        mPokemonsMap.put("132", "Ditto");
        mPokemonsMap.put("133", "Eevee");
        mPokemonsMap.put("134", "Vaporeon");
        mPokemonsMap.put("135", "Jolteon");
        mPokemonsMap.put("136", "Flareon");
        mPokemonsMap.put("137", "Porygon");
        mPokemonsMap.put("138", "Omanyte");
        mPokemonsMap.put("139", "Omastar");
        mPokemonsMap.put("140", "Kabuto");
        mPokemonsMap.put("141", "Kabutops");
        mPokemonsMap.put("142", "Aerodactyl");
        mPokemonsMap.put("143", "Snorlax");
        mPokemonsMap.put("144", "Articuno");
        mPokemonsMap.put("145", "Zapdos");
        mPokemonsMap.put("146", "Moltres");
        mPokemonsMap.put("147", "Dratini");
        mPokemonsMap.put("148", "Dragonair");
        mPokemonsMap.put("149", "Dragonite");
        mPokemonsMap.put("150", "Mewtwo");
        mPokemonsMap.put("151", "Mew");

    }

    public void getFiltros() {
        SharedPreferences prefs_pokeWhere = mContext.getSharedPreferences(Constants.PREFS_POKEWHERE, mContext.MODE_PRIVATE);

        mAllMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_ALL_MARKERS, false);
        mBusquedaMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_BUSQUEDA_MARKERS, false);
        mNormalPokeStopMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_NORMAL_POKESTOPS_MARKERS, false);
        mLuredPokeStopMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_LURED_POKESTOPS_MARKERS, false);
        mBlueGymMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_BLUE_GYMS_MARKERS, false);
        mRedGymMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_RED_GYMS_MARKERS, false);
        mYellowGymsMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_YELLOW_GYMS_MARKERS, false);
        mWhiteGymsMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_WHITE_GYMS_MARKERS, false);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean containsEncounteredId(Object object, String enconunteredId) {

        if (object instanceof LocalPokemon) {
            for (LocalPokemon localPokemon : mLocalPokemons) {
                if (String.valueOf(localPokemon.getId()).equals(enconunteredId)) {
                    return true;
                }
            }
        } else if (object instanceof LocalGym) {
            for (LocalGym localGym : mLocalGyms) {
                if (String.valueOf(localGym.getId()).equals(enconunteredId)) {
                    return true;
                }
            }
        } else {
            for (LocalPokeStop localPokeStop : mLocalPokeStops) {
                if (String.valueOf(localPokeStop.getId()).equals(enconunteredId)) {
                    return true;
                }
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return false;
    }

    public Boolean isinRange(LocalPokeStop localPokeStop) {

        Boolean isInRange;

        double earthRadius = 6371000; //meters

        double dLat = Math.toRadians(localPokeStop.getLatitude() - userPosition.latitude);
        double dLng = Math.toRadians(localPokeStop.getLongitude() - userPosition.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userPosition.latitude)) * Math.cos(Math.toRadians(localPokeStop.getLatitude())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        if (dist < 90) {
            isInRange = true;
        } else {
            isInRange = false;
        }

        return isInRange;
    }

    public boolean shouldMarkerRemove(Object object) {

        Boolean remove = false;

        if (object instanceof LocalGym || object instanceof LocalPokeStop) {

            double earthRadius = 6371000; //meters

            double dLat;
            double dLng;
            double a = 0.0f;

            if (object instanceof LocalGym) {
                dLat = Math.toRadians(((LocalGym) object).getLatitude() - userPosition.latitude);
                dLng = Math.toRadians(((LocalGym) object).getLongitude() - userPosition.longitude);
                a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(userPosition.latitude)) * Math.cos(Math.toRadians(((LocalGym) object).getLatitude())) *
                                Math.sin(dLng / 2) * Math.sin(dLng / 2);
            }
            else if (object instanceof LocalPokeStop) {
                dLat = Math.toRadians(((LocalPokeStop) object).getLatitude() - userPosition.latitude);
                dLng = Math.toRadians(((LocalPokeStop) object).getLongitude() - userPosition.longitude);
                a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(userPosition.latitude)) * Math.cos(Math.toRadians(((LocalPokeStop) object).getLatitude())) *
                                Math.sin(dLng / 2) * Math.sin(dLng / 2);
            }


            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            float dist = (float) (earthRadius * c);

            if (dist > 150) {
                remove = true;
            } else {
                remove = false;
            }


        } else if (object instanceof LocalPokemon) {
            //LOCAL POKEMON
            //Get the local time
            Calendar localCalendar = Calendar.getInstance(Locale.getDefault());
            int localMinutes = localCalendar.get(Calendar.MINUTE);
            int localSeconds = localCalendar.get(Calendar.SECOND);

            //Getting the pokemon time expiration
            Date date = new Date(((LocalPokemon) object).getExpiration_time());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int expirationMinutes = calendar.get(Calendar.MINUTE);
            int expirationSeconds = calendar.get(Calendar.SECOND);


            int diferrenceMinutes = expirationMinutes - localMinutes;
            int diferrenceSeconds = expirationSeconds - localSeconds;


            if (diferrenceMinutes < 1) {
                if (diferrenceSeconds < 10) {
                    //removeMarker
                    remove = true;
                }
            } else {
                remove = false;
            }
        } else if (object instanceof Long) {
            //Get the local time
            Calendar localCalendar = Calendar.getInstance(Locale.getDefault());
            int localMinutes = localCalendar.get(Calendar.MINUTE);

            //Getting the pokemon time expiration
            Date date = new Date((Long) object);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int expirationMinutes = calendar.get(Calendar.MINUTE);

            int diferrenceMinutes = localMinutes - expirationMinutes;

            if (diferrenceMinutes >= 2) {

                remove = true;
            } else {
                remove = false;
            }
        }

        return remove;
    }

    private class CounterToRemoveMarkers extends CountDownTimer {

        public CounterToRemoveMarkers(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mCounterToRemoveMarkers = null;
            mCounterToRemoveMarkers = new CounterToRemoveMarkers(60000, 10000);
            mCounterToRemoveMarkers.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mMarkers != null) {
                final List<Marker> mMarkersToRemove = new ArrayList<>();
                for (Marker marker : mMarkers) {
                    if (shouldMarkerRemove(marker.getTag())) {
                        mMarkersToRemove.add(marker);
                        marker.remove();

                        Object object = marker.getTag();

                        if (object instanceof LocalPokeStop){
                            mLocalPokeStops.remove(object);
                        }
                        else if(object instanceof  LocalGym){
                            mLocalGyms.remove(object);
                        }
                        else if (object instanceof LocalPokemon){
                            mLocalPokemons.remove(object);
                        }
                    }
                }

                for (Marker markerToRemove : mMarkersToRemove) {
                    mMarkers.remove(markerToRemove);
                }
            }
        }
    }

    public void showToast(final String message){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext,message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showCustomDialog(){

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
                .title(getString(R.string.dialog_title_unaviable_service))
                .content(getString(R.string.dialog_content_unaviable_service))
                .autoDismiss(false)
                .cancelable(false)
                .positiveText(getString(R.string.dialog_positive_unaviable_service))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        ActivityDashboard.mDrawerLayout.openDrawer(ActivityDashboard.mNavigationView);
                    }
                });

        MaterialDialog dialog = builder.build();
        dialog.show();
    }
}