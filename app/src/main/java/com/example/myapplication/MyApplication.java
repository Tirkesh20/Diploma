package com.example.myapplication;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {
    private static MyApplication singleton;
    private Location location;

    public MyApplication getInstance() {
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
        singleton = this;
        ;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
