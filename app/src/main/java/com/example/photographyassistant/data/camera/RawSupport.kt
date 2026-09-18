package com.example.photographyassistant.data.camera

/**
 * Camera RAW sensor support status.
 */
enum class RawSupport {
    /**
     * Camera supports RAW capture
     */
    SUPPORTED,
    
    /**
     * Camera does not support RAW capture
     */
    NOT_SUPPORTED,
    
    /**
     * RAW support status unknown
     */
    UNKNOWN
}