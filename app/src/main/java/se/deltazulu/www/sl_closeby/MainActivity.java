package se.deltazulu.www.sl_closeby;

import android.Manifest;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    ListView listView;

    StationAdapter stationAdapter;

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

    boolean permissionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        getMySharedPreferences();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //createLocationRequest();
        userInput = new UserInput(0, 0, settingsMaxResults, settingsRadius);
        getCurrentLocation();


        //GetStreetViewImage streetViewImage = new GetStreetViewImage(this,this);
        //streetViewImage.execute(59.286475,18.079402);
    }

    public void getMySharedPreferences() {
        savedSettings = this.getSharedPreferences("se.deltazulu.www.sl_closeby", Context.MODE_PRIVATE);
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius", 500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults", 10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createLocationRequest();
        getMySharedPreferences();
        userInput.setRadius(settingsRadius);
        Log.d(TAG, "onResume: radie: " + settingsRadius);
        userInput.setMax(settingsMaxResults);
        Log.d(TAG, "onResume: max: " + settingsMaxResults);
        getCurrentLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
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
            case R.id.menu_map:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getStationsList() {
        getStations = new GetStations(this);
        getStations.execute(userInput);
    }

    public void settings() {
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius", 500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults", 10);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.settings);
        LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(getApplicationContext(), R.style.DialogTheme));
        View alertView = inflater.inflate(R.layout.dialog_settings, null);
        TextView settingsLat = (TextView) alertView.findViewById(R.id.settings_lat);
        settingsLat.setText("" + this.lat);
        TextView settingsLon = (TextView) alertView.findViewById(R.id.settings_lon);
        settingsLon.setText("" + this.lon);
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

    @Override
    public void loadingStart() {
        loading = new ProgressDialog(this);
        loading.setMessage("Loading");
        loading.show();
    }

    @Override
    public void loadingEnd() {
        if (loading.isShowing()) {
            loading.dismiss();
        }
    }

    @Override
    public void processFinished(ArrayList<Station> list) {
        listView = (ListView) findViewById(R.id.listView);
        stationAdapter = new StationAdapter(this, R.layout.item_station, list);
        listView.setAdapter(stationAdapter);
        if (list.size() == 0) {
            Log.d(TAG, "processFinished: 0 results");
            Toast.makeText(this, "Inga stationer hittades", Toast.LENGTH_LONG).show();
        }
        final Context context = this;
        final AsyncResponse asyncResponse = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView latView = (TextView) view.findViewById(R.id.item_lat);
                double itemLat = Double.parseDouble(String.valueOf(latView.getText()));
                TextView lonView = (TextView) view.findViewById(R.id.item_lon);
                double itemLon = Double.parseDouble(String.valueOf(lonView.getText()));
                TextView nameView = (TextView) view.findViewById(R.id.item_name);
                String name = nameView.getText().toString();

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(name);
                LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(getApplicationContext(), R.style.DialogTheme));
                View alertView = inflater.inflate(R.layout.dialog_station, null);
                final ImageView stationImage = (ImageView) alertView.findViewById(R.id.station_image);

                ImageResponse imageResponse = new ImageResponse() {
                    @Override
                    public void showImage(Bitmap bitmap) {
                        stationImage.setImageBitmap(bitmap);
                    }
                };

                GetStreetViewImage getImage = new GetStreetViewImage(imageResponse,context);
                getImage.execute(itemLat,itemLon);
                dialog.setView(alertView);
                dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }

    public void getCurrentLocation() {
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }else {
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
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.d(TAG, "onRequestPermissionsResult: 12345");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}