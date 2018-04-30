package com.lonelyyhu.androidBeacon

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap


class MyApplication: Application(), BootstrapNotifier {

    lateinit var beaconManager: BeaconManager
    lateinit var regionBootstrap: RegionBootstrap

    companion object {
        const val CHANNEL_ID = "Beacon Notify Channel"

        lateinit var INSTANCE: MyApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        beaconManager = BeaconManager.getInstanceForApplication(this)

        Log.wtf("MyApplication", "onCreate => beaconManager:$beaconManager")

        beaconManager.beaconParsers.add(BeaconInfo.getBeaconParsor())

        regionBootstrap = RegionBootstrap(this, BeaconInfo.getMonitorRegion())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotification()
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {

        Log.wtf("MyApplication", "didDetermineStateForRegion => Region State:$state")

        if (state == MonitorNotifier.INSIDE) {
            sendRegionChangeNotification(true)
        } else {
            sendRegionChangeNotification(false)
        }
    }

    override fun didEnterRegion(p0: Region?) {
        Log.wtf("MyApplication", "didEnterRegion =>")

    }

    override fun didExitRegion(p0: Region?) {
        Log.wtf("MyApplication", "didExitRegion =>")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotification() {
        val channelBeacon = NotificationChannel(Constant.NOTIFICATION_CHANNEL_ID, "Channel Beacon", NotificationManager.IMPORTANCE_HIGH)
        channelBeacon.description = "Beacon Notify"
//        channelBeacon.enableLights(true)
//        channelBeacon.enableVibration(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channelBeacon)
    }

    private fun sendRegionChangeNotification(isInRegion: Boolean) {

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Region Change Detect")


        if (isInRegion) {
            builder.setContentText("You entered the beacon region!!")
        } else {
            builder.setContentText("You left the beacon region!!")
        }

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
}