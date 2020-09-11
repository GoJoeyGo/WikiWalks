package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Picture {
    private int id;
    private int parentId;
    private String url;
    private int width;
    private int height;
    private String description;
    private String submitter;
    private boolean editable;
    private PictureType type;

    public interface GetPicturesCallback {
        void onGetPicturesSuccess();
        void onGetPicturesFailure();
    }

    public interface EditPictureCallback {
        void onEditPictureSuccess();
        void onEditPictureFailure();
        void onDeletePictureSuccess();
        void onDeletePictureFailure();
    }

    public Picture(PictureType type, int id, int parentId, String url, int width, int height, String description, String submitter, boolean editable) {
        this.type = type;
        this.id = id;
        this.parentId = parentId;
        this.url = url;
        this.width = width;
        this.height = height;
        this.description = description;
        this.submitter = submitter;
        this.editable = editable;
    }

    public static void submit(Context context, PictureType type, int parentId, Uri uri, String description, EditPictureCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            RequestBody imageBody = RequestBody.create(MediaType.parse(context.getContentResolver().getType(uri)), buffer);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "new_picture.jpg", imageBody);
            RequestBody deviceIdBody = RequestBody.create(MultipartBody.FORM, PreferencesManager.getInstance(context).getDeviceId());
            RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
            Call<JsonElement> newPicture = (type == PictureType.PATH) ? MainActivity.getRetrofitRequests(context).newPathPicture(parentId, imagePart, deviceIdBody, descriptionBody) : MainActivity.getRetrofitRequests(context).newPoIPicture(parentId, imagePart, deviceIdBody, descriptionBody);
            newPicture.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("picture");
                            Picture newPicture = new Picture(type, responseJson.getInt("id"), parentId, responseJson.getString("url"), responseJson.getInt("width"), responseJson.getInt("height"), responseJson.getString("description"), responseJson.getString("submitter"), true);
                            if (type == PictureType.PATH) {
                                PathMap.getInstance().getPathList().get(parentId).getPicturesList().add(0, newPicture);
                            } else {
                                PathMap.getInstance().getPointOfInterestList().get(parentId).getPicturesList().add(0, newPicture);
                            }
                            PreferencesManager.getInstance(context).changePicturesUploaded(false);
                            callback.onEditPictureSuccess();
                        } catch (JSONException e) {
                            Log.e("Picture", "Getting photo from response", e);
                            callback.onEditPictureSuccess();
                        }
                    } else {
                        callback.onEditPictureFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Picture", "Sending new photo request", t);
                    callback.onEditPictureFailure();
                }
            });
        } catch (Exception e) {
            Log.e("Picture", "Creating new photo request", e);
            callback.onEditPictureFailure();
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

    public void edit(Context context, String description, EditPictureCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("description", description);
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editPicture = (type == PictureType.PATH) ? MainActivity.getRetrofitRequests(context).editPathPicture(parentId, id, body) : MainActivity.getRetrofitRequests(context).editPoIPicture(parentId, id, body);
            editPicture.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        Picture.this.description = description;
                        callback.onEditPictureSuccess();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Picture", "Sending edit photo request", t);
                    callback.onEditPictureFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("Picture", "Creating edit photo request", e);
            callback.onEditPictureFailure();
        }
    }

    public void delete(Context context, EditPictureCallback callback) {
        JSONObject request = new JSONObject();
        try {
            request.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deletePicture = (type == PictureType.PATH) ? MainActivity.getRetrofitRequests(context).deletePathPicture(parentId, id, body) : MainActivity.getRetrofitRequests(context).deletePoIPicture(parentId, id, body);
            deletePicture.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        if (type == PictureType.PATH) {
                            PathMap.getInstance().getPathList().get(parentId).getPicturesList().remove(Picture.this);
                        } else {
                            PathMap.getInstance().getPointOfInterestList().get(parentId).getPicturesList().remove(Picture.this);
                        }
                        PreferencesManager.getInstance(context).changePicturesUploaded(true);
                        callback.onDeletePictureSuccess();
                    } else {
                        callback.onDeletePictureFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("Picture", "Sending delete photo request", t);
                    callback.onDeletePictureFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("Picture", "Creating delete photo request", e);
            callback.onDeletePictureFailure();
        }
    }

    public enum PictureType {PATH, POINT_OF_INTEREST}
}
