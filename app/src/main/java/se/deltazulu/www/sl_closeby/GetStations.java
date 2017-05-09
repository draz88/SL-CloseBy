package se.deltazulu.www.sl_closeby;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by Dexter Zetterman on 2017-05-04.
 */

public class GetStations extends AsyncTask<UserInput,String,String> {

    private static final String TAG = "GetStations";

    private ArrayList<Station> stationsList = new ArrayList<>();

    AsyncResponse deligate;

    public GetStations(AsyncResponse deligate) {
        this.deligate = deligate;
    }

    @Override
    protected void onPreExecute(){
        deligate.loadingStart();
    }

    @Override
    protected String doInBackground(UserInput... params) {

        HttpURLConnection connection;
        BufferedReader reader;

        final String KEY = "2b5250718bd84e11926ab79a5f2e4888";
        final double LAT = params[0].getLat();
        final double LON = params[0].getLon();
        final int MAX = params[0].getMax();
        final int RADUIS = params[0].getRadius();

        try {
            URL url = new URL("http://api.sl.se/api2/nearbystops.json?key="+KEY+"&originCoordLat="+LAT+"&originCoordLong="+LON+"&maxresults="+MAX+"&radius="+RADUIS);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"),8);
            StringBuilder builder = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            String finalJson = builder.toString();
            return finalJson;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject apiResult = new JSONObject(s);
            JSONObject locationList = apiResult.getJSONObject("LocationList");
            JSONArray stopLocation = locationList.getJSONArray("StopLocation");
            for(int i = 0; i < stopLocation.length(); i++){
                JSONObject station = stopLocation.getJSONObject(i);
                int id = station.getInt("id");
                String name = station.getString("name");
                double lat = station.getDouble("lat");
                double lon = station.getDouble("lon");
                int dist = station.getInt("dist");
                stationsList.add(new Station(id,name,lat,lon,dist));
            }
            deligate.loadingEnd();
            deligate.processFinished(stationsList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}