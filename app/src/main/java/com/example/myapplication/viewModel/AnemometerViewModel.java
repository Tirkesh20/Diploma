package com.example.myapplication.viewModel;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.myapplication.service.ConnectThread;
import com.example.myapplication.service.ConnectedThread;
import com.example.myapplication.service.KalmanFilterService;

import java.util.UUID;


public class AnemometerViewModel extends ViewModel {
    private final static int ERROR_READ = 0;
    private final static int MSG_READ = 1;
    private final static String TAG = "AnemometerViewModel";
    private final static int KAlMAN_SIZE = 5;
    private BluetoothDevice arduinoBTModule;
    private UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    static String[] values;
    private MutableLiveData<Float> angleValue;
    private MutableLiveData<String> speed;

    private final KalmanFilterService kalmanFilterService = new KalmanFilterService(KAlMAN_SIZE);

    public final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR_READ: {
                    break;
                }
                case MSG_READ:
                    String arduinoMsg = msg.obj.toString();
                    values = arduinoMsg.split(",");
                    if (angleValue != null & speed != null) {
                        kalmanFilterService.add(Double.parseDouble(values[0]));
                        angleValue.postValue(kalmanFilterService.getAverage());
                        speed.postValue(values[1]);//
                    }
                    break;
            }
        }
    };


    public void executeViewModel() {
        new ConnectToBt().execute();
    }

    public LiveData<Float> getAngleValue() {
        if (angleValue == null)
            angleValue = new MutableLiveData<>(0f);

        return angleValue;
    }

    public LiveData<String> getSpeedValue() {
        if (speed == null)
            speed = new MutableLiveData<>("0");
        return speed;
    }

    private class ConnectToBt extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "Calling connectThread class");
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
                ConnectedThread connectedThread = new ConnectedThread(bluetoothSocket, handler);
                connectedThread.start();
            }
            return null;
        }
    }

    public BluetoothDevice getArduinoBTModule() {
        return arduinoBTModule;
    }

    public void setArduinoBTModule(BluetoothDevice arduinoBTModule) {
        this.arduinoBTModule = arduinoBTModule;
    }

    public UUID getArduinoUUID() {
        return arduinoUUID;
    }

    public void setArduinoUUID(UUID arduinoUUID) {
        this.arduinoUUID = arduinoUUID;
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public void setAngleValue(MutableLiveData<Float> angleValue) {
        this.angleValue = angleValue;
    }

    public void setSpeed(MutableLiveData<String> speed) {
        this.speed = speed;
    }
}
