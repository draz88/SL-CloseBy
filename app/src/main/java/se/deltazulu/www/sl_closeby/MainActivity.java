package se.deltazulu.www.sl_closeby;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.NumberPicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    private static final String TAG = "MainActivity";

    ListView listView;

    StationAdapter stationAdapter;

    ProgressDialog loading;

    UserInput userInput;

    Toolbar toolbar;

    SharedPreferences savedSettings;
    int settingsMaxResults;
    int settingsRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        savedSettings = this.getSharedPreferences("se.deltazulu.www.sl_closeby", Context.MODE_PRIVATE);
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius",500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults",10);

        //DEBUG -------------------------------------
        //SKA INTE FINNAS KVAR I SLUTPRODUKTEN
        if(settingsMaxResults==1){
            settingsMaxResults=10;
            savedSettings.edit().putInt("se.deltazulu.www.sl_closeby.maxResults",settingsMaxResults).apply();
        }
        //DEBUG -------------------------------------

        userInput = new UserInput(59.293525,18.083519,settingsMaxResults,settingsRadius);
        getStationsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.menu_settings:
                settings();
                break;
            case R.id.menu_reload:
                getStationsList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getStationsList(){
        GetStations getStations = new GetStations(this);
        getStations.execute(userInput);
    }

    public void settings(){
        settingsRadius = savedSettings.getInt("se.deltazulu.www.sl_closeby.radius",500);
        settingsMaxResults = savedSettings.getInt("se.deltazulu.www.sl_closeby.maxResults",10);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.settings);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View alertView = inflater.inflate(R.layout.dialog_settings, null);
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
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userInput.setMax(maxResults.getValue());
                userInput.setRadius(radius.getValue());
                savedSettings.edit().putInt("se.deltazulu.www.sl_closeby.maxResults",userInput.getMax()).apply();
                savedSettings.edit().putInt("se.deltazulu.www.sl_closeby.radius",userInput.getRadius()).apply();
                getStationsList();
                Log.d(TAG, "onClick: 1");
            }
        });
        dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
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
        loading.setCancelable(false);
        loading.show();
    }

    @Override
    public void loadingEnd() {
        if(loading.isShowing()){
            loading.dismiss();
        }
    }

    @Override
    public void processFinished(ArrayList<Station> list) {
        listView = (ListView) findViewById(R.id.listView);
        stationAdapter = new StationAdapter(this, R.layout.item_station,list);
        listView.setAdapter(stationAdapter);
    }
}