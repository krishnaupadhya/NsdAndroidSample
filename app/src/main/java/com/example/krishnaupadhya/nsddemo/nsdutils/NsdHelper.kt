/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.krishnaupadhya.nsddemo.nsdutils

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import java.io.IOException
import java.net.ServerSocket


class NsdHelper(private var mContext: Context, private var nsdDiscoveryListener: NsdDiscoveryListener) {
    var mNsdDiscoveryListener: NsdDiscoveryListener? = null
    internal var mNsdManager: NsdManager
    internal lateinit var mResolveListener: NsdManager.ResolveListener
    internal lateinit var mDiscoveryListener: NsdManager.DiscoveryListener
    internal lateinit var mRegistrationListener: NsdManager.RegistrationListener
    var mServiceName: String? = AppConstants.SERVICE_TYPE

    var chosenServiceInfo: NsdServiceInfo? = null
        get() = field
    private var serverSocket: ServerSocket? = null
    private var mLocalPort: Int = 0

    init {
        mNsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        mNsdDiscoveryListener = nsdDiscoveryListener
    }

    fun initializeNsdServer() {
        initializeServerPort()
        initializeRegistrationListener()
    }

    fun initializeNsdClient() {
        initializeDiscoveryListener()
        initializeResolveListener()
    }

    private fun initializeDiscoveryListener() {
        mDiscoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service discovery success" + service)
                mNsdDiscoveryListener?.onNsdServiceDiscovered(chosenServiceInfo)
                if (service.serviceType != SERVICE_TYPE) {
                    Log.d(TAG, "Unknown Service Type: " + service.serviceType)
                } else if (service.serviceName.contains(AppConstants.SERVICE_TYPE)) {
                    mNsdManager.resolveService(service, mResolveListener)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "service lost" + service)
                if (chosenServiceInfo == service) {
                    chosenServiceInfo = null
                }
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

    private fun initializeResolveListener() {
        mResolveListener = object : NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed" + errorCode)
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo)

                if (serviceInfo.serviceName.equals(mServiceName)) {
                    Log.d(TAG, "Same IP.")
                    chosenServiceInfo = serviceInfo
                    val port = serviceInfo.getPort()
                    val host = serviceInfo.getHost()
                    return
                }


            }
        }
    }

    private fun initializeRegistrationListener() {
        mRegistrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                mServiceName = nsdServiceInfo.serviceName
                Log.d(TAG, "mServiceName " + mServiceName)
           //     mNsdDiscoveryListener?.onNsdServiceRegistered(nsdServiceInfo)

            }

            override fun onRegistrationFailed(nsdServiceInfo: NsdServiceInfo, arg1: Int) {
             //   mNsdDiscoveryListener?.onNsdServiceRegistrationFailed(nsdServiceInfo)
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {}

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}

        }
    }

    fun registerService() {
        val serviceInfo = NsdServiceInfo()
        serviceInfo.port = mLocalPort
        serviceInfo.serviceName = mServiceName
        serviceInfo.serviceType = SERVICE_TYPE

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
    }

    private fun initializeServerPort() {
        try {
            serverSocket = ServerSocket(0)
            mLocalPort = serverSocket!!.localPort
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
    }

    fun stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener)
    }

    fun tearDown() {
        mNsdManager.unregisterService(mRegistrationListener)
    }

    companion object {

        val SERVICE_TYPE = "_inresto._tcp."

        val TAG = "NsdHelper"
    }
}
