package com.wikiwalks.wikiwalks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Path {
    private boolean isNew = false;

    private int id;
    private String name;
    private int walkCount;

    private Path parentPath;
    private ArrayList<Path> childPaths = new ArrayList<>();
    private ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();

    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();

    public Path(JSONObject pathJson) throws JSONException {
        id = pathJson.getInt("id");
        name = pathJson.getString("name");
        walkCount = pathJson.getInt("walk_count");
        JSONArray json_latitudes = pathJson.getJSONArray("latitudes");
        JSONArray json_longitudes = pathJson.getJSONArray("longitudes");
        for (int i = 0; i < json_latitudes.length(); i++) {
            latitudes.add(json_latitudes.getDouble(i));
            longitudes.add(json_longitudes.getDouble(i));
        }
        JSONArray points_of_interest = pathJson.getJSONArray("points_of_interest");
        for (int i = 0; i < points_of_interest.length(); i++) {
            JSONObject pointOfInterest = points_of_interest.getJSONObject(i);
            pointsOfInterest.add(new PointOfInterest(pointOfInterest.getInt("id"), pointOfInterest.getString("name"), pointOfInterest.getDouble("latitude"), pointOfInterest.getDouble("longitude"), this));
        }
        if (!pathJson.isNull("parent_path")) {
            parentPath = PathMap.getInstance().getPathList().get(pathJson.getInt("parent_path"));
            parentPath.addChild(this);
        }
    }

    public Path(int parentId) {
        parentPath = PathMap.getInstance().getPathList().get(parentId);
        isNew = true;
    }

    public Path() {
        isNew = true;
    }

    public void addChild(Path child) {
        childPaths.add(child);
    }

    public void addPointOfInterest(PointOfInterest pointOfInterest) {
        pointsOfInterest.add(pointOfInterest);
    }

    public void edit(String name) {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWalkCount() {
        return walkCount;
    }

    public Path getParentPath() {
        return parentPath;
    }

    public ArrayList<Path> getChildPaths() {
        return childPaths;
    }

    public ArrayList<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public ArrayList<Double> getLatitudes() {
        return latitudes;
    }

    public ArrayList<Double> getLongitudes() {
        return longitudes;
    }

    public ArrayList<Double> getAltitudes() {
        return altitudes;
    }
}
