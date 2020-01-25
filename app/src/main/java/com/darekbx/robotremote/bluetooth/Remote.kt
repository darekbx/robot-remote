package com.darekbx.robotremote.bluetooth

import android.bluetooth.BluetoothGatt
import android.content.Context
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleManagerCallbacks
import java.util.*
import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.ble.data.Data

class Remote(context: Context) : BleManager<BleManagerCallbacks>(context) {

    companion object {
        val BT_SERVICE = UUID.fromString("09fc95c0-c111-11e3-9904-0002a5d5c51b")
        val BT_WRITE_CHARACTERISTIC = UUID.fromString("16fe0d80-c111-11e3-b8c8-0002a5d5c51b")
        val BT_NOTIFY_CHARACTERISTIC = UUID.fromString("1c927b50-c116-11e3-8a33-0800200c9a66")
    }

    var onLostConnection: (() -> Unit)? = null
    var onInitialized: (() -> Unit)? = null
    var onData: ((Data) -> Unit)? = null

    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    fun write(
        data: String,
        onSuccess : () -> Unit,
        onFailed : (Int) -> Unit) {
        writeCharacteristic(writeCharacteristic, data.toByteArray())
            .done { device -> onSuccess() }
            .fail { device, status -> onFailed(status) }
            .enqueue()
    }

    override fun getGattCallback() = object : BleManagerGattCallback() {

        override fun onDeviceDisconnected() {
            onLostConnection?.invoke()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(BT_SERVICE)?.run {
                notifyCharacteristic = getCharacteristic(BT_NOTIFY_CHARACTERISTIC)
                writeCharacteristic = getCharacteristic(BT_WRITE_CHARACTERISTIC)
            }

            var notifyValid = false
            notifyCharacteristic?.let { notifyCharacteristic->
                val properties = notifyCharacteristic.getProperties()
                notifyValid = (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
            }

            var writeRequestValid = false
            writeCharacteristic?.let { writeCharacteristic->
                val properties = writeCharacteristic.getProperties()
                writeRequestValid = (properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            }

            return notifyValid && writeRequestValid
        }

        override fun initialize() {
            onInitialized?.invoke()
            setNotificationCallback(notifyCharacteristic)
                .with { device, data -> onData?.invoke(data) }
            enableNotifications(notifyCharacteristic).enqueue()
        }

        override fun onDeviceReady() {
            super.onDeviceReady()
        }
    }
}