package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.SimpleMultiPartRequest;

import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.wikiwalks.wikiwalks.MainActivity.getFileName;

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

    public static void upload(Context context, Uri uri, String description, Path path, PictureUploadCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url =  context.getString(R.string.local_url) + String.format("/paths/%d/pictures/new", path.getId());
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(SimpleMultiPartRequest.Method.POST, url, response -> {
            try {
                JSONObject picture = new JSONObject(response).getJSONObject("path_picture");
                PathPicture pathPicture = new PathPicture(picture.getInt("id"), path, picture.getString("url"), picture.getInt("width"), picture.getInt("height"), picture.getString("description"), picture.getString("submitter"), true);
                path.getPathPictures().add(0, pathPicture);
                callback.onSubmitPictureSuccess();
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onSubmitPictureFailure();
            }
        }, error -> {
            Log.e("UPLOAD_PATH_PICTURE", Arrays.toString(error.getStackTrace()));
            callback.onSubmitPictureFailure();
        });
        request.addFile("image",  context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + getFileName(context, uri));
        request.addStringParam("device_id", MainActivity.getDeviceId(context));
        request.addStringParam("description", description);
        requestQueue.add(request);
    }
}
