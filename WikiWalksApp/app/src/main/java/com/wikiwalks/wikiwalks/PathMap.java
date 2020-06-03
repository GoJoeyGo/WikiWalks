package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PathMap {

    private static PathMap instance = null;
    private HashMap<Integer, Path> pathList = new HashMap<>();

    public interface PathMapListener {
        void OnPathMapChange();
        void OnPathMapUpdateFailure();
    }

    public ArrayList<PathMapListener> changeListeners = new ArrayList<>();

    private PathMap() {}

    public static PathMap getInstance() {
        if (instance == null) {
            instance = new PathMap();
        }
        return instance;
    }

    public void updatePaths(LatLngBounds bounds, Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final JsonObjectRequest paths = new JsonObjectRequest(Request.Method.GET, String.format(context.getString(R.string.local_url) + "/paths/?s=%f&w=%f&n=%f&e=%f", bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude), null, response -> {
            try {
                JSONArray array = response.getJSONArray("paths");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject pathJson = array.getJSONObject(i);
                    if (!pathList.containsKey(pathJson.getInt("id"))) {
                        pathList.put(pathJson.getInt("id"), new Path(pathJson));
                    }
                }
                triggerChangeListeners();
            } catch (JSONException e) {
                triggerFailedListeners();
                Log.e("PATH_UPDATE", Arrays.toString(e.getStackTrace()));

            }
        }, error -> {
            triggerFailedListeners();
            Log.e("PATH_UPDATE", Arrays.toString(error.getStackTrace()));
        });
        requestQueue.add(paths);
    }

    private void triggerChangeListeners() {
        for (PathMapListener listener : changeListeners) {
            listener.OnPathMapChange();
        }
    }

    private void triggerFailedListeners() {
        for (PathMapListener listener : changeListeners) {
            listener.OnPathMapUpdateFailure();
        }
    }

    public void addListener(PathMapListener pathMapChangeListener) {
        changeListeners.add(pathMapChangeListener);
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
}