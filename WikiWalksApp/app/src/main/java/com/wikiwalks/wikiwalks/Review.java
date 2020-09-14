package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    public interface GetReviewsCallback {
        void onGetReviewSuccess();
        void onGetReviewFailure();
    }

    public interface EditReviewCallback {
        void onEditReviewSuccess();
        void onEditReviewFailure();
        void onDeleteReviewSuccess();
        void onDeleteReviewFailure();
    }

    public Review(JsonObject attributes, int parentId, ReviewType type) {
        id = attributes.get("id").getAsInt();
        name = attributes.get("submitter").getAsString();
        message = attributes.get("text").getAsString();
        rating = attributes.get("rating").getAsInt();
        this.parentId = parentId;
        this.type = type;
    }

    public static void submit(Context context, ReviewType type, int parentId, String message, int rating, EditReviewCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        request.addProperty("text", message);
        request.addProperty("rating", rating);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).newPathReview(parentId, body) : MainActivity.getRetrofitRequests(context).newPoIReview(parentId, body);
        newReview.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body().getAsJsonObject().get("review").getAsJsonObject();
                    Review newReview = new Review(responseJson, parentId, type);
                    if (type == ReviewType.PATH) {
                        Path parentPath = PathMap.getInstance().getPathList().get(parentId);
                        parentPath.setOwnReview(newReview);
                        parentPath.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                    } else {
                        PointOfInterest parentPointOfInterest = PathMap.getInstance().getPointOfInterestList().get(parentId);
                        parentPointOfInterest.setOwnReview(newReview);
                        parentPointOfInterest.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                    }
                    PreferencesManager.getInstance(context).changeReviewsWritten(false);
                    callback.onEditReviewSuccess();
                } else {
                    callback.onEditReviewFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Review", "Sending new review request", t);
                callback.onEditReviewFailure();
            }
        });
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

    public void edit(Context context, String message, int rating, EditReviewCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("text", message);
        request.addProperty("rating", rating);
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> editReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).editPathReview(parentId, id, body) : MainActivity.getRetrofitRequests(context).editPoIReview(parentId, id, body);
        editReview.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Review.this.message = message;
                    Review.this.rating = rating;
                    if (type == ReviewType.PATH) {
                        Path parentPath = PathMap.getInstance().getPathList().get(parentId);
                        parentPath.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                    } else {
                        PointOfInterest parentPointOfInterest = PathMap.getInstance().getPointOfInterestList().get(parentId);
                        parentPointOfInterest.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                    }
                    callback.onEditReviewSuccess();
                } else {
                    callback.onEditReviewFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Review", "Sending edit review request", t);
                callback.onEditReviewFailure();
            }
        });
    }

    public void delete(Context context, EditReviewCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> deleteReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).deletePathReview(parentId, id, body) : MainActivity.getRetrofitRequests(context).deletePoIReview(parentId, id, body);
        deleteReview.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    if (type == ReviewType.PATH) {
                        Path parentPath = PathMap.getInstance().getPathList().get(parentId);
                        parentPath.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                        parentPath.setOwnReview(null);
                    } else {
                        PointOfInterest parentPointOfInterest = PathMap.getInstance().getPointOfInterestList().get(parentId);
                        parentPointOfInterest.setRating(response.body().getAsJsonObject().get("average_rating").getAsDouble());
                        parentPointOfInterest.setOwnReview(null);
                    }
                    PreferencesManager.getInstance(context).changeReviewsWritten(true);
                    callback.onDeleteReviewSuccess();
                } else {
                    callback.onDeleteReviewFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Review", "Sending delete review request", t);
                callback.onDeleteReviewFailure();
            }
        });
    }

    public enum ReviewType {PATH, POINT_OF_INTEREST}
}
