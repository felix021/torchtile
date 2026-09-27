package com.felix021.torchtile

import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class TorchTileService : TileService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == TorchController.cameraId) {
                TorchController.torchOn = enabled
                if (!enabled) TorchController.strengthLevel = 0
                updateTile()
            }
        }

        override fun onTorchModeUnavailable(id: String) {
            if (id == TorchController.cameraId) {
                TorchController.torchOn = false
                TorchController.strengthLevel = 0
                updateTile()
            }
        }

        override fun onTorchStrengthLevelChanged(id: String, strengthLevel: Int) {
            if (id == TorchController.cameraId) {
                TorchController.strengthLevel = strengthLevel
                updateTile()
            }
        }
    }

    private val cameraManager: CameraManager
        get() = getSystemService(CameraManager::class.java)

    override fun onStartListening() {
        super.onStartListening()
        Log.i(TAG, "onStartListening")
        TorchController.ensureCamera(cameraManager)
        if (TorchController.cameraId != null) {
            cameraManager.registerTorchCallback(torchCallback, mainHandler)
        }
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    override fun onClick() {
        val id = TorchController.cameraId
        Log.i(TAG, "onClick cameraId=$id torchOn=${TorchController.torchOn}")
        if (id == null) {
            updateTile()
            return
        }
        try {
            val turnOn = !TorchController.torchOn
            cameraManager.setTorchMode(id, turnOn)
            if (turnOn && TorchController.supportsLevels) {
                val saved = TorchController.savedLevel(this)
                TorchController.applyLevel(cameraManager, saved)
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggle failed", e)
        }
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        tile.state = when {
            TorchController.cameraId == null -> Tile.STATE_UNAVAILABLE
            TorchController.torchOn -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.subtitle = if (TorchController.torchOn && TorchController.supportsLevels) {
            val level = if (TorchController.strengthLevel > 0) {
                TorchController.strengthLevel
            } else {
                TorchController.savedLevel(this)
            }
            "亮度 $level/${TorchController.maxLevel}"
        } else {
            null
        }
        tile.updateTile()
    }

    companion object {
        private const val TAG = TorchController.TAG
    }
}
