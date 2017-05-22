package se.deltazulu.www.sl_closeby;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Dexter Zetterman on 2017-05-13.
 */

public class GetStreetViewImage extends AsyncTask<Double,Void,Bitmap> {

    //Bitmap bitmap;

    Context context;

    ImageResponse deligate;

    public GetStreetViewImage(ImageResponse imageResponse,Context context) {
        this.context = context;
        this.deligate = imageResponse;
    }

    @Override
    protected Bitmap doInBackground(Double... params) {
        final String KEY = "AIzaSyB21qQCvFT-UnOf3ssKC99ZYr6S0Xq0yxs";
        final int HEIGHT = 300;
        final int WIDTH = 600;
        final double LAT = params[0];
        final double LON = params[1];
        String imageURL = "https://maps.googleapis.com/maps/api/streetview?size="+WIDTH+"x"+HEIGHT+"&location="+LAT+","+LON+"&key="+KEY;
        Log.d("IMAGE", "doInBackground: "+imageURL);
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
