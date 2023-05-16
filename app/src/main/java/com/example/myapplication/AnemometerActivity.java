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
    TextView btReadings, btReadings2, beuafort;
    Button gpsAct, mainMenu;
    AnemometerViewModel anemometerViewModel;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anemometer);
        mainMenu = findViewById(R.id.mainMenu);
        imageView = findViewById(R.id.imageView2);
        beuafort = findViewById(R.id.beuafort);
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
            btReadings.setText("  " +speed + " m/s");
            beuafort.setText(" " + getBeaufortScale(speed).getValue());

        });

        anemometerViewModel.getAngleValue().observe(this, angle -> {
            imageView.setRotation(angle);
            btReadings2.setText("  " + angle);
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

    public BeaufortScale getBeaufortScale(String windSpeed) {
        float speed = Float.parseFloat(windSpeed);
        if (speed > 0 && speed <= 0.3) {
            return BeaufortScale.LIGHT_AIR;
        }
        if (speed >= 0.4 && speed <= 0.6) {
            return BeaufortScale.LIGHT_BREEZE;
        } else if (speed >= 0.7 && speed <= 1.2) {
            return BeaufortScale.GENTLE_BREEZE;
        } else if (speed >= 1.3 && speed <= 2.0) {
            return BeaufortScale.MODERATE_BREEZE;
        } else if (speed >= 2.1 && speed <= 3.0) {
            return BeaufortScale.FRESH_BREEZE;
        } else if (speed >= 3.1 && speed <= 4.0) {
            return BeaufortScale.STRONG_BREEZE;
        } else if (speed >= 4.1 && speed <= 5.5) {
            return BeaufortScale.HIGH_WIND;
        } else if (speed >= 5.6 && speed <= 7.5) {
            return BeaufortScale.GALE;
        } else if (speed >= 7.6 && speed <= 10) {
            return BeaufortScale.STRONG_GALE;
        }
        return BeaufortScale.CALM;

    }

}
