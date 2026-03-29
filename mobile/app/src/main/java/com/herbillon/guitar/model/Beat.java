package com.herbillon.guitar.model;

public class Beat {
    public boolean isRest;
    public int position;
    public int duration;
    public String type;
    public boolean dot;

    // Partition
    public String pitchStep;
    public int pitchOctave;
    public int pitchAlter;

    // Tablature
    public int string;
    public int fret;
}
