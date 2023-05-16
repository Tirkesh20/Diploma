package com.example.myapplication;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private static final String HC_06 = "HC-06";
    private static final int REQUEST_ENABLE_BT = 1;
    Button about, connectToDevice;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @SuppressLint({"CheckResult"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        connectToDevice = findViewById(R.id.connectToDevice);
        about = findViewById(R.id.btn_about);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "device doesnt support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                Log.d(TAG, "Bluetooth is enabled");
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    Log.d(TAG, deviceName);
                    if (deviceName.equals(HC_06)) {
                        arduinoUUID = device.getUuids()[0].getUuid();
                        arduinoBTModule = device;
                        connectToDevice.setEnabled(true);
                    }
                }
            }
        }
        about.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        connectToDevice.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AnemometerActivity.class);

            if (arduinoBTModule != null) {
                intent.putExtra("arduinoBTModule", arduinoBTModule);
                intent.putExtra("uuid", arduinoUUID.toString());
                startActivity(intent);
            } else {
                Log.d(TAG, "module null");
            }
        });
    }

}