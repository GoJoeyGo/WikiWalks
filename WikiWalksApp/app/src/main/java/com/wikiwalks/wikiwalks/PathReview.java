package com.wikiwalks.wikiwalks;

public class PathReview {
    private int id;
    private String name;
    private int rating;
    private String message;
    private boolean editable;

    public PathReview(int id, String name, int rating, String message, boolean editable) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.message = message;
        this.editable = editable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEditable() {
        return editable;
    }
}
