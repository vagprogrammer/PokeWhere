package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.ActivityFiltros;
import com.javic.pokewhere.R;
import com.javic.pokewhere.adapters.AdapterCatchablePokemon;
import com.javic.pokewhere.broadcast.BroadcastScheduleStartServiceMap;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.models.PlaceSuggestion;
import com.javic.pokewhere.models.PokemonMove;
import com.javic.pokewhere.services.ServiceMapObjects;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.api.settings.PokeballSelector;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.NoSuchItemException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class FragmentMapa extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks,
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

    private LinearLayout mBottomSheet;
    private AdapterCatchablePokemon mAdapter;
    private ViewPager mViewPager;
    private BottomSheetBehavior bsb;


    // API PokemonGO
    private static PokemonGo mPokemonGo;
    private PokemonsTask mPokemonTask;

    //Marcadores y listas
    private List<LocalUserPokemon> mLocalPokemons = new ArrayList<>();
    private List<Marker> mMarkers = new ArrayList();
    private ArrayList<PlaceSuggestion> mPlaceSuggestionList = new ArrayList<>();

    //Filtros
    private Boolean mAllMarkers;
    private Boolean mBusquedaMarkers;

    //Variables
    private Context mContext;
    private LatLng mSearchPoint;
    private String mLastQuery = "";
    private Circle mMapCircleSearchPoint;
    private Circle mMapCircleRandomPoint;

    //Class and interface
    private CounterToRemoveMarkers mCounterToRemoveMarkers;
    private OnFragmentListener mListener;

    //Handlers
    HandlerThread mHandlerThread;
    Handler mThreadHandler;


    private ServiceMapObjects s;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            ServiceMapObjects.MyBinder b = (ServiceMapObjects.MyBinder) binder;
            s = b.getService();

            Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT)
                    .show();
        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };


    public FragmentMapa() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(mContext);

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

        //Instantiate Counter
        mCounterToRemoveMarkers = new CounterToRemoveMarkers(60000, 10000);
    }

    @Override
    public void onResume() {
        super.onResume();

        //First we need to check if the GoogleMap was not created in onViewCreated
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

        Intent intent= new Intent(mContext, ServiceMapObjects.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (s!=null) {
            mLocalPokemons = s.getLocalUserPokemonList();

            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        //Cnacel
        cancelTask();

        if (mGoogleMap != null) {

            if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }
        }

        mContext.unbindService(mConnection);
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

        if (mPokemonGo != null) {

            mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_MAPA);

            mapView = (MapView) mView.findViewById(R.id.map);
            mSearchView = (FloatingSearchView) mView.findViewById(R.id.floating_search_view);
            mUserMarker = (ImageView) mView.findViewById(R.id.user_marker);
            mGetPokemonsButton = (FloatingActionButton) mView.findViewById(R.id.fab);

            mAdapter = new AdapterCatchablePokemon(mContext, mLocalPokemons);
            mBottomSheet = (LinearLayout) mView.findViewById(R.id.bottomSheet);
            mViewPager = (ViewPager) mView.findViewById(R.id.pager);
            mViewPager.setClipToPadding(false);
            mViewPager.setPageMargin(40);
            mViewPager.setAdapter(mAdapter);


            bsb = BottomSheetBehavior.from(mBottomSheet);
            bsb.setState(BottomSheetBehavior.STATE_HIDDEN);

            bsb.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    String nuevoEstado = "";

                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            nuevoEstado = "STATE_COLLAPSED";
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            nuevoEstado = "STATE_EXPANDED";
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            nuevoEstado = "STATE_HIDDEN";
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            nuevoEstado = "STATE_DRAGGING";
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            nuevoEstado = "STATE_SETTLING";
                            break;
                    }

                    Log.i("BottomSheets", "Nuevo estado: " + nuevoEstado);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    Log.i("BottomSheets", "Offset: " + slideOffset);
                }
            });


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

            getFiltros();
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
    public boolean onMarkerClick(Marker marker) {

        if (marker.getTag() instanceof LocalUserPokemon) {

            LocalUserPokemon localUserPokemon = (LocalUserPokemon) marker.getTag();

            int index = mLocalPokemons.indexOf(localUserPokemon);
            mViewPager.setCurrentItem(index);

            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }


        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {

        Log.i(TAG, "Change Camera to Latitude:" + point.latitude + " Longitude: " + point.longitude);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, Constants.USER_ZOOM));

        cancelTask();

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

            if (mLastLocation != null) {

                userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

            } else {
                showAlert(Constants.ALERT_ADDRESS_RESULT_RECIVER);
            }
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
                getUserLocation();
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

    public void drawPokemon(LocalUserPokemon localPokemon) {

        AssetManager assetManager = mContext.getAssets();

        try {
            InputStream is = null;

           /* if (localPokemon.getPokemonId() < 10) {
                is = assetManager.open(String.valueOf("00" + localPokemon.getNumber()) + ".ico");
            } else if (localPokemon.getPokemonId() < 100) {
                is = assetManager.open(String.valueOf("0" + localPokemon.getNumber()) + ".ico");
            } else {
                is = assetManager.open(String.valueOf(localPokemon.getNumber()) + ".ico");
            }*/

            if (localPokemon.getNumber() < 10) {
                is = assetManager.open(String.valueOf("00" + localPokemon.getNumber()) + ".png");
            } else if (localPokemon.getNumber() < 100) {
                is = assetManager.open(String.valueOf("0" + localPokemon.getNumber()) + ".png");
            } else {
                is = assetManager.open(String.valueOf(localPokemon.getNumber()) + ".png");
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(localPokemon.getLatitude(), localPokemon.getLongitude()))
                    .title(localPokemon.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .snippet(createDate(localPokemon.getExpirationTimeMs()))
            );

            mMarker.setTag(localPokemon);
            mMarkers.add(mMarker);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Google api callback methods
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        getUserLocation();
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


        startService();


       /* userPosition = mGoogleMap.getCameraPosition().target;

        mMapCircleSearchPoint = mGoogleMap.addCircle(new CircleOptions()
                .center(userPosition)
                .radius(100)
                .strokeColor(getResources().getColor(R.color.colorPrimaryDark))
                .fillColor(getResources().getColor(R.color.colorPrimaryD)));

        if (mPokemonTask == null) {
            mPokemonTask = new PokemonsTask();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokemonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokemonTask.execute();
            }
        }

        if (mCounterToRemoveMarkers == null) {
            mCounterToRemoveMarkers.start();
        }

        mGetPokemonsButton.setVisibility(View.GONE);

        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

        //onlyCancel
        if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED || bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        mSnackBar = Snackbar.make(mView, R.string.message_snackbar_searching_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snack_bar_neg_btn, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        cancelTask();
                    }
                });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mSnackBar.getView().getLayoutParams();
        params.gravity = Gravity.TOP;
        mSnackBar.getView().setLayoutParams(params);
        mSnackBar.show();

        mSearchView.showProgress();*/
    }

    public void startService(){

        Intent intent = new Intent(BroadcastScheduleStartServiceMap.ACTION_SHEDULE);

        Bundle extras1 = new Bundle();
        extras1.putString("action", "schedule");
        intent.putExtras(extras1);
        mContext.sendBroadcast(intent);

        Bundle extras2 = new Bundle();
        extras2.putString("action", "cancel");
        intent.putExtras(extras2);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_NO_CREATE);

        Intent notificationIntent = new Intent(mContext, ActivityDashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE);

        // Cancel all previous notifications
        NotificationManagerCompat.from(mContext).cancelAll();

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        // Set a small icon that will appear in the upper right corner of the // notification card
        builder.setOngoing(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        // Content title and description
        builder.setContentTitle("PokéEasy");
        builder.setContentText("PokéEasy se está ejecutando");

        // Set a content intent to return to this example.
        builder.setContentIntent(pendingIntent);

        builder.addAction(R.drawable.ic_clear_black_24dp, "CERRAR PokéEASY", pendingIntent2);

        // Get an instance of the NotificationManagerCompat service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        // Build the notification and notify it using notification manager.
        notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());

    }

    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class PokemonsTask extends AsyncTask<Void, Object, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                while (!isCancelled()) {

                    if (mSearchPoint == null) {
                        mSearchPoint = new LatLng(userPosition.latitude, userPosition.longitude);
                    } else {
                        mSearchPoint = getRandomeLocation(userPosition.longitude, userPosition.latitude, 100);
                    }

                    publishProgress(mSearchPoint);

                    mPokemonGo.setLocation(mSearchPoint.latitude, mSearchPoint.longitude, 1);

                    try {
                        List<CatchablePokemon> catchablePokemonList = mPokemonGo.getMap().getCatchablePokemon();

                        int pokeballs = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_POKE_BALL).getCount();
                        int greatBalls = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL).getCount();
                        int ultraBalls = mPokemonGo.getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL).getCount();

                        Log.i(TAG, "POKEBALL:" + String.valueOf(pokeballs) + " GREAT_BALL: " + String.valueOf(greatBalls) + " ULTRA_BALL: " + String.valueOf(ultraBalls));

                        if (catchablePokemonList != null) {

                            for (CatchablePokemon catchablePokemon : catchablePokemonList) {

                                // You need to Encounter first.
                                EncounterResult encResult = catchablePokemon.encounterPokemon();

                                if (encResult.wasSuccessful()) {

                                    PokemonDataOuterClass.PokemonData pokemonData = encResult.getPokemonData();

                                    LocalUserPokemon localUserPokemon = new LocalUserPokemon();

                                    localUserPokemon.setId(catchablePokemon.getEncounterId());
                                    localUserPokemon.setExpirationTimeMs(catchablePokemon.getExpirationTimestampMs());
                                    localUserPokemon.setNumber(catchablePokemon.getPokemonId().getNumber());
                                    localUserPokemon.setName(catchablePokemon.getPokemonId().name());
                                    localUserPokemon.setLatitude(catchablePokemon.getLatitude());
                                    localUserPokemon.setLongitude(catchablePokemon.getLongitude());
                                    localUserPokemon.setBitmap(getBitmapFromAssets(catchablePokemon.getPokemonId().getNumber()));
                                    localUserPokemon.setCp(pokemonData.getCp());
                                    Double iv_ratio = ((pokemonData.getIndividualAttack() + pokemonData.getIndividualDefense() + pokemonData.getIndividualStamina()) / 45.0) * (100.0);
                                    localUserPokemon.setIv(iv_ratio.intValue());
                                    localUserPokemon.setAttack(pokemonData.getIndividualAttack());
                                    localUserPokemon.setDefense(pokemonData.getIndividualDefense());
                                    localUserPokemon.setStamina(pokemonData.getIndividualStamina());

                                    PokemonMoveMeta moveMeta1 = PokemonMoveMetaRegistry.getMeta(pokemonData.getMove1());
                                    PokemonMoveMeta moveMeta2 = PokemonMoveMetaRegistry.getMeta(pokemonData.getMove2());
                                    PokemonMove move1 = new PokemonMove(moveMeta1.getMove().name(), moveMeta1.getAccuracy(), moveMeta1.getCritChance(), moveMeta1.getEnergy(),moveMeta1.getPower(), moveMeta1.getTime());
                                    PokemonMove move2 = new PokemonMove(moveMeta2.getMove().name(), moveMeta2.getAccuracy(), moveMeta2.getCritChance(), moveMeta2.getEnergy(),moveMeta2.getPower(), moveMeta2.getTime());
                                    final List<PokemonMove> moves = new ArrayList<>();
                                    moves.add(move1);
                                    moves.add(move2);
                                    localUserPokemon.setMoves(moves);

                                    Boolean isEncountered = containsEncounteredId(localUserPokemon, String.valueOf(localUserPokemon.getId()));

                                    if (!isEncountered) {

                                        Log.i(TAG, encResult.getPokemonData().getPokemonId() + " Encountered..." + " CP: " + encResult.getPokemonData().getCp() + " ExpirationTime: " + String.valueOf(catchablePokemon.getExpirationTimestampMs()));
                                        mLocalPokemons.add(localUserPokemon);
                                        publishProgress(localUserPokemon);
                                    }

                                    if (Constants.DEBUG_MODE) {
                                        catchPokemon(catchablePokemon);
                                    }
                                }
                            }

                            sleep(10000);
                        }
                    } catch (LoginFailedException | RemoteServerException e) {
                        Log.e(TAG, "Failed to get pokemons or server issue Login or RemoteServer exception: ", e);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get pokemons or server issue General exception: ", e);
            }

            return false;

        }

        @Override
        protected void onProgressUpdate(Object... obj) {

            super.onProgressUpdate(obj);


            if (obj[0] instanceof LocalUserPokemon){
                drawPokemon((LocalUserPokemon)obj[0]);

                mAdapter.notifyDataSetChanged();

                if (bsb.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
            else{
                if (mMapCircleRandomPoint==null) {
                    mMapCircleRandomPoint = mGoogleMap.addCircle(new CircleOptions()
                            .center((LatLng) obj[0])
                            .radius(40)
                            .strokeColor(getResources().getColor(R.color.black))
                            .fillColor(getResources().getColor(R.color.colorPrimaryD)));
                }
                else{
                    mMapCircleRandomPoint.setCenter((LatLng) obj[0]);
                }
            }


        }

        @Override
        protected void onPostExecute(Boolean succes) {
            mPokemonTask = null;

            if (mSnackBar != null && mSearchView != null && mGetPokemonsButton != null) {
                mSnackBar.dismiss();
                mGetPokemonsButton.setVisibility(View.VISIBLE);
                mSearchView.hideProgress();

                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

                //onlyCancel
                if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED || bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

        }

        @Override
        protected void onCancelled() {
            mPokemonTask = null;
        }
    }

    public void cancelTask() {

        if (mSearchPoint != null) {
            mSearchPoint = null;
        }

        if (mPokemonTask != null) {
            mPokemonTask.cancel(true);
        }

        if (mCounterToRemoveMarkers != null) {
            mCounterToRemoveMarkers.cancel();
        }

        if (mMapCircleSearchPoint != null) {
            mMapCircleSearchPoint.remove();
            mMapCircleSearchPoint =null;
        }
        if (mMapCircleRandomPoint != null) {
            mMapCircleRandomPoint.remove();
            mMapCircleRandomPoint = null;
        }

        Log.i(TAG, "ONLY CANCEL TASK");

        if (mSnackBar != null && mSearchView != null && mGetPokemonsButton != null) {
            mSnackBar.dismiss();
            mGetPokemonsButton.setVisibility(View.VISIBLE);
            mSearchView.hideProgress();

            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

            //onlyCancel
            if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED || bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
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
                            LatLngBounds bounds = new LatLngBounds(new LatLng(18.7887956, -98.3336627), new LatLng(19.0110955, -97.0607641));

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
            mGoogleMap.setOnMarkerClickListener(this);
            mGoogleMap.setOnMapLongClickListener(this);

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
                    getUserLocation();
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
                    mMarkers = new ArrayList();

                    attemptSearch();
                }
            }
        });
        dialog.show();
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

        Log.e(TAG, "Randome Point --> Latitude:" +foundLocation.latitude + " Longuitude: " + foundLocation.longitude);

        return foundLocation;
    }

    public void getFiltros() {
        SharedPreferences prefs_pokeWhere = mContext.getSharedPreferences(Constants.PREFS_POKEWHERE, mContext.MODE_PRIVATE);

        mAllMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_ALL_MARKERS, false);
        mBusquedaMarkers = prefs_pokeWhere.getBoolean(Constants.KEY_PREF_BUSQUEDA_MARKERS, false);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean containsEncounteredId(Object object, String enconunteredId) {

        boolean encountered = false;

        if (object instanceof LocalUserPokemon) {
            for (LocalUserPokemon localUserPokemon : mLocalPokemons) {
                if (String.valueOf(localUserPokemon.getId()).equals(enconunteredId)) {
                    encountered = true;
                }
            }
        }

        //If the encontered id exist, return true, if it doesn't exist return false
        return encountered;
    }

    public boolean shouldMarkerRemove(Object object) {

        Boolean remove = false;

        if (object instanceof LocalUserPokemon) {
            //LOCAL POKEMON
            //Get the local time
            Calendar localCalendar = Calendar.getInstance(Locale.getDefault());
            int localMinutes = localCalendar.get(Calendar.MINUTE);
            int localSeconds = localCalendar.get(Calendar.SECOND);

            //Getting the pokemon time expiration
            Date date = new Date(((LocalUserPokemon) object).getExpirationTimeMs());
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

                        if (marker.getTag() instanceof LocalUserPokemon) {

                            mAdapter.notifyDataSetChanged();

                            mLocalPokemons.remove(marker.getTag());

                        }
                    }
                }

                for (Marker markerToRemove : mMarkersToRemove) {
                    mMarkers.remove(markerToRemove);
                }
            }
        }

    }

    public void showToast(final String message) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void catchPokemon(CatchablePokemon catchablePokemon) {
        try {
            CatchOptions options = new CatchOptions(mPokemonGo)
                    .withPokeballSelector(PokeballSelector.SMART);

            if (options.getRazzberries() > 20) {
                options.useRazzberry(true);
            }

            List<Pokeball> useablePokeballs = mPokemonGo.getInventories().getItemBag().getUseablePokeballs();
            double probability = catchablePokemon.getCaptureProbability();
            Pokeball pokeball = PokeballSelector.SMART.select(useablePokeballs, probability);

            Log.i(TAG, "Attempting to catch: " + catchablePokemon.getPokemonId() + " with " + pokeball
                    + " (" + probability + ")");

            CatchResult result = catchablePokemon.catchPokemon(options);

            Log.i(TAG, catchablePokemon.getPokemonId() + " " + result.getStatus());

            showToast(catchablePokemon.getPokemonId() + " " + result.getStatus());

        } catch (LoginFailedException | NoSuchItemException | RemoteServerException | CaptchaActiveException e) {
            // failed to login, invalid credentials, auth issue or server issue.
            Log.e(TAG, "Failed to login, captcha or server issue: ", e);

        }
    }

    private Bitmap getBitmapFromAssets(int pokemonIdNumber) {
        AssetManager assetManager = mContext.getAssets();

        Bitmap bitmap = null;

        try {
            InputStream is = null;

            if (pokemonIdNumber < 10) {
                is = assetManager.open(String.valueOf("00" + pokemonIdNumber) + ".png");
            } else if (pokemonIdNumber < 100) {
                is = assetManager.open(String.valueOf("0" + pokemonIdNumber) + ".png");
            } else {
                is = assetManager.open(String.valueOf(pokemonIdNumber) + ".png");
            }

            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        }

        return bitmap;
    }

}