package com.darekbx.robotremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import no.nordicsemi.android.support.v18.scanner.*

class DevicesScanner(val callback: (BluetoothDevice) -> Unit) : ScanCallback() {

    fun scan() {
        val settings = ScanSettings.Builder()
            .setLegacy(true)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(true)
            .build()

        val filters: MutableList<ScanFilter> = ArrayList()
        filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("09fc95c0-c111-11e3-9904-0002a5d5c51b")).build())

        BluetoothLeScannerCompat.getScanner().startScan(filters, settings, this)
    }

    fun stop() {
        BluetoothLeScannerCompat.getScanner().stopScan(this)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>) {
        super.onBatchScanResults(results)
        results.forEach {
            callback(it.device)
        }
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        callback(result.device)
    }
}