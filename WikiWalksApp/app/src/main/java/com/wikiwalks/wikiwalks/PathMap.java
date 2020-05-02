package com.wikiwalks.wikiwalks;
import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class PathMap {

    private static PathMap instance = null;
    private HashMap<Integer,Path> pathList = new HashMap<>();

    private PathMap() {};

    public static PathMap getInstance() {
        if (instance == null) {
            instance = new PathMap();
        }
        return instance;
    }

    public void updatePaths(float northBoundary, float westBoundary, float southBoundary, float eastBoundary, Activity activity, final PathCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        final JsonObjectRequest paths = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.50:5000/paths/", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("paths");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject pathJson = array.getJSONObject(i);
                        if (pathJson.isNull("parent_path") || pathList.containsKey(pathJson.getInt("parent_path"))) {
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
