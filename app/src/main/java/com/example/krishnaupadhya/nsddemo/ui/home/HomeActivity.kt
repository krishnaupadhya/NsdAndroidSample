package com.example.krishnaupadhya.nsddemo.ui.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import com.example.krishnaupadhya.nsddemo.R
import com.example.krishnaupadhya.nsddemo.ui.nsd.master.NsdMasterActivity
import com.example.krishnaupadhya.nsddemo.ui.nsd.slave.NsdSlaveActivity
import com.example.krishnaupadhya.nsddemo.utils.AppConstants
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_master.setOnClickListener(this)
        btn_slave.setOnClickListener(this)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this,
                message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_master -> {
                if (!TextUtils.isEmpty(input_name.text)) {
                    val intent = Intent(this, NsdMasterActivity::class.java)
                    intent.putExtra(AppConstants.KEY_NAME, input_name!!.text.toString())
                    startActivity(intent)
                } else {
                    showToast(getString(R.string.error_msg_name))
                }
            }
            R.id.btn_slave -> {
                if (!TextUtils.isEmpty(input_name.text)) {
                    val intent = Intent(this, NsdSlaveActivity::class.java)
                    intent.putExtra(AppConstants.KEY_NAME, input_name.text.toString())
                    startActivity(intent)
                } else {
                    showToast(getString(R.string.error_msg_name))
                }
            }
        }
    }
}
