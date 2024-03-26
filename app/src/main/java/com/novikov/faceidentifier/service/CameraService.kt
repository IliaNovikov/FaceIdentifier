package com.novikov.faceidentifier.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.util.Log

class CameraService(private val cameraManager: CameraManager, private val cameraId: String) {

    val FRONT_CAMERA = 1
    val BACK_CAMERA = 0
    private var cameraDevice: CameraDevice? = null

    private val cameraCallback = object: StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            TODO("Not yet implemented")
        }

        override fun onDisconnected(camera: CameraDevice) {
            TODO("Not yet implemented")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            TODO("Not yet implemented")
        }
    }

    fun isOpen(): Boolean{
        if(cameraDevice == null)
            return false
        else
            return true
    }

    fun openCamera(context: Context){
        try{
            if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(cameraId, cameraCallback, null)
            }
        }
        catch (e: Exception){
            Log.e("cameraService", e.message.toString())
        }
    }
    fun closeCamera(){
        if (cameraDevice != null) {
            cameraDevice?.close()
        }
    }
}