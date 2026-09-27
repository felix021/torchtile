package com.felix021.torchtile

import android.app.Activity
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.Gravity
import android.widget.SeekBar
import android.widget.TextView

class LevelPanelActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setGravity(Gravity.CENTER)

        val manager = getSystemService(CameraManager::class.java)
        TorchController.ensureCamera(manager)
        if (!TorchController.supportsLevels || TorchController.cameraId == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_level_panel)

        val max = TorchController.maxLevel
        val seek = findViewById<SeekBar>(R.id.seek)
        val levelText = findViewById<TextView>(R.id.level_text)

        fun showLevel(level: Int) {
            levelText.text = "$level / $max"
        }

        seek.max = max - 1
        seek.progress = TorchController.savedLevel(this) - 1
        showLevel(seek.progress + 1)

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                showLevel(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val level = (seekBar?.progress ?: 0) + 1
                TorchController.saveLevel(this@LevelPanelActivity, level)
                TorchController.applyLevel(manager, level)
                finish()
            }
        })
    }
}
