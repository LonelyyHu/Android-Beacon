package com.lonelyyhu.androidBeacon

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.altbeacon.beacon.*
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.app.NotificationChannel
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {

    lateinit var beaconManager: BeaconManager

    private var permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val rangeNotifier = RangeNotifier { beacons, p1 ->
        beacons?.forEach {

            val dis = it.distance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

            Log.wtf("MainActivity",
                    "address:${it.bluetoothAddress}, rssi:${it.rssi}, id1:${it.id1}, dis:$dis, measureCount:${it.measurementCount}")



            val logStr = "rssi:${it.rssi}, dis:$dis\n"

            runOnUiThread {
                tv_log.append(logStr)
                scroll_view.smoothScrollTo(0, tv_log.bottom)
            }

        }
    }

    val monitorNotifier = object : MonitorNotifier {
        override fun didDetermineStateForRegion(state: Int, p1: Region?) {
            Log.wtf("MainActivity", "didDetermineStateForRegion =>")

            if (state == MonitorNotifier.INSIDE) {
                sendRegionChangeNotification(true)
            } else {
                sendRegionChangeNotification(false)
            }

        }

        override fun didEnterRegion(p0: Region?) {
            Log.wtf("MainActivity", "didEnterRegion =>")
        }

        override fun didExitRegion(p0: Region?) {
            Log.wtf("MainActivity", "didExitRegion =>")
        }
    }

    companion object {
        const val REQ_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BeaconScanService.startService(this, Constant.ACTION_SERVICE_START)

        sw_show.setOnCheckedChangeListener { buttonView, isChecked ->

            Log.wtf("MainActivity", "is show switch checked: $isChecked")

            beaconManager = BeaconManager.getInstanceForApplication(this)


            if (isChecked) {
                beaconManager.addMonitorNotifier(monitorNotifier)
                beaconManager.addRangeNotifier(rangeNotifier)
            } else {
                beaconManager.removeMonitorNotifier(monitorNotifier)
                beaconManager.removeRangeNotifier(rangeNotifier)
            }

        }

        tb_fg_service.setOnCheckedChangeListener { compoundButton, isChecked ->

            Log.wtf("MainActivity","is foreground srvice checked: $isChecked")

            if (isChecked) {
                BeaconScanService.startService(this, Constant.ACTION_START_FOREGROUND)
            } else {
                BeaconScanService.startService(this, Constant.ACTION_STOP_FOREGROUND)
            }

        }

        checkPermissions()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotification()
        }

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotification() {
        val channelBeacon = NotificationChannel(Constant.NOTIFICATION_CHANNEL_ID, "Channel Beacon", NotificationManager.IMPORTANCE_HIGH)
        channelBeacon.description = "Beacon Notify"
        channelBeacon.enableLights(true)
        channelBeacon.enableVibration(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channelBeacon)
    }

    private fun sendRegionChangeNotification(isInRegion: Boolean) {

        val builder = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Region Change Detect")


        if (isInRegion) {
            builder.setContentText("You entered the beacon region!!")
        } else {
            builder.setContentText("You left the beacon region!!")
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constant.NOTIFICATION_ID_REGION_CHENGE, builder.build())
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded = arrayListOf<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQ_PERMISSION_CODE)
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startBeaconScan()
                } else {
                    checkPermissions()
                }
            }
        }
    }
}
