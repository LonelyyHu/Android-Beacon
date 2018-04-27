package com.lonelyyhu.androidBeacon

import android.util.Log
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region

object BeaconInfo {

    private var region: Region
    private var rangingRegion: Region
    private var beaconParser: BeaconParser

    init {
        val beaconId = "e2c56db5-dffb-48d2-b060-d0f5a71096e0"
        region = Region("myMonitoringUniqueId", Identifier.parse(beaconId), null, null)

        rangingRegion = Region("myRangingUniqueId", Identifier.parse(beaconId), null, null)

        val beaconLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        beaconParser = BeaconParser().setBeaconLayout(beaconLayout)

    }

    fun getMonitorRegion(): Region {

        Log.wtf("BeaconInfo", "getMonitorRegion => ${region.id1}")

        return region
    }

    fun getRangingRegion(): Region {
        return rangingRegion
    }

    fun getBeaconParsor(): BeaconParser {
        return beaconParser
    }




}