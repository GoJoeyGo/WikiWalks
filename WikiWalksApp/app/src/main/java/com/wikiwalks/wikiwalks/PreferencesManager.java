package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesManager {

    private static PreferencesManager instance;

    Context context;
    SharedPreferences preferences;
    SharedPreferences statistics;

    private PreferencesManager(Context context) {
        preferences = context.getSharedPreferences("preferences", MODE_PRIVATE);
        statistics = context.getSharedPreferences("statistics", MODE_PRIVATE);
        this.context = context.getApplicationContext();
    }

    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public void addDistanceWalked(float distance) {
        float oldDistance = statistics.getFloat("distance_walked", 0);
        statistics.edit().putFloat("distance_walked", distance + oldDistance).apply();
        ArrayList<JSONObject> goals = getGoals();
        try {
            for (JSONObject goal : goals) {
                if (Calendar.getInstance().getTimeInMillis() < goal.getLong("end_time")) {
                    goal.put("progress", goal.getDouble("progress") + distance);
                }
            }
            setGoals(goals);
        } catch (JSONException e) {
            Log.e("PreferencesManager", "Adding distance walked", e);
        }
    }

    public void increaseTimesWalked() {
        int timesWalked = statistics.getInt("times_walked", 0);
        statistics.edit().putInt("times_walked", timesWalked + 1).apply();
    }

    public void changeRoutesRecorded(boolean deleted) {
        int routesRecorded = statistics.getInt("routes_recorded", 0);
        if (deleted) {
            routesRecorded--;
        } else {
            routesRecorded++;
        }
        statistics.edit().putInt("routes_recorded", routesRecorded).apply();
    }

    public void changePointsOfInterestMarked(boolean deleted) {
        int pointsMarked = statistics.getInt("points_marked", 0);
        if (deleted) {
            pointsMarked--;
        } else {
            pointsMarked++;
        }
        statistics.edit().putInt("points_marked", pointsMarked).apply();
    }

    public void changeReviewsWritten(boolean deleted) {
        int reviewsWritten = statistics.getInt("reviews_written", 0);
        if (deleted) {
            reviewsWritten--;
        } else {
            reviewsWritten++;
        }
        statistics.edit().putInt("reviews_written", reviewsWritten).apply();
    }

    public void changePicturesUploaded(boolean deleted) {
        int picturesUploaded = statistics.getInt("pictures_uploaded", 0);
        if (deleted) {
            picturesUploaded--;
        } else {
            picturesUploaded++;
        }
        statistics.edit().putInt("pictures_uploaded", picturesUploaded).apply();
    }

    public String getBookmarks() {
        return preferences.getString("bookmarks", "");
    }

    public boolean isBookmarked(int pathId) {
        String bookmarks = preferences.getString("bookmarks", "");
        ArrayList<String> bookmarksList = (bookmarks.equals("")) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(bookmarks.split(",")));
        return bookmarksList.contains(String.valueOf(pathId));
    }

    public boolean toggleBookmark(int pathId) {
        String bookmarks = preferences.getString("bookmarks", "");
        ArrayList<String> bookmarksList = (bookmarks.equals("")) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(bookmarks.split(",")));
        if (bookmarksList.contains(String.valueOf(pathId))) {
            bookmarksList.remove(String.valueOf(pathId));
            preferences.edit().putString("bookmarks", TextUtils.join(",", bookmarksList)).apply();
            return false;
        } else {
            bookmarksList.add(String.valueOf(pathId));
            preferences.edit().putString("bookmarks", TextUtils.join(",", bookmarksList)).apply();
            return true;
        }
    }

    public String getName() {
        return preferences.getString("name", "Anonymous");
    }

    public void setName(String name) {
        preferences.edit().putString("name", (name.isEmpty()) ? "Anonymous" : name).apply();
    }

    public String getDeviceId() {
        if (!preferences.contains("device_id")) {
            preferences.edit().putString("device_id", UUID.randomUUID().toString()).apply();
        }
        return preferences.getString("device_id", null);
    }

    public String[] getStatistics() {
        String[] strings = new String[6];
        String country = Locale.getDefault().getCountry();
        if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
            strings[0] = String.format(context.getString(R.string.distance_walked), statistics.getFloat("distance_walked", 0) * 0.000621371, context.getString(R.string.miles));
        } else {
            strings[0] = String.format(context.getString(R.string.distance_walked), statistics.getFloat("distance_walked", 0) / 1000, context.getString(R.string.kilometres));
        }
        strings[1] = String.format(context.getString(R.string.times_walked), statistics.getInt("times_walked", 0));
        strings[2] = String.format(context.getString(R.string.routes_recorded), statistics.getInt("routes_recorded", 0));
        strings[3] = String.format(context.getString(R.string.points_marked), statistics.getInt("points_marked", 0));
        strings[4] = String.format(context.getString(R.string.reviews_written), statistics.getInt("reviews_written", 0));
        strings[5] = String.format(context.getString(R.string.pictures_uploaded), statistics.getInt("pictures_uploaded", 0));
        return strings;
    }

    public ArrayList<JSONObject> getGoals() {
        ArrayList<JSONObject> goalsList = new ArrayList<>();
        try {
            JSONArray goalsJsonArray = new JSONArray(preferences.getString("goals", "[]"));
            for (int i = 0; i < goalsJsonArray.length(); i++) {
                goalsList.add(goalsJsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e("PreferencesManager", "Getting goals", e);
        }
        return goalsList;
    }

    public void setGoals(ArrayList<JSONObject> goals) {
        JSONArray goalsJsonArray = new JSONArray();
        for (JSONObject goal : goals) {
            goalsJsonArray.put(goal);
        }
        preferences.edit().putString("goals", goalsJsonArray.toString()).apply();
    }

    public void addGoal(long startTime, long endTime, double distanceGoal) {
        try {
            JSONObject newGoal = new JSONObject();
            newGoal.put("start_time", startTime);
            newGoal.put("end_time", endTime);
            newGoal.put("distance_goal", distanceGoal);
            newGoal.put("progress", 0.0);
            ArrayList<JSONObject> goals = getGoals();
            goals.add(0, newGoal);
            setGoals(goals);
        } catch (JSONException e) {
            Log.e("PreferencesManager", "Adding goal", e);
        }
    }

    public void editGoal(int position, long endTime, double distanceGoal) {
        try {
            ArrayList<JSONObject> goals = getGoals();
            JSONObject goal = goals.get(position);
            goal.put("end_time", endTime);
            goal.put("distance_goal", distanceGoal);
            setGoals(goals);
        } catch (JSONException e) {
            Log.e("PreferencesManager", "Editing goal", e);
        }
    }

    public void removeGoal(int position) {
        ArrayList<JSONObject> goals = getGoals();
        goals.remove(position);
        setGoals(goals);
    }

    public void exportPreferences(Uri location) {
        JSONObject preferencesJson = new JSONObject();
        JSONObject statisticsJson = new JSONObject();
        try {
            for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
                preferencesJson.put(entry.getKey(), entry.getValue().toString());
            }
            for (Map.Entry<String, ?> entry : statistics.getAll().entrySet()) {
                statisticsJson.put(entry.getKey(), entry.getValue());
            }
            preferencesJson.put("statistics", statisticsJson);
            OutputStream outputStream = context.getContentResolver().openOutputStream(location);
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(preferencesJson.toString());
            writer.close();
        } catch (JSONException | IOException e) {
            Log.e("PreferencesManager", "Exporting preferences", e);
        }
    }

    public void importPreferences(Uri location) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(location);
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getKey().matches("statistics")) {
                    JsonObject importedStatistics = jsonObject.get("statistics").getAsJsonObject();
                    for (Map.Entry<String, JsonElement> statisticsEntry : importedStatistics.entrySet()) {
                        if (statisticsEntry.getKey().matches("distance_walked")) {
                            statistics.edit().putFloat(statisticsEntry.getKey(), statisticsEntry.getValue().getAsFloat()).apply();
                        } else {
                            statistics.edit().putInt(statisticsEntry.getKey(), statisticsEntry.getValue().getAsInt()).apply();
                        }
                    }
                } else {
                    preferences.edit().putString(entry.getKey(), entry.getValue().getAsString()).apply();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("PreferencesManager", "Importing preferences", e);
        }
    }
}
