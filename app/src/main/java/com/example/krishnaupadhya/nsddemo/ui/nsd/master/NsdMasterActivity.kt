package com.example.krishnaupadhya.nsddemo.ui.nsd.master

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.krishnaupadhya.nsddemo.R
import com.example.krishnaupadhya.nsddemo.nsdutils.ChatConnection
import com.example.krishnaupadhya.nsddemo.nsdutils.NsdRegistrationHelper
import com.example.krishnaupadhya.nsddemo.nsdutils.NsdRegistrationListener
import com.example.krishnaupadhya.nsddemo.nsdutils.SocketServerThread
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import com.example.krishnaupadhya.nsddemo.utils.AppUtility
import kotlinx.android.synthetic.main.activity_nsd_master.*
import java.util.*

class NsdMasterActivity : AppCompatActivity(), NsdRegistrationListener, SocketConnectionListener {

    private var mServerDeviceName = AppConstants.SERVERS_DEVICE
    private var socketServerThread: SocketServerThread? = null
    private var mNsdRegistrationHelper: NsdRegistrationHelper? = null
    private var clientIPs: MutableList<String>? = null
    private var mConnection: ChatConnection? = null
    private var mCustomerName: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nsd_master)
        if (intent != null && intent.hasExtra(AppConstants.KEY_NAME)) {
            mCustomerName = intent.getStringExtra(AppConstants.KEY_NAME)
            customer_name.text = String.format(getString(R.string.welcome), mCustomerName)
        }
        mServerDeviceName = AppUtility.getLocalBluetoothName()
        initRegistration()
    }


    fun addChatLine(line: String) {
        msg_text_view.text = getString(R.string.customer_mobile_number) + " " + line
    }

    private fun initRegistration() {
        mNsdRegistrationHelper = NsdRegistrationHelper(this, this)
        if (!mNsdRegistrationHelper!!.isServiceRunning)
            mNsdRegistrationHelper?.initializeNsdServer()
        clientIPs = ArrayList()
        socketServerThread = SocketServerThread(this, AppConstants.SERVER_SOCKET_PORT)
        socketServerThread!!.start()
    }

    override fun onNsdServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
        val mServiceName = nsdServiceInfo.serviceName
        mServerDeviceName = mServiceName
        Log.d(TAG, "Registered name : $mServiceName")
        this@NsdMasterActivity.runOnUiThread { status_master_device.text = nsdServiceInfo.serviceName + " " + getString(R.string.master_connection_status) }
        mConnection?.localPort = nsdServiceInfo.port
    }

    override fun onNsdServiceRegistrationFailed(service: NsdServiceInfo?, errorCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNsdServiceUnregistered(nsdServiceInfo: NsdServiceInfo?) {
        this@NsdMasterActivity.runOnUiThread {
            status_master_device.text = getString(R.string.master_connection_status) + " : " + getString(R.string.service_stopped)
        }
    }

    override fun onMessageReceived(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            this@NsdMasterActivity.runOnUiThread { msg_text_view.text = getString(R.string.customer_mobile_number) + " " + msg }
        }
    }

    fun showToast(toast: String) {
        this@NsdMasterActivity.runOnUiThread { Toast.makeText(this@NsdMasterActivity, toast, Toast.LENGTH_LONG).show() }
    }

    override fun onPause() {
        if (mNsdRegistrationHelper?.isServiceRunning!!)
            mNsdRegistrationHelper?.tearDown()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!mNsdRegistrationHelper!!.isServiceRunning)

            mNsdRegistrationHelper?.resumeService()
    }

    override fun onDestroy() {
        if (mNsdRegistrationHelper!!.isServiceRunning)
            mNsdRegistrationHelper?.tearDown()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_CONNECT_CLIENT = "request-connect-client"

        private const val TAG = "NSDServer"
    }

}