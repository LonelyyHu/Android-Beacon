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
import android.os.Build
import android.support.annotation.RequiresApi
import java.math.RoundingMode


class MainActivity : AppCompatActivity(), BeaconConsumer {

    lateinit var beaconManager: BeaconManager

    val beaconId = "e2c56db5-dffb-48d2-b060-d0f5a71096e0"
    lateinit var region: Region

    private var permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    companion object {
        const val REQ_PERMISSION_CODE = 100
        val CHANNEL_ID = "Beacon Notify Channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switch_scan.setOnCheckedChangeListener { buttonView, isChecked ->

            Log.wtf("MainActivity", "onCreate => isChecked:$isChecked")

            if (isChecked) {
                startBeaconManager()
            } else {
                stopBeaconManger()
            }

        }

        checkPermissions()

        initBeaconManager()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotification()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        beaconManager.unbind(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotification() {
        val channelBeacon = NotificationChannel(CHANNEL_ID, "Channel Beacon", NotificationManager.IMPORTANCE_HIGH)
        channelBeacon.description = "Beacon Notify"
//        channelBeacon.enableLights(true)
//        channelBeacon.enableVibration(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channelBeacon)
    }

    private fun initBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.bind(this)

        region = Region("myRangingUniqueId", Identifier.parse(beaconId), null, null)

    }


    private fun startBeaconManager() {
        beaconManager.startRangingBeaconsInRegion(region)
        beaconManager.startMonitoringBeaconsInRegion(region)
    }

    private fun stopBeaconManger() {
        beaconManager.stopRangingBeaconsInRegion(region)
        beaconManager.stopMonitoringBeaconsInRegion(region)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didDetermineStateForRegion(state: Int, p1: Region?) {
                Log.wtf("MainActivity", "didDetermineStateForRegion =>")

                if (state == MonitorNotifier.INSIDE) {
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Region Change Detect")
                            .setContentText("You entered the beacon region!!")
                            .build()

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(100, notification)
                } else {
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Region Change Detect")
                            .setContentText("You left the beacon region!!")
                            .build()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(100, notification)
                }

            }

            override fun didEnterRegion(p0: Region?) {
                
                Log.wtf("MainActivity", "didEnterRegion =>")

            }

            override fun didExitRegion(p0: Region?) {
                
                Log.wtf("MainActivity", "didExitRegion =>")

            }
        })

        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, p1: Region?) {
                beacons?.forEach {

                    val dis = it.distance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

                    Log.wtf("MainActivity",
                            "address:${it.bluetoothAddress}, rssi:${it.rssi}, id1:${it.id1}, dis:$dis, measureCount:${it.measurementCount}")



                    val logStr = "rssi:${it.rssi}, dis:$dis\n"
                    tv_log.append(logStr)
                    scroll_view.smoothScrollTo(0, tv_log.bottom)

                }
            }
        })
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
                    startBeaconManager()
                } else {
                    checkPermissions()
                }
            }
        }
    }
}
