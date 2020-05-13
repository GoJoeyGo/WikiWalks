package com.wikiwalks.wikiwalks;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class PathMap {

    private static PathMap instance = null;
    private HashMap<Integer,Path> pathList = new HashMap<>();
    private Context context;

    private PathMap() {};

    public static PathMap getInstance() {
        if (instance == null) {
            instance = new PathMap();
        }
        return instance;
    }

    public void updatePaths(GoogleMap map, Activity activity, final PathCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        String url = R.string.local_url + "/paths/?s=%f&w=%f&n=%f&e=%f";
        Log.e("TEST", url);
        final JsonObjectRequest paths = new JsonObjectRequest(Request.Method.GET, String.format(activity.getString(R.string.local_url) + "/paths/?s=%f&w=%f&n=%f&e=%f", bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray array = response.getJSONArray("paths");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject pathJson = array.getJSONObject(i);
                        if (!pathList.containsKey(pathJson.getInt("id"))) {
                            pathList.put(pathJson.getInt("id"), new Path(pathJson));
                        }
                    }
                    callback.onSuccess(array.toString());
                } catch (JSONException e) {
                    Log.e("PATH_UPDATE", Arrays.toString(e.getStackTrace()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure("Failed to get paths...");
            }
        });
        requestQueue.add(paths);
    }

    public HashMap<Integer,Path> getPathList() {
        return pathList;
    }
}
