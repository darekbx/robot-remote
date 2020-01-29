package com.darekbx.robotremote

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.darekbx.robotremote.bluetooth.DevicesScanner
import com.darekbx.robotremote.bluetooth.Remote
import kotlinx.android.synthetic.main.activity_main.*
import no.nordicsemi.android.ble.BleManagerCallbacks

class MainActivity : AppCompatActivity(), BleManagerCallbacks{

    companion object {
        val DEVICE_NAME = "Cosmose Robot #1"
    }

    lateinit var remote: Remote
    lateinit var devicesScanner: DevicesScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicesScanner = DevicesScanner {
            onDevice(it)
            devicesScanner.stop()
        }
            .apply {
                state_text.setText("Scanning...")
                scan()
            }

        remote = Remote(this).apply {
            onInitialized = { setButtonsState(true) }
            onLostConnection = { setButtonsState(false) }
            onData = { data ->
                runOnUiThread {
                    state_text.setText("Data: '${data.getStringValue(0) ?: "Null"}'")
                }
            }
            setGattCallbacks(this@MainActivity)
        }

        start_button.setOnClickListener { sendAction("start") }
        stop_button.setOnClickListener { sendAction("stop") }
        left_action.setOnClickListener { sendAction("left") }
        right_action.setOnClickListener { sendAction("right") }
        device_name.setText(DEVICE_NAME)

        setButtonsState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        devicesScanner.stop()
        remote.close()
    }

    private fun setButtonsState(value: Boolean) {
        start_button.isEnabled = value
        stop_button.isEnabled = value
        left_action.isEnabled = value
        right_action.isEnabled = value
        device_state.isChecked = value
    }

    private fun onDevice(bluetoothDevice: BluetoothDevice) {
        if (bluetoothDevice.name != null && bluetoothDevice.name.trim().equals(DEVICE_NAME, true)) {
            state_text.setText("Device found")
            devicesScanner.stop()
            stop_button.postDelayed({ connectToDevice(bluetoothDevice) }, 1000)
        }
    }

    private fun connectToDevice(bluetoothDevice: BluetoothDevice) {
        remote.connect(bluetoothDevice)
            .timeout(100000)
            .retry(3, 100)
            .done { setButtonsState(true) }
            .fail { device, status -> setButtonsState(false) }
            .enqueue()
    }

    private fun sendAction(action: String) {
        state_text.setText("Enqued action '$action'")
        remote.write(action,
            { notifySuccess("Action '$action' successful") },
            { notifyFailed(it) })
    }

    private fun notifySuccess(message: String) {
        runOnUiThread {
            state_text.setText(message)
        }
    }

    private fun notifyFailed(state: Int) {
        runOnUiThread {
            state_text.setText("Action failed: $state")
        }
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        setButtonsState(false)
        Log.v("---------", "onDeviceDisconnecting")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        setButtonsState(false)
        Log.v("---------", "onDeviceDisconnected")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        setButtonsState(true)
    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {
    }

    override fun onBondingFailed(device: BluetoothDevice) {
    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {
    }

    override fun onBondingRequired(device: BluetoothDevice) {
    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {
    }

    override fun onBonded(device: BluetoothDevice) {
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        setButtonsState(true)
    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        setButtonsState(false)
        Log.v("---------", "onError $message $errorCode")
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
    }
}