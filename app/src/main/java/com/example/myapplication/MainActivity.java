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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.service.ConnectThread;
import com.example.myapplication.service.ConnectedThread;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // Global variables we will use in the
    private static final String TAG = "Main";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection statys
    public static Handler handler;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable

    @SuppressLint({"HandlerLeak", "CheckResult"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //Intances of the Android UI elements that will will use during the execution of the APP

        Button connectToDevice = findViewById(R.id.connectToDevice);
        Log.d(TAG, "Begin Execution");


        //Using a handler to update the interface in case of an error connecting to the BT device
        //My idea is to show handler vs RxAndroid
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String bluetoothMsg = msg.obj.toString(); // Read message from Arduino
                        Toast.makeText(MainActivity.this, bluetoothMsg, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        //Check if the phone supports BT
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(TAG, "Device doesn't support Bluetooth");
        } else {
            Log.d(TAG, "Device support Bluetooth");
            //Check BT enabled. If disabled, we ask the user to enable BT
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is disabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.d(TAG, "We don't BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                } else {
                    Log.d(TAG, "We have BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                }

            } else {
                Log.d(TAG, "Bluetooth is enabled");
            }
            String btDevicesString = "";
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    Log.d(TAG, deviceName);
                    //If we find the HC 05 device
                    //We assign the device value to the Global variable BluetoothDevice
                    //We enable the button "Connect to HC 05 device"
                    if (deviceName.equals("HC-06")) {
                        Log.d(TAG, "HC-06 found");
                        arduinoUUID = device.getUuids()[0].getUuid();
                        arduinoBTModule = device;
                        //HC -05 Found, enabling the button to read results
                        connectToDevice.setEnabled(true);
                    }
                    Log.d(TAG, "HC-06 not found");
                }
            }
        }
        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            //Call the constructor of the ConnectThread class
            //Passing the Arguments: an Object that represents the BT device,
            // the UUID and then the handler to update the UI
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.start();
            connectThread.join();
            //Check if Socket connected
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.start();
                connectedThread.join();
                if (connectedThread.getValueRead() != null) {
                    // If we have read a value from the Arduino
                    // we call the onNext() function
                    //This value will be observed by the observer
                    emitter.onNext(connectedThread.getValueRead());
                }
                //We just want to stream 1 value, so we close the BT stream
                connectedThread.cancel();
            } else {
                connectThread.getMmSocket().connect();
                Log.d(TAG, "Calling ConnectedThread class");
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.start();
                connectedThread.join();
                if (connectedThread.getValueRead() != null) {
                    // If we have read a value from the Arduino
                    // we call the onNext() function
                    //This value will be observed by the observer
                    emitter.onNext(connectedThread.getValueRead());
                }
                //We just want to stream 1 value, so we close the BT stream
                connectedThread.cancel();
            }
            //  SystemClock.sleep(5000); // simulate delay
            //Then we close the socket connection

            //We could Override the onComplete function
            emitter.onComplete();

        });

        connectToDevice.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AnemometerActivity.class);

            if (arduinoBTModule != null) {
                Log.d(TAG, "module not null");
                //We subscribe to the observable until the onComplete() is called
                //We also define control the thread management with
                // subscribeOn:  the thread in which you want to execute the action
                // observeOn: the thread in which you want to get the response
                connectToBTObservable.
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribeOn(Schedulers.io()).
                        subscribe(valueRead -> {
                            //valueRead returned by the onNext() from the Observable
                            //btReadings.setText(valueRead);
                            intent.putExtra("valueRead", valueRead);
                            intent.putExtra("arduinoBTModule", arduinoBTModule);
                            intent.putExtra("uuid", arduinoUUID.toString());
                            startActivity(intent);
                            //We just scratched the surface with RxAndroid
                        });

            } else {
                Log.d(TAG, "module null");
            }
        });
    }

}