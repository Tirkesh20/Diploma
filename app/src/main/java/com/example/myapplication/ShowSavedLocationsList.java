package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class ShowSavedLocationsList extends AppCompatActivity {

    ListView lv_wayPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);
        MyApplication myApplication = (MyApplication) getApplicationContext();
        List<Location> locations = myApplication.getLocations();
        lv_wayPoints = findViewById(R.id.lv_wayPoints);
        lv_wayPoints.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1, locations));
    }
}