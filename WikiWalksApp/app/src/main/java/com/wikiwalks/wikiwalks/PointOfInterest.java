package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointOfInterest {

    private int id;
    private String name;

    private ArrayList<Picture> picturesList = new ArrayList<>();
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private Review ownReview;
    private int nextReviewPage = 1;
    private int nextPicturePage = 1;
    private boolean isLoadingReviews = false;
    private boolean isLoadingPictures = false;
    LatLng coordinates;

    private Path path;

    public PointOfInterest(int id, String name, double latitude, double longitude, Path path) {
        this.id = id;
        this.name = name;
        this.coordinates = new LatLng(latitude, longitude);
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public Path getPath() {
        return path;
    }

    public void makeMarker(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(coordinates));
    }

    public void setOwnReview(Review pointOfInterestReview) {
        ownReview = pointOfInterestReview;
    }

    public Review getOwnReview() {
        return ownReview;
    }

    public ArrayList<Review> getReviewsList() {
        return reviewsList;
    }

    public ArrayList<Picture> getPicturesList() {
        return picturesList;
    }

    public void getReviews(Context context, Review.GetReviewCallback callback) {
        if (!isLoadingReviews) {
            isLoadingReviews = true;
            JSONObject request = new JSONObject();
            JSONObject attributes = new JSONObject();
            try {
                attributes.put("device_id", MainActivity.getDeviceId(context));
                request.put("attributes", attributes);
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
                Call<JsonElement> getReviews = MainActivity.getRetrofitRequests(context).getPoIReviews(id, nextReviewPage, body);
                getReviews.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString());
                                JSONArray reviews = responseJson.getJSONArray("reviews");
                                for (int i = 0; i < reviews.length(); i++) {
                                    JSONObject review = reviews.getJSONObject(i);
                                    boolean exists = false;
                                    for (Review pointOfInterestReview : reviewsList) {
                                        if (pointOfInterestReview.getId() == review.getInt("id")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        Review newReview = new Review(Review.ReviewType.POINT_OF_INTEREST, review.getInt("id"), id, review.getString("submitter"), review.getInt("rating"), review.getString("text"));
                                        reviewsList.add(newReview);
                                    }
                                }
                                if (responseJson.has("own_review")) {
                                    JSONObject review = responseJson.getJSONArray("own_review").getJSONObject(0);
                                    ownReview = new Review(Review.ReviewType.POINT_OF_INTEREST, review.getInt("id"), id, review.getString("submitter"), review.getInt("rating"), review.getString("text"));
                                }
                                nextReviewPage++;
                                isLoadingReviews = false;
                                callback.onGetReviewSuccess();
                            } catch (JSONException e) {
                                Log.e("GET_REVIEWS1", Arrays.toString(e.getStackTrace()));
                                isLoadingReviews = false;
                                callback.onGetReviewFailure();
                            }
                        } else {
                            if (nextReviewPage > 1) {
                                Toast.makeText(context, "No more reviews!", Toast.LENGTH_SHORT).show();
                            } else {
                                isLoadingReviews = false;
                                callback.onGetReviewFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("GET_REVIEWS2", Arrays.toString(t.getStackTrace()));
                        callback.onGetReviewFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("GET_REVIEWS3", Arrays.toString(e.getStackTrace()));
                isLoadingReviews = false;
                callback.onGetReviewFailure();
            }
        }
    }

    public void getPictures(Context context, Picture.GetPictureCallback callback) {
        if (!isLoadingPictures) {
            isLoadingPictures = true;
            JSONObject request = new JSONObject();
            JSONObject attributes = new JSONObject();
            try {
                attributes.put("device_id", MainActivity.getDeviceId(context));
                request.put("attributes", attributes);
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
                Call<JsonElement> getPictures = MainActivity.getRetrofitRequests(context).getPoIPictures(id, nextPicturePage, body);
                getPictures.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONArray pictures = new JSONObject(response.body().getAsJsonObject().toString()).getJSONArray("pictures");
                                for (int i = 0; i < pictures.length(); i++) {
                                    JSONObject picture = pictures.getJSONObject(i);
                                    boolean exists = false;
                                    for (Picture poiPicture : PointOfInterest.this.picturesList) {
                                        if (poiPicture.getId() == picture.getInt("id")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        Picture newPicture = new Picture(Picture.PictureType.POINT_OF_INTEREST, picture.getInt("id"), id, picture.getString("url"), picture.getInt("width"), picture.getInt("height"), picture.getString("description"), picture.getString("submitter"), picture.getBoolean("editable"));
                                        PointOfInterest.this.picturesList.add(newPicture);
                                    }
                                }
                                nextPicturePage++;
                                isLoadingPictures = false;
                                callback.onGetPictureSuccess();
                            } catch (JSONException e) {
                                Log.e("GET_PICTURES1", Arrays.toString(e.getStackTrace()));
                                isLoadingPictures = false;
                                callback.onGetPictureFailure();
                            }
                        } else {
                            if (nextPicturePage > 1) {
                                Toast.makeText(context, "No more pictures!", Toast.LENGTH_SHORT).show();
                            } else {
                                isLoadingPictures = false;
                                callback.onGetPictureFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("GET_PICTURES2", Arrays.toString(t.getStackTrace()));
                        callback.onGetPictureFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("GET_PICTURES3", Arrays.toString(e.getStackTrace()));
                isLoadingPictures = false;
                callback.onGetPictureFailure();
            }
        }
    }
}
