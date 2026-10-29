package com.herbillon.guitar.model;

public class Beat {
    public boolean isRest;
    public int position;
    public int duration;
    public String type;
    public boolean dot;
    public boolean tied;
    public boolean hammerOn;
    public boolean pullOff;
    public boolean slide;
    public boolean palmMute;

    // Partition
    public String pitchStep;
    public int pitchOctave;
    public int pitchAlter;

    // Tablature
    public int string;
    public int fret;

    // Chords
    public String harmonyText;
    public String strumDirection;

}
