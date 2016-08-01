package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.javic.pokewhere.models.Pokemon;
import com.javic.pokewhere.services.FetchAddressIntentService;
import com.javic.pokewhere.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentMap extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FragmentMap.class.getSimpleName();

    public static final int PERMISSION_ACCESS_COARSE_LOCATION =0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int ALERT_ADDRESS_RESULT_RECIVER =0;
    public static final int ALERT_PERMISSION_DENNIED =1;

    private Context mContext;

    private OnFragmentInteractionListener mListener;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private LatLng userPosition;

    private Map<String,String> mPokemonsMap = new HashMap<String, String>();
    private List<Pokemon> mPokemons = new ArrayList<>();


    private View mView;
    private GoogleMap mGoogleMap;
    private MapView mapView;
    public static FloatingSearchView mSearchView;
    private FloatingActionButton mGetPokemonsButton;
    private ImageView mUserMarker;

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

    // TODO: Rename and change types and number of parameters
    public static FragmentMap newInstance() {
        FragmentMap fragment = new FragmentMap();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapsInitializer.initialize(mContext);
        setUpData();
        mResultReceiver = new AddressResultReceiver(new Handler());

        // First we need to check availability of play services
        if (checkPlayServices()){
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
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // Check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            if (mGoogleApiClient==null){
                buildGoogleApiClient();
            }
            else{

                if (mLastLocation != null) {
                    userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));
                }
                else{
                    Log.i(TAG,"We can't center the map");
                    //getLocation();
                }

            }

        }


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) mView.findViewById(R.id.map);
        mSearchView = (FloatingSearchView) mView.findViewById(R.id.floating_search_view);
        mUserMarker = (ImageView) mView.findViewById(R.id.user_marker);
        mGetPokemonsButton = (FloatingActionButton) mView.findViewById(R.id.fab);

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    public void getPokemons(String LatLong){

        Log.i(TAG, "URL request --> " + Constants.URL_FEED+ LatLong);

        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                Constants.URL_FEED+ LatLong, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

    }

    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("pokemon");

            Log.i(TAG, "user position:" + userPosition.latitude + " Longitude: " + userPosition.longitude);

            mPokemons.clear();
            
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                Pokemon pokemon = new Pokemon();
                pokemon.setId(feedObj.getLong("id"));
                //pokemon.setData(feedObj.getString("data"));
                pokemon.setExpiration_time(feedObj.getLong("expiration_time"));
                pokemon.setPokemonId(feedObj.getLong("pokemonId"));
                pokemon.setPokemonName(mPokemonsMap.get(String.valueOf(feedObj.getLong("pokemonId"))));
                pokemon.setLatitude(feedObj.getDouble("latitude"));
                pokemon.setLongitude(feedObj.getDouble("longitude"));
                //pokemon.setUid(feedObj.getString("uid"));
                //pokemon.setIs_alive(feedObj.getBoolean("is_alive"));

                mPokemons.add(pokemon);

                Log.i(TAG, String.valueOf(pokemon.getPokemonName()));
            }

            if (!mPokemons.isEmpty()){
                // notify data changes to list adapater
                drawPokemons();
            }
            else{
                showMessage(getString(R.string.message_json_request_empty));
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(mContext);

        mGoogleMap = googleMap;
        setUpGoogleMap();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                        setUpGoogleMap();

                } else {
                    // permission denied, boo!

                    showAlert(ALERT_PERMISSION_DENNIED);
                }
                return;
            }
        }
    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    /**
     * Method to display the location on UI
     * */
    private void getLocation() {

        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

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
            }

            else{
                showAlert(ALERT_ADDRESS_RESULT_RECIVER);
            }
        }
        else {
            showAlert(ALERT_PERMISSION_DENNIED);
        }
    }


    public void drawPokemons(){

        mGoogleMap.clear();

        AssetManager assetManager = mContext.getAssets();

        for (Pokemon pokemon:mPokemons)
        {
            try {
                InputStream is= null;

                if (pokemon.getPokemonId()<10){
                    is = assetManager.open(String.valueOf("00"+pokemon.getPokemonId())+".png");
                }
                else if(pokemon.getPokemonId()<100){
                    is = assetManager.open(String.valueOf("0"+pokemon.getPokemonId())+".png");
                }
                else{
                    is = assetManager.open(String.valueOf(pokemon.getPokemonId())+".png");
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

            showMessage(getString(R.string.message_json_request_succes_1) + " " +String.valueOf(mPokemons.size()) + " " + getString(R.string.message_json_request_succes_2));
        }

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
                Log.i(TAG,getString(R.string.address_found));


                if (resultData.getString(Constants.RESULT_DATA_KEY)!=null){
                    String[] splited = resultData.getString(Constants.RESULT_DATA_KEY).split("\\s+");

                    showMessage(splited[0]);

                    mContext.stopService(new Intent(mContext, FetchAddressIntentService.class));
                }
                else{
                    //You should try to show the user to select their position
                    showAlert(ALERT_ADDRESS_RESULT_RECIVER);
                }

            }

            if( resultCode == Constants.FAILURE_RESULT){
                //You should try to show the user to select their position
                showAlert(ALERT_ADDRESS_RESULT_RECIVER);
            }

        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        mContext.startService(intent);
    }

    public void showMessage(String message){

        Snackbar.make(mView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    public void showAlert(final int action){

        String title, message, positive_btn_title, negative_btn_title;

        if (action==ALERT_ADDRESS_RESULT_RECIVER){
            title = mContext.getResources().getString(R.string.location_alert_title);
            message = mContext.getResources().getString(R.string.location_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.location_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        }
        else {
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

                if(action==0){
                    getLocation();
                }
                else{
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

    public void setUpSearchView(){

/*        //this shows the top left circular progress
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
                    switch (item.getItemId()){
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
                    return;
                }
                else{
                    Geocoder gc = new Geocoder(mContext);

                    try {
                        List<Address> list = gc.getFromLocationName(query, 1);
                        Address address = list.get(0);

                        userPosition = new LatLng(address.getLatitude(), address.getLongitude());

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

                        getPokemons(String.valueOf(userPosition.latitude)+"/"+String.valueOf(userPosition.longitude));


                    }
                    catch (Exception e){

                    }


                }

            }
        });
    }

    public void setUpGoogleMap(){

        if (mGoogleMap!=null){

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                this.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_ACCESS_COARSE_LOCATION);
            }
            else{

                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setCompassEnabled(false);
                mGoogleMap.setOnCameraChangeListener(this);

                mUserMarker.setVisibility(View.VISIBLE);

                mGetPokemonsButton.setVisibility(View.VISIBLE);
                mGetPokemonsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getPokemons(String.valueOf(userPosition.latitude)+"/"+String.valueOf(userPosition.longitude));

                    }
                });
            }

        }
    }

    public static String createDate(long timestamp) {
        Date d = new Date(timestamp *1000);
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String reportDate = df.format(d);
        return reportDate;
    }

    public void setUpData(){

        mPokemonsMap.put("1","Bulbasaur");
        mPokemonsMap.put("2","Ivysaur");
        mPokemonsMap.put("3","Venusaur");
        mPokemonsMap.put("4","Charmander");
        mPokemonsMap.put("5","Charmeleon");
        mPokemonsMap.put("6","Charizard");
        mPokemonsMap.put("7","Squirtle");
        mPokemonsMap.put("8","Wartortle");
        mPokemonsMap.put("9","Blastoise");
        mPokemonsMap.put("10","Caterpie");
        mPokemonsMap.put("11","Metapod");
        mPokemonsMap.put("12","Butterfree");
        mPokemonsMap.put("13","Weedle");
        mPokemonsMap.put("14","Kakuna");
        mPokemonsMap.put("15","Beedrill");
        mPokemonsMap.put("16","Pidgey");
        mPokemonsMap.put("17","Pidgeotto");
        mPokemonsMap.put("18","Pidgeot");
        mPokemonsMap.put("19","Rattata");
        mPokemonsMap.put("20","Raticate");
        mPokemonsMap.put("21","Spearow");
        mPokemonsMap.put("22","Fearow");
        mPokemonsMap.put("23","Ekans");
        mPokemonsMap.put("24","Arbok");
        mPokemonsMap.put("25","Pikachu");
        mPokemonsMap.put("26","Raichu");
        mPokemonsMap.put("27","Sandshrew");
        mPokemonsMap.put("28","Sandslash");
        mPokemonsMap.put("29","Nidoran♀");
        mPokemonsMap.put("30","Nidorina");
        mPokemonsMap.put("31","Nidoqueen");
        mPokemonsMap.put("32","Nidoran♂");
        mPokemonsMap.put("33","Nidorino");
        mPokemonsMap.put("34","Nidoking");
        mPokemonsMap.put("35","Clefairy");
        mPokemonsMap.put("36","Clefable");
        mPokemonsMap.put("37","Vulpix");
        mPokemonsMap.put("38","Ninetales");
        mPokemonsMap.put("39","Jigglypuff");
        mPokemonsMap.put("40","Wigglytuff");
        mPokemonsMap.put("41","Zubat");
        mPokemonsMap.put("42","Golbat");
        mPokemonsMap.put("43","Oddish");
        mPokemonsMap.put("44","Gloom");
        mPokemonsMap.put("45","Vileplume");
        mPokemonsMap.put("46","Paras");
        mPokemonsMap.put("47","Parasect");
        mPokemonsMap.put("48","Venonat");
        mPokemonsMap.put("49","Venomoth");
        mPokemonsMap.put("50","Diglett");
        mPokemonsMap.put("51","Dugtrio");
        mPokemonsMap.put("52","Meowth");
        mPokemonsMap.put("53","Persian");
        mPokemonsMap.put("54","Psyduck");
        mPokemonsMap.put("55","Golduck");
        mPokemonsMap.put("56","Mankey");
        mPokemonsMap.put("57","Primeape");
        mPokemonsMap.put("58","Growlithe");
        mPokemonsMap.put("59","Arcanine");
        mPokemonsMap.put("60","Poliwag");
        mPokemonsMap.put("61","Poliwhirl");
        mPokemonsMap.put("62","Poliwrath");
        mPokemonsMap.put("63","Abra");
        mPokemonsMap.put("64","Kadabra");
        mPokemonsMap.put("65","Alakazam");
        mPokemonsMap.put("66","Machop");
        mPokemonsMap.put("67","Machoke");
        mPokemonsMap.put("68","Machamp");
        mPokemonsMap.put("69","Bellsprout");
        mPokemonsMap.put("70","Weepinbell");
        mPokemonsMap.put("71","Victreebel");
        mPokemonsMap.put("72","Tentacool");
        mPokemonsMap.put("73","Tentacruel");
        mPokemonsMap.put("74","Geodude");
        mPokemonsMap.put("75","Graveler");
        mPokemonsMap.put("76","Golem");
        mPokemonsMap.put("77","Ponyta");
        mPokemonsMap.put("78","Rapidash");
        mPokemonsMap.put("79","Slowpoke");
        mPokemonsMap.put("80","Slowbro");
        mPokemonsMap.put("81","Magnemite");
        mPokemonsMap.put("82","Magneton");
        mPokemonsMap.put("83","Farfetch'd");
        mPokemonsMap.put("84","Doduo");
        mPokemonsMap.put("85","Dodrio");
        mPokemonsMap.put("86","Seel");
        mPokemonsMap.put("87","Dewgong");
        mPokemonsMap.put("88","Grimer");
        mPokemonsMap.put("89","Muk");
        mPokemonsMap.put("90","Shellder");
        mPokemonsMap.put("91","Cloyster");
        mPokemonsMap.put("92","Gastly");
        mPokemonsMap.put("93","Haunter");
        mPokemonsMap.put("94","Gengar");
        mPokemonsMap.put("95","Onix");
        mPokemonsMap.put("96","Drowzee");
        mPokemonsMap.put("97","Hypno");
        mPokemonsMap.put("98","Krabby");
        mPokemonsMap.put("99","Kingler");
        mPokemonsMap.put("100","Voltorb");
        mPokemonsMap.put("101","Electrode");
        mPokemonsMap.put("102","Exeggcute");
        mPokemonsMap.put("103","Exeggutor");
        mPokemonsMap.put("104","Cubone");
        mPokemonsMap.put("105","Marowak");
        mPokemonsMap.put("106","Hitmonlee");
        mPokemonsMap.put("107","Hitmonchan");
        mPokemonsMap.put("108","Lickitung");
        mPokemonsMap.put("109","Koffing");
        mPokemonsMap.put("110","Weezing");
        mPokemonsMap.put("111","Rhyhorn");
        mPokemonsMap.put("112","Rhydon");
        mPokemonsMap.put("113","Chansey");
        mPokemonsMap.put("114","Tangela");
        mPokemonsMap.put("115","Kangaskhan");
        mPokemonsMap.put("116","Horsea");
        mPokemonsMap.put("117","Seadra");
        mPokemonsMap.put("118","Goldeen");
        mPokemonsMap.put("119","Seaking");
        mPokemonsMap.put("120","Staryu");
        mPokemonsMap.put("121","Starmie");
        mPokemonsMap.put("122","Mr. Mime");
        mPokemonsMap.put("123","Scyther");
        mPokemonsMap.put("124","Jynx");
        mPokemonsMap.put("125","Electabuzz");
        mPokemonsMap.put("126","Magmar");
        mPokemonsMap.put("127","Pinsir");
        mPokemonsMap.put("128","Tauros");
        mPokemonsMap.put("129","Magikarp");
        mPokemonsMap.put("130","Gyarados");
        mPokemonsMap.put("131","Lapras");
        mPokemonsMap.put("132","Ditto");
        mPokemonsMap.put("133","Eevee");
        mPokemonsMap.put("134","Vaporeon");
        mPokemonsMap.put("135","Jolteon");
        mPokemonsMap.put("136","Flareon");
        mPokemonsMap.put("137","Porygon");
        mPokemonsMap.put("138","Omanyte");
        mPokemonsMap.put("139","Omastar");
        mPokemonsMap.put("140","Kabuto");
        mPokemonsMap.put("141","Kabutops");
        mPokemonsMap.put("142","Aerodactyl");
        mPokemonsMap.put("143","Snorlax");
        mPokemonsMap.put("144","Articuno");
        mPokemonsMap.put("145","Zapdos");
        mPokemonsMap.put("146","Moltres");
        mPokemonsMap.put("147","Dratini");
        mPokemonsMap.put("148","Dragonair");
        mPokemonsMap.put("149","Dragonite");
        mPokemonsMap.put("150","Mewtwo");
        mPokemonsMap.put("151","Mew");

    }
}
