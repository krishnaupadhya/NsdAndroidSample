package com.example.krishnaupadhya.nsddemo.nsdutils

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import com.example.krishnaupadhya.nsddemo.utils.AppUtility

/**
 * Created by Krishna.Upadhya on 02-03-2018.
 */

class NsdDiscoverHelper(private var mContext: Context, private var nsdDiscoveryListener: NsdDiscoveryListener) {
    var mNsdDiscoveryListener: NsdDiscoveryListener? = null
    internal var mNsdManager: NsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
    private lateinit var mDiscoveryListener: NsdManager.DiscoveryListener

    private lateinit var mClientDeviceName: String

    init {
        mNsdDiscoveryListener = nsdDiscoveryListener
    }


    fun initializeNsdClient() {
        mClientDeviceName = AppUtility.getLocalBluetoothName()
        initializeDiscoveryListener()
        discoverServices()
    }

    private fun initializeDiscoveryListener() {
        mDiscoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service discovery success" + service)
                if (service.serviceType != AppConstants.SERVICE_TYPE) {
                    Log.d(TAG, "Unknown Service Type: " + service.serviceType)
                } else if (service.getServiceName().equals(mClientDeviceName)) {
                    Log.d(TAG, "Same IP.");
                } else {
                    mNsdManager.resolveService(service, createResolveListener())
                }

            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "service lost" + service)

                mNsdDiscoveryListener?.onNsdServiceLost(service)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: " + serviceType)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode)
                mNsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode)
                mNsdManager.stopServiceDiscovery(this)
            }
        }
    }

    private fun createResolveListener(): NsdManager.ResolveListener {
        return object : NsdManager.ResolveListener {
            public override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed " + errorCode)
                Log.e(TAG, "serivce = " + serviceInfo)
            }

            public override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo)
                mNsdDiscoveryListener?.onNsdServiceDiscovered(serviceInfo)

            }
        }
    }


    fun discoverServices() {
        mNsdManager.discoverServices(
                AppConstants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
    }

    fun stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener)
    }

    companion object {

        val TAG = "NsdDiscoverHelper"
    }
}
