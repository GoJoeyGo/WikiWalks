package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    private LatLng coordinates;
    private int id;
    private String name;
    private ArrayList<Picture> picturesList = new ArrayList<>();
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private Review ownReview;
    private int nextReviewPage = 1;
    private int nextPicturePage = 1;
    private boolean isLoadingReviews = false;
    private boolean isLoadingPictures = false;
    private double rating;
    private boolean editable;
    private Path path;
    private ArrayList<Marker> markers = new ArrayList<>();

    public interface PointOfInterestSubmitCallback {
        void onSubmitPointOfInterestSuccess(PointOfInterest pointOfInterest);
        void onSubmitPointOfInterestFailure();
    }

    public interface PointOfInterestEditCallback {
        void onEditPointOfInterestSuccess();
        void onEditPointOfInterestFailure();
        void onDeletePointOfInterestSuccess();
        void onDeletePointOfInterestFailure();
    }

    public PointOfInterest(int id, String name, double rating, double latitude, double longitude, Path path, boolean editable) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.coordinates = new LatLng(latitude, longitude);
        this.path = path;
        this.editable = editable;
    }

    public static void submit(Context context, String name, double latitude, double longitude, Path path, PointOfInterestSubmitCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            attributes.put("path", path.getId());
            attributes.put("latitude", latitude);
            attributes.put("longitude", longitude);
            attributes.put("name", name);
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> newPointOfInterest = MainActivity.getRetrofitRequests(context).newPoI(body);
            newPointOfInterest.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().toString()).getJSONObject("poi");
                            PointOfInterest newPointOfInterest = new PointOfInterest(responseJson.getInt("id"), responseJson.getString("name"), responseJson.getDouble("average_rating"), responseJson.getDouble("latitude"), responseJson.getDouble("longitude"), path, true);
                            PathMap.getInstance().getPointOfInterestList().put(responseJson.getInt("id"), newPointOfInterest);
                            path.addPointOfInterest(newPointOfInterest);
                            callback.onSubmitPointOfInterestSuccess(newPointOfInterest);
                        } catch (JSONException e) {
                            Log.e("PointOfInterest", "Getting point from submit response", e);
                        }
                        PreferencesManager.getInstance(context).changePointsOfInterestMarked(false);
                    } else {
                        callback.onSubmitPointOfInterestFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("PointOfInterest", "Sending new point request", t);
                    callback.onSubmitPointOfInterestFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("PointOfInterest", "Creating new point request", e);
            callback.onSubmitPointOfInterestFailure();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public Path getPath() {
        return path;
    }

    public Marker makeMarker(GoogleMap map, float hue) {
        Marker marker = map.addMarker(new MarkerOptions().position(coordinates).icon(BitmapDescriptorFactory.defaultMarker(hue)));
        markers.add(marker);
        marker.setTag(id);
        return marker;
    }

    public Review getOwnReview() {
        return ownReview;
    }

    public void setOwnReview(Review pointOfInterestReview) {
        ownReview = pointOfInterestReview;
    }

    public ArrayList<Review> getReviewsList() {
        return reviewsList;
    }

    public ArrayList<Picture> getPicturesList() {
        return picturesList;
    }

    public boolean isEditable() {
        return editable;
    }

    public void edit(Context context, String name, PointOfInterestEditCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("name", name);
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editPointOfInterest = MainActivity.getRetrofitRequests(context).editPoI(id, body);
            editPointOfInterest.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        PointOfInterest.this.name = name;
                        callback.onEditPointOfInterestSuccess();
                    } else {
                        callback.onEditPointOfInterestFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("PointOfInterest", "Sending edit point request", t);
                    callback.onEditPointOfInterestFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("PointOfInterest", "Creating edit point request", e);
            callback.onEditPointOfInterestFailure();
        }
    }

    public void delete(Context context, PointOfInterestEditCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editPointOfInterest = MainActivity.getRetrofitRequests(context).deletePoI(id, body);
            editPointOfInterest.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        path.getPointsOfInterest().remove(PointOfInterest.this);
                        for (Marker marker : markers) marker.remove();
                        markers.clear();
                        PathMap.getInstance().getPointOfInterestList().remove(id);
                        PreferencesManager.getInstance(context).changePointsOfInterestMarked(true);
                        callback.onDeletePointOfInterestSuccess();
                    } else {
                        callback.onDeletePointOfInterestFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("PointOfInterest", "Sending delete point request", t);
                    callback.onEditPointOfInterestFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("PointOfInterest", "Creating delete point request", e);
            callback.onDeletePointOfInterestFailure();
        }
    }

    public void getReviews(Context context, boolean refresh, Review.GetReviewCallback callback) {
        if (refresh) {
            reviewsList.clear();
            nextReviewPage = 1;
            isLoadingReviews = false;
        }
        if (!isLoadingReviews) {
            isLoadingReviews = true;
            JSONObject request = new JSONObject();
            JSONObject attributes = new JSONObject();
            try {
                attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
                                Log.e("PointOfInterest", "Getting reviews from response", e);
                                isLoadingReviews = false;
                                callback.onGetReviewFailure();
                            }
                        } else {
                            if (nextReviewPage > 1) {
                                Toast.makeText(context, R.string.no_more_reviews, Toast.LENGTH_SHORT).show();
                            } else {
                                isLoadingReviews = false;
                                callback.onGetReviewFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("PointOfInterest", "Sending get reviews request", t);
                        callback.onGetReviewFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("PointOfInterest", "Creating get reviews request", e);
                isLoadingReviews = false;
                callback.onGetReviewFailure();
            }
        }
    }

    public void getPictures(Context context, boolean refresh, Picture.GetPicturesCallback callback) {
        if (refresh) {
            picturesList.clear();
            nextPicturePage = 1;
            isLoadingPictures = false;
        }
        if (!isLoadingPictures) {
            isLoadingPictures = true;
            JSONObject request = new JSONObject();
            JSONObject attributes = new JSONObject();
            try {
                attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
                                callback.onGetPicturesSuccess();
                            } catch (JSONException e) {
                                Log.e("PointOfInterest", "Getting photos from response", e);
                                isLoadingPictures = false;
                                callback.onGetPicturesFailure();
                            }
                        } else {
                            if (nextPicturePage > 1) {
                                Toast.makeText(context, R.string.no_more_photos, Toast.LENGTH_SHORT).show();
                            } else {
                                isLoadingPictures = false;
                                callback.onGetPicturesFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("PointOfInterest", "Sending get photos request", t);
                        callback.onGetPicturesFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("PointOfInterest", "Creating get photos request", e);
                isLoadingPictures = false;
                callback.onGetPicturesFailure();
            }
        }
    }
}
