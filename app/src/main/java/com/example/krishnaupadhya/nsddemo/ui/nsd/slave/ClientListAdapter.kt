package com.example.krishnaupadhya.nsddemo.ui.nsd.slave

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.krishnaupadhya.nsddemo.R
import com.example.krishnaupadhya.nsddemo.model.Customer

/**
 * Created by Krishna.Upadhya on 04-03-2018.
 */
class ClientListAdapter(private var clientsList: ArrayList<Customer>?, var clientListListener: ClientListListener?) : RecyclerView.Adapter<ClientListAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun getItemCount(): Int = clientsList!!.size

    override fun onBindViewHolder(holder: CustomerViewHolder?, position: Int) {
        var customer: Customer = clientsList!!.get(position)
        holder?.title?.text = customer.clientDeviceName
        var status: String = if (customer.isConnected) "disconnect" else "connect"
        holder?.connectBtn?.text = status
        holder?.connectBtn?.setOnClickListener {
            clientListListener?.onConnectClick(customer, position)
        }
    }

    fun setList(list: java.util.ArrayList<Customer>) {
        clientsList = list
        notifyDataSetChanged()
    }

    inner class CustomerViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.findViewById(R.id.customer_name)
        val connectBtn: Button = mView.findViewById(R.id.connect_btn)
    }
}