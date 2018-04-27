package com.lonelyyhu.androidBeacon

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BeaconScanService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        if (intent.action.equals(Consta))

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
