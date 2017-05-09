package se.deltazulu.www.sl_closeby;

/**
 * Created by Dexter Zetterman on 2017-05-08.
 */

public class UserInput {
    private double lat;
    private double lon;
    private int max;
    private int radius;

    public UserInput(double lat, double lon, int max, int radius) {
        this.lat = lat;
        this.lon = lon;
        this.max = max;
        this.radius = radius;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
