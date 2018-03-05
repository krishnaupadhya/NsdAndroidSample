package com.example.krishnaupadhya.nsddemo.nsdutils

import android.net.nsd.NsdServiceInfo

/**
 * Created by Krishna.Upadhya on 02-03-2018.
 */
interface NsdRegistrationListener {

    fun onNsdServiceRegistered(service: NsdServiceInfo)
    fun onNsdServiceRegistrationFailed(service: NsdServiceInfo?, errorCode: Int)
    fun onNsdServiceUnregistered(service: NsdServiceInfo?)
}