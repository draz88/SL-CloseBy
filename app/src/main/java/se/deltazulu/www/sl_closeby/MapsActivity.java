package se.deltazulu.www.sl_closeby;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AsyncResponse {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    ProgressDialog loading;

    UserInput userInput;

    Toolbar toolbar;

    SharedPreferences savedSettings;
    int settingsMaxResults;
    int settingsRadius;

    GetStations getStations;

    GoogleApiClient mGoogleApiClient;
    double lat;
    double lon;

    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        savedSettings = this.getSharedPreferences("se.deltazulu.www.sl_closeby", Context.MODE_PRIVATE);
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius", 500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults", 10);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
        userInput = new UserInput(0, 0, settingsMaxResults, settingsRadius);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                settings();
                break;
            case R.id.menu_reload:
                getCurrentLocation();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void getCurrentLocation() {
        mGoogleApiClient.connect();
    }

    public void getStationsList() {
        getStations = new GetStations(this);
        getStations.execute(userInput);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        getCurrentLocation();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
        });
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            this.lat = mLastLocation.getLatitude();
            this.lon = mLastLocation.getLongitude();
            userInput.setLat(this.lat);
            userInput.setLon(this.lon);
            Log.d(TAG, "onConnected: " + lat);
            Log.d(TAG, "onConnected: " + lon);
            getStationsList();
        } else {
            Toast.makeText(this, "Kunde inte hämta koordinater", Toast.LENGTH_LONG).show();
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void loadingStart() {

    }

    @Override
    public void loadingEnd() {

    }

    @Override
    public void processFinished(ArrayList<Station> list) {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
        mMap.setMyLocationEnabled(true);
        LatLng myPos = new LatLng(this.lat,this.lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos,15f));
        for(int i = 0; list.size() > i; i++){
            LatLng pos = new LatLng(list.get(i).getLat(),list.get(i).getLon());
            mMap.addMarker(new MarkerOptions().position(pos).title(list.get(i).getName()).snippet(list.get(i).getDist()+"m"));
        }
    }

    @Override
    public void showImage(Bitmap bitmap) {

    }

    public void settings() {
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius", 500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults", 10);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.settings);
        LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(getApplicationContext(), R.style.DialogTheme));
        View alertView = inflater.inflate(R.layout.dialog_settings, null);
        TextView settingsLat = (TextView) alertView.findViewById(R.id.settings_lat);
        settingsLat.setText(""+this.lat);
        TextView settingsLon = (TextView) alertView.findViewById(R.id.settings_lon);
        settingsLon.setText(""+this.lon);
        final NumberPicker radius = (NumberPicker) alertView.findViewById(R.id.settings_radius);
        radius.setMinValue(50);
        radius.setMaxValue(2000);
        radius.setValue(settingsRadius);
        final NumberPicker maxResults = (NumberPicker) alertView.findViewById(R.id.settings_maxResults);
        maxResults.setMinValue(1);
        maxResults.setMaxValue(100);
        maxResults.setValue(settingsMaxResults);
        dialog.setView(alertView);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userInput.setMax(maxResults.getValue());
                userInput.setRadius(radius.getValue());
                savedSettings.edit().putInt("se.deltazulu.www.sl_closeby.maxResults", userInput.getMax()).apply();
                savedSettings.edit().putInt("se.deltazulu.www.sl_closeby.radius", userInput.getRadius()).apply();
                getStationsList();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

}
