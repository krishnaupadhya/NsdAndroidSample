package com.example.krishnaupadhya.nsddemo.nsdutils

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import com.example.krishnaupadhya.nsddemo.utils.AppUtility
import java.io.IOException
import java.net.ServerSocket

/**
 * Created by Krishna.Upadhya on 02-03-2018.
 */
class NsdRegistrationHelper(private var mContext: Context, private var nsdRegistrationListener: NsdRegistrationListener) {

    internal var isServiceRunning: Boolean = false
        get() = field

    private var mServerDeviceName = AppConstants.SERVERS_DEVICE
    var mNsdRegistrationListener: NsdRegistrationListener? = null
    private lateinit var mNsdManager: NsdManager
    private lateinit var mRegistrationListener: NsdManager.RegistrationListener
    var mServiceName: String? = AppConstants.SERVICE_TYPE
    private var serviceInfo: NsdServiceInfo? = null
    private var serverSocket: ServerSocket? = null
    private var mLocalPort: Int = 0


    fun initializeNsdServer() {
        mServerDeviceName = AppUtility.getLocalBluetoothName()
        mNsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        mNsdRegistrationListener = nsdRegistrationListener
        initializeServerPort()
        initializeRegistrationListener()
        initServiceInfo()
        registerService()

    }

    private fun initializeRegistrationListener() {
        mRegistrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                mServiceName = nsdServiceInfo.serviceName
                Log.d(TAG, "onServiceRegistered - $mServiceName")
                mNsdRegistrationListener?.onNsdServiceRegistered(nsdServiceInfo)
            }

            override fun onRegistrationFailed(nsdServiceInfo: NsdServiceInfo, errorCode: Int) {
                Log.d(TAG, "onRegistrationFailed - $mServiceName errorCode - $errorCode")
                mNsdRegistrationListener?.onNsdServiceRegistrationFailed(nsdServiceInfo, errorCode)
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {}

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}

        }
    }

    private fun initServiceInfo() {
        serviceInfo = NsdServiceInfo()
        serviceInfo?.port = mLocalPort
        serviceInfo?.serviceName = mServerDeviceName
        serviceInfo?.serviceType = AppConstants.SERVICE_TYPE
    }

    private fun registerService() {
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
        isServiceRunning = true
    }

    fun resumeService() {
        initializeRegistrationListener()
        registerService()
    }

    private fun initializeServerPort() {
        try {
            serverSocket = ServerSocket(0)
            mLocalPort = serverSocket!!.localPort
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun tearDown() {
        mNsdManager.unregisterService(mRegistrationListener)
        isServiceRunning = false
    }

    companion object {

        val TAG = "NsdRegistrationHelper"
    }
}