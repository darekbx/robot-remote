package com.darekbx.robotremote.bluetooth

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings


class DevicesScanner(val callback: (BluetoothDevice) -> Unit) : ScanCallback() {

    fun scan() {
        val settings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(true)
            .build()
        BluetoothLeScannerCompat.getScanner().startScan(null, settings, this)
    }

    fun stop() {
        BluetoothLeScannerCompat.getScanner().stopScan(this)
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        callback(result.device)
    }
}