package com.herbillon.guitar.model;

import java.util.List;

public class Scale {
    private int id;
    private String name;
    private String type;
    private String rootLabel;
    private List<ScalePosition> positions;

    public Scale(int id, String n, String t){
        this.id = id;
        this.name = n;
        this.type = t;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }

    public String getRootLabel() { return rootLabel; }
    public void setRootLabel(String rootLabel) { this.rootLabel = rootLabel; }

    public List<ScalePosition> getPositions() { return positions; }
    public void setPositions(List<ScalePosition> positions) { this.positions = positions; }
}
