package com.wikiwalks.wikiwalks;

import android.util.TypedValue;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PointOfInterest {
    private int id;
    private String name;

    LatLng coordinates;

    private Path path;

    public PointOfInterest(int id, String name, double latitude, double longitude, Path path) {
        this.id = id;
        this.name = name;
        this.coordinates = new LatLng(latitude, longitude);
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public Path getPath() {
        return path;
    }

    public void makeMarker(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(coordinates));
    }
}
