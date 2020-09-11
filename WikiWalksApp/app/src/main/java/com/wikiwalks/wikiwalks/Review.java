package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

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

    public interface GetReviewCallback {
        void onGetReviewSuccess();
        void onGetReviewFailure();
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

    public static void submit(Context context, ReviewType type, int parentId, String message, int rating, EditReviewCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("text", message);
            request.put("rating", rating);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newReview = (type == ReviewType.PATH) ? MainActivity.getRetrofitRequests(context).newPathReview(parentId, body) : MainActivity.getRetrofitRequests(context).newPoIReview(parentId, body);
            newReview.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("review");
                            Review newReview = new Review(type, responseJson.getInt("id"), parentId, responseJson.getString("submitter"), rating, message);
                            if (type == ReviewType.PATH) {
                                Path parentPath = PathMap.getInstance().getPathList().get(parentId);
                                parentPath.setOwnReview(newReview);
                                parentPath.setRating(new JSONObject(response.body().getAsJsonObject().toString()).getDouble("average_rating"));
                            } else {
                                PointOfInterest parentPointOfInterest = PathMap.getInstance().getPointOfInterestList().get(parentId);
                                parentPointOfInterest.setOwnReview(newReview);
                                parentPointOfInterest.setRating(new JSONObject(response.body().getAsJsonObject().toString()).getDouble("average_rating"));
                            }
                            PreferencesManager.getInstance(context).changeReviewsWritten(false);
                            callback.onEditReviewSuccess();
                        } catch (JSONException e) {
                            Log.e("Review", "Getting review from response", e);
                            callback.onEditReviewSuccess();
                        }
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
        } catch (JSONException e) {
            Log.e("Review", "Creating new review request", e);
            callback.onEditReviewFailure();
        }
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
        JSONObject request = new JSONObject();
        try {
            request.put("text", message);
            request.put("rating", rating);
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
        } catch (JSONException e) {
            Log.e("Review", "Creating edit review request", e);
            callback.onDeleteReviewFailure();
        }
    }

    public void delete(final Context context, EditReviewCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
        } catch (JSONException e) {
            Log.e("Review", "Creating delete review request", e);
            callback.onDeleteReviewFailure();
        }
    }

    public enum ReviewType {PATH, POINT_OF_INTEREST}
}
