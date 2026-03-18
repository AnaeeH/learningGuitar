package com.herbillon.guitar.model;

public class Music {
    private int id;
    private String title;
    private boolean favorite;
    private String timeSignature;
    private String comment;

    public Music(int id, String n, boolean f){
        this.id = id;
        this.title = n;
        this.favorite = f;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public boolean getFavorite() {
        return favorite;
    }
    public String getTimeSignature() {
        return timeSignature;
    }
    public String getComment() {
        return comment;
    }

    public void setFavorite(boolean f) { this.favorite = f; }
    public void setTimeSignature(String t) { this.timeSignature = t; }
    public void setComment(String c) { this.comment = c; }
}
