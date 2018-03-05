package com.example.krishnaupadhya.nsddemo.ui.nsd.slave;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.krishnaupadhya.nsddemo.model.Customer;
import com.example.krishnaupadhya.nsddemo.utils.AppConstants;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Krishna.Upadhya on 05-03-2018.
 */

public class SocketServerTask extends AsyncTask<JSONObject, Void, Void> {

    private String TAG = SocketServerTask.class.getSimpleName();
    private JSONObject jsonData;
    private boolean success;
    private Customer mCustomer;
    SocketClientListener socketClientListener;

    public SocketServerTask(Customer customer, SocketClientListener socketClientListener) {
        this.mCustomer = customer;
        this.socketClientListener = socketClientListener;
    }

    @Override
    protected Void doInBackground(JSONObject... params) {
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        jsonData = params[0];

        try {
            // Create a new Socket instance and connect to host
            socket = new Socket(mCustomer.getClientIp(), AppConstants.SERVER_SOCKET_PORT);

            dataOutputStream = new DataOutputStream(
                    socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            // transfer JSONObject as String to the server
            dataOutputStream.writeUTF(jsonData.toString());
            Log.i(TAG, "waiting for response from host");

            // Thread will wait till server replies
            String response = dataInputStream.readUTF();
            if (response != null && response.equals("Connection Accepted")) {
                success = true;
            } else {
                success = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        } finally {

            // close socket
            if (socket != null) {
                try {
                    Log.i(TAG, "closing the socket");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // close input stream
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // close output stream
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (success) {
            socketClientListener.onServerConnectionSuccess();
            //Toast.makeText(NsdSlaveActivity.this, "Connection Done", Toast.LENGTH_SHORT).show();
        } else {
            socketClientListener.onServerConnectionFailure();
        }
    }
}