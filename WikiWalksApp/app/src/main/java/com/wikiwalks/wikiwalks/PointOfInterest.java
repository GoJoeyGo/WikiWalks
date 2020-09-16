package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointOfInterest {

    private LatLng coordinates;
    private int id;
    private String name;
    private ArrayList<Photo> photosList = new ArrayList<>();
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private Review ownReview;
    private int nextReviewPage = 1;
    private int nextPhotoPage = 1;
    private boolean isLoadingReviews = false;
    private boolean isLoadingPhotos = false;
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

    public PointOfInterest(JsonObject attributes, Path path) {
        id = attributes.get("id").getAsInt();
        name = attributes.get("name").getAsString();
        rating = attributes.get("average_rating").getAsDouble();
        coordinates = new LatLng(attributes.get("latitude").getAsDouble(), attributes.get("longitude").getAsDouble());
        editable = attributes.get("editable").getAsBoolean();
        this.path = path;
    }

    public static void submit(Context context, String name, double latitude, double longitude, Path path, PointOfInterestSubmitCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        request.addProperty("path", path.getId());
        request.addProperty("latitude", latitude);
        request.addProperty("longitude", longitude);
        request.addProperty("name", name);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newPointOfInterest = MainActivity.getRetrofitRequests(context).newPoI(body);
        newPointOfInterest.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body().getAsJsonObject().get("poi").getAsJsonObject();
                    PointOfInterest newPointOfInterest = new PointOfInterest(responseJson, path);
                    DataMap.getInstance().getPointOfInterestList().put(responseJson.get("id").getAsInt(), newPointOfInterest);
                    path.addPointOfInterest(newPointOfInterest);
                    callback.onSubmitPointOfInterestSuccess(newPointOfInterest);
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

    public ArrayList<Photo> getPhotosList() {
        return photosList;
    }

    public boolean isEditable() {
        return editable;
    }

    public void edit(Context context, String name, PointOfInterestEditCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("name", name);
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
    }

    public void delete(Context context, PointOfInterestEditCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> editPointOfInterest = MainActivity.getRetrofitRequests(context).deletePoI(id, body);
        editPointOfInterest.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    path.getPointsOfInterest().remove(PointOfInterest.this);
                    for (Marker marker : markers) marker.remove();
                    markers.clear();
                    DataMap.getInstance().getPointOfInterestList().remove(id);
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
    }

    public void getReviews(Context context, boolean refresh, Review.GetReviewsCallback callback) {
        if (refresh) {
            reviewsList.clear();
            nextReviewPage = 1;
            isLoadingReviews = false;
        }
        if (!isLoadingReviews) {
            isLoadingReviews = true;
            JsonObject request = new JsonObject();
            request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> getReviews = MainActivity.getRetrofitRequests(context).getPoIReviews(id, nextReviewPage, body);
            getReviews.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseJson = response.body().getAsJsonObject();
                        JsonArray reviews = responseJson.get("reviews").getAsJsonArray();
                        for (int i = 0; i < reviews.size(); i++) {
                            JsonObject review = reviews.get(i).getAsJsonObject();
                            boolean exists = false;
                            for (Review pointOfInterestReview : reviewsList) {
                                if (pointOfInterestReview.getId() == review.get("id").getAsInt()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                Review newReview = new Review(review, id, Review.ReviewType.POINT_OF_INTEREST);
                                reviewsList.add(newReview);
                            }
                        }
                        if (responseJson.has("own_review")) {
                            JsonObject review = responseJson.get("own_review").getAsJsonArray().get(0).getAsJsonObject();
                            ownReview = new Review(review, id, Review.ReviewType.POINT_OF_INTEREST);
                        }
                        rating = responseJson.get("average_rating").getAsDouble();
                        nextReviewPage++;
                        isLoadingReviews = false;
                        callback.onGetReviewSuccess();
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
        }
    }

    public void getPhotos(Context context, boolean refresh, Photo.GetPhotosCallback callback) {
        if (refresh) {
            photosList.clear();
            nextPhotoPage = 1;
            isLoadingPhotos = false;
        }
        if (!isLoadingPhotos) {
            isLoadingPhotos = true;
            JsonObject request = new JsonObject();
            request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> getPhotos = MainActivity.getRetrofitRequests(context).getPoIPhotos(id, nextPhotoPage, body);
            getPhotos.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        JsonArray photos = response.body().getAsJsonObject().get("pictures").getAsJsonArray();
                        for (int i = 0; i < photos.size(); i++) {
                            JsonObject photo = photos.get(i).getAsJsonObject();
                            boolean exists = false;
                            for (Photo poiPhoto : PointOfInterest.this.photosList) {
                                if (poiPhoto.getId() == photo.get("id").getAsInt()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                Photo newPhoto = new Photo(photo, id, Photo.PhotoType.POINT_OF_INTEREST);
                                PointOfInterest.this.photosList.add(newPhoto);
                            }
                        }
                        nextPhotoPage++;
                        isLoadingPhotos = false;
                        callback.onGetPhotosSuccess();
                    } else {
                        if (nextPhotoPage > 1) {
                            Toast.makeText(context, R.string.no_more_photos, Toast.LENGTH_SHORT).show();
                        } else {
                            isLoadingPhotos = false;
                            callback.onGetPhotosFailure();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("PointOfInterest", "Sending get photos request", t);
                    callback.onGetPhotosFailure();
                }
            });
        }
    }
}
