package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        increaseTimesWalked();
        updateLongestWalk(distance);
        ArrayList<JsonObject> goals = getGoals();
        for (JsonObject goal : goals) {
            if (Calendar.getInstance().getTimeInMillis() < goal.get("end_time").getAsLong()) {
                goal.addProperty("progress", goal.get("progress").getAsDouble() + distance);
            }
        }
        setGoals(goals);
    }

    public void increaseTimesWalked() {
        int timesWalked = statistics.getInt("times_walked", 0);
        statistics.edit().putInt("times_walked", timesWalked + 1).apply();
    }

    public void updateLongestWalk(float distance) {
        float oldDistance = statistics.getFloat("longest_walk", 0);
        if (distance > oldDistance) {
            statistics.edit().putFloat("longest_walk", distance).apply();
        }
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

    public void changePhotosUploaded(boolean deleted) {
        int photosUploaded = statistics.getInt("photos_uploaded", 0);
        if (deleted) {
            photosUploaded--;
        } else {
            photosUploaded++;
        }
        statistics.edit().putInt("photos_uploaded", photosUploaded).apply();
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
        String[] strings = new String[9];
        String country = Locale.getDefault().getCountry();
        float distanceWalked = statistics.getFloat("distance_walked", 0);
        int timesWalked = statistics.getInt("times_walked", 0);
        if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
            strings[0] = String.format(context.getString(R.string.distance_walked), distanceWalked * 0.000621371, context.getString(R.string.miles));
            strings[2] = String.format(context.getString(R.string.longest_walk), statistics.getFloat("longest_walk", 0) * 0.000621371, context.getString(R.string.miles));
            strings[3] = String.format(context.getString(R.string.average_walk), (distanceWalked / timesWalked) * 0.000621371, context.getString(R.string.miles));
        } else {
            strings[0] = String.format(context.getString(R.string.distance_walked), distanceWalked * 0.001, context.getString(R.string.kilometres));
            strings[2] = String.format(context.getString(R.string.longest_walk), statistics.getFloat("longest_walk", 0) * 0.001, context.getString(R.string.kilometres));
            strings[3] = String.format(context.getString(R.string.average_walk), (distanceWalked / timesWalked) * 0.001, context.getString(R.string.kilometres));
        }
        if (timesWalked == 0) {
            strings[3] = context.getString(R.string.average_walk_n_a);
        }
        strings[1] = String.format(context.getString(R.string.earth_circumference_walked), statistics.getFloat("distance_walked", 0) / 400750000);
        strings[4] = String.format(context.getString(R.string.times_walked), timesWalked);
        strings[5] = String.format(context.getString(R.string.routes_recorded), statistics.getInt("routes_recorded", 0));
        strings[6] = String.format(context.getString(R.string.points_marked), statistics.getInt("points_marked", 0));
        strings[7] = String.format(context.getString(R.string.reviews_written), statistics.getInt("reviews_written", 0));
        strings[8] = String.format(context.getString(R.string.photos_uploaded), statistics.getInt("photos_uploaded", 0));
        return strings;
    }

    public ArrayList<JsonObject> getGoals() {
        ArrayList<JsonObject> goalsList = new ArrayList<>();
        JsonArray goalsJsonArray = JsonParser.parseString(preferences.getString("goals", "[]")).getAsJsonArray();
        for (int i = 0; i < goalsJsonArray.size(); i++) {
            goalsList.add(goalsJsonArray.get(i).getAsJsonObject());
        }
        return goalsList;
    }

    public void setGoals(ArrayList<JsonObject> goals) {
        JsonArray goalsJsonArray = new JsonArray();
        for (JsonObject goal : goals) {
            goalsJsonArray.add(goal);
        }
        preferences.edit().putString("goals", goalsJsonArray.toString()).apply();
    }

    public void addGoal(long startTime, long endTime, double distanceGoal) {
        JsonObject newGoal = new JsonObject();
        newGoal.addProperty("start_time", startTime);
        newGoal.addProperty("end_time", endTime);
        newGoal.addProperty("distance_goal", distanceGoal);
        newGoal.addProperty("progress", 0.0);
        ArrayList<JsonObject> goals = getGoals();
        goals.add(0, newGoal);
        setGoals(goals);
    }

    public void editGoal(int position, long endTime, double distanceGoal) {
        ArrayList<JsonObject> goals = getGoals();
        JsonObject goal = goals.get(position);
        goal.addProperty("end_time", endTime);
        goal.addProperty("distance_goal", distanceGoal);
        setGoals(goals);
    }

    public void removeGoal(int position) {
        ArrayList<JsonObject> goals = getGoals();
        goals.remove(position);
        setGoals(goals);
    }

    public void exportPreferences(Uri location) {
        JsonObject preferencesJson = new JsonObject();
        JsonObject statisticsJson = new JsonObject();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            preferencesJson.addProperty(entry.getKey(), entry.getValue().toString());
        }
        for (Map.Entry<String, ?> entry : statistics.getAll().entrySet()) {
            statisticsJson.addProperty(entry.getKey(), (Number) entry.getValue());
        }
        preferencesJson.add("statistics", statisticsJson);
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(location);
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(preferencesJson.toString());
            writer.close();
            Toast.makeText(context, R.string.export_data_success, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("PreferencesManager", "Exporting preferences", e);
            Toast.makeText(context, R.string.export_data_failure, Toast.LENGTH_SHORT).show();
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
                        if (statisticsEntry.getKey().matches("(distance_walked|longest_walk)")) {
                            statistics.edit().putFloat(statisticsEntry.getKey(), statisticsEntry.getValue().getAsFloat()).apply();
                        } else {
                            statistics.edit().putInt(statisticsEntry.getKey(), statisticsEntry.getValue().getAsInt()).apply();
                        }
                    }
                } else {
                    preferences.edit().putString(entry.getKey(), entry.getValue().getAsString()).apply();
                }
            }
            Toast.makeText(context, R.string.import_data_success, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.e("PreferencesManager", "Importing preferences", e);
            Toast.makeText(context, R.string.import_data_failure, Toast.LENGTH_SHORT).show();
        }
    }
}
