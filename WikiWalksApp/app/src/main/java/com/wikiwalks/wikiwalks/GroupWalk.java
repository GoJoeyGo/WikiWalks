package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupWalk extends DialogFragment {

    private int id;
    private String title;
    private long time;
    private ArrayList<String> attendees = new ArrayList<>();
    private String hostName;
    private boolean attending;
    private boolean editable;
    private Path path;

    public interface GetGroupWalksCallback{
        void onGetGroupWalksSuccess();
        void onGetGroupWalksFailure();
    }

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

    public GroupWalk(JsonObject attributes, Path path) {
        id = attributes.get("id").getAsInt();
        title = attributes.get("title").getAsString();
        time = attributes.get("time").getAsLong();
        JsonArray attendees = attributes.get("attendees").getAsJsonArray();
        ArrayList<String> attendeesList = new ArrayList<>();
        for (int i = 0; i < attendees.size(); i++) {
            attendeesList.add(attendees.get(i).getAsJsonObject().get("nickname").getAsString());
        }
        hostName = attributes.get("submitter").getAsString();
        attending = attributes.get("attending").getAsBoolean();
        editable = attributes.get("editable").getAsBoolean();
        this.path = path;
    }

    public static void submit(Context context, Path path, long time, String title, EditGroupWalkCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        request.addProperty("path_id", path.getId());
        request.addProperty("time", time);
        request.addProperty("title", title);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newGroupWalk = MainActivity.getRetrofitRequests(context).addGroupWalk(path.getId(), body);
        newGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body().getAsJsonObject().get("group_walk").getAsJsonObject();
                    GroupWalk newGroupWalk = new GroupWalk(responseJson, path);
                    path.getGroupWalksList().add(newGroupWalk);
                    callback.onEditSuccess();
                } else {
                    callback.onEditFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("GroupWalk", "Sending new group walk request", t);
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
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
                Log.e("GroupWalk", "Sending toggle group walk attendance request", t);
                callback.toggleAttendanceFailure();
            }
        });
    }

    public void edit(Context context, long time, String title, EditGroupWalkCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("title", title);
        request.addProperty("time", time);
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
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
                Log.e("GroupWalk", "Sending edit group walk request", t);
                callback.onEditFailure();
            }
        });
    }

    public void delete(Context context, EditGroupWalkCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("device_id", PreferencesManager.getInstance(context).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> deleteGroupWalk = MainActivity.getRetrofitRequests(context).deleteGroupWalk(path.getId(), id, body);
        deleteGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    path.getGroupWalksList().remove(GroupWalk.this);
                    callback.onDeleteSuccess();
                } else {
                    callback.onDeleteFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("GroupWalk", "Creating delete group walk request", t);
                callback.onDeleteFailure();
            }
        });
    }
}
