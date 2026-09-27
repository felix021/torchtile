package com.felix021.torchtile

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

object TorchController {

    const val TAG = "TorchTile"

    private const val PREFS = "torch_prefs"
    private const val KEY_LEVEL = "level"

    var cameraId: String? = null
        private set
    var maxLevel = 1
        private set
    var torchOn = false
    var strengthLevel = 0

    val supportsLevels: Boolean
        get() = Build.VERSION.SDK_INT >= 33 && maxLevel > 1

    fun ensureCamera(manager: CameraManager) {
        if (cameraId != null) return
        try {
            val id = manager.cameraIdList.firstOrNull { c ->
                manager.getCameraCharacteristics(c)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (id != null) {
                val ch = manager.getCameraCharacteristics(id)
                cameraId = id
                if (Build.VERSION.SDK_INT >= 33) {
                    maxLevel = ch.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    val def = ch.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: maxLevel
                    if (def in 1..maxLevel) defaultLevel = def
                }
            }
            Log.i(TAG, "camera=$cameraId maxLevel=$maxLevel defaultLevel=$defaultLevel")
        } catch (e: Exception) {
            Log.e(TAG, "ensureCamera failed", e)
        }
    }

    private var defaultLevel = 1

    fun savedLevel(context: Context): Int {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LEVEL, defaultLevel)
        return saved.coerceIn(1, maxLevel.coerceAtLeast(1))
    }

    fun saveLevel(context: Context, level: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_LEVEL, level).apply()
    }

    fun nextLevel(context: Context): Int {
        val max = if (supportsLevels) maxLevel else 1
        return savedLevel(context) % max + 1
    }

    fun applyLevel(manager: CameraManager, level: Int): Boolean {
        if (!supportsLevels) return false
        val id = cameraId ?: return false
        return try {
            manager.turnOnTorchWithStrengthLevel(id, level)
            Log.i(TAG, "turnOnTorchWithStrengthLevel($id, $level) ok")
            true
        } catch (e: Exception) {
            Log.e(TAG, "turnOnTorchWithStrengthLevel($id, $level) failed", e)
            false
        }
    }
}
