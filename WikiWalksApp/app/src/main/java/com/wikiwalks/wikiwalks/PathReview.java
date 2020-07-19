package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PathReview {
    private int id;
    private Path path;
    private String name;
    private int rating;
    private String message;
    private boolean editable;

    public interface SubmitReviewCallback {
        void onSubmitReviewSuccess();

        void onSubmitReviewFailure();
    }

    public PathReview(int id, Path path, String name, int rating, String message, boolean editable) {
        this.id = id;
        this.path = path;
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
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("text", message);
            attributes.put("rating", rating);
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newPathReview = MainActivity.getRetrofitRequests(context).newPathReview(path.getId(), body);
            newPathReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("path_review");
                        name = responseJson.getString("submitter");
                        id = responseJson.getInt("id");
                        path.setOwnReview(PathReview.this);
                        callback.onSubmitReviewSuccess();
                    } catch (JSONException e) {
                        Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
                        Log.e("SUBMIT_PATH_REVIEW1", Arrays.toString(e.getStackTrace()));
                        callback.onSubmitReviewFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
                    Log.e("SUBMIT_PATH_REVIEW2", Arrays.toString(t.getStackTrace()));
                    callback.onSubmitReviewFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
            Log.e("SUBMIT_PATH_REVIEW3", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }

    public void edit(Context context, String message, int rating, SubmitReviewCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("text", message);
            attributes.put("rating", rating);
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editPathReview = MainActivity.getRetrofitRequests(context).editPathReview(path.getId(), id, body);
            editPathReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    PathReview.this.message = message;
                    PathReview.this.rating = rating;
                    callback.onSubmitReviewSuccess();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
                    Log.e("EDIT_PATH_REVIEW1", Arrays.toString(t.getStackTrace()));
                    callback.onSubmitReviewFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
            Log.e("EDIT_PATH_REVIEW2", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }

    public void delete(final Context context, SubmitReviewCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deletePathReview = MainActivity.getRetrofitRequests(context).deletePathReview(path.getId(), id, body);
            deletePathReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    path.setOwnReview(null);
                    callback.onSubmitReviewSuccess();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
                    Log.e("DELETE_PATH_REVIEW1", Arrays.toString(t.getStackTrace()));
                    callback.onSubmitReviewFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
            Log.e("DELETE_PATH_REVIEW2", Arrays.toString(e.getStackTrace()));
            callback.onSubmitReviewFailure();
        }
    }
}
