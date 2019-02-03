package com.sp.gravitask;

import com.google.firebase.firestore.GeoPoint;

public class Errands {

    private String name;
    private String description;
    private String uid;
    private GeoPoint gpstart;
    private GeoPoint gpend;

    public Errands() {
    }

    public Errands(String name, String description, String uid, GeoPoint gpstart, GeoPoint gpend) {
        this.name = name;
        this.description = description;
        this.uid = uid;
        this.gpstart = gpstart;
        this.gpend = gpend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public GeoPoint getGpstart() {
        return gpstart;
    }

    public void setGpstart(GeoPoint gpstart) {
        this.gpstart = gpstart;
    }

    public GeoPoint getGpend() {
        return gpend;
    }

    public void setGpend(GeoPoint gpend) {
        this.gpend = gpend;
    }
}
