package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class PointOfInterestReview {
    private int id;
    private PointOfInterest pointOfInterest;
    private String name;
    private int rating;
    private String message;
    private boolean editable;

    public interface SubmitReviewCallback {
        void onSubmitReviewSuccess();
        void onSubmitReviewFailure();
    }

    public PointOfInterestReview(int id, PointOfInterest pointOfInterest, String name, int rating, String message, boolean editable) {
        this.id = id;
        this.pointOfInterest = pointOfInterest;
        this.name = name;
        this.rating = rating;
        this.message = message;
        this.editable = editable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEditable() {
        return editable;
    }

    public void submit(Context context, SubmitReviewCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + String.format("/pois/%d/reviews/new", pointOfInterest.getId());
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("text", message);
            attributes.put("rating", rating);
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                try {
                    JSONObject responseJson = response.getJSONObject("path_review");
                    name = responseJson.getString("submitter");
                    id = responseJson.getInt("id");
                    pointOfInterest.setOwnReview(this);
                    callback.onSubmitReviewSuccess();
                } catch (JSONException e) {
                    Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH_REVIEW", Arrays.toString(e.getStackTrace()));
                }
            }, error -> {
                Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
                Log.e("SUBMIT_PATH_REVIEW", Arrays.toString(error.getStackTrace()));
                callback.onSubmitReviewFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH_REVIEW", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }

    public void edit(Context context, String message, int rating, SubmitReviewCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + String.format("/pois/%d/reviews/%d/edit", pointOfInterest.getId(), id);
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("text", message);
            attributes.put("rating", rating);
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                this.message = message;
                this.rating = rating;
                callback.onSubmitReviewSuccess();
            }, error -> {
                Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
                Log.e("EDIT_PATH_REVIEW", Arrays.toString(error.getStackTrace()));
                callback.onSubmitReviewFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
            Log.e("EDIT_PATH_REVIEW", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }

    public void delete(final Context context, SubmitReviewCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = context.getString(R.string.local_url) + String.format("/pois/%d/reviews/%d/delete", pointOfInterest.getId(), id);
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, request, response -> {
                pointOfInterest.setOwnReview(null);
                callback.onSubmitReviewSuccess();
            }, error -> {
                Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
                Log.e("DELETE_PATH_REVIEW", Arrays.toString(error.getStackTrace()));
                callback.onSubmitReviewFailure();
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
            Log.e("DELETE_PATH_REVIEW", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }
}
