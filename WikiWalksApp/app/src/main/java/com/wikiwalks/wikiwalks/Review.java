package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.wikiwalks.wikiwalks.ui.dialogs.EditReviewDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Review {
    private int id;
    private int parentId;
    private String name;
    private int rating;
    private String message;
    private ReviewType type;

    public enum ReviewType {PATH, POINT_OF_INTEREST}

    public interface GetReviewCallback {
        void onGetReviewSuccess();
        void onGetReviewFailure();
    }

    public interface SubmitReviewCallback {
        void onSubmitReviewSuccess();
        void onSubmitReviewFailure();
    }
    
    public interface EditReviewCallback {
        void onEditReviewSuccess();
        void onEditReviewFailure();
        void onDeleteReviewSuccess();
        void onDeleteReviewFailure();
    }

    public Review(ReviewType type, int id, int parentId, String name, int rating, String message) {
        this.type = type;
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.rating = rating;
        this.message = message;
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

    public static void submit(Context context, ReviewType type, int parentId, String message, int rating, SubmitReviewCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("text", message);
            attributes.put("rating", rating);
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).newPathReview(parentId, body) : MainActivity.getRetrofitRequests(context).newPoIReview(parentId, body);
            newReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("path_review");
                            Review newReview = new Review(type, responseJson.getInt("id"), parentId, responseJson.getString("submitter"), rating, message);
                            if (type == ReviewType.PATH) PathMap.getInstance().getPathList().get(parentId).setOwnReview(newReview);
                            callback.onSubmitReviewSuccess();
                        } catch (JSONException e) {
                            Toast.makeText(context, "Failed to submit review...", Toast.LENGTH_SHORT).show();
                            Log.e("SUBMIT_PATH_REVIEW1", Arrays.toString(e.getStackTrace()));
                            callback.onSubmitReviewFailure();
                        }
                    } else {
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

    public void edit(Context context, String message, int rating, EditReviewCallback callback) {
        Log.e("test", Integer.toString(rating));
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("text", message);
            attributes.put("rating", rating);
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).editPathReview(parentId, id, body) : MainActivity.getRetrofitRequests(context).editPoIReview(parentId, id, body);
            editReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        Review.this.message = message;
                        Review.this.rating = rating;
                        callback.onEditReviewSuccess();
                    } else {
                        callback.onEditReviewFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
                    Log.e("EDIT_REVIEW1", Arrays.toString(t.getStackTrace()));
                    callback.onEditReviewFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to edit review...", Toast.LENGTH_SHORT).show();
            Log.e("EDIT_REVIEW2", Arrays.toString(e.getStackTrace()));
            callback.onDeleteReviewFailure();
        }
    }

    public void delete(final Context context, EditReviewCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deleteReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).deletePathReview(parentId, id, body) : MainActivity.getRetrofitRequests(context).deletePoIReview(parentId, id, body);
            deleteReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        if (type == ReviewType.PATH) PathMap.getInstance().getPathList().get(parentId).setOwnReview(null);
                        else PathMap.getInstance().getPointOfInterestList().get(parentId).setOwnReview(null);
                        callback.onDeleteReviewSuccess();
                    } else {
                        callback.onDeleteReviewFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
                    Log.e("DELETE_PATH_REVIEW1", Arrays.toString(t.getStackTrace()));
                    callback.onDeleteReviewFailure();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(context, "Failed to delete review...", Toast.LENGTH_SHORT).show();
            Log.e("DELETE_PATH_REVIEW2", Arrays.toString(e.getStackTrace()));
            callback.onDeleteReviewFailure();
        }
    }
}
