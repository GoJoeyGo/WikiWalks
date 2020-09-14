package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

public class Path {

    int id;
    private String name;
    private int walkCount;
    private double rating;

    private ArrayList<Route> routeList = new ArrayList<>();
    private ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();
    private ArrayList<Photo> photos = new ArrayList<>();
    private ArrayList<Review> reviews = new ArrayList<>();
    private ArrayList<GroupWalk> groupWalks = new ArrayList<>();
    private Review ownReview;
    private int nextReviewPage = 1;
    private int nextPhotoPage = 1;
    private boolean isLoadingReviews = false;
    private boolean isLoadingPhotos = false;

    private ArrayList<Marker> markers = new ArrayList<>();
    private LatLng markerPoint;
    private LatLngBounds bounds;

    public interface GetPathCallback {
        void onGetPathSuccess(Path path);
        void onGetPathFailure();
    }

    public interface PathChangeCallback {
        void onEditSuccess();
        void onEditFailure();
    }

    public Path(JsonObject attributes) {
        id = attributes.get("id").getAsInt();
        name = attributes.get("name").getAsString();
        walkCount = attributes.get("walk_count").getAsInt();
        rating = attributes.get("average_rating").getAsDouble();
        markerPoint = new LatLng(attributes.get("marker_point").getAsJsonArray().get(0).getAsDouble(), attributes.get("marker_point").getAsJsonArray().get(1).getAsDouble());
        JsonArray boundaries = attributes.get("boundaries").getAsJsonArray();
        bounds = new LatLngBounds(new LatLng(boundaries.get(0).getAsDouble(), boundaries.get(1).getAsDouble()), new LatLng(boundaries.get(2).getAsDouble(), boundaries.get(3).getAsDouble()));
        JsonArray points_of_interest = attributes.get("points_of_interest").getAsJsonArray();
        for (int i = 0; i < points_of_interest.size(); i++) {
            PointOfInterest newPointOfInterest = new PointOfInterest(points_of_interest.get(i).getAsJsonObject(), this);
            pointsOfInterest.add(newPointOfInterest);
            PathMap.getInstance().getPointOfInterestList().put(newPointOfInterest.getId(), newPointOfInterest);
        }
        JsonArray groupWalks = attributes.get("group_walks").getAsJsonArray();
        for (int i = 0; i < groupWalks.size(); i++) {
            GroupWalk newGroupWalk = new GroupWalk(groupWalks.get(i).getAsJsonObject(), this);
            this.groupWalks.add(newGroupWalk);
        }
        JsonArray routes = attributes.get("routes").getAsJsonArray();
        for (int i = 0; i < routes.size(); i++) {
            JsonObject route = routes.get(i).getAsJsonObject();
            routeList.add(new Route(route, this));
        }
    }

