package com.mioty.fabianbuentello.nrinject;

/**
 * Created by fabianBuentello on 1/24/15.
 */
public class Nodes {
    private String nID;
    private String nName;

    public Nodes() {

    }

    public Nodes(String id, String name) {
        super();
        this.nID = id;
        this.nName = name;

    }

    // Getters
    public String getnName() { return nName; }

    public String getnID() { return nID; }

    // Setters
    public void setnName(String nName) { this.nName = nName; }

    public void setnID(String nID) { this.nID = nID; }
}
