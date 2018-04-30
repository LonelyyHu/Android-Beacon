package com.lonelyyhu.androidBeacon

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.altbeacon.beacon.*
import java.math.RoundingMode

class BeaconScanService : Service(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private var isForeground = false
    private var userState = 0

    private val monitorNotifier = object : MonitorNotifier {
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

    private val rangeNotifier = RangeNotifier { beacons, p1 ->
        beacons?.forEach {

            val dis = it.distance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

            Log.wtf("MainActivity",
                    "address:${it.bluetoothAddress}, rssi:${it.rssi}, id1:${it.id1}, dis:$dis, measureCount:${it.measurementCount}")

            if (dis < 1 && userState != 2) {
                userState = 2
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Constant.NOTIFICATION_ID_FOREGROUND_SERVICE, buildNotification("nearBy"))
            } else if (dis > 1 && userState != 1) {
                userState = 1
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Constant.NOTIFICATION_ID_FOREGROUND_SERVICE, buildNotification("entered"))
            }


        }
    }

    companion object {

        @JvmStatic
        fun startService(context: Context, action: String) {
            val intent = Intent(context, BeaconScanService::class.java)
            intent.action = action
            context.startService(intent)
        }

    }

    override fun onCreate() {
        super.onCreate()
        initBeaconManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action.equals(Constant.ACTION_START_FOREGROUND)) {
            startForegroundService()
            addBeaconNotifier()
            isForeground = true
        } else if (intent?.action.equals(Constant.ACTION_STOP_FOREGROUND)) {
            removeBeaconNotifier()
            stopForeground(true)
            isForeground = false
//            stopSelf()
        } else if (intent?.action.equals(Constant.ACTION_START_SCAN)) {
            startBeaconScan()
        } else if (intent?.action.equals(Constant.ACTION_STOP_SCAN)) {
            stopBeaconScan()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    private fun startForegroundService() {
        startForeground(Constant.NOTIFICATION_ID_FOREGROUND_SERVICE, buildNotification("scanning"))
        isForeground = true
    }

    private fun buildNotification(contentText: String): Notification {
        val notiIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, 0)

        val notifyBuilder = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setContentTitle("Beacon Signal Scaning")
                .setContentText(contentText)
                .setContentIntent(pendingIntent)

        if (isForeground) {
            notifyBuilder.setOngoing(true)
        }

        return notifyBuilder.build()
    }

    private fun initBeaconManager() {

        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)

        Log.wtf("BeaconScanService", "initBeaconManager =>")

        beaconManager.beaconParsers.add(BeaconInfo.getBeaconParsor())

        beaconManager.bind(this)
    }

    private fun startBeaconScan() {
        Log.wtf("MainActivity", "startBeaconScan =>")

        beaconManager.startRangingBeaconsInRegion(BeaconInfo.getRangingRegion())
        beaconManager.startMonitoringBeaconsInRegion(BeaconInfo.getMonitorRegion())
    }

    private fun stopBeaconScan() {
        Log.wtf("BeaconScanService", "stopBeaconScan =>")

        beaconManager.stopRangingBeaconsInRegion(BeaconInfo.getRangingRegion())
        beaconManager.stopMonitoringBeaconsInRegion(BeaconInfo.getMonitorRegion())
    }

    private fun addBeaconNotifier() {
        beaconManager.addMonitorNotifier(monitorNotifier)
        beaconManager.addRangeNotifier(rangeNotifier)
    }

    private fun removeBeaconNotifier() {
        beaconManager.removeMonitorNotifier(monitorNotifier)
        beaconManager.removeRangeNotifier(rangeNotifier)
    }

    private fun sendRegionChangeNotification(isInRegion: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val text = if (isInRegion) {
            userState = 1
            "entered"
        } else {
            userState = 0
            "left"
        }

        notificationManager.notify(Constant.NOTIFICATION_ID_FOREGROUND_SERVICE, buildNotification(text))
    }

    override fun onBeaconServiceConnect() {
        startBeaconScan()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
