package com.example.myapplication;

import android.app.Application;
import android.location.Location;




public class MySingleton extends Application {
    private static MySingleton singleton;
    private Location location;

    public MySingleton getInstance() {
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
