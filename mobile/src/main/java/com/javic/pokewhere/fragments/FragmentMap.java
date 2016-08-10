package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.javic.pokewhere.R;
import com.javic.pokewhere.app.AppController;
import com.javic.pokewhere.models.PokeStop;
import com.javic.pokewhere.models.Pokemon;
import com.javic.pokewhere.services.FetchAddressIntentService;
import com.javic.pokewhere.util.Constants;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.OkHttpClient;

import static android.Manifest.permission.READ_CONTACTS;

public class FragmentMap extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FragmentMap.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String mParamUser;
    private String mParamPass;
    private String mParamRefreshToken;


    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Context mContext;
    private OnFragmentInteractionListener mListener;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private LatLng userPosition;


    // FragmentMap UI
    private View mView;
    private GoogleMap mGoogleMap;
    private MapView mapView;
    public static FloatingSearchView mSearchView;
    private FloatingActionButton mGetPokemonsButton;
    private ImageView mUserMarker;

    // API PokemonGO
    private OkHttpClient httpClient = new OkHttpClient();
    private PokemonGo go;
    private PokemonsTask mpokemonTask;
    private Map<String, String> mPokemonsMap = new HashMap<String, String>();
    private List<Pokemon> mPokemons = new ArrayList<>();
    private List<PokeStop> mPokeStops = new ArrayList<>();

    Snackbar mSnackBar;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int action);
    }


    public FragmentMap() {
        // Required empty public constructor
    }

    /**
     * @param paramUser User.
     * @param paramPass Password.
     * @return A new instance of fragment FragmentMap.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMap newInstance(String paramUser, String paramPass) {
        FragmentMap fragment = new FragmentMap();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER, paramUser);
        args.putString(Constants.ARG_PASS, paramPass);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * @param paramRefreshToken Google token.
     * @return A new instance of fragment FragmentMap.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMap newInstance(String paramRefreshToken) {
        FragmentMap fragment = new FragmentMap();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_REFRESHTOKEN, paramRefreshToken);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            if (getArguments().getString(Constants.ARG_REFRESHTOKEN) != null) {
                mParamRefreshToken = getArguments().getString(Constants.ARG_REFRESHTOKEN);
            } else {
                mParamUser = getArguments().getString(Constants.ARG_USER);
                mParamPass = getArguments().getString(Constants.ARG_PASS);
            }
        }

        MapsInitializer.initialize(mContext);

        mResultReceiver = new AddressResultReceiver(new Handler());

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

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

        setUpData();

        setUpSearchView();

        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            mapView.onResume();
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // Check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            if (mGoogleApiClient == null) {

                if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()){
                    buildGoogleApiClient();
                }
            } else {

                if (mLastLocation != null) {
                    userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));
                } else {
                    Log.i(TAG, "We can't center the map");
                    //getLocation();
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mpokemonTask != null) {
            if (mpokemonTask.getStatus() == AsyncTask.Status.RUNNING) {
                mpokemonTask.cancel(true);
            }
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
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

        if (!mayRequestLocation()) {
            return;
        }

        if (go == null) {
            if (isDeviceOnline()) {
                connectWithPokemonGO();
            }
        }

        setUpGoogleMap();

    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    /**
     * Method to display the location on UI
     */
    private void getLocation() {

        if (!mayRequestLocation()) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

            // Determine whether a Geocoder is available.
                /*if (!Geocoder.isPresent()) {
                    Toast.makeText(mContext, R.string.no_geocoder_available,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                startIntentService();*/
        } else {
            showAlert(ALERT_ADDRESS_RESULT_RECIVER);
        }

    }

    private boolean mayRequestLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpGoogleMap();
            }
        }
    }

    public void drawPokemon(Pokemon pokemon) {

        Log.i(TAG, pokemon.getPokemonName());

        AssetManager assetManager = mContext.getAssets();

        try {
            InputStream is = null;

            if (pokemon.getPokemonId() < 10) {
                is = assetManager.open(String.valueOf("00" + pokemon.getPokemonId()) + ".png");
            } else if (pokemon.getPokemonId() < 100) {
                is = assetManager.open(String.valueOf("0" + pokemon.getPokemonId()) + ".png");
            } else {
                is = assetManager.open(String.valueOf(pokemon.getPokemonId()) + ".png");
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))
                    .title(pokemon.getPokemonName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .snippet(createDate(pokemon.getExpiration_time()))
            );

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void drawPokeStop(PokeStop pokestop)  {

        //Log.i(TAG, pokestop.getName());

        int resourceId;


        resourceId= R.drawable.ic_pokestop;

            /*if (!pokestop.getHasLure()) {
                resourceId= R.drawable.ic_pokestop;
            }
            else{
                resourceId= R.drawable.ic_pokestope_lucky;
            }*/

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pokestop.getLatitude(), pokestop.getLongitude()))
                    //.title(pokestop.getName())
                    .icon(BitmapDescriptorFactory.fromResource(resourceId))
                    //.snippet(pokestop.getDescription())
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
        getLocation();
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

    protected void startIntentService() {
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        mContext.startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }


        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.i(TAG, getString(R.string.address_found));


                if (resultData.getString(Constants.RESULT_DATA_KEY) != null) {
                    String[] splited = resultData.getString(Constants.RESULT_DATA_KEY).split("\\s+");

                    showMessage(splited[0]);

                    mContext.stopService(new Intent(mContext, FetchAddressIntentService.class));
                } else {
                    //You should try to show the user to select their position
                    showAlert(ALERT_ADDRESS_RESULT_RECIVER);
                }

            }

            if (resultCode == Constants.FAILURE_RESULT) {
                //You should try to show the user to select their position
                showAlert(ALERT_ADDRESS_RESULT_RECIVER);
            }

        }
    }

    public void showMessage(String message) {

        Snackbar.make(mView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    /**
     * Represents an asynchronous get pokemons
     * with a location.
     */
    public class PokemonsTask extends AsyncTask<Void, Object, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mPokemons.clear();
            mPokeStops.clear();
            mGoogleMap.clear();

            if (mGetPokemonsButton.getVisibility() == View.VISIBLE) {
                mGetPokemonsButton.setVisibility(View.GONE);
            }

            mSearchView.showProgress();

            mSnackBar = Snackbar.make(mView, R.string.message_snackbar_searching_text, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.location_alert_neg_btn, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            if (mpokemonTask != null) {
                                mpokemonTask.cancel(true);
                            }
                        }
                    });

            mSnackBar.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                for (int i = 0; i < 12; i++) {

                    if (isCancelled())
                        break;

                    LatLng ltn = getLocation(userPosition.longitude, userPosition.latitude, 150);
                    go.setLocation(ltn.latitude, ltn.longitude, 1);

                    publishProgress(ltn);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    Collection<Pokestop> pokeStops = go.getMap().getMapObjects().getPokestops();

                    for (Pokestop pkStop : pokeStops) {

                        PokeStop pokeStop = new PokeStop();
                        pokeStop.setId(pkStop.getId());

                        //pokeStop.setName(pkStop.getDetails().getName());
                        //pokeStop.setHasLure(pkStop.hasLure());
                        //pokeStop.setDescription(pkStop.getDetails().getDescription());
                        //pokeStop.setDistance(pkStop.getDistance());

                        Object obj = pokeStop;
                        if (!containsEncounteredPokeStopId(mPokeStops, pokeStop.getId())) {
                            mPokeStops.add(pokeStop);
                            publishProgress(obj);
                        }

                    }

                    List<CatchablePokemon> chablePokemons = go.getMap().getCatchablePokemon();

                    for (CatchablePokemon pokemon : chablePokemons) {

                        Pokemon poke = new Pokemon();
                        poke.setId(pokemon.getEncounterId());
                        poke.setExpiration_time(pokemon.getExpirationTimestampMs());
                        poke.setPokemonId(pokemon.getPokemonId().getNumber());
                        poke.setPokemonName(mPokemonsMap.get(String.valueOf(poke.getPokemonId())));
                        poke.setLatitude(pokemon.getLatitude());
                        poke.setLongitude(pokemon.getLongitude());

                        Object obj = poke;
                        if (!containsEncounteredId(mPokemons, poke.getId())) {
                            mPokemons.add(poke);
                            publishProgress(obj);
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
        protected void onProgressUpdate(Object... object) {

            if (object[0] instanceof Pokemon){
                drawPokemon((Pokemon) object[0]);
            }

            if (object[0] instanceof LatLng){

                drawLocation((LatLng) object[0]);
            }


            if (object[0] instanceof PokeStop){

                drawPokeStop((PokeStop) object[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean succes) {
            mpokemonTask = null;
            mGetPokemonsButton.setVisibility(View.VISIBLE);
            mSearchView.hideProgress();

            mSnackBar.dismiss();

            if (!mPokemons.isEmpty()) {
                showMessage(getString(R.string.message_json_request_succes_1) + " " + String.valueOf(mPokemons.size()) + " " + getString(R.string.message_json_request_succes_2));
            } else {
                showMessage(getString(R.string.message_json_request_empty));
            }

        }

        @Override
        protected void onCancelled() {
            mpokemonTask = null;

            mGetPokemonsButton.setVisibility(View.VISIBLE);

            if (mSnackBar != null) {
                mSnackBar.dismiss();
            }

            if (mSearchView != null) {
                mSearchView.hideProgress();
            }
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
                        View myLocationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));

                        myLocationButton.performClick();
                        break;
                }
            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHamburger"
        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {

                Log.i(TAG, "onMenuOpened()");

                if (mListener != null) {
                    mListener.onFragmentInteraction(Constants.ACTION_OPEN_DRAWER);
                }
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
    }

    public void setUpGoogleMap() {

        if (mGoogleMap != null) {

                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setCompassEnabled(false);
                mGoogleMap.setOnCameraChangeListener(this);

                mSearchView.setVisibility(View.VISIBLE);
                mUserMarker.setVisibility(View.VISIBLE);
        }
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
                    getLocation();
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
        Date d = new Date(timestamp * 1000);
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String reportDate = df.format(d);
        return reportDate;
    }

    public void connectWithPokemonGO() {

        new Thread(new Runnable() {
            public void run() {
                try {

                    if (mParamRefreshToken == null) {
                        //User is logged in with username and password
                        go = new PokemonGo(new PtcCredentialProvider(httpClient, mParamUser, mParamPass), httpClient);
                    } else {
                        //User is logged in with Google Account
                        go = new PokemonGo(new GoogleUserCredentialProvider(httpClient, mParamRefreshToken), httpClient);
                    }

                    /*String userName = go.getPlayerProfile().getPlayerData().getUsername();

                    showMessage("Bienvenido: " + userName);*/

                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Your code to run in GUI thread here
                                mGetPokemonsButton.setVisibility(View.VISIBLE);

                                mGetPokemonsButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        mpokemonTask = new PokemonsTask();
                                        mpokemonTask.execute();
                                    }
                                });
                            }
                        });

                    }

                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public static LatLng getLocation(double x0, double y0, int radius) {
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

        return new LatLng(foundLatitude, foundLongitude);
    }

    public static boolean containsEncounteredId(List<Pokemon> c, long enconunteredId) {
        for (Pokemon pokemon : c) {
            if (pokemon.getId() == enconunteredId) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsEncounteredPokeStopId(List<PokeStop> c, String enconunteredId) {
        for (PokeStop pokeStop : c) {
            if (pokeStop.getId().equals(enconunteredId)) {
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
}