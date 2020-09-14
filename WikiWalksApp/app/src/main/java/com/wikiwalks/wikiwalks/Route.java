package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Route {

    private int id;
    private Path path;
    private boolean editable;

    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();
    private double distance;

    private ArrayList<Polyline> polylines = new ArrayList<>();

    public interface RouteModifyCallback {
        void onRouteEditSuccess(Path path);
        void onRouteEditFailure();
    }

    public Route(JsonObject attributes, Path path) {
        id = attributes.get("id").getAsInt();
        editable = attributes.get("editable").getAsBoolean();
        JsonArray latitudesJson = attributes.get("latitudes").getAsJsonArray();
        JsonArray longitudesJson = attributes.get("longitudes").getAsJsonArray();
        JsonArray altitudesJson = attributes.get("altitudes").getAsJsonArray();
        Location lastLocation = null;
        for (int i = 0; i < latitudesJson.size(); i++) {
            latitudes.add(latitudesJson.get(i).getAsDouble());
            longitudes.add(longitudesJson.get(i).getAsDouble());
            altitudes.add(altitudesJson.get(i).getAsDouble());
            Location newLocation = new Location("import");
            newLocation.setLatitude(latitudesJson.get(i).getAsDouble());
            newLocation.setLongitude(longitudesJson.get(i).getAsDouble());
            newLocation.setAltitude(altitudesJson.get(i).getAsDouble());
            if (lastLocation != null) {
                distance += (newLocation.distanceTo(lastLocation));
            }
            lastLocation = newLocation;
        }
        this.path = path;
    }

    public static void submit(Context context, Path path, String title, ArrayList<Double> latitudes, ArrayList<Double> longitudes, ArrayList<Double> altitudes, RouteModifyCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        Gson gson = new Gson();
        request.add("latitudes", JsonParser.parseString(gson.toJson(latitudes)));
        request.add("longitudes", JsonParser.parseString(gson.toJson(longitudes)));
        request.add("altitudes", JsonParser.parseString(gson.toJson(altitudes)));
        if (path != null) {
            request.addProperty("path", path.id);
        } else {
            request.addProperty("name", title);
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newRoute = MainActivity.getRetrofitRequests(context).newRoute(body);
        newRoute.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body().getAsJsonObject().get("path").getAsJsonObject();
                    Path newPath = new Path(responseJson);
                    PathMap.getInstance().addPath(newPath);
                    callback.onRouteEditSuccess(newPath);
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

    public void delete(Context context, RouteModifyCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
    }
}
