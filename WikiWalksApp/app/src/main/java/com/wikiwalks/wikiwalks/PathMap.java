package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private PathMap() {}

    public static PathMap getInstance() {
        if (instance == null) {
            instance = new PathMap();
        }
        return instance;
    }

    public void updatePaths(LatLngBounds bounds, Context context) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> getPaths = MainActivity.getRetrofitRequests(context).getPaths(bounds.northeast.latitude, bounds.northeast.longitude, bounds.southwest.latitude, bounds.southwest.longitude, body);
            getPaths.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONArray responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONArray("paths");
                            for (int i = 0; i < responseJson.length(); i++) {
                                JSONObject pathJson = responseJson.getJSONObject(i);
                                if (!pathList.containsKey(pathJson.getInt("id"))) {
                                    pathList.put(pathJson.getInt("id"), new Path(pathJson));
                                }
                            }
                            triggerChangeListeners();
                        } catch (JSONException e) {
                            triggerFailedListeners();
                            Log.e("PathMap", "Getting paths from response", e);
                        }
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
        } catch (JSONException e) {
            triggerFailedListeners();
            Log.e("PathMap", "Creating path update request", e);
        }
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