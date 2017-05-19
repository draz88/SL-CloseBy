package se.deltazulu.www.sl_closeby;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Dexter Zetterman on 2017-05-13.
 */

public class GetStreetViewImage extends AsyncTask<String,Void,Bitmap> {

    //Bitmap bitmap;

    Context context;

    AsyncResponse deligate;

    public GetStreetViewImage(AsyncResponse asyncResponse,Context context) {
        this.context = context;
        this.deligate = asyncResponse;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        final String KEY = "AIzaSyB21qQCvFT-UnOf3ssKC99ZYr6S0Xq0yxs";
        final int HEIGHT = 300;
        final int WIDTH = 600;
        final double LON = 59.286475;
        final double LAT = 18.079402;
        String imageURL = "https://maps.googleapis.com/maps/api/streetview?size="+WIDTH+"x"+HEIGHT+"&location="+LON+","+LAT+"&key="+KEY;
        Bitmap bitmap = null;
        try {
            InputStream in = new URL(imageURL).openStream();
             bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        deligate.showImage(bitmap);
        super.onPostExecute(bitmap);
    }
}
