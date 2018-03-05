package com.example.krishnaupadhya.nsddemo.ui.nsd.slave

import com.example.krishnaupadhya.nsddemo.model.Customer

/**
 * Created by Krishna.Upadhya on 27-02-2018.
 */

interface ClientListListener {
    fun onConnectClick(customer: Customer?, position: Int)
}
