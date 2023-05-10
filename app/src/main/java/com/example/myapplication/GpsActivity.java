package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.service.GpsService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class GpsActivity extends AppCompatActivity implements GpsService {

    public static final int DEF_INT = 1000;
    public static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_lat, tv_labellat, tv_labellon, tv_lon, tv_labelaltitude,
            tv_altitude, tv_labelaccuracy, tv_accuracy, tv_labelspeed,
            tv_speed, tv_labelsensor, tv_sensor,
            tv_labelupdates, tv_updates, tv_address, tv_lbladdress, wayPointCounts;
    View divider;
    Button btnNewPaint, btnShowWayPoints;
    Switch sw_locationsupdates, sw_gps;
    Location currentLocation;
    List<Location> savedLocations;

    LocationCallback locationCallback;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_activity);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_labellat = findViewById(R.id.tv_labellat);
        tv_labellon = findViewById(R.id.tv_labellon);
        tv_lon = findViewById(R.id.tv_lon);
        tv_labelaltitude = findViewById(R.id.tv_labelaltitude);
        tv_labelaccuracy = findViewById(R.id.tv_labelaccuracy);
        tv_lat = findViewById(R.id.tv_lat);
        tv_labelspeed = findViewById(R.id.tv_labelspeed);
        tv_speed = findViewById(R.id.tv_speed);
        tv_labelsensor = findViewById(R.id.tv_labelsensor);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_labelupdates = findViewById(R.id.tv_labelupdates);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_lbladdress = findViewById(R.id.tv_lbladdress);
        tv_altitude = findViewById(R.id.tv_altitude);
        btnNewPaint = findViewById(R.id.btn_newWayPoint);
        btnShowWayPoints = findViewById(R.id.btn_showWayPointList);
        wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
        //view
        divider = findViewById(R.id.divider);

        //switch
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        /////locationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEF_INT * 30L);
        locationRequest.setFastestInterval(DEF_INT);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Using GPS sensors");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Using Towers + WIFI");
            }
        });
        sw_locationsupdates.setOnClickListener(v -> {
            if (sw_locationsupdates.isChecked()) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(Objects.requireNonNull(locationResult.getLastLocation()));
            }
        };
        btnNewPaint.setOnClickListener(v -> {
            MyApplication myApplication = (MyApplication) getApplicationContext();
            savedLocations = myApplication.getLocations();
            savedLocations.add(currentLocation);
        });
        btnShowWayPoints.setOnClickListener(v -> {
            Intent intent = new Intent(GpsActivity.this, ShowSavedLocationsList.class);
            startActivity(intent);
        });
        updateGPS();
    }

    public void stopLocationUpdates() {
        tv_updates.setText("Location not tracked");
        tv_lat.setText("Location not tracked");
        tv_lon.setText("Location not tracked");
        tv_speed.setText("Location not tracked");
        tv_address.setText("Location not tracked");
        tv_accuracy.setText("Location not tracked");
        tv_sensor.setText("Location not tracked");
        tv_altitude.setText("Location not tracked");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    public void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(GpsActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                    currentLocation = location;
                }
            });

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * update all the text view objects with new Location
     */
    public void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText("Not available");
        }
        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));

        } else {
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(GpsActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                tv_address.setText(address.getAddressLine(0) + "," + address.getCountryName());
            }
        } catch (IOException e) {
            tv_address.setText("Couldn't get the location");
        }
        //show the number of wayPoints
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getLocations();
        wayPointCounts.setText(String.valueOf(savedLocations.size()));
    }
}