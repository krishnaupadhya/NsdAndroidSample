package com.example.krishnaupadhya.nsddemo.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.net.InetAddress

/**
 * Created by Krishna.Upadhya on 02-03-2018.
 */

@SuppressLint("ParcelCreator")
@Parcelize
class Customer(var clientIp: InetAddress, var clientPort: Int, var clientDeviceName: String, var isConnected: Boolean) : Parcelable
