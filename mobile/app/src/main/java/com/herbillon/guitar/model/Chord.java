package com.herbillon.guitar.model;

import java.util.List;

public class Chord {
    private int id;
    private String name;
    private String label;
    private boolean isMajor;

    private Integer barreFret;
    private Integer barreFromString;
    private Integer barreToString;

    private List<Integer> mutedStrings;
    private List<ChordPosition> positions;

    public Chord(int id, String n, String l, boolean m){
        this.id = id;
        this.name = n;
        this.label = l;
        this.isMajor = m;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getLabel() {
        return label;
    }
    public boolean getIsMajor() {
        return isMajor;
    }

    public Integer getBarreFret() { return barreFret; }
    public void setBarreFret(Integer barreFret) { this.barreFret = barreFret; }
    public Integer getBarreFromString() { return barreFromString; }
    public void setBarreFromString(Integer barreFromString) { this.barreFromString = barreFromString; }
    public Integer getBarreToString() { return barreToString; }
    public void setBarreToString(Integer barreToString) { this.barreToString = barreToString; }

    public List<Integer> getMutedStrings() { return mutedStrings; }
    public void setMutedStrings(List<Integer> mutedStrings) { this.mutedStrings = mutedStrings; }

    public List<ChordPosition> getPositions() { return positions; }
    public void setPositions(List<ChordPosition> positions) { this.positions = positions; }

    public boolean hasBarre() {
        return barreFret != null && barreFret > 0;
    }
}
