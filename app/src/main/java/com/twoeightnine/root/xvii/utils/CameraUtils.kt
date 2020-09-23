package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import com.twoeightnine.root.xvii.lg.L
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

private const val TAG = "camera"

fun CameraManager.getFrontCameraId(): String? {
    try {
        for (cameraId in cameraIdList) {
            val chars = getCameraCharacteristics(cameraId)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }
    } catch (e: CameraAccessException) {
        L.tag(TAG).throwable(e).log("unable to find front camera")
    }
    return null
}

@SuppressLint("MissingPermission")
fun CameraManager.openFrontCamera(backgroundHandler: Handler, onOpened: (CameraDevice) -> Unit) {
    val cameraId = getFrontCameraId()
    if (cameraId != null) {
        openCamera(cameraId, object : CameraDeviceStateCallback() {
            override fun onOpened(camera: CameraDevice) {
                L.tag(TAG).log("opened")
                onOpened(camera)
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                L.tag(TAG).log("closed")
            }
        }, backgroundHandler)
    }
}

fun CameraManager.takePicture(
        cameraDevice: CameraDevice,
        file: File,
        readerHandler: Handler,
        cameraHandler: Handler,
        onCaptured: () -> Unit
) {
    try {
        val characteristics = getCameraCharacteristics(cameraDevice.id)
        val jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)

        val width = (jpegSizes?.getOrNull(0)?.width ?: 2560) / 2
        val height = (jpegSizes?.getOrNull(0)?.height ?: 1920) / 2
        L.tag(TAG).log("size $width*$height")

        val imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurfaces = arrayListOf(imageReader.surface)
        val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                .apply {
                    addTarget(imageReader.surface)
//                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                }

        @Suppress("ControlFlowWithEmptyBody")
        while (imageReader.acquireNextImage() != null) {}

        val readerListener = ImageReader.OnImageAvailableListener { reader ->
            try {
                reader.acquireLatestImage().use { image ->
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)

                    FileOutputStream(file).use { output ->
                        output.write(bytes)
                        L.tag(TAG).log("written to file ${file.absolutePath}")
                    }

                    onCaptured()
                }
            } catch (e: Exception) {
                L.tag(TAG).throwable(e).log("unable to save image")
            }
        }
        imageReader.setOnImageAvailableListener(readerListener, readerHandler)
        cameraDevice.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    session.capture(captureBuilder.build(), null, cameraHandler)
                } catch (e: CameraAccessException) {
                    L.tag(TAG).throwable(e).log("unable to capture")
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                L.tag(TAG).warn().log("configure failed")
            }
        }, cameraHandler)
    } catch (e: CameraAccessException) {
        L.tag(TAG).throwable(e).log("error occurred during capturing")
    }
}

abstract class CameraDeviceStateCallback : CameraDevice.StateCallback() {

    override fun onDisconnected(cameraDevice: CameraDevice) {
        onClosed(cameraDevice)
    }

    override fun onError(cameraDevice: CameraDevice, error: Int) {
        L.tag(TAG).warn().log("camera callback returned error $error")
        onClosed(cameraDevice)
    }

}