package com.example.collectortest.ui.scope.photos

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collectortest.data.dto.Accelerometer
import com.example.collectortest.data.dto.Gyroscope
import com.example.collectortest.data.storage.Storage
import com.example.collectortest.database.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val storage: Storage,
    private val database: Database
) : ViewModel() {


    fun uploadToDatabase(
        timestamp: String,
        accelerometerData: List<Float>,
        gyroscopeData: List<Float>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            database.getAccelerometerReference(timestamp)
                .setValue(
                    Accelerometer(
                        accelerometerData[0],
                        accelerometerData[1],
                        accelerometerData[2]
                    )
                )
            database.getGyroscopeReference(timestamp)
                .setValue(Gyroscope(gyroscopeData[0], gyroscopeData[1], gyroscopeData[2]))
        }
    }

    fun uploadImage(timestamp: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO){
            val imageRef = storage.getImageReference()
            val photoRef = imageRef.child(timestamp)

            val baos = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = photoRef.putBytes(data)
            uploadTask.addOnFailureListener {

            }.addOnSuccessListener { taskSnapshot -> }
        }
    }
}