package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointOfInterestPicture {
    private int id;
    private PointOfInterest pointOfInterest;
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


    public PointOfInterestPicture(int id, PointOfInterest pointOfInterest, String url, int width, int height, String description, String submitter, boolean editable) {
        this.id = id;
        this.pointOfInterest = pointOfInterest;
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

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
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

    public static void upload(Context context, String filename, Uri uri, String description, PointOfInterest pointOfInterest, PictureUploadCallback callback) {
        File file = new File(filename);
        RequestBody imageBody = RequestBody.create(MediaType.parse(context.getContentResolver().getType(uri)), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
        RequestBody deviceIdBody = RequestBody.create(MultipartBody.FORM, MainActivity.getDeviceId(context));
        RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
        Call<JsonElement> newPoIPicture = MainActivity.getRetrofitRequests(context).newPoIPicture(pointOfInterest.getId(), imagePart, deviceIdBody, descriptionBody);
        newPoIPicture.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body().getAsJsonObject().toString()).getJSONObject("poi_picture");
                        pointOfInterest.getPointOfInterestPictures().add(0, new PointOfInterestPicture(responseJson.getInt("id"), pointOfInterest, responseJson.getString("url"), responseJson.getInt("width"), responseJson.getInt("height"), responseJson.getString("description"), responseJson.getString("submitter"), true));
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
}
