package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Photo {
    private int id;
    private int parentId;
    private String url;
    private int width;
    private int height;
    private String description;
    private String submitter;
    private boolean editable;
    private PhotoType type;

    public interface GetPhotosCallback {
        void onGetPhotosSuccess();
        void onGetPhotosFailure();
    }

    public interface EditPhotoCallback {
        void onEditPhotoSuccess();
        void onEditPhotoFailure();
        void onDeletePhotoSuccess();
        void onDeletePhotoFailure();
    }

    public Photo(JsonObject attributes, int parentId, PhotoType type) {
        id = attributes.get("id").getAsInt();
        url = attributes.get("url").getAsString();
        width = attributes.get("width").getAsInt();
        height = attributes.get("height").getAsInt();
        description = attributes.get("description").getAsString();
        submitter = attributes.get("submitter").getAsString();
        editable = attributes.get("editable").getAsBoolean();
        this.parentId = parentId;
        this.type = type;
    }

    public static void submit(Context context, PhotoType type, int parentId, Uri uri, String description, EditPhotoCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            RequestBody imageBody = RequestBody.create(MediaType.parse(context.getContentResolver().getType(uri)), buffer);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "new_photo.jpg", imageBody);
            RequestBody deviceIdBody = RequestBody.create(MultipartBody.FORM, PreferencesManager.getInstance(context).getDeviceId());
            RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
            Call<JsonElement> newPhoto = (type == PhotoType.PATH) ? MainActivity.getRetrofitRequests(context).newPathPhoto(parentId, imagePart, deviceIdBody, descriptionBody) : MainActivity.getRetrofitRequests(context).newPoIPhoto(parentId, imagePart, deviceIdBody, descriptionBody);
            newPhoto.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseJson = response.body().getAsJsonObject().get("picture").getAsJsonObject();
                        Photo newPhoto = new Photo(responseJson, parentId, type);
                        if (type == PhotoType.PATH) {
                            PathMap.getInstance().getPathList().get(parentId).getPhotosList().add(0, newPhoto);
                        } else {
                            PathMap.getInstance().getPointOfInterestList().get(parentId).getPhotosList().add(0, newPhoto);
                        }
                        PreferencesManager.getInstance(context).changePhotosUploaded(false);
                        callback.onEditPhotoSuccess();
                    } else {
                        callback.onEditPhotoFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Photo", "Sending new photo request", t);
                    callback.onEditPhotoFailure();
                }
            });
        } catch (IOException e) {
            Log.e("Photo", "Creating new photo request", e);
            callback.onEditPhotoFailure();
        }
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDescription() {
        return description;
    }

    public String getSubmitter() {
        return submitter;
    }

    public boolean isEditable() {
        return editable;
    }

    public void edit(Context context, String description, EditPhotoCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("description", description);
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> editPhoto = (type == PhotoType.PATH) ? MainActivity.getRetrofitRequests(context).editPathPhoto(parentId, id, body) : MainActivity.getRetrofitRequests(context).editPoIPhoto(parentId, id, body);
        editPhoto.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Photo.this.description = description;
                    callback.onEditPhotoSuccess();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Photo", "Sending edit photo request", t);
                callback.onEditPhotoFailure();
            }
        });
    }

    public void delete(Context context, EditPhotoCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> deletePhoto = (type == PhotoType.PATH) ? MainActivity.getRetrofitRequests(context).deletePathPhoto(parentId, id, body) : MainActivity.getRetrofitRequests(context).deletePoIPhoto(parentId, id, body);
        deletePhoto.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    if (type == PhotoType.PATH) {
                        PathMap.getInstance().getPathList().get(parentId).getPhotosList().remove(Photo.this);
                    } else {
                        PathMap.getInstance().getPointOfInterestList().get(parentId).getPhotosList().remove(Photo.this);
                    }
                    PreferencesManager.getInstance(context).changePhotosUploaded(true);
                    callback.onDeletePhotoSuccess();
                } else {
                    callback.onDeletePhotoFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("Photo", "Sending delete photo request", t);
                callback.onDeletePhotoFailure();
            }
        });
    }

    public enum PhotoType {PATH, POINT_OF_INTEREST}
}
