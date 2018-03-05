package com.example.krishnaupadhya.nsddemo.ui.nsd.slave

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.widget.Button

import com.example.krishnaupadhya.nsddemo.R
import com.example.krishnaupadhya.nsddemo.model.Customer
import com.example.krishnaupadhya.nsddemo.nsdutils.NsdDiscoverHelper
import com.example.krishnaupadhya.nsddemo.nsdutils.NsdDiscoveryListener
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import com.example.krishnaupadhya.nsddemo.utils.AppUtility
import kotlinx.android.synthetic.main.activity_nsd_slave.*
import org.json.JSONException
import org.json.JSONObject

import java.net.InetAddress
import java.util.ArrayList

class NsdSlaveActivity : AppCompatActivity(), NsdDiscoveryListener, ClientListListener, SocketClientListener {

    private var mClientDeviceName = AppConstants.CUSTOMERS_DEVICE

    private var hostAddress: InetAddress? = null
    private var hostPort: Int = 0
    private var isDiscovered = false
    private var customerList: ArrayList<Customer>? = null

    private var mNsdDiscoverHelper: NsdDiscoverHelper? = null
    private var mClientListAdapter: ClientListAdapter? = null
    private var mConnectedCustomer: Customer? = null

    private val localIpAddress: String
        get() {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nsd_slave)

        customerList = ArrayList()
        mNsdDiscoverHelper = NsdDiscoverHelper(this, this)
        if (intent != null && intent.hasExtra(AppConstants.KEY_NAME))
            mClientDeviceName = intent.getStringExtra(AppConstants.KEY_NAME)
        else
            mClientDeviceName = AppUtility.getLocalBluetoothName()
        if (!isDiscovered) {
            mNsdDiscoverHelper!!.initializeNsdClient()
            isDiscovered = true
        }

        val sendBtn = findViewById<Button>(R.id.send_msg_btn)
        sendBtn.setOnClickListener {
            connectToHost(mConnectedCustomer)
        }
    }

    private fun connectToHost(customer: Customer?) {

        if (hostAddress == null) {
            Log.e(TAG, "Host Address is null")
            return
        }

        val ipAddress = localIpAddress
        val jsonData = JSONObject()

        try {
            jsonData.put("request", REQUEST_CONNECT_CLIENT)
            jsonData.put("client_name", customer!!.clientDeviceName)
            jsonData.put("ipAddress", ipAddress)
            if (!TextUtils.isEmpty(msg_text_slave.text)) {
                jsonData.put("message", msg_text_slave.text)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "can't put request")
            return
        }

        SocketServerTask(customer, this).execute(jsonData)
    }

    override fun onNsdServiceDiscovered(serviceInfo: NsdServiceInfo?) {
        Log.d(TAG, "Resolve Succeeded. " + serviceInfo!!)

        if (serviceInfo.serviceName == mClientDeviceName) {
            Log.d(TAG, "Same IP.")
            return
        }
        // Obtain port and IP
        hostPort = serviceInfo.port
        hostAddress = serviceInfo.host
        Log.d(TAG, "NEW HOST DETECTED - $hostPort")
        Log.d(TAG, "NEW HOST IP - " + hostAddress!!)
        Thread(Runnable {
            if (!isCustomerPresent(hostPort, hostAddress)) {
                showSnackBar(String.format(getString(R.string.new_customer_available_msg), serviceInfo.serviceName))
                val customer = Customer(hostAddress!!, hostPort, serviceInfo.serviceName, false)
                customerList!!.add(customer)
                updateCustomersAdapter()
            }
        }).start()
    }

    private fun showSnackBar(message: String) {
        this.runOnUiThread {
            Snackbar.make(findViewById(R.id.root_slave_lyt),
                    message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun isCustomerPresent(port: Int, hostAddress: InetAddress?): Boolean {
        if (customerList != null && customerList!!.size > 0) {
            for (s in customerList!!) {
                if (s.clientPort == port && s.clientIp === hostAddress)
                    return true
            }
            return false
        } else
            return false
    }

    private fun getCustomer(name: String): Customer? {
        if (customerList != null && customerList!!.size > 0) {
            for (s in customerList!!) {
                if (!TextUtils.isEmpty(s.clientDeviceName) && s.clientDeviceName == name)
                    return s
            }
            return null
        } else
            return null
    }

    private fun updateCustomersAdapter() {
        this.runOnUiThread(java.lang.Runnable {
            if (customerList == null || customerList!!.size == 0) return@Runnable
            if (mClientListAdapter == null) {
                mClientListAdapter = ClientListAdapter(customerList, this@NsdSlaveActivity)
                customer_list.layoutManager = LinearLayoutManager(this@NsdSlaveActivity)
                customer_list.adapter = mClientListAdapter
            } else {
                mClientListAdapter!!.setList(customerList!!)
            }
        })

    }

    override fun onNsdServiceDiscoveryFailed(serviceInfo: NsdServiceInfo?) {

    }

    override fun onPause() {
        if (mNsdDiscoverHelper != null && isDiscovered) {
            mNsdDiscoverHelper!!.stopDiscovery()
            isDiscovered = false
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (mNsdDiscoverHelper != null && !isDiscovered) {
            mNsdDiscoverHelper!!.discoverServices()
            isDiscovered = true
        }
    }

    override fun onDestroy() {
        if (mNsdDiscoverHelper != null && isDiscovered) {
            mNsdDiscoverHelper!!.stopDiscovery()
        }
        super.onDestroy()
    }

    override fun onNsdServiceLost(service: NsdServiceInfo?) {
        if (service != null && !TextUtils.isEmpty(service.serviceName)) {
            val customer = getCustomer(service.serviceName)
            if (customer != null)
                customerList!!.remove(customer)
        }
    }

    override fun onConnectClick(customer: Customer?, position: Int) {
        connectToHost(customer)
        mConnectedCustomer = customer
    }

    override fun onServerConnectionSuccess() {
        var message = String.format(getString(R.string.connected_customer), mConnectedCustomer?.clientDeviceName)
        showSnackBar(message)
        connection_status.text = message
        for (customer in customerList!!) {
            customer.isConnected = customer?.clientDeviceName.equals(mConnectedCustomer?.clientDeviceName)
        }
        mClientListAdapter?.setList(customerList!!)
    }

    override fun onServerConnectionFailure() {
        var message = String.format(getString(R.string.unable_connect_customer), mConnectedCustomer?.clientDeviceName)
        showSnackBar(message)
    }

    companion object {
        private val REQUEST_CONNECT_CLIENT = "request-connect-client"
        private val TAG = "NSDClient"
    }

}