package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.myapplication.viewModel.AnemometerViewModel;

import java.io.IOException;
import java.util.UUID;


public class AnemometerActivity extends AppCompatActivity {
    ImageView imageView;
    TextView btReadings, btReadings2;
    Button gpsAct, mainMenu;
    AnemometerViewModel anemometerViewModel;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anemometer);
        mainMenu = findViewById(R.id.mainMenu);
        imageView = findViewById(R.id.imageView2);
        anemometerViewModel = new ViewModelProvider(this).get(AnemometerViewModel.class);
        if (getIntent().getExtras() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                anemometerViewModel.setArduinoBTModule(getIntent().getExtras().getParcelable("arduinoBTModule", BluetoothDevice.class));
                anemometerViewModel.setArduinoUUID(getIntent().getExtras().getParcelable("uuid", UUID.class));
            } else {
                anemometerViewModel.setArduinoBTModule(getIntent().getExtras().getParcelable("arduinoBTModule"));
                anemometerViewModel.setArduinoUUID(UUID.fromString(getIntent().getStringExtra("uuid")));
            }
        }

        btReadings2 = findViewById(R.id.btReadings2);
        btReadings = findViewById(R.id.btReadings);
        gpsAct = findViewById(R.id.gpsAct);
        gpsAct.setOnClickListener(v -> {
            Intent intent = new Intent(AnemometerActivity.this, GpsActivity.class);
            startActivity(intent);
        });

        mainMenu.setOnClickListener(v -> {
            onBackPressed();
        });
        anemometerViewModel.executeViewModel();

        anemometerViewModel.getSpeedValue().observe(this, speed -> {
            btReadings.setText(speed);
        });

        anemometerViewModel.getAngleValue().observe(this, angle -> {
            imageView.setRotation(angle);
            btReadings2.setText(String.valueOf(angle));
        });


    }

    @Override
    public void onBackPressed() {
        try {
            if (anemometerViewModel.getBluetoothSocket() != null)
                anemometerViewModel.getBluetoothSocket().close();
        } catch (IOException e) {
            Log.d("exc", e.getMessage());
        }
        super.onBackPressed();
        finish();
    }

}
