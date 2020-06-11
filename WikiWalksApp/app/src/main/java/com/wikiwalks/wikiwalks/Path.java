package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Path {
    private boolean isNew = false;
    private boolean editable;

    int id;
    private String name;
    private int walkCount;
    private double rating;

    private ArrayList<Route> routeList = new ArrayList<>();
    private ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();

    private ArrayList<Marker> markers = new ArrayList<>();
    private LatLng markerPoint;
    private LatLngBounds bounds;

    public interface PathChangeCallback {
        void onEditSuccess();
        void onEditFailure();
    }

    public Path(int id, String name, int walkCount, double rating, double[] bounds) {
        this.id = id;
        this.name = name;
        this.walkCount = walkCount;
        this.rating = rating;
        this.bounds = new LatLngBounds(new LatLng(bounds[0], bounds[1]), new LatLng(bounds[2], bounds[3]));
    }

    public Path() {}

    public Path(JSONObject pathJson) throws JSONException {
        id = pathJson.getInt("id");
        name = pathJson.getString("name");
        walkCount = pathJson.getInt("walk_count");
        rating = pathJson.getDouble("average_rating");
        editable = pathJson.getBoolean("editable");
        markerPoint = new LatLng(pathJson.getJSONArray("marker_point").getDouble(0), pathJson.getJSONArray("marker_point").getDouble(1));
        JSONArray boundaries = pathJson.getJSONArray("boundaries");
        bounds = new LatLngBounds(new LatLng(boundaries.getDouble(0), boundaries.getDouble(1)), new LatLng(boundaries.getDouble(2), boundaries.getDouble(3)));
        JSONArray points_of_interest = pathJson.getJSONArray("points_of_interest");
        for (int i = 0; i < points_of_interest.length(); i++) {
            JSONObject pointOfInterest = points_of_interest.getJSONObject(i);
            pointsOfInterest.add(new PointOfInterest(pointOfInterest.getInt("id"), pointOfInterest.getString("name"), pointOfInterest.getDouble("latitude"), pointOfInterest.getDouble("longitude"), this));
        }
        JSONArray routes = pathJson.getJSONArray("routes");
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            ArrayList<Double> routeLatitudes = new ArrayList<>();
            ArrayList<Double> routeLongitudes = new ArrayList<>();
            ArrayList<Double> routeAltitudes = new ArrayList<>();
            for (int j = 0; j < route.getJSONArray("latitudes").length(); j++) {
                routeLatitudes.add(route.getJSONArray("latitudes").getDouble(j));
                routeLongitudes.add(route.getJSONArray("longitudes").getDouble(j));
                routeAltitudes.add(route.getJSONArray("altitudes").getDouble(j));
            }
            boolean editable = route.getBoolean("editable");
            int routeId = route.getInt("id");
            routeList.add(new Route(routeId, this, editable, routeLatitudes, routeLongitudes, routeAltitudes));
        }
    }

    public void addPointOfInterest(PointOfInterest pointOfInterest) {
        pointsOfInterest.add(pointOfInterest);
    }

    public void removeRoute(Route route) {
        routeList.remove(route);
    }

    public boolean isEditable() {
        return editable;
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

    public ArrayList<Double> getAllLatitudes() {
        ArrayList<Double> allLatitudes = new ArrayList<>();
        for (Route route : routeList) {
            allLatitudes.addAll(route.getLatitudes());
        }
        return allLatitudes;
    }

    public ArrayList<Double> getAllLongitudes() {
        ArrayList<Double> allLongitudes = new ArrayList<>();
        for (Route route : routeList) {
            allLongitudes.addAll(route.getLongitudes());
        }
        return allLongitudes;
    }

    public ArrayList<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public ArrayList<Route> getRoutes() {
        return routeList;
    }

    public LatLng getMarkerPoint() {
        return markerPoint;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public Marker makeMarker(GoogleMap map) {
        Marker marker = map.addMarker(new MarkerOptions().position(markerPoint));
        marker.setTag(id);
        marker.setTitle(name);
        markers.add(marker);
        return marker;
    }

    public void update(final Context context) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, context.getString(R.string.local_url) + String.format("/paths/%d", id), null, response -> {
            try {
                JSONObject responseJson = response.getJSONObject("path");
                PathMap.getInstance().addPath(new Path(responseJson));
            } catch (JSONException e) {
                Toast.makeText(context, "Failed to update path...", Toast.LENGTH_SHORT).show();
                Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
            }
        }, error -> {
            Toast.makeText(context, "Failed to update path...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void edit(Context context, String title, PathChangeCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + String.format("/paths/%d/edit", id);
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("name", title);
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                this.name = title;
                callback.onEditSuccess();
            }, error -> {
                Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
                callback.onEditFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
            callback.onEditFailure();
        }
    }
}
