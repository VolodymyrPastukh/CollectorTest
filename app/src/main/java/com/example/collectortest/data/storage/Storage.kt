package com.example.collectortest.data.storage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Storage {

    private lateinit var auth: FirebaseAuth

    private var _storage: FirebaseStorage? = null
    private val storage: FirebaseStorage
        get() = checkNotNull(_storage)

    private lateinit var storageRef: StorageReference

    init {
        _storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
    }

    fun getImageReference() = storageRef.child("images")


}