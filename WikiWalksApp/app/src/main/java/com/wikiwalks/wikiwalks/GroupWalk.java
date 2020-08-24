package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupWalk extends DialogFragment {

    int id;
    String title;
    long time;
    ArrayList<String> attendees;
    String hostName;
    boolean attending;
    boolean editable;
    Path path;

    public interface EditGroupWalkCallback {
        void onEditSuccess();
        void onEditFailure();
        void onDeleteSuccess();
        void onDeleteFailure();
    }

    public interface AttendGroupWalkCallback {
        void toggleAttendanceSuccess();
        void toggleAttendanceFailure();
    }

    public GroupWalk(Path path, int id, String title, long time, ArrayList<String> attendees, String hostName, boolean attending, boolean editable) {
        this.path = path;
        this.id = id;
        this.title = title;
        this.time = time;
        this.attendees = attendees;
        this.hostName = hostName;
        this.attending = attending;
        this.editable = editable;
    }

    public static void submit(Context context, Path path, long time, String title, EditGroupWalkCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            attributes.put("path_id", path.getId());
            attributes.put("time", time);
            attributes.put("title", title);
            request.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newGroupWalk = MainActivity.getRetrofitRequests(context).addGroupWalk(path.getId(), body);
        newGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body().getAsJsonObject().get("group_walk").getAsJsonObject();
                    GroupWalk newGroupWalk = new GroupWalk(path, responseJson.get("id").getAsInt(), title, time, new ArrayList<>(), responseJson.get("submitter").getAsString(), false, true);
                    path.getGroupWalks().add(newGroupWalk);
                    callback.onEditSuccess();
                } else {
                    callback.onEditFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onEditFailure();
            }
        });
    }

    public int getGroupWalkId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public ArrayList<String> getAttendees() {
        return attendees;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isAttending() {
        return attending;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getTitle() {
        return title;
    }

    public void toggleAttendance(Context context, AttendGroupWalkCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> attendGroupWalk = MainActivity.getRetrofitRequests(context).attendGroupWalk(path.getId(), this.id, body);
        attendGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject groupWalk = response.body().getAsJsonObject().get("group_walk").getAsJsonObject();
                    attending = groupWalk.get("attending").getAsBoolean();
                    attendees.clear();
                    JsonArray newAttendees = groupWalk.get("attendees").getAsJsonArray();
                    for (int i = 0; i < newAttendees.size(); i++) {
                        attendees.add(newAttendees.get(i).getAsJsonObject().get("nickname").getAsString());
                    }
                    callback.toggleAttendanceSuccess();
                } else {
                    callback.toggleAttendanceFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.toggleAttendanceFailure();
            }
        });
    }

    public void edit(Context context, long time, String title, EditGroupWalkCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("title", title);
            attributes.put("time", time);
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> editGroupWalk = MainActivity.getRetrofitRequests(context).editGroupWalk(path.getId(), id, body);
            editGroupWalk.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        GroupWalk.this.title = title;
                        GroupWalk.this.time = time;
                        callback.onEditSuccess();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("SUBMIT_PATH1", Arrays.toString(t.getStackTrace()));
                    callback.onEditFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("SUBMIT_PATH2", Arrays.toString(e.getStackTrace()));
            callback.onEditFailure();
        }
    }

    public void delete(Context context, EditGroupWalkCallback callback) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(context).getDeviceId());
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> deleteGroupWalk = MainActivity.getRetrofitRequests(context).deleteGroupWalk(path.getId(), id, body);
            deleteGroupWalk.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        path.getGroupWalks().remove(GroupWalk.this);
                        callback.onDeleteSuccess();
                    } else {
                        callback.onDeleteFailure();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("DELETE_PATH_PICTURE1", Arrays.toString(t.getStackTrace()));
                    callback.onDeleteFailure();
                }
            });
        } catch (JSONException e) {
            Log.e("DELETE_PATH_PICTURE2", Arrays.toString(e.getStackTrace()));
            callback.onDeleteFailure();
        }
    }
}