    public static void getPath(Context context, int id, GetPathCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> updatePath = MainActivity.getRetrofitRequests(context).updatePath(id, body);
        updatePath.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Path newPath = new Path(response.body().getAsJsonObject().get("path").getAsJsonObject());
                    PathMap.getInstance().addPath(newPath);
                    callback.onGetPathSuccess(newPath);
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Path", "Sending path update request", t);
                Toast.makeText(context, R.string.get_path_failure, Toast.LENGTH_SHORT).show();
                callback.onGetPathFailure();
            }
        });
    }

    public void addPointOfInterest(PointOfInterest pointOfInterest) {
        pointsOfInterest.add(pointOfInterest);
    }

    public void removeRoute(Route route) {
        routeList.remove(route);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWalkCount() {
        return walkCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public ArrayList<Photo> getPhotosList() {
        return photos;
    }

    public ArrayList<Review> getReviewsList() {
        return reviews;
    }

    public ArrayList<GroupWalk> getGroupWalksList() {
        return groupWalks;
    }

    public Review getOwnReview() {
        return ownReview;
    }

    public void setOwnReview(Review ownReview) {
        this.ownReview = ownReview;
    }

    public ArrayList<Double> getAllAltitudes() {
        ArrayList<Double> allAltitudes = new ArrayList<>();
        for (Route route : routeList) {
            allAltitudes.addAll(route.getAltitudes());
        }
        return allAltitudes;
    }

    public ArrayList<Double> getAllLatitudes() {
        ArrayList<Double> allLatitudes = new ArrayList<>();
        for (Route route : routeList) {
            allLatitudes.addAll(route.getLatitudes());
        }
        return allLatitudes;
    }

    public ArrayList<Double> getAllLongitudes() {
        ArrayList<Double> allLongitudes = new ArrayList<>();
        for (Route route : routeList) {
            allLongitudes.addAll(route.getLongitudes());
        }
        return allLongitudes;
    }

    public ArrayList<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public ArrayList<Route> getRoutes() {
        return routeList;
    }

    public LatLng getMarkerPoint() {
        return markerPoint;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public Marker makeMarker(GoogleMap map) {
        Marker marker = map.addMarker(new MarkerOptions().position(markerPoint));
        marker.setTag(id);
        marker.setTitle(name);
        markers.add(marker);
        return marker;
    }

    public void edit(Context context, String title, PathChangeCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("name", title);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> editPath = MainActivity.getRetrofitRequests(context).editPath(id, body);
        editPath.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Path.this.name = title;
                    callback.onEditSuccess();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Path", "Sending edit group walk request", t);
                callback.onEditFailure();
            }
        });
    }

    public void walk(Context context) {
        Call<JsonElement> walkPath = MainActivity.getRetrofitRequests(context).walkPath(id);
        walkPath.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Path.this.walkCount = response.body().getAsJsonObject().get("new_count").getAsInt();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Path", "Sending walk path request", t);
            }
        });
    }

    public void getReviews(Context context, boolean refresh, Review.GetReviewsCallback callback) {
        if (refresh) {
            reviews.clear();
            nextReviewPage = 1;
            isLoadingReviews = false;
        }
        if (!isLoadingReviews) {
            isLoadingReviews = true;
            JsonObject request = new JsonObject();
            request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> getReviews = MainActivity.getRetrofitRequests(context).getPathReviews(id, nextReviewPage, body);
            getReviews.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseJson = response.body().getAsJsonObject();
                        JsonArray reviews = responseJson.get("reviews").getAsJsonArray();
                        for (int i = 0; i < reviews.size(); i++) {
                            JsonObject review = reviews.get(i).getAsJsonObject();
                            boolean exists = false;
                            for (Review pathReview : Path.this.reviews) {
                                if (pathReview.getId() == review.get("id").getAsInt()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                Review newReview = new Review(review, id, Review.ReviewType.PATH);
                                Path.this.reviews.add(newReview);
                            }
                        }
                        if (responseJson.has("own_review")) {
                            ownReview = new Review(responseJson.get("own_review").getAsJsonArray().get(0).getAsJsonObject(), id, Review.ReviewType.PATH);
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
                    Log.e("Path", "Sending get reviews request", t);
                    callback.onGetReviewFailure();
                }
            });
        }
    }

    public void getPhotos(Context context, boolean refresh, Photo.GetPhotosCallback callback) {
        if (refresh) {
            photos.clear();
            nextPhotoPage = 1;
            isLoadingPhotos = false;
        }
        if (!isLoadingPhotos) {
            isLoadingPhotos = true;
            JsonObject request = new JsonObject();
            request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> getPhotos = MainActivity.getRetrofitRequests(context).getPathPhotos(id, nextPhotoPage, body);
            getPhotos.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        JsonArray photos = response.body().getAsJsonObject().get("pictures").getAsJsonArray();
                        for (int i = 0; i < photos.size(); i++) {
                            JsonObject photo = photos.get(i).getAsJsonObject();
                            boolean exists = false;
                            for (Photo pathPhoto : Path.this.photos) {
                                if (pathPhoto.getId() == photo.get("id").getAsInt()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                Photo newPhoto = new Photo(photo, id, Photo.PhotoType.PATH);
                                Path.this.photos.add(newPhoto);
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
                    Log.e("Path", "Sending get photos request", t);
                    callback.onGetPhotosFailure();
                }
            });
        }
    }

    public void getGroupWalks(Context context, GroupWalk.GetGroupWalksCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> getGroupWalks = MainActivity.getRetrofitRequests(context).getGroupWalks(id, body);
        getGroupWalks.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    groupWalks.clear();
                    JsonArray newGroupWalks = response.body().getAsJsonObject().get("group_walks").getAsJsonArray();
                    for (JsonElement groupWalk : newGroupWalks) {
                        groupWalks.add(new GroupWalk(groupWalk.getAsJsonObject(), Path.this));
                    }
                    callback.onGetGroupWalksSuccess();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Path", "Sending get group walks request", t);
                callback.onGetGroupWalksFailure();
            }
        });
    }
}
