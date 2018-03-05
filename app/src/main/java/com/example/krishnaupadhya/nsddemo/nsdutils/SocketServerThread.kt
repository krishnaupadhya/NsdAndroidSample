package com.example.krishnaupadhya.nsddemo.nsdutils

import android.text.TextUtils
import android.util.Log
import com.example.krishnaupadhya.nsddemo.R
import com.example.krishnaupadhya.nsddemo.ui.nsd.master.NsdMasterActivity
import com.example.krishnaupadhya.nsddemo.ui.nsd.master.SocketConnectionListener
import com.example.krishnaupadhya.nsddemo.ui.nsd.slave.SocketServerTask
import kotlinx.android.synthetic.main.activity_nsd_master.*
import org.json.JSONException
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by Krishna.Upadhya on 05-03-2018.
 */

class SocketServerThread(private var socketConnectionListener: SocketConnectionListener, private var mSocketServerPort: Int) : Thread() {


    companion object {
        private val REQUEST_CONNECT_CLIENT = "request-connect-client"
        private val TAG = SocketServerThread::class.java.simpleName
    }

    override fun run() {

        var socket: Socket? = null
        var serverSocket: ServerSocket? = null
        var dataInputStream: DataInputStream? = null
        var dataOutputStream: DataOutputStream? = null

        try {
            Log.i(TAG, "Creating server socket")
            serverSocket = ServerSocket(mSocketServerPort)

            while (true) {
                socket = serverSocket.accept()
                dataInputStream = DataInputStream(
                        socket!!.getInputStream())
                dataOutputStream = DataOutputStream(
                        socket.getOutputStream())

                val messageFromClient: String
                val messageToClient: String
                val request: String

                //If no message sent from client, this code will block the Thread
                messageFromClient = dataInputStream.readUTF()

                val jsondata: JSONObject

                try {
                    jsondata = JSONObject(messageFromClient)
                    request = jsondata.getString("request")

                    if (request == REQUEST_CONNECT_CLIENT) {
                        val clientIPAddress = jsondata.getString("ipAddress")
                        if (jsondata.has("message")) {
                            val msg = jsondata.getString("message")
                            if (!TextUtils.isEmpty(msg)) {
                                socketConnectionListener.onMessageReceived(msg)
                            }
                        }
                        // Add client IP to a list
                        messageToClient = "Connection Accepted"

                        // Important command makes client able to send message
                        dataOutputStream.writeUTF(messageToClient)
                        // ****** Paste here Bonus 1

                        // ****** Paste here Bonus 1
                    } else {
                        // There might be other queries, but as of now nothing.
                        dataOutputStream.flush()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "Unable to get request")
                    dataOutputStream.flush()
                }

            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            if (dataInputStream != null) {
                try {
                    dataInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
