package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.service.ConnectThread;
import com.example.myapplication.service.ConnectedThread;

import java.io.IOException;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AnemometerActivity extends AppCompatActivity {
    BluetoothDevice arduinoBTModule;
    ImageView imageView;
    String[] values;
    TextView btReadings, btReadings2;
    BluetoothSocket bluetoothSocket;
    public static Handler handler;
    Button gpsAct;
    private static final String TAG = "AnemometerActivity";
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    private final static int MSG_READ = 1;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anemometer);
        imageView = findViewById(R.id.imageView2);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR_READ: {
                        break;
                    }
                    case MSG_READ:
                        String arduinoMsg = msg.obj.toString();
                        values = arduinoMsg.split(",");
                        imageView.setRotation((float) Math.abs(Integer.parseInt(values[0])));
                        btReadings.setText("  " + values[1]);// Read message from Arduino
                        btReadings2.setText("  " + values[0]);
                        break;
                }
            }
        };
        if (getIntent().getExtras() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arduinoBTModule = getIntent().getExtras().getParcelable("arduinoBTModule", BluetoothDevice.class);
                arduinoUUID = getIntent().getExtras().getParcelable("arduinoUUID", UUID.class);
            } else {
                arduinoBTModule = getIntent().getParcelableExtra("arduinoBTModule");
                arduinoUUID = UUID.fromString(getIntent().getStringExtra("uuid"));
            }
        }

        btReadings2 = findViewById(R.id.btReadings2);
        btReadings = findViewById(R.id.btReadings);
        gpsAct = findViewById(R.id.gpsAct);
        gpsAct.setOnClickListener(v -> {
            Intent intent = new Intent(AnemometerActivity.this, GpsActivity.class);
            startActivity(intent);
        });


        Button connectToDevice = findViewById(R.id.receiveData);
        new ConnectToBt().execute();


    }

    private class ConnectToBt extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "Calling connectThread class");
            //Call the constructor of the ConnectThread class
            //Passing the Arguments: an Object that represents the BT device,
            // the UUID and then the handler to update the UI
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.start();
            try {
                connectThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                bluetoothSocket = connectThread.getMmSocket();
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                ConnectedThread connectedThread = new ConnectedThread(bluetoothSocket, handler);
                connectedThread.start();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finish();
        super.onBackPressed();
    }
}
    