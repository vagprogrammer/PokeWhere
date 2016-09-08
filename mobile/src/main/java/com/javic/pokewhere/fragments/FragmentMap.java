package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javic.pokewhere.ActivityDashboard;
import com.javic.pokewhere.interfaces.OnFragmentCreatedViewListener;
import com.javic.pokewhere.R;
import com.javic.pokewhere.models.LocalGym;
import com.javic.pokewhere.models.LocalPokeStop;
import com.javic.pokewhere.models.LocalPokemon;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class FragmentMap extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private OnFragmentCreatedViewListener mListener;

    private static final String TAG = FragmentMap.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private Context mContext;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng userPosition;

    // FragmentMap UI
    private View mView;
    private GoogleMap mGoogleMap;
    private MapView mapView;
    private FloatingSearchView mSearchView;
    private FloatingActionButton mGetPokemonsButton;
    private ImageView mUserMarker;

    // API PokemonGO
    private static PokemonGo mPokemonGo;
    private PokemonsTask mPokemonTask;
    private PokeStopsTask mPokeStopsTask;
    private GymsTask mGymsTask;

    private Map<String, String> mPokemonsMap = new HashMap<String, String>();
    private List<LocalPokemon> mLocalPokemons = new ArrayList<>();
    private List<LocalPokeStop> mLocalPokeStops = new ArrayList<>();
    private List<LocalGym> mLocalGyms = new ArrayList<>();

    private Snackbar mSnackBar;
    private Snackbar mSnackBarPermisions;

    public static Boolean isEnabled = true;


    public FragmentMap() {
        // Required empty public constructor
    }

    /**
     * @param
     * @return A new instance of fragment FragmentMap.
     */
    public static FragmentMap newInstance(PokemonGo pokemonGo) {
        FragmentMap fragment = new FragmentMap();

        mPokemonGo = pokemonGo;

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(mContext);

        setUpData();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentCreatedViewListener) {
            mListener = (OnFragmentCreatedViewListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentCreatedViewListener");
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
        if (mGoogleMap==null){

            // We need to check availability of play services
            if (checkPlayServices()) {

                if (mGoogleApiClient==null){
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
        else{
            if (!mayRequestLocation()) {
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (FragmentMap.isEnabled != null) {
            if (FragmentMap.isEnabled) {
                FragmentMap.isEnabled = false;
            }
        }

        if (mSnackBar != null && mSearchView != null && mGetPokemonsButton != null) {
            mGetPokemonsButton.setVisibility(View.VISIBLE);
            mSnackBar.dismiss();
            mSearchView.hideProgress();
        }

        if (mGoogleMap != null) {

            if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }

        }
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
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
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
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

        mListener.onFragmentCreatedViewStatus(true);

        mGoogleApiClient.connect();

    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
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
            showAlert(ALERT_ADDRESS_RESULT_RECIVER);
        }

    }

    private boolean mayRequestLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (!mGoogleMap.isMyLocationEnabled()){
                mGoogleMap.setMyLocationEnabled(true);
            }

            return true;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!mGoogleMap.isMyLocationEnabled())
            {
                mGoogleMap.setMyLocationEnabled(true);
            }
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {

            if (mSnackBarPermisions==null){
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });
                mSnackBarPermisions.show();
            }
            else{
                if (!mSnackBarPermisions.isShown()){
                    mSnackBarPermisions.show();
                }
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }else{
            if (mSnackBarPermisions==null){
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });

            }
        }


    }

    public void drawPokemon(LocalPokemon localPokemon) {

        Log.i(TAG, localPokemon.getPokemonName());

        AssetManager assetManager = mContext.getAssets();

        try {
            InputStream is = null;

            if (localPokemon.getPokemonId() < 10) {
                is = assetManager.open(String.valueOf("00" + localPokemon.getPokemonId()) + ".png");
            } else if (localPokemon.getPokemonId() < 100) {
                is = assetManager.open(String.valueOf("0" + localPokemon.getPokemonId()) + ".png");
            } else {
                is = assetManager.open(String.valueOf(localPokemon.getPokemonId()) + ".png");
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(localPokemon.getLatitude(), localPokemon.getLongitude()))
                    .title(localPokemon.getPokemonName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .snippet(createDate(localPokemon.getExpiration_time()))
            );

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void drawPokeStop(LocalPokeStop pokestop) {

        int resourceId;

        if (!pokestop.getHasLure()) {
            resourceId = R.drawable.ic_pokestop;
        } else {
            resourceId = R.drawable.ic_pokestope_lucky;
        }

        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(pokestop.getLatitude(), pokestop.getLongitude()))
                .title(pokestop.getName())
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
                .snippet(pokestop.getDescription())
        );
    }

    public void drawGym(LocalGym localGym) {

        int icon_gym;

        switch (localGym.getTeam()) {
            case 1:
                icon_gym= R.drawable.ic_gym_team_yellow;
                break;
            case 2:
                icon_gym= R.drawable.ic_gym_team_blue;
                break;
            case 3:
                icon_gym= R.drawable.ic_gym_team_red;
                break;
            default:
                icon_gym= R.drawable.ic_gym_team_white;
                break;
        }

        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(localGym.getLatitude(), localGym.getLongitude()))
                .title(localGym.getName())
                .icon(BitmapDescriptorFactory.fromResource(icon_gym))
                .snippet(localGym.getDescription())
        );
    }

    public void drawLocation(LatLng position) {

        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(position.latitude, position.longitude)));
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

       /* if (mPokeStopsTask == null) {
            mPokeStopsTask = new PokeStopsTask(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokeStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokeStopsTask.execute();
            }
        }*/

        if (mPokemonTask == null) {
            mPokemonTask = new PokemonsTask(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mPokemonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mPokemonTask.execute();
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


        mSnackBar = Snackbar.make(mView, R.string.message_snackbar_searching_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snack_bar_neg_btn, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {

                        FragmentMap.isEnabled = false;

                        mGetPokemonsButton.setVisibility(View.VISIBLE);
                        mSnackBar.dismiss();
                        mSearchView.hideProgress();
                    }
                });

        mGoogleMap.clear();
        mGetPokemonsButton.setVisibility(View.GONE);
        mSnackBar.show();
        mSearchView.showProgress();

    }

    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class PokemonsTask extends AsyncTask<Void, LocalPokemon, Boolean> {



        PokemonsTask(Boolean isEnabled) {
            FragmentMap.isEnabled = isEnabled;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLocalPokemons.clear();

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            LatLng ltn = null;

            try {
                while (FragmentMap.isEnabled) {

                    if (ltn == null) {
                        ltn = new LatLng(userPosition.longitude, userPosition.latitude);
                    } else {
                        ltn = getLocation(userPosition.longitude, userPosition.latitude, 200);
                    }


                    mPokemonGo.setLocation(ltn.latitude, ltn.longitude, 1);
                    sleep(10000);
                    List<CatchablePokemon> chablePokemons = mPokemonGo.getMap().getCatchablePokemon();

                    for (CatchablePokemon pokemon : chablePokemons) {

                        LocalPokemon poke = new LocalPokemon();
                        poke.setId(pokemon.getEncounterId());
                        poke.setExpiration_time(pokemon.getExpirationTimestampMs());
                        poke.setPokemonId(pokemon.getPokemonId().getNumber());
                        poke.setPokemonName(mPokemonsMap.get(String.valueOf(poke.getPokemonId())));
                        poke.setLatitude(pokemon.getLatitude());
                        poke.setLongitude(pokemon.getLongitude());

                        if (!containsEncounteredPokemonId(mLocalPokemons, poke.getId())) {
                            mLocalPokemons.add(poke);
                            publishProgress(poke);
                        }

                    }
                }
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
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
            FragmentMap.isEnabled = false;
        }

        @Override
        protected void onCancelled() {
            mPokemonTask = null;
            FragmentMap.isEnabled = false;
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
            mLocalPokeStops.clear();
        }

        PokeStopsTask(Boolean isEnabled) {
            FragmentMap.isEnabled = isEnabled;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                while (FragmentMap.isEnabled) {

                    LatLng ltn = getLocation(userPosition.longitude, userPosition.latitude, 200);
                    mPokemonGo.setLocation(ltn.latitude, ltn.longitude, 1);
                    sleep(5000);
                    List<Pokestop> pokeStops = new ArrayList<>(mPokemonGo.getMap().getMapObjects().getPokestops());


                    if (pokeStops != null) {

                        for (Pokestop pkStop: pokeStops) {

                            LocalPokeStop localPokeStop = new LocalPokeStop();
                            localPokeStop.setId(pkStop.getId());
                            Boolean isEncountered = containsEncounteredPokeStopId(mLocalPokeStops, localPokeStop.getId());

                            localPokeStop.setLatitude(pkStop.getLatitude());
                            localPokeStop.setLongitude(pkStop.getLongitude());

                            if (!isEncountered) {

                                sleep(1000);
                                localPokeStop.setHasLure(pkStop.hasLure());
                                localPokeStop.setName(pkStop.getDetails().getName());
                                localPokeStop.setDescription(pkStop.getDetails().getDescription());
                                //localPokeStop.setDistance(pkStop.getDistance());

                                mLocalPokeStops.add(localPokeStop);
                                publishProgress(localPokeStop);

                            }

                        }

                    }

                }

            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(LocalPokeStop... localPokeStop) {

            super.onProgressUpdate(localPokeStop);
            drawPokeStop(localPokeStop[0]);

        }

        @Override
        protected void onPostExecute(Boolean succes) {

            mPokeStopsTask = null;
            FragmentMap.isEnabled = false;
        }

        @Override
        protected void onCancelled() {
            mPokeStopsTask = null;
            FragmentMap.isEnabled = false;
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
            mLocalGyms.clear();

        }

        GymsTask(Boolean isEnabled) {
            FragmentMap.isEnabled = isEnabled;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                while (FragmentMap.isEnabled) {

                    LatLng ltn = getLocation(userPosition.longitude, userPosition.latitude, 200);
                    mPokemonGo.setLocation(ltn.latitude, ltn.longitude, 1);
                    sleep(5000);
                    List<Gym> gyms = new ArrayList<>(mPokemonGo.getMap().getGyms());

                    if (gyms != null) {

                        for (Gym gym : gyms) {
                            LocalGym localGym = new LocalGym();
                            localGym.setId(gym.getId());

                            Boolean isEncountered = containsEncounteredGymId(mLocalGyms, localGym.getId());

                            if (!isEncountered) {

                                sleep(500);
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

                }

            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
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
            FragmentMap.isEnabled = false;
        }

        @Override
        protected void onCancelled() {
            mGymsTask = null;
            FragmentMap.isEnabled = false;
        }
    }

    public void setUpSearchView() {

/*       //this shows the top left circular progress
        //you can call it where ever you want, but
        //it makes sense to do it when loading something in
        //the background.
        mSearchView.showProgress();

        //let the users know that the background
        //process has completed
        mSearchView.hideProgress();*/

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mGetPokemonsButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFocusCleared() {
                mGetPokemonsButton.setVisibility(View.VISIBLE);
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                Log.i(TAG, "onSearchTextChanged()");
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
                        }else {
                            View myLocationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent())
                                    .findViewById(Integer.parseInt("2"));

                            myLocationButton.performClick();
                        }
                        break;
                    case R.id.action_start_service:
                        mListener.onFragmentActionPerform(Constants.ACTION_START_SERVICE);
                        break;
                    default:
                        Log.i(TAG, "Action Default");
                        break;
                }
            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHamburger"
        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {

                Log.i(TAG, "onMenuOpened()");
            }

            @Override
            public void onMenuClosed() {
                Log.i(TAG, "onMenuClosed()");
            }
        });


        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                Log.i(TAG, "onSuggestionClicked()");
            }

            @Override
            public void onSearchAction(String query) {

                Log.i(TAG, "onSearchAction()");

                // Determine whether a Geocoder is available.
                if (!Geocoder.isPresent()) {
                    showMessage(getString(R.string.no_geocoder_available));
                } else {
                    Geocoder gc = new Geocoder(mContext);

                    try {
                        List<Address> list = gc.getFromLocationName(query, 1);
                        Address address = list.get(0);

                        userPosition = new LatLng(address.getLatitude(), address.getLongitude());

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

                        //getPokemons(String.valueOf(userPosition.latitude) + "/" + String.valueOf(userPosition.longitude));


                    } catch (Exception e) {

                    }


                }

            }
        });

        mSearchView.attachNavigationDrawerToMenuButton(ActivityDashboard.mDrawerLayout);
    }

    public void setUpGoogleMap() {

        if (mGoogleMap != null) {

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.setOnCameraChangeListener(this);
            //mGoogleMap.setPadding(0, 0, 0, 150);

            mSearchView.setVisibility(View.VISIBLE);
            mUserMarker.setVisibility(View.VISIBLE);
            mGetPokemonsButton.setVisibility(View.VISIBLE);
        }
    }

    public void showMessage(String message) {

        Snackbar.make(mView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    public void showAlert(final int action) {

        String title, message, positive_btn_title, negative_btn_title;

        if (action == ALERT_ADDRESS_RESULT_RECIVER) {
            title = mContext.getResources().getString(R.string.location_alert_title);
            message = mContext.getResources().getString(R.string.location_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.location_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
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

                if (action == 0) {
                    getUserLocation();
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
                //mContext.startActivity(new Intent(mContext, ActivityEstados.class));
            }
        });
        dialog.show();
    }

    public boolean isDeviceOnline() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

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

    public static String createDate(long timestamp) {

        //Fri Aug 26 19:54:06 CDT 2016
        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String time = "Desaparecerá a las: " + String.valueOf(hours)+ ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds);
        return time;

    }

    public LatLng getLocation(double x0, double y0, int radius) {
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

        Log.i(TAG, "Longitude: " + foundLongitude + "  Latitude: " + foundLatitude);

        final LatLng foundLocation= new LatLng(foundLatitude, foundLongitude);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawLocation(foundLocation);
            }
        });

        return foundLocation;
    }

    public static boolean containsEncounteredPokemonId(List<LocalPokemon> c, long enconunteredId) {
        for (LocalPokemon OPokemon : c) {
            if (OPokemon.getId() == enconunteredId) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEncounteredPokeStopId(List<LocalPokeStop> c, String enconunteredId) {
        for (LocalPokeStop localPokeStop : c) {
            if (localPokeStop.getId().equals(enconunteredId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEncounteredGymId(List<LocalGym> c, String enconunteredGymId) {
        for (LocalGym localGym : c) {
            if (localGym.getId().equals(enconunteredGymId)) {
                return true;
            }
        }
        return false;
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

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}