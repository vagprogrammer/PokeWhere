package com.javic.pokewhere.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentMap extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int PERMISSION_ACCESS_COARSE_LOCATION =0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final int ALERT_ADDRESS_RESULT_RECIVER =0;
    public static final int ALERT_PERMISSION_DENNIED =1;

    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private GoogleMap mGoogleMap;
    private MapView mapView;

    private Context mContext;
    private View mView;

    private Map<String,String> pokemonMap = new HashMap<String, String>();

    private static final String TAG = FragmentMap.class.getSimpleName();

    private List<Pokemon> pokemons = new ArrayList<>();

    private FloatingActionButton fab;


    private LatLng userPosition;

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
                if (userPosition!=null){
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));
                }

            }

        }


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = (FloatingActionButton) mView.findViewById(R.id.fab);

        mapView = (MapView) mView.findViewById(R.id.map);

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

            pokemons.clear();
            
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                Pokemon pokemon = new Pokemon();
                pokemon.setId(feedObj.getLong("id"));
                pokemon.setData(feedObj.getString("data"));
                pokemon.setExpiration_time(feedObj.getLong("expiration_time"));
                pokemon.setPokemonId(feedObj.getLong("pokemonId"));
                pokemon.setPokemonName(pokemonMap.get(String.valueOf(feedObj.getLong("pokemonId"))));
                pokemon.setLatitude(feedObj.getDouble("latitude"));
                pokemon.setLongitude(feedObj.getDouble("longitude"));
                pokemon.setUid(feedObj.getString("uid"));
                pokemon.setIs_alive(feedObj.getBoolean("is_alive"));

                pokemons.add(pokemon);

                Log.i(TAG, String.valueOf(pokemon.getPokemonName()));
            }

            if (!pokemons.isEmpty()){
                // notify data changes to list adapater
                mGoogleMap.clear();
                drawPokemons();
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


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if (userPosition!=null){

                    getPokemons(String.valueOf(userPosition.latitude)+"/"+String.valueOf(userPosition.longitude));
                }
            }
        });
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
                    if (!mGoogleMap.isMyLocationEnabled()){
                        setUpGoogleMap();
                    }

                } else {
                    // permission denied, boo!
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

            } else {
                showAlert(ALERT_PERMISSION_DENNIED);
            }
        }
    }


    public void drawPokemons(){

        AssetManager assetManager = mContext.getAssets();

        for (Pokemon pokemon:pokemons)
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
                        //.snippet(createDate(pokemon.getExpiration_time()))
                );

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

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

        Snackbar.make(mView, message, Snackbar.LENGTH_SHORT)
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

    public void setUpGoogleMap(){

        if (mGoogleMap!=null){

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                this.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_ACCESS_COARSE_LOCATION);
            }
            else{
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setCompassEnabled(false);

            }

        }
    }


    public static CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(d);
    }

    public void setUpData(){

        pokemonMap.put("1","Bulbasaur");
        pokemonMap.put("2","Ivysaur");
        pokemonMap.put("3","Venusaur");
        pokemonMap.put("4","Charmander");
        pokemonMap.put("5","Charmeleon");
        pokemonMap.put("6","Charizard");
        pokemonMap.put("7","Squirtle");
        pokemonMap.put("8","Wartortle");
        pokemonMap.put("9","Blastoise");
        pokemonMap.put("10","Caterpie");
        pokemonMap.put("11","Metapod");
        pokemonMap.put("12","Butterfree");
        pokemonMap.put("13","Weedle");
        pokemonMap.put("14","Kakuna");
        pokemonMap.put("15","Beedrill");
        pokemonMap.put("16","Pidgey");
        pokemonMap.put("17","Pidgeotto");
        pokemonMap.put("18","Pidgeot");
        pokemonMap.put("19","Rattata");
        pokemonMap.put("20","Raticate");
        pokemonMap.put("21","Spearow");
        pokemonMap.put("22","Fearow");
        pokemonMap.put("23","Ekans");
        pokemonMap.put("24","Arbok");
        pokemonMap.put("25","Pikachu");
        pokemonMap.put("26","Raichu");
        pokemonMap.put("27","Sandshrew");
        pokemonMap.put("28","Sandslash");
        pokemonMap.put("29","Nidoran♀");
        pokemonMap.put("30","Nidorina");
        pokemonMap.put("31","Nidoqueen");
        pokemonMap.put("32","Nidoran♂");
        pokemonMap.put("33","Nidorino");
        pokemonMap.put("34","Nidoking");
        pokemonMap.put("35","Clefairy");
        pokemonMap.put("36","Clefable");
        pokemonMap.put("37","Vulpix");
        pokemonMap.put("38","Ninetales");
        pokemonMap.put("39","Jigglypuff");
        pokemonMap.put("40","Wigglytuff");
        pokemonMap.put("41","Zubat");
        pokemonMap.put("42","Golbat");
        pokemonMap.put("43","Oddish");
        pokemonMap.put("44","Gloom");
        pokemonMap.put("45","Vileplume");
        pokemonMap.put("46","Paras");
        pokemonMap.put("47","Parasect");
        pokemonMap.put("48","Venonat");
        pokemonMap.put("49","Venomoth");
        pokemonMap.put("50","Diglett");
        pokemonMap.put("51","Dugtrio");
        pokemonMap.put("52","Meowth");
        pokemonMap.put("53","Persian");
        pokemonMap.put("54","Psyduck");
        pokemonMap.put("55","Golduck");
        pokemonMap.put("56","Mankey");
        pokemonMap.put("57","Primeape");
        pokemonMap.put("58","Growlithe");
        pokemonMap.put("59","Arcanine");
        pokemonMap.put("60","Poliwag");
        pokemonMap.put("61","Poliwhirl");
        pokemonMap.put("62","Poliwrath");
        pokemonMap.put("63","Abra");
        pokemonMap.put("64","Kadabra");
        pokemonMap.put("65","Alakazam");
        pokemonMap.put("66","Machop");
        pokemonMap.put("67","Machoke");
        pokemonMap.put("68","Machamp");
        pokemonMap.put("69","Bellsprout");
        pokemonMap.put("70","Weepinbell");
        pokemonMap.put("71","Victreebel");
        pokemonMap.put("72","Tentacool");
        pokemonMap.put("73","Tentacruel");
        pokemonMap.put("74","Geodude");
        pokemonMap.put("75","Graveler");
        pokemonMap.put("76","Golem");
        pokemonMap.put("77","Ponyta");
        pokemonMap.put("78","Rapidash");
        pokemonMap.put("79","Slowpoke");
        pokemonMap.put("80","Slowbro");
        pokemonMap.put("81","Magnemite");
        pokemonMap.put("82","Magneton");
        pokemonMap.put("83","Farfetch'd");
        pokemonMap.put("84","Doduo");
        pokemonMap.put("85","Dodrio");
        pokemonMap.put("86","Seel");
        pokemonMap.put("87","Dewgong");
        pokemonMap.put("88","Grimer");
        pokemonMap.put("89","Muk");
        pokemonMap.put("90","Shellder");
        pokemonMap.put("91","Cloyster");
        pokemonMap.put("92","Gastly");
        pokemonMap.put("93","Haunter");
        pokemonMap.put("94","Gengar");
        pokemonMap.put("95","Onix");
        pokemonMap.put("96","Drowzee");
        pokemonMap.put("97","Hypno");
        pokemonMap.put("98","Krabby");
        pokemonMap.put("99","Kingler");
        pokemonMap.put("100","Voltorb");
        pokemonMap.put("101","Electrode");
        pokemonMap.put("102","Exeggcute");
        pokemonMap.put("103","Exeggutor");
        pokemonMap.put("104","Cubone");
        pokemonMap.put("105","Marowak");
        pokemonMap.put("106","Hitmonlee");
        pokemonMap.put("107","Hitmonchan");
        pokemonMap.put("108","Lickitung");
        pokemonMap.put("109","Koffing");
        pokemonMap.put("110","Weezing");
        pokemonMap.put("111","Rhyhorn");
        pokemonMap.put("112","Rhydon");
        pokemonMap.put("113","Chansey");
        pokemonMap.put("114","Tangela");
        pokemonMap.put("115","Kangaskhan");
        pokemonMap.put("116","Horsea");
        pokemonMap.put("117","Seadra");
        pokemonMap.put("118","Goldeen");
        pokemonMap.put("119","Seaking");
        pokemonMap.put("120","Staryu");
        pokemonMap.put("121","Starmie");
        pokemonMap.put("122","Mr. Mime");
        pokemonMap.put("123","Scyther");
        pokemonMap.put("124","Jynx");
        pokemonMap.put("125","Electabuzz");
        pokemonMap.put("126","Magmar");
        pokemonMap.put("127","Pinsir");
        pokemonMap.put("128","Tauros");
        pokemonMap.put("129","Magikarp");
        pokemonMap.put("130","Gyarados");
        pokemonMap.put("131","Lapras");
        pokemonMap.put("132","Ditto");
        pokemonMap.put("133","Eevee");
        pokemonMap.put("134","Vaporeon");
        pokemonMap.put("135","Jolteon");
        pokemonMap.put("136","Flareon");
        pokemonMap.put("137","Porygon");
        pokemonMap.put("138","Omanyte");
        pokemonMap.put("139","Omastar");
        pokemonMap.put("140","Kabuto");
        pokemonMap.put("141","Kabutops");
        pokemonMap.put("142","Aerodactyl");
        pokemonMap.put("143","Snorlax");
        pokemonMap.put("144","Articuno");
        pokemonMap.put("145","Zapdos");
        pokemonMap.put("146","Moltres");
        pokemonMap.put("147","Dratini");
        pokemonMap.put("148","Dragonair");
        pokemonMap.put("149","Dragonite");
        pokemonMap.put("150","Mewtwo");
        pokemonMap.put("151","Mew");

    }
}
