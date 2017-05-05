package se.deltazulu.www.sl_closeby;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    ListView listView;

    StationAdapter stationAdapter;

    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SLCloseBy test = new SLCloseBy(this);
        test.execute();

    }


    @Override
    public void loadingStart() {
        loading = new ProgressDialog(this);
        loading.setMessage("Loading");
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