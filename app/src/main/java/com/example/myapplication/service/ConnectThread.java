package com.example.myapplication.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import android.os.Handler;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private static final String TAG = "Thread-connect";
    public static Handler handler;
    private final static int ERROR_READ = 0;

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        ConnectThread.handler = handler;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            if (!mmSocket.isConnected()) {
                mmSocket.connect();
            }
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            handler.obtainMessage(ERROR_READ, connectException.toString()).sendToTarget();
            Log.e(TAG, "connectException: " + connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }
}
