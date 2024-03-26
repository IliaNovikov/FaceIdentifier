package com.novikov.faceidentifier

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.novikov.faceidentifier.databinding.ActivityMainBinding
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private lateinit var cameraId: String
    private lateinit var cameraManager: CameraManager
    private lateinit var binding: ActivityMainBinding
    private var cameraDevice: CameraDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            cameraId = cameraManager.cameraIdList.last()

            Log.i("camera facing", cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING).toString())
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback(){
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        Log.i("on opened", "yes")
                        Log.i("on opened", cameraDevice.toString())
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        camera.close()
                        Log.e("camera error", error.toString())
                    }

                }, null)
            }

            Handler().postDelayed({

                binding.mainSurfaceView.holder.setSizeFromLayout()

                cameraDevice!!.createCaptureSession(listOf(binding.mainSurfaceView.holder.surface), object : CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.i("on configured", "yes")
                        val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        builder.addTarget(binding.mainSurfaceView.holder.surface)
                        builder.set(CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                        session.setRepeatingRequest(builder.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("capture session error", "error")
                    }

                }, null)



                Log.i("cameraDevice", cameraDevice.toString())
            }, 1000)

        }
        catch (e: Exception){
            Log.e("cameraError  ", e.message.toString())
        }

        setContentView(binding.root)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
            else{
                Toast.makeText(this, "Нужно предоставить все разрешения", Toast.LENGTH_LONG).show()
            }
        }

    }
}