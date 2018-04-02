package com.ppg.spunky_kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

/**
 * Created by mariapc on 29/03/18.
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        var TYPE_WIFI = 1
        var TYPE_MOBILE = 2
        var TYPE_NOT_CONNECTED = 0
        val NETWORK_STATUS_NOT_CONNECTED = 0
        val NETWORK_STAUS_WIFI = 1
        val NETWORK_STATUS_MOBILE = 2
    }

    override fun onReceive(context: Context, intent: Intent) {

        val status = getConnectivityStatusString(context)
        if ("android.net.conn.CONNECTIVITY_CHANGE" != intent.action) {
            if (status == NETWORK_STATUS_NOT_CONNECTED) {
                //Se desconectó
            } else {
                //No se desconectó
            }

        }
    }

    fun getConnectivityStatusString(context: Context): Int {
        val conn = getConnectivityStatus(context)
        var status = 0

        when(conn){
            TYPE_WIFI -> status= NETWORK_STAUS_WIFI
            TYPE_MOBILE -> status = NETWORK_STATUS_MOBILE
            TYPE_NOT_CONNECTED -> status= NETWORK_STATUS_NOT_CONNECTED
        }

        return status
    }

    private fun getConnectivityStatus(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {

            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI

            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }
}
