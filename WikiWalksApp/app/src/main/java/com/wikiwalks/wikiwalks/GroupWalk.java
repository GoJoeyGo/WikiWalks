package com.wikiwalks.wikiwalks;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupWalk extends DialogFragment{
    public interface groupWalkCallBack {
        void groupWalkCallBackSucsess(Call<JsonElement> newGroupWalk);
        void groupWalkCallBackFailure();
    }
    public static void createGroupWalk(Context context,int pathID) {
        Calendar date;
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    date.set(Calendar.MINUTE, minute);
                    createGroupWalkSubmit(context,date.getTimeInMillis(),pathID);
                }
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createGroupWalkSubmit(Context context, long time, int pathID) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            attributes.put("path_id", pathID);
            attributes.put("time", time);
            request.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> newGroupWalk = MainActivity.getRetrofitRequests(context).add_group_walk(pathID,body);

        newGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response){
            }
            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
            }
        });
    }

    public static void joinGroupWalk(Context context,int pathID,int walKid) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", MainActivity.getDeviceId(context));
            request.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> attendGroupWalk = MainActivity.getRetrofitRequests(context).toggle_group_walk_attendance(pathID,walKid,body);
        attendGroupWalk.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response){

            }
            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
            }
        });
    }
}
