package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private ArrayList<Picture> pictures = new ArrayList<>();
    private ArrayList<Review> reviews = new ArrayList<>();
    private ArrayList<GroupWalk> groupWalks = new ArrayList<>();
    private Review ownReview;
    private int nextReviewPage = 1;
    private int nextPicturePage = 1;
    private boolean isLoadingReviews = false;
    private boolean isLoadingPictures = false;

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

    public Path(int id, String name, int walkCount, double rating, double[] bounds) {
        this.id = id;
        this.name = name;
        this.walkCount = walkCount;
        this.rating = rating;
        this.bounds = new LatLngBounds(new LatLng(bounds[0], bounds[1]), new LatLng(bounds[2], bounds[3]));
    }

    public Path() {
        id = -1;
    }

    public Path(JSONObject pathJson) throws JSONException {
        id = pathJson.getInt("id");
        name = pathJson.getString("name");
        walkCount = pathJson.getInt("walk_count");
        rating = pathJson.getDouble("average_rating");
        markerPoint = new LatLng(pathJson.getJSONArray("marker_point").getDouble(0), pathJson.getJSONArray("marker_point").getDouble(1));
        JSONArray boundaries = pathJson.getJSONArray("boundaries");
        bounds = new LatLngBounds(new LatLng(boundaries.getDouble(0), boundaries.getDouble(1)), new LatLng(boundaries.getDouble(2), boundaries.getDouble(3)));
        JSONArray points_of_interest = pathJson.getJSONArray("points_of_interest");
        for (int i = 0; i < points_of_interest.length(); i++) {
            JSONObject pointOfInterest = points_of_interest.getJSONObject(i);
            PointOfInterest newPointOfInterest = new PointOfInterest(pointOfInterest.getInt("id"), pointOfInterest.getString("name"), pointOfInterest.getDouble("average_rating"), pointOfInterest.getDouble("latitude"), pointOfInterest.getDouble("longitude"), this, pointOfInterest.getBoolean("editable"));
            pointsOfInterest.add(newPointOfInterest);
            PathMap.getInstance().getPointOfInterestList().put(newPointOfInterest.getId(), newPointOfInterest);
        }
        JSONArray group_walks = pathJson.getJSONArray("group_walks");
        for (int i = 0; i < group_walks.length(); i++) {
            JSONObject groupWalk = group_walks.getJSONObject(i);
            JSONArray attendees = groupWalk.getJSONArray("attendees");
            ArrayList<String> attendeesList = new ArrayList<>();
            for (int j = 0; j < attendees.length(); j++) {
                attendeesList.add(attendees.getJSONObject(j).getString("nickname"));
            }
            GroupWalk newGroupWalk = new GroupWalk(this, groupWalk.getInt("id"), groupWalk.getString("title"), groupWalk.getLong("time"), attendeesList, groupWalk.getString("submitter"), groupWalk.getBoolean("attending"), groupWalk.getBoolean("editable"));
            groupWalks.add(newGroupWalk);
        }
        JSONArray routes = pathJson.getJSONArray("routes");
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            ArrayList<Double> routeLatitudes = new ArrayList<>();
            ArrayList<Double> routeLongitudes = new ArrayList<>();
            ArrayList<Double> routeAltitudes = new ArrayList<>();
            for (int j = 0; j < route.getJSONArray("latitudes").length(); j++) {
                routeLatitudes.add(route.getJSONArray("latitudes").getDouble(j));
                routeLongitudes.add(route.getJSONArray("longitudes").getDouble(j));
                routeAltitudes.add(route.getJSONArray("altitudes").getDouble(j));
            }
            boolean editable = route.getBoolean("editable");
            int routeId = route.getInt("id");
            routeList.add(new Route(routeId, this, editable, routeLatitudes, routeLongitudes, routeAltitudes));
        }
    }

    public static void getPath(Context context, int id, GetPathCallback callback) {
        Call<JsonElement> updatePath = MainActivity.getRetrofitRequests(context).updatePath(id);
        updatePath.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        Path newPath = new Path(new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("path"));
                        PathMap.getInstance().addPath(newPath);
                        callback.onGetPathSuccess(newPath);
                    } catch (JSONException e) {
                        Log.e("Path", "Getting path from update response", e);
                    }
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
    public static double findDistance(Path path){
        final int R = 6371; // this will be a tad off but it will only be really noticable if people do walks that are really long
        ArrayList<Double> latitudes =  path.getAllLatitudes();
        ArrayList<Double> longitudes =  path.getAllLongitudes();
        ArrayList<Double> Altitudes =  path.getAllAltitudes();
        double toatalDistance = 0.0;
        for(int x = 1; x<latitudes.size();x++){
            double lon1 = Math.toRadians(longitudes.get(x-1));
            double lon2 = Math.toRadians(longitudes.get(x));
            double lat1 = Math.toRadians(latitudes.get(x-1));
            double lat2 = Math.toRadians(latitudes.get(x));
            double height =  Altitudes.get(x-1) -  Altitudes.get(x);
            double latDistance = lat2 - lat1;
            double lonDistance = lon2 - lon1;
            double a =
                    Math.sin(latDistance/2)*Math.sin(latDistance/2) +
                    Math.cos(lat1)*Math.cos(lat2)*
                    Math.sin(lonDistance/2)*Math.sin(lonDistance/2);
            double c = 2* Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = Math.sqrt(Math.pow( R * c * 1000, 2) + Math.pow(height, 2));
            toatalDistance+=distance;
        }
        return toatalDistance;
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

    public ArrayList<Picture> getPicturesList() {
        return pictures;
    }

    public ArrayList<Review> getReviewsList() {
        return reviews;
    }

    public ArrayList<GroupWalk> getGroupWalks() {
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
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("name", title);
            request.put("attributes", attributes);
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
        } catch (JSONException e) {
            Log.e("Path", "Creating edit path request", e);
            callback.onEditFailure();
        }
    }

    public void walk(Context context) {
        Call<JsonElement> walkPath = MainActivity.getRetrofitRequests(context).walkPath(id);
        walkPath.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        Path.this.walkCount = new JSONObject(response.body().toString()).getInt("new_count");
                    } catch (JSONException e) {
                        Log.e("Path", "Getting walk count from walk response", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Path", "Sending walk path request", t);
            }
        });
    }

    public void getReviews(Context context, boolean refresh, Review.GetReviewCallback callback) {
        if (refresh) {
            reviews.clear();
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
                Call<JsonElement> getReviews = MainActivity.getRetrofitRequests(context).getPathReviews(id, nextReviewPage, body);
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
                                    for (Review pathReview : Path.this.reviews) {
                                        if (pathReview.getId() == review.getInt("id")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        Review newReview = new Review(Review.ReviewType.PATH, review.getInt("id"), id, review.getString("submitter"), review.getInt("rating"), review.getString("text"));
                                        Path.this.reviews.add(newReview);
                                    }
                                }
                                if (responseJson.has("own_review")) {
                                    JSONObject review = responseJson.getJSONArray("own_review").getJSONObject(0);
                                    ownReview = new Review(Review.ReviewType.PATH, review.getInt("id"), id, review.getString("submitter"), review.getInt("rating"), review.getString("text"));
                                }
                                nextReviewPage++;
                                isLoadingReviews = false;
                                callback.onGetReviewSuccess();
                            } catch (JSONException e) {
                                Log.e("Path", "Getting reviews from response", e);
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
                        Log.e("Path", "Sending get reviews request", t);
                        callback.onGetReviewFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("Path", "Creating get reviews request", e);
                isLoadingReviews = false;
                callback.onGetReviewFailure();
            }
        }
    }

    public void getPictures(Context context, boolean refresh, Picture.GetPicturesCallback callback) {
        if (refresh) {
            pictures.clear();
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
                Call<JsonElement> getPictures = MainActivity.getRetrofitRequests(context).getPathPictures(id, nextPicturePage, body);
                getPictures.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONArray pictures = new JSONObject(response.body().getAsJsonObject().toString()).getJSONArray("pictures");
                                for (int i = 0; i < pictures.length(); i++) {
                                    JSONObject picture = pictures.getJSONObject(i);
                                    boolean exists = false;
                                    for (Picture pathPicture : Path.this.pictures) {
                                        if (pathPicture.getId() == picture.getInt("id")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        Picture newPicture = new Picture(Picture.PictureType.PATH, picture.getInt("id"), id, picture.getString("url"), picture.getInt("width"), picture.getInt("height"), picture.getString("description"), picture.getString("submitter"), picture.getBoolean("editable"));
                                        Path.this.pictures.add(newPicture);
                                    }
                                }
                                nextPicturePage++;
                                isLoadingPictures = false;
                                callback.onGetPicturesSuccess();
                            } catch (JSONException e) {
                                Log.e("Path", "Getting photos from response", e);
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
                        Log.e("Path", "Sending get photos request", t);
                        callback.onGetPicturesFailure();
                    }
                });
            } catch (JSONException e) {
                Log.e("Path", "Creating get photos request", e);
                isLoadingPictures = false;
                callback.onGetPicturesFailure();
            }
        }
    }
}
