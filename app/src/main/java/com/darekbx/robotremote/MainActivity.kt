package com.darekbx.robotremote

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.darekbx.robotremote.bluetooth.DevicesScanner
import com.darekbx.robotremote.bluetooth.Remote
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val DEVICE_NAME = "Cosmose Robot #1"
    }

    lateinit var remote: Remote
    lateinit var devicesScanner: DevicesScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicesScanner = DevicesScanner { onDevice(it) }
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
        }

        start_button.setOnClickListener { sendAction("start") }
        stop_button.setOnClickListener { sendAction("stop") }
        left_action.setOnClickListener { sendAction("left") }
        right_action.setOnClickListener { sendAction("right") }
        device_name.setText(DEVICE_NAME)
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
        if (bluetoothDevice.name.equals(DEVICE_NAME, true)) {
            state_text.setText("Device found")
            devicesScanner.stop()
            connectToDevice(bluetoothDevice)
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
}