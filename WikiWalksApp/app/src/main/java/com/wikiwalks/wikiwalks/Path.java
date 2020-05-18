package com.wikiwalks.wikiwalks;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class Path {
    private boolean isNew = false;

    private int id;
    private String name;
    private int walkCount;
    private double rating;

    private Path parentPath;
    private ArrayList<Path> childPaths = new ArrayList<>();
    private ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();

    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();

    private LatLngBounds bounds;

    public Path(JSONObject pathJson) throws JSONException {
        id = pathJson.getInt("id");
        name = pathJson.getString("name");
        walkCount = pathJson.getInt("walk_count");
        rating = pathJson.getDouble("average_rating");
        for (int i = 0; i < pathJson.getJSONArray("latitudes").length(); i++) {
            latitudes.add(pathJson.getJSONArray("latitudes").getDouble(i));
            longitudes.add(pathJson.getJSONArray("longitudes").getDouble(i));
        }
        double south_bound = pathJson.getJSONArray("boundaries").getDouble(0);
        double west_bound = pathJson.getJSONArray("boundaries").getDouble(1);
        double north_bound = pathJson.getJSONArray("boundaries").getDouble(2);
        double east_bound = pathJson.getJSONArray("boundaries").getDouble(3);
        bounds = new LatLngBounds(new LatLng(south_bound, west_bound), new LatLng(north_bound, east_bound));
        JSONArray points_of_interest = pathJson.getJSONArray("points_of_interest");
        for (int i = 0; i < points_of_interest.length(); i++) {
            JSONObject pointOfInterest = points_of_interest.getJSONObject(i);
            pointsOfInterest.add(new PointOfInterest(pointOfInterest.getInt("id"), pointOfInterest.getString("name"), pointOfInterest.getDouble("latitude"), pointOfInterest.getDouble("longitude"), this));
        }
        if (!pathJson.isNull("parent_path")) {
            parentPath = PathMap.getInstance().getPathList().get(pathJson.getInt("parent_path"));
            parentPath.addChild(this);
            LatLngBounds parentBounds = parentPath.getBounds();
            if (parentBounds.northeast.latitude > north_bound) {
                north_bound = parentBounds.northeast.latitude;
            }
            if (parentBounds.northeast.longitude > east_bound) {
                east_bound = parentBounds.northeast.longitude;
            }
            if (parentBounds.southwest.latitude < south_bound) {
                south_bound = parentBounds.southwest.latitude;
            }
            if (parentBounds.southwest.longitude < west_bound) {
                west_bound = parentBounds.southwest.longitude;
            }
            parentPath.setBounds(new LatLngBounds(new LatLng(south_bound, west_bound), new LatLng(north_bound, east_bound)));
        }
    }

    public Path(int parentId) {
        parentPath = PathMap.getInstance().getPathList().get(parentId);
        isNew = true;
    }

    public Path() {
        isNew = true;
    }

    public void addChild(Path child) {
        childPaths.add(child);
    }

    public void addPointOfInterest(PointOfInterest pointOfInterest) {
        pointsOfInterest.add(pointOfInterest);
    }

    public void edit(String name) {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWalkCount() {
        return walkCount;
    }

    public double getRating() {
        return rating;
    }

    public Path getParentPath() {
        return parentPath;
    }

    public ArrayList<Path> getChildPaths() {
        return childPaths;
    }

    public ArrayList<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public ArrayList<PointOfInterest> getAllPointsOfInterest() {
        ArrayList<PointOfInterest> poiList = new ArrayList<>(pointsOfInterest);
        for (Path child : childPaths) {
            poiList.addAll(child.getAllPointsOfInterest());
        }
        return poiList;
    }

    public ArrayList<Double> getAllLatitudes() {
        ArrayList<Double> latitudeList = new ArrayList<>(latitudes);
        for (Path child : childPaths) {
            latitudeList.addAll(child.getAllLatitudes());
        }
        return latitudeList;
    }

    public ArrayList<Double> getAllLongitudes() {
        ArrayList<Double> longitudeList = new ArrayList<>(longitudes);
        for (Path child : childPaths) {
            longitudeList.addAll(child.getAllLongitudes());
        }
        return longitudeList;
    }

    public ArrayList<Double> getLatitudes() {
        return latitudes;
    }

    public ArrayList<Double> getLongitudes() {
        return longitudes;
    }

    public ArrayList<Double> getAltitudes() {
        return altitudes;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    public void setBounds(LatLngBounds bounds) {
        this.bounds = bounds;
    }

    public Polyline makePolyLine(GoogleMap map) {
        LinkedList<LatLng> points = new LinkedList<>();
        for (int i = 0; i < getLatitudes().size(); i++) {
            points.add(new LatLng(getLatitudes().get(i), getLongitudes().get(i)));
        }
        Polyline polyline = map.addPolyline(new PolylineOptions().clickable(true).addAll(points));
        int walkCount = getWalkCount();
        if (walkCount < 10) polyline.setColor(0xffffe49c);
        else if (walkCount < 100) polyline.setColor(0xffff9100);
        else if (walkCount < 1000) polyline.setColor(0xffff1e00);
        else polyline.setColor(0xff000000);
        polyline.setWidth(20);
        return polyline;
    }
}
