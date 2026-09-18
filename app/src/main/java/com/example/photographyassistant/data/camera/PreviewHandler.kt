package com.example.photographyassistant.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.photographyassistant.feature.exposuremeter.ExposureMeterAnalyzer
import com.example.photographyassistant.domain.exposure.MeteringMode
import com.example.photographyassistant.domain.exposure.SceneBrightness

/**
 * Camera preview and analysis handler.
 * 
 * Manages CameraX preview, lifecycle, and frame processing pipeline.
 * Handles camera configuration, preview setup, and image analysis.
 * 
 * @param context Android context
 * @param previewView PreviewView for displaying camera preview
 * @param exposureMeterAnalyzer Exposure analyzer for frame processing
 * @param executor Camera processing executor
 */
class PreviewHandler(
    private val context: Context,
    private val previewView: PreviewView,
    private val exposureMeterAnalyzer: ExposureMeterAnalyzer? = null,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
) {

    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var previewResolution: Size = Size(1920, 1080)
    
    // Frame processing callbacks
    private var onExposureAnalysis: ((Float) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    
    /**
     * Camera lifecycle states.
     */
    enum class LifecycleState {
        IDLE,
        STARTING,
        STARTED,
        STOPPING,
        ERROR
    }
    
    private var currentState: LifecycleState = LifecycleState.IDLE
    
    /**
     * Camera availability and health status.
     */
    data class CameraStatus(
        val isAvailable: Boolean = false,
        val hasPermission: Boolean = false,
        val errorMessage: String? = null,
        val cameraInfo: CameraInfo? = null
    ) {
        data class CameraInfo(
            val cameraId: String,
            val lensFacing: LensFacing,
            val availableResolutions: List<Resolution>
        )
    }
    
    /**
     * Configure frame analysis with exposure calculation.
     * 
     * @param onExposureAnalysis Callback for exposure analysis results
     * @param onError Callback for error handling
     */
    fun configureFrameAnalysis(
        onExposureAnalysis: ((Float) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        this.onExposureAnalysis = onExposureAnalysis
        this.onError = onError
    }
    
    /**
     * Start camera preview with frame analysis.
     * 
     * @param onStarted Callback when preview starts successfully
     * @param onError Callback when camera error occurs
     */
    fun startPreview(
        onStarted: (Camera) -> Unit,
        onError: (String) -> Unit
    ) {
        if (currentState == LifecycleState.STARTED) {
            return
        }
        
        currentState = LifecycleState.STARTING
        
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context)
            cameraProvider.await()
            
            // Setup preview use case
            preview = Preview.Builder()
                .setTargetResolution(previewResolution)
                .setTargetRotation(previewView.display.rotation)
                .build()
                
            // Setup image analysis with exposure meter analyzer
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(previewResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                    if (exposureMeterAnalyzer != null) {
                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            processFrame(imageProxy)
                        }
                    }
                }
            
            // Bind camera to lifecycle
            camera = cameraProvider.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            // Setup preview surface
            preview?.setSurfaceProvider(previewView.surfaceProvider)
            
            currentState = LifecycleState.STARTED
            onStarted(camera!!)
            
        } catch (e: Exception) {
            currentState = LifecycleState.ERROR
            val errorMsg = "Failed to start camera preview: ${e.message}"
            onError(errorMsg)
        }
    }
    
    /**
     * Process camera frame for exposure analysis.
     */
    private fun processFrame(imageProxy: ImageProxy) {
        try {
            // Convert ImageProxy to Bitmap
            val bitmap = imageProxy.toBitmap()
            
            if (bitmap != null && exposureMeterAnalyzer != null) {
                // Analyze frame for exposure
                val exposureResult = exposureMeterAnalyzer.analyzeFrame(
                    frame = bitmap,
                    meteringMode = MeteringMode.CENTER_WEIGHTED,
                    sceneBrightness = SceneBrightness.NORMAL,
                    currentEv = 0.0
                )
                
                // Send exposure analysis result
                onExposureAnalysis?.invoke(exposureResult.exposureValue.iso.toFloat())
                
            } else {
                onError?.invoke("Failed to process frame or analyzer not configured")
            }
            
            imageProxy.close()
            
        } catch (e: Exception) {
            onError?.invoke("Frame processing error: ${e.message}")
        }
    }
    
    /**
     * Convert ImageProxy to Bitmap.
     */
    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            val yBuffer = this.planes[0].buffer
            val uvBuffer = this.planes[1].buffer
            
            val ySize = yBuffer.remaining()
            val uvSize = uvBuffer.remaining()
            
            val nv21 = ByteArray(ySize + uvSize)
            yBuffer.get(nv21, 0, ySize)
            uvBuffer.get(nv21, ySize, uvSize)
            
            BitmapFactory.decodeByteArray(nv21, 0, nv21.size)
        } catch (e: Exception) {
            null
        }
    }