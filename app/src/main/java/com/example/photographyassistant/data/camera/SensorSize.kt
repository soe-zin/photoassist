package com.example.photographyassistant.data.camera

/**
 * Camera sensor dimensions.
 * 
 * @param width Sensor width in pixels
 * @param height Sensor height in pixels
 */
data class SensorSize(
    val width: Int,
    val height: Int
) {
    /**
     * Get total pixel count.
     * 
     * @return Total number of pixels on sensor
     */
    fun getPixelCount(): Long = width.toLong() * height.toLong()
    
    /**
     * Check if sensor resolution meets minimum requirements.
     * 
     * @param minWidth Minimum required width
     * @param minHeight Minimum required height
     * @return true if sensor meets minimum requirements
     */
    fun meetsMinimumRequirements(minWidth: Int, minHeight: Int): Boolean {
        return width >= minWidth && height >= minHeight
    }
}