package com.herbillon.guitar.model;

import com.herbillon.guitar.utils.MusicConstants;

public class Music {
    private int id;
    private String title;
    private String artist;
    private boolean favorite;
    private String comment;
    private String difficulty;
    private int progress;
    private boolean riff;

    private int tempo;
    private String timeSignature;

    public Music(int id, String n, String a, boolean f, String d, int p, boolean r){
        this.id = id;
        this.title = n;
        this.favorite = f;
        this.artist = a;
        this.difficulty = d;
        this.progress = p;
        this.riff = r;
    }

    public Music(int id, String n, String a, boolean f, String d, int p, int t, String timeSig, String c, boolean r){
        this.id = id;
        this.title = n;
        this.favorite = f;
        this.artist = a;
        this.difficulty = d;
        this.progress = p;
        this.riff = r;

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
    public String getArtist() {
        return artist;
    }
    public boolean getFavorite() {
        return favorite;
    }
    public String getDifficulty() {
        return difficulty;
    }
    public int getProgress() {
        return progress;
    }
    public int getTempo() {
        return tempo;
    }
    public String getTimeSignature() {
        return timeSignature;
    }
    public String getComment() {
        return comment;
    }
    public boolean getRiff() {
        return riff;
    }

}
