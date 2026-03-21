package com.herbillon.guitar.model;

public class Music {
    private int id;
    private String title;
    private boolean favorite;
    private int tempo;
    private String timeSignature;
    private String comment;

    public Music(int id, String n, boolean f){
        this.id = id;
        this.title = n;
        this.favorite = f;
    }

    public Music(int id, String n, boolean f, int t, String timeSig, String c){
        this.id = id;
        this.title = n;
        this.favorite = f;
        this.tempo = t;
        this.timeSignature = timeSig;
        this.comment = c;
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
    public int getTempo() { return tempo; }
    public String getTimeSignature() {
        return timeSignature;
    }
    public String getComment() { return comment; }

}
