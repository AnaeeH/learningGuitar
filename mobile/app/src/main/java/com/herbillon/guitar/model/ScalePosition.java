package com.herbillon.guitar.model;

import java.util.List;

public class ScalePosition {
    private int id;
    private int number;
    private int startingFret;
    private List<ScaleNote> notes;

    public ScalePosition(int id, int n, int sf){
        this.id = id;
        this.number = n;
        this.startingFret = sf;
    }

    public int getId() {
        return id;
    }
    public int getNumber() {
        return number;
    }
    public int getStartingFret() {
        return startingFret;
    }

    public List<ScaleNote> getNotes() { return notes; }
    public void setNotes(List<ScaleNote> notes) { this.notes = notes; }
}
