package se.deltazulu.www.sl_closeby;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Dexter Zetterman on 2017-05-04.
 */

public interface AsyncResponse {
    void loadingStart();
    void loadingEnd();
    void processFinished(ArrayList<Station> list);
}
