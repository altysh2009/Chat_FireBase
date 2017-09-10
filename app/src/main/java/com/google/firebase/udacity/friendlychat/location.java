package com.google.firebase.udacity.friendlychat;

/**
 * Created by Home- on 09/09/2017.
 */

public class location {
    private int location;
    private String path;

    public location(int postion, String path) {
        location = postion;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

}
