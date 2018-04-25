package com.lonelyyhu.androidBeacon

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.altbeacon.beacon.*
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode


class MainActivity : AppCompatActivity(), BeaconConsumer {

    lateinit var beaconManager: BeaconManager

    var permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val REQ_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkPermissions()) {
            startBeaconManager()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        beaconManager.unbind(this)
    }

    private fun startBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.bind(this)
    }

    override fun onBeaconServiceConnect() {
//        beaconManager.addMonitorNotifier(object : MonitorNotifier {
//            override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun didEnterRegion(p0: Region?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun didExitRegion(p0: Region?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//        })

        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, p1: Region?) {
                beacons?.forEach {

                    val dis = it.distance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

                    Log.wtf("MainActivity",
                            "address:${it.bluetoothAddress}, rssi:${it.rssi}, id1:${it.id1}, dis:$dis")

                    if (it.id1.toUuid().toString().equals("e2c56db5-dffb-48d2-b060-d0f5a71096e0")) {
                        val logStr = "address:${it.bluetoothAddress}, rssi:${it.rssi}, dis:$dis\n"
                        et_log.text.append(logStr)
//                        et_log.text.append(logStr)
                    }
                }
            }
        })

        beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))

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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBeaconManager()
                } else {
                    checkPermissions()
                }
            }
        }
    }
}
