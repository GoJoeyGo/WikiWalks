package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Path {

    int id;
    private String name;
    private int walkCount;
    private double rating;

    private ArrayList<Route> routeList = new ArrayList<>();
    private ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();
    private ArrayList<PathReview> pathReviews = new ArrayList<>();
    private PathReview ownReview;

    private ArrayList<Marker> markers = new ArrayList<>();
    private LatLng markerPoint;
    private LatLngBounds bounds;

    public interface PathChangeCallback {
        void onEditSuccess();
        void onEditFailure();
    }

    public interface GetReviewsCallback {
        void onGetReviewsSuccess();
        void onGetReviewsFailure();
    }

    public interface SubmitReviewCallback {
        void onSubmitReviewSuccess();
        void onSubmitReviewFailure();
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

    public ArrayList<PathReview> getPathReviews() {
        return pathReviews;
    }

    public PathReview getOwnReview() {
        return ownReview;
    }

    public void setOwnReview(PathReview ownReview) {
        this.ownReview = ownReview;
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

    public void walk(Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + String.format("/paths/%d/walk", id);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, null, response -> {
            try {
                this.walkCount = response.getInt("new_count");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void getReviews(Context context, GetReviewsCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = context.getString(R.string.local_url) + String.format("/paths/%d/reviews", id);
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                try {
                    JSONArray reviews = response.getJSONArray("reviews");
                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject review = reviews.getJSONObject(i);
                        boolean exists = false;
                        for (PathReview pathReview : pathReviews) {
                            if (pathReview.getId() == review.getInt("id")) {
                                exists = true;
                                break;
                            }
                        }
                        if (ownReview != null && ownReview.getId() == review.getInt("id")) exists = true;
                        if (!exists) {
                            PathReview newReview = new PathReview(review.getInt("id"), this, review.getString("submitter"), review.getInt("rating"), review.getString("text"), review.getBoolean("editable"));
                            if (newReview.isEditable()) {
                                ownReview = newReview;
                            } else {
                                pathReviews.add(newReview);
                            }
                        }
                    }
                    callback.onGetReviewsSuccess();
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onGetReviewsFailure();
                }
            }, error -> {
                Log.e("GET_REVIEWS", Arrays.toString(error.getStackTrace()));
                callback.onGetReviewsFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onGetReviewsFailure();
        }
    }
}
