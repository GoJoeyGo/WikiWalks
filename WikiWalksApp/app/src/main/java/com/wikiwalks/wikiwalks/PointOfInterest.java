package com.wikiwalks.wikiwalks;

public class PointOfInterest {
    private int id;
    private String name;

    private double latitude;
    private double longitude;

    private Path path;

    public PointOfInterest(int id, String name, double latitude, double longitude, Path path) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Path getPath() {
        return path;
    }
}
