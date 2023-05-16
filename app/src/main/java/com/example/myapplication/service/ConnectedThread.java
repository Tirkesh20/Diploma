package com.example.myapplication.service;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private static final String TAG = "ConnectedThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public static Handler handler;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    private final static int MSG_READ = 1;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        ConnectedThread.handler = handler;
    }

    public void run() {

        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        //We just want to get 1 data readings from the device
        while (mmSocket.isConnected()) {
            try {

                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                // If I detect a "\n" means I already read a full measurement
                if (buffer[bytes] == '\n') {
                    readMessage = new String(buffer, 0, bytes);
                    handler.obtainMessage(MSG_READ, readMessage).sendToTarget();
                    //Value to be read by the Observer streamed by the Obervable
                    valueRead = readMessage;
                    bytes = 0;

                } else {
                    bytes++;
                }

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            } finally {
                try {
                    if (!mmSocket.isConnected() &mmOutStream != null & mmInStream != null) {
                        mmSocket.close();
                        mmInStream.close();
                        mmOutStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }

    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            Log.e(TAG, "close the connect socket");
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}