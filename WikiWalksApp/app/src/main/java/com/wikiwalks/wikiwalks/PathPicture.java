package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PathPicture {
    private int id;
    private Path path;
    private String url;
    private int width;
    private int height;
    private String description;
    private String submitter;
    private boolean editable;

    public interface PictureUploadCallback {
        void onSubmitPictureSuccess();
        void onSubmitPictureFailure();
    }

    public interface PictureEditCallback {
        void onEditPictureSuccess();
        void onEditPictureFailure();
        void onDeletePictureSuccess();
        void onDeletePictureFailure();
    }


    public PathPicture(int id, Path path, String url, int width, int height, String description, String submitter, boolean editable) {
        this.id = id;
        this.path = path;
        this.url = url;
        this.width = width;
        this.height = height;
        this.description = description;
        this.submitter = submitter;
        this.editable = editable;
    }

    public int getId() {
        return id;
    }

    public Path getPath() {
        return path;
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

    public static void upload(Context context, String filename, Uri uri, String description, Path path, PictureUploadCallback callback) {
        File file = new File(filename);
        RequestBody imageBody = RequestBody.create(MediaType.parse(context.getContentResolver().getType(uri)), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
        RequestBody deviceIdBody = RequestBody.create(MultipartBody.FORM, MainActivity.getDeviceId(context));
        RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
        Call<JsonElement> newPathPicture = MainActivity.getRetrofitRequests(context).newPathPicture(path.getId(), imagePart, deviceIdBody, descriptionBody);
        newPathPicture.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("path_picture");
                        path.getPathPictures().add(0, new PathPicture(responseJson.getInt("id"), path, responseJson.getString("url"), responseJson.getInt("width"), responseJson.getInt("height"), responseJson.getString("description"), responseJson.getString("submitter"), true));
                        callback.onSubmitPictureSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onSubmitPictureFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                callback.onSubmitPictureFailure();
            }
        });
    }

    public void edit(Context context, String description, PictureEditCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("description", description);
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editPathPicture = MainActivity.getRetrofitRequests(context).editPathPicture(path.getId(), id, body);
            editPathPicture.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        PathPicture.this.description = description;
                        callback.onEditPictureSuccess();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("EDIT_PATH_PICTURE1", Arrays.toString(t.getStackTrace()));
                    callback.onEditPictureFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("EDIT_PATH_PICTURE2", Arrays.toString(e.getStackTrace()));
            callback.onEditPictureFailure();
        }
    }

    public void delete(Context context, PictureEditCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deletePathPicture = MainActivity.getRetrofitRequests(context).deletePathPicture(path.getId(), id, body);
            deletePathPicture.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        path.getPathPictures().remove(PathPicture.this);
                        callback.onDeletePictureSuccess();
                    } else {
                        callback.onDeletePictureFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("DELETE_PATH_PICTURE1", Arrays.toString(t.getStackTrace()));
                    callback.onDeletePictureFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("DELETE_PATH_PICTURE2", Arrays.toString(e.getStackTrace()));
            callback.onDeletePictureFailure();
        }
    }
}
