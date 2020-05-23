package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    private ArrayList<Polyline> polylines = new ArrayList<>();

    public Path(String name, ArrayList<Double> latitudes, ArrayList<Double> longitudes, ArrayList<Double> altitudes, Path parentPath) {
        this.name = name;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.altitudes = altitudes;
        this.isNew = true;
        this.parentPath = parentPath;
    }

    public Path(JSONObject pathJson) throws JSONException {
        id = pathJson.getInt("id");
        name = pathJson.getString("name");
        walkCount = pathJson.getInt("walk_count");
        rating = pathJson.getDouble("average_rating");
        for (int i = 0; i < pathJson.getJSONArray("latitudes").length(); i++) {
            latitudes.add(pathJson.getJSONArray("latitudes").getDouble(i));
            longitudes.add(pathJson.getJSONArray("longitudes").getDouble(i));
            altitudes.add(pathJson.getJSONArray("altitudes").getDouble(i));
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
        }
    }

    public void addChild(Path child) {
        childPaths.add(child);
    }

    public void removeChild(Path child) {
        childPaths.remove(child);
    }

    public void addPointOfInterest(PointOfInterest pointOfInterest) {
        pointsOfInterest.add(pointOfInterest);
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

    public ArrayList<Path> getAllChildPaths() {
        ArrayList<Path> pathList = new ArrayList<>();
        pathList.addAll(childPaths);
        for (Path child : childPaths) {
            pathList.addAll(child.getAllChildPaths());
        }
        return pathList;
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

    public ArrayList<Double> getAllAltitudes() {
        ArrayList<Double> altitudeList = new ArrayList<>(altitudes);
        for (Path child : childPaths) {
            altitudeList.addAll(child.getAllLongitudes());
        }
        return altitudeList;
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
        polylines.add(polyline);
        int walkCount = getWalkCount();
        if (walkCount < 10) polyline.setColor(0xffffe49c);
        else if (walkCount < 100) polyline.setColor(0xffff9100);
        else if (walkCount < 1000) polyline.setColor(0xffff1e00);
        else polyline.setColor(0xff000000);
        polyline.setWidth(20);
        return polyline;
    }

    public ArrayList<Polyline> makeAllPolyLines(GoogleMap map) {
        ArrayList<Polyline> polylines = new ArrayList<>();
        polylines.add(makePolyLine(map));
        for (Path child : childPaths) {
            polylines.addAll(child.makeAllPolyLines(map));
        }
        return polylines;
    }

    public void update(final Context context) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, context.getString(R.string.local_url) + String.format("/paths/%d", id), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject responseJson = null;
                try {
                    responseJson = response.getJSONObject("path");
                    name = responseJson.getString("name");
                    walkCount = responseJson.getInt("walk_count");
                    rating = responseJson.getDouble("average_rating");
                    double south_bound = responseJson.getJSONArray("boundaries").getDouble(0);
                    double west_bound = responseJson.getJSONArray("boundaries").getDouble(1);
                    double north_bound = responseJson.getJSONArray("boundaries").getDouble(2);
                    double east_bound = responseJson.getJSONArray("boundaries").getDouble(3);
                    bounds = new LatLngBounds(new LatLng(south_bound, west_bound), new LatLng(north_bound, east_bound));
                    if (parentPath != null) {
                        parentPath.update(context);
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "Failed to update path...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Failed to update path...", Toast.LENGTH_SHORT).show();
                Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void submit(final Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final Path path = this;
        String url =  context.getString(R.string.local_url);
        url = (isNew) ? url + String.format("/paths/%d/edit", id) : url + "/paths/new";
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("name", name);
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("latitudes", new JSONArray(latitudes));
            attributes.put("longitudes", new JSONArray(longitudes));
            attributes.put("altitudes", new JSONArray(altitudes));
            if (parentPath != null) attributes.put("parent_path", parentPath.id);
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject responseJson = response.getJSONObject("path");
                        id = responseJson.getInt("id");
                        walkCount = 1;
                        rating = 0;
                        isNew = false;
                        double south_bound = responseJson.getJSONArray("boundaries").getDouble(0);
                        double west_bound = responseJson.getJSONArray("boundaries").getDouble(1);
                        double north_bound = responseJson.getJSONArray("boundaries").getDouble(2);
                        double east_bound = responseJson.getJSONArray("boundaries").getDouble(3);
                        bounds = new LatLngBounds(new LatLng(south_bound, west_bound), new LatLng(north_bound, east_bound));
                        if (parentPath != null) {
                            parentPath.addChild(path);
                            parentPath.update(context);
                        }
                        PathMap.getInstance().addPath(path);
                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                        Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
        }
    }

    public void delete(final Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final Path path = this;
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, context.getString(R.string.local_url) + String.format("/paths/%d/delete", id), request, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (parentPath != null) {
                        parentPath.removeChild(path);
                        parentPath.update(context);
                    }
                    PathMap pathMap = PathMap.getInstance();
                    for (Path child : childPaths) pathMap.deletePath(child);
                    pathMap.deletePath(path);
                    for (Polyline polyline : polylines) polyline.remove();
                    Toast.makeText(context, "Successfully deleted path!", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Failed to delete path...", Toast.LENGTH_SHORT).show();
                    Log.e("DELETE_PATH", Arrays.toString(error.getStackTrace()));
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to delete path...", Toast.LENGTH_SHORT).show();
            Log.e("DELETE_PATH", Arrays.toString(e.getStackTrace()));
        }
    }
}
