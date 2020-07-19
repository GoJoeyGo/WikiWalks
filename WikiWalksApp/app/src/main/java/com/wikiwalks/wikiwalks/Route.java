package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Route implements Serializable {
    @SerializedName("id")
    int id;

    Path path;
    @SerializedName("editable")
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
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newRoute = MainActivity.getRetrofitRequests(context).newRoute(body);
            newRoute.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().toString()).getJSONObject("path");
                            PathMap.getInstance().addPath(new Path(responseJson));
                            callback.onSuccess();
                        } catch (JSONException e) {
                            Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                            Log.e("SUBMIT_PATH1", Arrays.toString(e.getStackTrace()));
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH2", Arrays.toString(t.getStackTrace()));
                    callback.onFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to upload path...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH3", Arrays.toString(e.getStackTrace()));
            callback.onFailure();
        }
    }

    public void delete(final Context context, RouteSubmitCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deleteRoute = MainActivity.getRetrofitRequests(context).deleteRoute(id, body);
            deleteRoute.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        path.removeRoute(Route.this);
                        if (path.getRoutes().size() == 0) {
                            for (Marker marker : path.getMarkers()) {
                                marker.remove();
                            }
                            PathMap.getInstance().deletePath(path);
                        }
                        for (Polyline polyline : polylines) {
                            polyline.remove();
                        }
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("DELETE_PATH1", Arrays.toString(t.getStackTrace()));
                    callback.onFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("DELETE_PATH2", Arrays.toString(e.getStackTrace()));
            callback.onFailure();
        }
    }
}
