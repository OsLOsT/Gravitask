package com.sp.gravitask;

import com.google.firebase.firestore.GeoPoint;

public class Errands {

    private String name;
    private String description;
    private String uid;
    private GeoPoint gpstart;
    private GeoPoint gpend;
    private boolean taskFinished;

    public Errands() {
    }

    public Errands(String name, String description, String uid, boolean taskFinished, GeoPoint gpstart, GeoPoint gpend) {
        this.name = name;
        this.description = description;
        this.uid = uid;
        this.taskFinished = taskFinished;
        this.gpstart = gpstart;
        this.gpend = gpend;
    }

    public boolean isTaskFinished() {
        return taskFinished;
    }

    public void setTaskFinished(boolean taskFinished) {
        this.taskFinished = taskFinished;
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
