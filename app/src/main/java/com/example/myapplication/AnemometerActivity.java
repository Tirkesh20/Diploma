package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.service.ConnectThread;
import com.example.myapplication.service.ConnectedThread;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AnemometerActivity extends AppCompatActivity {
    BluetoothDevice arduinoBTModule;
    ImageView imageView;
    String[] values;
    public static Handler handler;
    private static final String TAG = "AnemometerActivity";
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anemometer);
        imageView = findViewById(R.id.imageView);
        String val = null;
        if (getIntent().getExtras() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arduinoBTModule = getIntent().getExtras().getParcelable("arduinoBTModule", BluetoothDevice.class);
                val = getIntent().getStringExtra("valueRead");
                arduinoUUID = getIntent().getExtras().getParcelable("arduinoUUID", UUID.class);
            } else {
                arduinoBTModule = getIntent().getParcelableExtra("arduinoBTModule");
                val = getIntent().getStringExtra("valueRead");
                String uuid = getIntent().getStringExtra("arduinoUUID");
                arduinoUUID = UUID.fromString(getIntent().getStringExtra("uuid"));
            }
        }
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ERROR_READ) {
                    String bluetoothMsg = msg.obj.toString(); // Read message from Arduino
                    Toast.makeText(AnemometerActivity.this, bluetoothMsg, Toast.LENGTH_SHORT).show();
                }
            }
        };

        TextView btReadings = findViewById(R.id.btReadings);
        if (val != null) {
            values = val.split(",");
            imageView.setRotation((float) (Integer.parseInt(values[0]) * 180 / 3.14159));
            btReadings.setText(values[0] + "--" + values[1]);
        }
        Button connectToDevice = findViewById(R.id.connectToDevice);
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
            }
            // SystemClock.sleep(5000); // simulate delay
            //Then we close the socket connection
            connectThread.cancel();
            //We could Override the onComplete function
            emitter.onComplete();

        });

        connectToDevice.setOnClickListener(view -> {
            btReadings.setText("");
            if (arduinoBTModule != null) {
                //We subscribe to the observable until the onComplete() is called
                //We also define control the thread management with
                // subscribeOn:  the thread in which you want to execute the action
                // observeOn: the thread in which you want to get the response
                //valueRead returned by the onNext() from the Observable
                //We just scratched the surface with RxAndroid
                connectToBTObservable.
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribeOn(Schedulers.io()).
                        subscribe(valueRead -> {
                            if (valueRead != null) {
                                values = valueRead.split(",");
                                imageView.setRotation((float) (Integer.parseInt(values[0]) * 180 / 3.14159));
                                btReadings.setText(values[0] + "--" + values[1]);
                            }
                            //We just scratched the surface with RxAndroid
                        });

            }
        });
    }


}
    