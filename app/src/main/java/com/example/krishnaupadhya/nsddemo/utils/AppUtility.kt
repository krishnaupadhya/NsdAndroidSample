package com.example.krishnaupadhya.nsddemo.utils

import android.bluetooth.BluetoothAdapter
import android.text.TextUtils

/**
 * Created by Krishna.Upadhya on 01-03-2018.
 */
class AppUtility {
    companion object {
        @JvmStatic
        fun getLocalBluetoothName(): String {
            var deviceName = AppConstants.CUSTOMERS_DEVICE
            var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            mBluetoothAdapter?.let {
                deviceName = mBluetoothAdapter.name
                if (TextUtils.isEmpty(deviceName)) {
                    deviceName = mBluetoothAdapter.address
                }
            }
            return deviceName
        }
    }
}