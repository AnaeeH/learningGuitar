package com.herbillon.guitar.model;

public class Chord {
    private int id;
    private String name;
    private String label;
    private String diagram;

    public Chord(int id, String n, String l, String d){
        this.id = id;
        this.name = n;
        this.label = l;
        this.diagram = d;
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

    public String getDiagram() {
        return diagram;
    }
}
