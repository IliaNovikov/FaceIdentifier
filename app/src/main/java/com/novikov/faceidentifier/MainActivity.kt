package com.novikov.faceidentifier

import android.Manifest
import android.R.string
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.provider.Telephony.Mms.Part.FILENAME
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.novikov.faceidentifier.databinding.ActivityMainBinding
import com.novikov.faceidentifier.service.ApiClient
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.io.File
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {

    private lateinit var cameraId: String
    private lateinit var cameraManager: CameraManager
    private lateinit var binding: ActivityMainBinding
    private var cameraDevice: CameraDevice? = null
    private lateinit var imageReader: ImageReader
    private lateinit var handlerThreadSession: HandlerThread
    private lateinit var handlerThreadImageReader: HandlerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)

        // Проверка разрешений на камеру и внешнее хранилище
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            Toast.makeText(this, "Необходимо дать все разрешения", Toast.LENGTH_LONG).show()
        }
        else{
            // Инициализация сервиса для камеры и считывателя изображения
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)

            try {
                // Получение id фронтальной камеры
                cameraId = cameraManager.cameraIdList.last()

                Log.i("camera facing", cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING).toString())
                //Открытие камеры для работы
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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

                    //Задержка необходимая для открытия камеры (пока костыль)
                    Handler().postDelayed({
                        binding.mainSurfaceView.holder.setSizeFromLayout()

                        handlerThreadSession = HandlerThread("session")
                        handlerThreadSession.start()

                        Log.i("cameraDevice", cameraDevice.toString())
                        // Создание сессии захвата изображения
                        cameraDevice!!.createCaptureSession(listOf(binding.mainSurfaceView.holder.surface, imageReader.surface), object : CameraCaptureSession.StateCallback(){
                            override fun onConfigured(session: CameraCaptureSession) {
                                Log.i("on configured", "yes")
                                //Создание запроса на захват
                                val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                builder.addTarget(binding.mainSurfaceView.holder.surface)
                                builder.addTarget(imageReader.surface)
                                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, 1000000000L)
                                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL)


//                                object: CountDownTimer(10000, 200){
//                                    override fun onTick(millisUntilFinished: Long) {
//                                        session.capture(builder.build(), null, null)
//                                    }
//
//                                    override fun onFinish() {
//
//                                    }
//
//                                }.start()

//                                Отправка повторяющегося запроса
                                session.setRepeatingRequest(builder.build(), object :
                                    CameraCaptureSession.CaptureCallback() {
                                    //Захват изображения каждую итерацию запроса
                                    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                                        session.capture(builder.build(), null, null)
                                    }
                                }, Handler(handlerThreadSession.looper))

                                session.setRepeatingRequest(builder.build(), null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.e("capture session error", "error")
                            }

                        }, null)

                    }, 1000)

                    handlerThreadImageReader = HandlerThread("imageReader")
                    handlerThreadImageReader.start()

                    //Слушатель доступности картинки из считывателя
                    imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
                        //Обработку изображения пихать сюда скорее всего
                        override fun onImageAvailable(reader: ImageReader?) {
                            val image = reader!!.acquireLatestImage()

                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())

                            val fos = openFileOutput("face.jpg", Context.MODE_PRIVATE)
                            fos.write(bytes)

                            fos.close()

//                            File("face.jpg").writeBytes(bytes)
//                            val file = OpenFileInput

//                            ApiClient().execute(file).wait()
//                            lifecycleScope.launch {
//                                ApiClient().sendPicture(file)
//                            }

                            Log.i("bytes size: ", bytes.size.toString())

                            Log.i("imageReader", "captured")
                            Toast.makeText(baseContext, "captured", Toast.LENGTH_SHORT).show()
                            image.close()
                        }
                    }, Handler(handlerThreadImageReader.looper))
                }
                else{
                    Toast.makeText(baseContext, "Нужны все разрешения", Toast.LENGTH_SHORT).show()
                }


            }
            catch (e: Exception){
                Log.e("cameraError  ", e.message.toString())
            }
        }

        setContentView(binding.root)

    }

    //Метод для запроса разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                return
            }
            else{
                Toast.makeText(this, "Нужно предоставить все разрешения", Toast.LENGTH_LONG).show()
            }
        }

    }
}