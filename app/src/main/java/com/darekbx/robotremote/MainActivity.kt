package com.darekbx.robotremote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.darekbx.robotremote.bluetooth.Remote
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var remote: Remote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        remote = Remote(this).apply {
            onInitialized = { device_state.isChecked = true }
            onLostConnection = { device_state.isChecked = false }
            onData = { data ->
                runOnUiThread {
                    state_text.setText("Data: ${data.getStringValue(0) ?: "Null"}")
                }
            }
        }

        action_button.setOnClickListener {
            remote.write("start",
                {
                    runOnUiThread {
                        state_text.setText("Action start successful")
                    }
                },
                { state ->
                    runOnUiThread {
                        state_text.setText("Action start failed: $state")
                    }
                })
        }
    }
}
