package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PathMap {

    private static PathMap instance = null;
    public ArrayList<PathMapListener> changeListeners = new ArrayList<>();
    private LinkedHashMap<Integer, Path> pathList = new LinkedHashMap<>();
    private LinkedHashMap<Integer, PointOfInterest> pointOfInterestList = new LinkedHashMap<>();

    public interface PathMapListener {
        void onPathMapUpdateSuccess();
        void onPathMapUpdateFailure();
    }

    private PathMap() {
    }

    public static PathMap getInstance() {
        if (instance == null) {
            instance = new PathMap();
        }
        return instance;
    }

    public void updatePaths(LatLngBounds bounds, Context context) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> getPaths = MainActivity.getRetrofitRequests(context).getPaths(bounds.northeast.latitude, bounds.northeast.longitude, bounds.southwest.latitude, bounds.southwest.longitude, body);
        getPaths.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonArray responseJson = response.body().getAsJsonObject().get("paths").getAsJsonArray();
                    for (int i = 0; i < responseJson.size(); i++) {
                        JsonObject pathJson = responseJson.get(i).getAsJsonObject();
                        if (!pathList.containsKey(pathJson.get("id").getAsInt())) {
                            pathList.put(pathJson.get("id").getAsInt(), new Path(pathJson));
                        }
                    }
                    triggerChangeListeners();
                } else {
                    triggerFailedListeners();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                triggerFailedListeners();
                Log.e("PathMap", "Sending path update request", t);
            }
        });
    }

    private void triggerChangeListeners() {
        for (PathMapListener listener : changeListeners) {
            listener.onPathMapUpdateSuccess();
        }
    }

    private void triggerFailedListeners() {
        for (PathMapListener listener : changeListeners) {
            listener.onPathMapUpdateFailure();
        }
    }

    public void addListener(PathMapListener pathMapChangeListener) {
        changeListeners.add(pathMapChangeListener);
    }

    public void removeListener(PathMapListener pathMapChangeListener) {
        changeListeners.remove(pathMapChangeListener);
    }

    public void addPath(Path path) {
        pathList.put(path.getId(), path);
        triggerChangeListeners();
    }

    public void deletePath(Path path) {
        pathList.remove(path.getId());
        triggerChangeListeners();
    }

    public HashMap<Integer, Path> getPathList() {
        return pathList;
    }

    public LinkedHashMap<Integer, PointOfInterest> getPointOfInterestList() {
        return pointOfInterestList;
    }
}