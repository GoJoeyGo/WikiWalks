package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Route implements Serializable {

    private int id;
    private Path path;
    private boolean editable;

    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<Double> altitudes;
    private double distance;

    private ArrayList<Polyline> polylines = new ArrayList<>();

    public interface RouteModifyCallback {
        void onRouteEditSuccess(Path path);
        void onRouteEditFailure();
    }

    public Route(int id, Path path, boolean editable, ArrayList<Double> latitudes, ArrayList<Double> longitudes, ArrayList<Double> altitudes) {
        this.id = id;
        this.path = path;
        this.editable = editable;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.altitudes = altitudes;
        Location lastLocation = new Location("import");
        lastLocation.setLatitude(latitudes.get(0));
        lastLocation.setLongitude(longitudes.get(0));
        lastLocation.setAltitude(altitudes.get(0));
        for (int i = 1; i < latitudes.size(); i++) {
            Location newLocation = new Location("import");
            newLocation.setLatitude(latitudes.get(i));
            newLocation.setLongitude(longitudes.get(i));
            newLocation.setAltitude(altitudes.get(i));
            distance += (newLocation.distanceTo(lastLocation));
            lastLocation = newLocation;
        }
    }

    public static void submit(Context context, Path path, String title, ArrayList<Double> latitudes, ArrayList<Double> longitudes, ArrayList<Double> altitudes, RouteModifyCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("latitudes", new JSONArray(latitudes));
            request.put("longitudes", new JSONArray(longitudes));
            request.put("altitudes", new JSONArray(altitudes));
            if (path != null) {
                request.put("path", path.id);
            } else {
                request.put("name", title);
            }
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newRoute = MainActivity.getRetrofitRequests(context).newRoute(body);
            newRoute.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().toString()).getJSONObject("path");
                            Path newPath = new Path(responseJson);
                            PathMap.getInstance().addPath(newPath);
                            callback.onRouteEditSuccess(newPath);
                        } catch (JSONException e) {
                            Log.e("Route", "Getting path from submit response", e);
                        }
                        PreferencesManager.getInstance(context).changeRoutesRecorded(false);
                    } else {
                        callback.onRouteEditFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Route", "Sending new route request", t);
                    callback.onRouteEditFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("Route", "Creating new route request", e);
            callback.onRouteEditFailure();
        }
    }

    public Polyline makePolyline(GoogleMap map) {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        for (int i = 0; i < latitudes.size(); i++) {
            coordinates.add(new LatLng(latitudes.get(i), longitudes.get(i)));
        }
        Polyline polyline = map.addPolyline(new PolylineOptions().addAll(coordinates));
        int walkCount = path.getWalkCount();
        if (walkCount < 10) {
            polyline.setColor(0xffffe49c);
        } else if (walkCount < 100) {
            polyline.setColor(0xffff9100);
        } else if (walkCount < 1000) {
            polyline.setColor(0xffff1e00);
        } else {
            polyline.setColor(0xff000000);
        }
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

    public double getDistance() {
        return distance;
    }

    public boolean isEditable() {
        return editable;
    }

    public void delete(final Context context, RouteModifyCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
                        PreferencesManager.getInstance(context).changeRoutesRecorded(true);
                        callback.onRouteEditSuccess(null);
                    } else {
                        callback.onRouteEditFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Route", "Sending delete route request", t);
                    callback.onRouteEditFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("Route", "Creating delete route request", e);
            callback.onRouteEditFailure();
        }
    }
}
