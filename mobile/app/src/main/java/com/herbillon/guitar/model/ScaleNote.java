package com.herbillon.guitar.model;

public class ScaleNote {
    private int id;
    private int string;
    private int fret;
    private boolean isRoot;

    public ScaleNote(int id, int s, int f, boolean r){
        this.id = id;
        this.string = s;
        this.fret = f;
        this.isRoot = r;
    }

    public int getId() {
        return id;
    }
    public int getString() {
        return string;
    }
    public int getFret() {
        return fret;
    }
    public boolean getIsRoot() {
        return isRoot;
    }
}
