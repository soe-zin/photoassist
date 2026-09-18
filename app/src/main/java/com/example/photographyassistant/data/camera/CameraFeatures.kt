package com.example.photographyassistant.data.camera

/**
 * Camera features data class.
 * 
 * Contains additional camera capabilities and specifications.
 */
import android.hardware.camera2.CameraCharacteristics

/**
 * Camera features data class.
 * 
 * @param colorFilterArrangement Color filter arrangement type
 * @param exposureTimeRange Range of supported exposure times
 * @param sensitivityRange Range of supported sensitivity values
 * @param maxFrameDuration Maximum frame duration
 */
data class CameraFeatures(
    var colorFilterArrangement: Int = CameraCharacteristics.COLOR_FILTER_ARRANGEMENT_MONO,
    var exposureTimeRange: LongArray = longArrayOf(1, 1000000000),
    var sensitivityRange: IntArray = intArrayOf(100, 1600),
    var maxFrameDuration: Long = 1000000000
) {
    /**
     * Check if camera supports manual exposure.
     * 
     * @return true if camera supports manual exposure control
     */
    fun supportsManualExposure(): Boolean {
        return exposureTimeRange.isNotEmpty() && sensitivityRange.isNotEmpty()
    }
    
    /**
     * Get maximum exposure time in microseconds.
     * 
     * @return Maximum exposure time in microseconds
     */
    fun getMaxExposureTimeMicroseconds(): Long {
        return if (exposureTimeRange.isNotEmpty()) exposureTimeRange[1] else 0
    }
    
    /**
     * Get minimum sensitivity (ISO) value.
     * 
     * @return Minimum ISO value
     */
    fun getMinSensitivity(): Int {
        return if (sensitivityRange.isNotEmpty()) sensitivityRange[0] else 100
    }
    
    /**
     * Get maximum sensitivity (ISO) value.
     * 
     * @return Maximum ISO value
     */
    fun getMaxSensitivity(): Int {
        return if (sensitivityRange.isNotEmpty()) sensitivityRange[1] else 1600
    }
    
    /**
     * Get maximum frame duration in microseconds.
     * 
     * @return Maximum frame duration in microseconds
     */
    fun getMaxFrameDurationMicroseconds(): Long {
        return maxFrameDuration
    }
}