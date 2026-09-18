package com.example.photographyassistant.data.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Camera capability discovery and management.
 * 
 * Uses Camera2 API to discover available camera features and capabilities.
 * Provides comprehensive camera information for both preview and processing.
 */
class CameraManager(private val context: Context) {
    
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val availableCameraIds = getAvailableCameraIds()
    
    /**
* Discover all available camera capabilities.
     * 
     * @return List of CameraCapabilities for all available cameras
     */
    fun discoverCameraCapabilities(): List<CameraCapabilities> {
        return availableCameraIds.mapNotNull { cameraId ->
            discoverCapabilitiesForCamera(cameraId)
        }
    }
    
    private fun getAvailableCameraIds(): List<String> {
        return try {
            cameraManager.cameraIdList.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getAvailableFocusModes(characteristics: CameraCharacteristics): List<FocusMode> {
        val availableModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
        if (availableModes == null) return emptyList()
        
        val modes = mutableListOf<FocusMode>()
        for (mode in availableModes) {
            when (mode) {
                CameraCharacteristics.CONTROL_AF_MODE_AUTO -> modes.add(FocusMode.AUTO)
                CameraCharacteristics.CONTROL_AF_MODE_MACRO -> modes.add(FocusMode.MACRO)
                CameraCharacteristics.CONTROL_AF_MODE_EDOF -> modes.add(FocusMode.EDOF)
                CameraCharacteristics.CONTROL_AF_MODE_FIXED -> modes.add(FocusMode.FIXED)
                CameraCharacteristics.CONTROL_AF_MODE_INFINITY -> modes.add(FocusMode.INFINITY)
                CameraCharacteristics.CONTROL_AF_MODE_MANUAL -> modes.add(FocusMode.MANUAL)
            }
        }
        return modes
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getAvailableExposureModes(characteristics: CameraCharacteristics): List<ExposureMode> {
        val availableModes = mutableListOf<ExposureMode>()
        
        // Check exposure compensation modes
        characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)?.let { modes ->
            if (modes.contains(CameraCharacteristics.CONTROL_AE_MODE_ON)) {
                availableModes.add(ExposureMode.AUTO)
            }
            if (modes.contains(CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH)) {
                availableModes.add(ExposureMode.FLASH)
            }
            if (modes.contains(CameraCharacteristics.CONTROL_AE_MODE_OFF)) {
                availableModes.add(ExposureMode.OFF)
            }
            if (modes.contains(CameraCharacteristics.CONTROL_AE_MODE_ON_DEMAND)) {
                availableModes.add(ExposureMode.MANUAL)
            }
        }
        
        return availableModes
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getSensorSize(characteristics: CameraCharacteristics): SensorSize {
        val sensorArray = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        if (sensorArray == null) {
            return SensorSize(0, 0)
        }
        return SensorSize(sensorArray.getWidth(), sensorArray.getHeight())
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getAvailableResolutions(characteristics: CameraCharacteristics): List<Resolution> {
        val resolutions = mutableListOf<Resolution>()
        
        // Add supported video sizes
        characteristics.get(CameraCharacteristics.SENSOR_INFO_AVAILABLE_VIDEO_SIZE)?.let { videoSizeRange ->
            if (videoSizeRange is intArrayArray) {
                for (size in videoSizeRange) {
                    if (size.size >= 2) {
                        resolutions.add(Resolution(size[0], size[1]))
                    }
                }
            }
        }
        
        // Add maximum preview size
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
            map.getOutputSizes(android.graphics.SurfaceTexture::class.java)?.forEach { size ->
                resolutions.add(Resolution(size.width, size.height))
            }
        }
        
        // Add sensor info sizes
        characteristics.get(CameraCharacteristics.SENSOR_INFO_SUPPORTED_SCENE_MODES)?.let { modes ->
            // Add different resolution modes for different scenes
            for (mode in modes) {
                // Add standard resolution presets based on mode
                resolutions.add(Resolution(1920, 1080)) // HD
                resolutions.add(Resolution(1280, 720))  // HD
                resolutions.add(Resolution(640, 480))   // VGA
            }
        }
        
        // Remove duplicates and sort by resolution
        return resolutions.distinct().sortedWith(compareBy({ it.width }, { it.height }))
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkRawSupport(characteristics: CameraCharacteristics): RawSupport {
        return try {
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            if (capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true) {
                RawSupport.SUPPORTED
            } else {
                RawSupport.NOT_SUPPORTED
            }
        } catch (e: Exception) {
            RawSupport.UNKNOWN
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCameraFeatures(characteristics: CameraCharacteristics): CameraFeatures {
        val features = CameraFeatures()
        
        // Check for various camera features
        characteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)?.let { arrangement ->
            features.colorFilterArrangement = arrangement
        }
        
        characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.let { exposureTimeRange ->
            features.exposureTimeRange = exposureTimeRange
        }
        
        characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.let { sensitivityRange ->
            features.sensitivityRange = sensitivityRange
        }
        
        characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)?.let { maxFrameDuration ->
            features.maxFrameDuration = maxFrameDuration
        }
        
        return features
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun discoverCapabilitiesForCamera(cameraId: String): CameraCapabilities? {
        return try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            
            val lensFacing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> LensFacing.FRONT
                CameraCharacteristics.LENS_FACING_BACK -> LensFacing.BACK
                CameraCharacteristics.LENS_FACING_EXTERNAL -> LensFacing.EXTERNAL
                else -> LensFacing.BACK
            }
            
            val focusModes = getAvailableFocusModes(characteristics)
            val exposureModes = getAvailableExposureModes(characteristics)
            val sensorSize = getSensorSize(characteristics)
            val resolutions = getAvailableResolutions(characteristics)
            val rawSupport = checkRawSupport(characteristics)
            val features = getCameraFeatures(characteristics)
            
            CameraCapabilities(
                cameraId = cameraId,
                lensFacing = lensFacing,
                focusModes = focusModes,
                exposureModes = exposureModes,
                sensorSize = sensorSize,
                resolutions = resolutions,
                rawSupport = rawSupport,
                features = features
            )
        } catch (e: Exception) {
            null
        }
    }
        return try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            
            val lensFacing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> LensFacing.FRONT
                CameraCharacteristics.LENS_FACING_BACK -> LensFacing.BACK
                CameraCharacteristics.LENS_FACING_EXTERNAL -> LensFacing.EXTERNAL
                else -> LensFacing.BACK
            }
            
            val focusModes = getAvailableFocusModes(characteristics)
            val exposureModes = getAvailableExposureModes(characteristics)
            val sensorSize = getSensorSize(characteristics)
            val resolutions = getAvailableResolutions(characteristics)
            val rawSupport = checkRawSupport(characteristics)
            val features = getCameraFeatures(characteristics)
            
            CameraCapabilities(
                cameraId = cameraId,
                lensFacing = lensFacing,
                focusModes = focusModes,
                exposureModes = exposureModes,
                sensorSize = sensorSize,
                resolutions = resolutions,
                rawSupport = rawSupport,
                features = features
            )
        } catch (e: Exception) {
            null
        }
    }