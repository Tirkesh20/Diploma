package com.example.myapplication.service;

import android.location.Location;

public interface GpsService {

    void updateUIValues(Location location);

    void updateGPS();

     void startLocationUpdates();

     void stopLocationUpdates();
}
