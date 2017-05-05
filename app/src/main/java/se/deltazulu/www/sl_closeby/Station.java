package se.deltazulu.www.sl_closeby;

/**
 * Created by Dexter Zetterman on 2017-05-04.
 */

public class Station {
    private int id;
    private String name;
    private double lat;
    private double lon;
    private int dist;

    public Station(int id, String name, double lat, double lon, int dist) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.dist = dist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }
}
