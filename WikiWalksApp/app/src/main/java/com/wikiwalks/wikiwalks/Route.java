package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Route implements Serializable {
    int id;
    Path path;
    boolean editable;

    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<Double> altitudes;

    private ArrayList<Polyline> polylines = new ArrayList<>();

    public interface RouteSubmitCallback {
        void onSuccess();
        void onFailure();
    }

    public Route(int id, Path path, boolean editable, ArrayList<Double> latitudes, ArrayList<Double> longitudes, ArrayList<Double> altitudes) {
        this.id = id;
        this.path = path;
        this.editable = editable;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.altitudes = altitudes;
    }

    public Polyline makePolyline(GoogleMap map) {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        for (int i = 0; i < latitudes.size(); i++) {
            coordinates.add(new LatLng(latitudes.get(i), longitudes.get(i)));
        }
        Polyline polyline = map.addPolyline(new PolylineOptions().addAll(coordinates));
        int walkCount = path.getWalkCount();
        if (walkCount < 10) polyline.setColor(0xffffe49c);
        else if (walkCount < 100) polyline.setColor(0xffff9100);
        else if (walkCount < 1000) polyline.setColor(0xffff1e00);
        else polyline.setColor(0xff000000);
        polyline.setWidth(20);
        polylines.add(polyline);
        return polyline;
    }

    public int getId() {
        return id;
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

    public boolean isEditable() {
        return editable;
    }

    public void submit(Context context, String title, RouteSubmitCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + "/routes/new";
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("latitudes", new JSONArray(latitudes));
            attributes.put("longitudes", new JSONArray(longitudes));
            attributes.put("altitudes", new JSONArray(altitudes));
            if (path != null) attributes.put("path", path.id);
            else attributes.put("name", title);
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                try {
                    JSONObject responseJson = response.getJSONObject("path");
                    PathMap.getInstance().addPath(new Path(responseJson));
                    callback.onSuccess();
                } catch (JSONException e) {
                    Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
                }
            }, error -> {
                Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                Log.e("SUBMIT_PATH", Arrays.toString(error.getStackTrace()));
                callback.onFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH", Arrays.toString(e.getStackTrace()));
            callback.onFailure();
        }
    }

    public void delete(final Context context, RouteSubmitCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, context.getString(R.string.local_url) + String.format("/routes/%d/delete", id), request, response -> {
                path.removeRoute(this);
                if (path.getRoutes().size() == 0)  {
                    for (Marker marker : path.getMarkers()) {
                        marker.remove();
                    }
                    PathMap.getInstance().deletePath(path);
                }
                for (Polyline polyline : polylines) {
                    polyline.remove();
                }
                callback.onSuccess();
            }, error -> {
                Log.e("DELETE_PATH", Arrays.toString(error.getStackTrace()));
                callback.onFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("DELETE_PATH", Arrays.toString(e.getStackTrace()));
            callback.onFailure();
        }
    }
}
