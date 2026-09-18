package com.example.photographyassistant.data.camera

/**
 * Camera resolution data class.
 * 
 * @param width Resolution width in pixels
 * @param height Resolution height in pixels
 */
data class Resolution(
    val width: Int,
    val height: Int
) {
    /**
     * Get total pixel count.
     * 
     * @return Total number of pixels in resolution
     */
    fun getPixelCount(): Long = width.toLong() * height.toLong()
    
    /**
     * Get aspect ratio as string.
     * 
     * @return Aspect ratio (e.g., "16:9", "4:3")
     */
    fun getAspectRatio(): String {
        val gcd = gcd(width, height)
        val w = width / gcd
        val h = height / gcd
        return "$w:$h"
    }
    
    /**
     * Check if resolution is higher quality than another resolution.
     * 
     * @param other Other resolution to compare
     * @return true if this resolution has more pixels
     */
    fun isHigherQualityThan(other: Resolution): Boolean {
        return getPixelCount() > other.getPixelCount()
    }
    
    /**
     * Calculate greatest common divisor.
     * 
     * @param a First number
     * @param b Second number
     * @return Greatest common divisor
     */
    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }
}