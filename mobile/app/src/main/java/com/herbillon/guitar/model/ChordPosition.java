package com.herbillon.guitar.model;

public class ChordPosition {
    private int string;
    private int fret;

    public ChordPosition(int string, int fret){
        this.string = string;
        this.fret = fret;
    }

    public int getString() {return string;}
    public int getFret() {return fret;}
}
