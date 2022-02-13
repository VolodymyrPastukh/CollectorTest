package com.example.collectortest.ui.scope.photos

import android.content.Context.SENSOR_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.*
import android.hardware.camera2.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.collectortest.databinding.FragmentPhotosBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class PhotosFragment : Fragment() {

    private var sensorsManager: SensorManager? = null
    private lateinit var sensorsListener: SensorEventListener
    private var accelerometerData: List<Float> = ArrayList()
    private var gyroscopeData: List<Float> = ArrayList()

    private var sensorAccelerometer: Sensor? = null
    private var sensorGyroscope: Sensor? = null

    var dummy: SurfaceView? = null

    private var _binding: FragmentPhotosBinding? = null
    private val binding: FragmentPhotosBinding
        get() = checkNotNull(_binding)
    private val viewModel: PhotosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sensorsManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorsManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorGyroscope = sensorsManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        return FragmentPhotosBinding.inflate(inflater).apply {
            _binding = this
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerSensors()
        start()
    }

    private fun start() {
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                sendData()
            }
        }
        timer.schedule(task, 0, 2000)
    }

    private fun uploadData(bitmap: Bitmap) = with(binding) {
        val timestamp = System.currentTimeMillis().toString()

        logsTv.text = "Parameters:\n" +
                "Time[$timestamp] \n" +
                "Accelerometer[$accelerometerData]\n" +
                "Gyroscope[$gyroscopeData]"

        viewModel.uploadImage(timestamp, bitmap)
        viewModel.uploadToDatabase(timestamp, accelerometerData, gyroscopeData)
    }

    @Suppress("Deprecation")
    private fun sendData() = with(binding) {
        var job: Job? = null
        val photoCallback = Camera.PictureCallback { data, camera ->

            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val image = bitmap.rotate(90F)
            uploadData(image)
            binding.imageIv.setImageBitmap(image)
            camera.release()
            job?.cancel()
        }


        job = GlobalScope.launch(Dispatchers.Unconfined) {
            val camera = getCamera()
            if (camera == null) {
                Toast.makeText(requireContext(), "Camera == null", Toast.LENGTH_LONG).show()
                return@launch
            } else {
                val params = camera.parameters
                try {

                    params.setPictureSize(480, 640)
                    camera.setParameters(params)
                    camera.setPreviewDisplay(dummy!!.getHolder())
                    camera.startPreview()

                    delay(500L)
                    camera.takePicture(null, null, photoCallback)
                    delay(2000L)
                } finally {
//                    camera.release()
                }
            }
        }
    }

    fun Bitmap.rotate(angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            this, 0, 0, this.width, this.height, matrix,
            true
        )
    }

    @Suppress("Deprecation")
    private fun getCamera(): Camera? {
        val camerasCount = Camera.getNumberOfCameras()
        for (i in 0..camerasCount) {
            val cameraInfo = Camera.CameraInfo()
            Log.i("CAMERA", "${cameraInfo.facing}")
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return Camera.open(i)
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        dummy = binding.surfaceIv
    }


    override fun onDestroyView() {
        super.onDestroyView()
        sensorsManager?.unregisterListener(sensorsListener, sensorAccelerometer)
        sensorsManager?.unregisterListener(sensorsListener, sensorGyroscope)
    }

    private fun registerSensors() {
        sensorsListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    when (it.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            accelerometerData = it.values.toList()
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                            gyroscopeData = it.values.toList()
                        }
                        else -> Log.i("SENSOR", "Another sensor ${it.sensor}")
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }

        }

        sensorsManager?.registerListener(
            sensorsListener,
            sensorAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorsManager?.registerListener(
            sensorsListener,
            sensorGyroscope,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}